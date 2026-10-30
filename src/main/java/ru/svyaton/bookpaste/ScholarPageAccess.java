package ru.svyaton.bookpaste;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ScholarPageAccess {
    private ScholarPageAccess() {
    }

    @SuppressWarnings("unchecked")
    public static void replacePages(Object screen, List<String> replacement, int spread) throws ReflectiveOperationException {
        List<String> pages = (List<String>) field(screen.getClass(), "pages").get(screen);
        List<String> previous = new ArrayList<>(pages);
        Field current = field(screen.getClass(), "currentSpread");
        Field modified = field(screen.getClass(), "bookModified");
        int previousSpread = current.getInt(screen);
        boolean previousModified = modified.getBoolean(screen);
        Method refresh = method(screen.getClass());
        try {
            pages.clear();
            pages.addAll(replacement);
            current.setInt(screen, spread);
            modified.setBoolean(screen, true);
            refresh.invoke(screen, false);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            pages.clear();
            pages.addAll(previous);
            current.setInt(screen, previousSpread);
            modified.setBoolean(screen, previousModified);
            try {
                refresh.invoke(screen, false);
            } catch (ReflectiveOperationException | RuntimeException restoreFailure) {
                failure.addSuppressed(restoreFailure);
            }
            throw failure;
        }
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

    private static Method method(Class<?> type) throws NoSuchMethodException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod("setTextBoxes", boolean.class);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException("setTextBoxes");
    }
}
