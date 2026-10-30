package ru.svyaton.bookpaste;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ScribblePageAccess {
    private ScribblePageAccess() {
    }

    public static String continueFormatting(String page, String remaining) {
        return PasteHelper.continueFormatting(page, remaining);
    }

    public static Object parseFormattedText(String text) {
        for (String name : new String[]{"me.chrr.scribble.text.StyledText", "me.chrr.scribble.book.RichText"}) {
            try {
                Class<?> type = Class.forName(name, false, ScribblePageAccess.class.getClassLoader());
                return type.getMethod("fromFormattedString", String.class).invoke(null, text);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    public static String formattedText(Object text) throws ReflectiveOperationException {
        if (text == null) return "";
        if (text instanceof String) return (String) text;
        for (String name : new String[]{"getAsFormattedStringLossy", "getAsFormattedString"}) {
            try {
                return (String) invoke(text, name);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException("Cannot read formatted Scribble text: " + text.getClass().getName());
    }

    public static List<String> readPages(Object screen) throws ReflectiveOperationException {
        Object value = field(screen.getClass(), "pages").get(screen);
        if (!(value instanceof List<?>)) throw new IllegalStateException("Missing Scribble pages");
        List<String> result = new ArrayList<>();
        for (Object page : (List<?>) value) result.add(formattedText(page));
        return result;
    }

    public static int focusedPage(Object screen) throws ReflectiveOperationException {
        int current = (int) field(screen.getClass(), "currentPage").get(screen);
        Object value = field(screen.getClass(), "textAreas").get(screen);
        if (value instanceof List<?>) {
            List<?> areas = (List<?>) value;
            for (int i = 0; i < areas.size(); i++) {
                if (Boolean.TRUE.equals(invoke(areas.get(i), "isFocused"))) return current + i;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    public static void replacePages(Object screen, List<String> text, int currentPage) throws ReflectiveOperationException {
        Field pagesField = field(screen.getClass(), "pages");
        List<Object> pages = (List<Object>) pagesField.get(screen);
        Class<?> styledText = pageType(pagesField, pages);
        Method convert = styledText.getMethod("fromFormattedString", String.class);
        List<Object> replacement = new ArrayList<>();
        for (String page : text) replacement.add(convert.invoke(null, page));
        List<Object> previous = new ArrayList<>(pages);
        Field current = field(screen.getClass(), "currentPage");
        Field dirty = field(screen.getClass(), "dirty");
        int previousPage = current.getInt(screen);
        boolean previousDirty = dirty.getBoolean(screen);
        try {
            pages.clear();
            pages.addAll(replacement);
            dirty.setBoolean(screen, true);
            invoke(screen, "showPage", new Class<?>[]{int.class, boolean.class}, currentPage, false);
            invoke(screen, "updateCurrentPages");
            invoke(screen, "saveChanges");
        } catch (ReflectiveOperationException | RuntimeException exception) {
            pages.clear();
            pages.addAll(previous);
            current.setInt(screen, previousPage);
            dirty.setBoolean(screen, previousDirty);
            try {
                invoke(screen, "updateCurrentPages");
            } catch (ReflectiveOperationException | RuntimeException restoreFailure) {
                exception.addSuppressed(restoreFailure);
            }
            throw exception;
        }
    }

    public static List<String> readLegacyCache(Object screen, int pageCount) throws ReflectiveOperationException {
        Object cache = optionalField(screen, "richPages");
        if (cache == null) cache = optionalField(screen, "synchronizedPages");
        if (cache == null) return null;
        List<String> result = new ArrayList<>();
        if (cache instanceof List<?>) {
            for (Object page : (List<?>) cache) result.add(formattedText(page));
        } else {
            for (int i = 0; i < pageCount; i++) {
                result.add(formattedText(invoke(cache, "get", new Class<?>[]{int.class}, i)));
            }
        }
        return result;
    }

    public static LegacyReplacement stageLegacy(Object screen, List<String> text, List<String> original) throws ReflectiveOperationException {
        Object cache = optionalField(screen, "richPages");
        if (cache == null) cache = optionalField(screen, "synchronizedPages");
        if (cache == null) return new LegacyReplacement(null, null, null, null);
        Class<?> richText = screen.getClass().getClassLoader().loadClass("me.chrr.scribble.book.RichText");
        Method converter = richText.getMethod("fromFormattedString", String.class);
        List<Object> replacement = new ArrayList<>();
        for (String page : text) replacement.add(converter.invoke(null, page));
        List<?> previous = cache instanceof List<?> ? new ArrayList<>((List<?>) cache) : new ArrayList<>(original);
        return new LegacyReplacement(cache, replacement, new ArrayList<>(text), previous);
    }

    public static final class LegacyReplacement {
        private final Object cache;
        private final List<?> richText;
        private final List<String> text;
        private final List<?> previous;

        private LegacyReplacement(Object cache, List<?> richText, List<String> text, List<?> previous) {
            this.cache = cache;
            this.richText = richText;
            this.text = text;
            this.previous = previous;
        }

        @SuppressWarnings("unchecked")
        public void commit() throws ReflectiveOperationException {
            if (cache instanceof List<?>) {
                List<Object> pages = (List<Object>) cache;
                pages.clear();
                pages.addAll(richText);
            } else if (cache != null) {
                invoke(cache, "populate", new Class<?>[]{List.class}, text);
            }
        }

        @SuppressWarnings("unchecked")
        public void rollback() throws ReflectiveOperationException {
            if (cache instanceof List<?>) {
                List<Object> pages = (List<Object>) cache;
                pages.clear();
                pages.addAll(previous);
            } else if (cache != null) {
                invoke(cache, "populate", new Class<?>[]{List.class}, previous);
            }
        }
    }

    private static Object optionalField(Object target, String name) throws IllegalAccessException {
        try {
            return field(target.getClass(), name).get(target);
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private static Class<?> pageType(Field pagesField, List<?> pages) throws NoSuchMethodException {
        java.lang.reflect.Type generic = pagesField.getGenericType();
        if (generic instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.Type page = ((java.lang.reflect.ParameterizedType) generic).getActualTypeArguments()[0];
            if (page instanceof Class<?>) return (Class<?>) page;
        }
        for (Object page : pages) {
            if (page != null) return page.getClass();
        }
        throw new NoSuchMethodException("Cannot determine Scribble page text type");
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Object invoke(Object target, String name) throws ReflectiveOperationException {
        return invoke(target, name, new Class<?>[0]);
    }

    private static Object invoke(Object target, String name, Class<?>[] types, Object... arguments) throws ReflectiveOperationException {
        for (Class<?> current = target.getClass(); current != null; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod(name, types);
                method.setAccessible(true);
                return method.invoke(target, arguments);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException(name);
    }
}
