package ru.svyaton.bookpaste;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class ScholarCompat {
    private static final String SCHOLAR_SCREEN = "io.github.mortuusars.scholar.client.gui.screen.edit.SpreadBookEditScreen";
    private static final String SCHOLAR_VALIDATOR = "io.github.mortuusars.scholar.client.gui.widget.textbox.text.FormattedStringEditor$Validator";
    private static final int PAGE_WIDTH = 114;
    private static final int PAGE_HEIGHT = 128;

    private ScholarCompat() {
    }

    public static boolean handlePaste() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof Screen)) {
            return false;
        }

        Screen screen = (Screen)client.currentScreen;
        if (!isScholarScreen(screen.getClass())) {
            return false;
        }

        String clipboard = client.keyboard.getClipboard();
        if (clipboard == null || clipboard.trim().isEmpty()) {
            return false;
        }

        try {
            Object focused = screen.getFocused();
            Object leftTextBox = getFieldValue(screen, "leftPageTextBox");
            Object rightTextBox = getFieldValue(screen, "rightPageTextBox");

            int sideOffset;
            if (focused == rightTextBox) {
                sideOffset = 1;
            } else if (focused == leftTextBox) {
                sideOffset = 0;
            } else {
                return false;
            }

            @SuppressWarnings("unchecked")
            List<String> pages = (List<String>)getFieldValue(screen, "pages");
            int currentSpread = (int)getFieldValue(screen, "currentSpread");
            int startPageIndex = currentSpread * 2 + sideOffset;

            while (pages.size() <= startPageIndex) {
                pages.add("");
            }

            String remaining = PasteHelper.normalizeClipboard(clipboard);
            int pagesUsed = 0;
            boolean changed = false;
            int lastTouchedPage = startPageIndex;

            for (int pageIndex = startPageIndex; pageIndex < BookPasteBookLimits.MAX_PAGE_COUNT; pageIndex++) {
                while (pages.size() <= pageIndex) {
                    pages.add("");
                }

                String base = pages.get(pageIndex);
                PasteHelper.PageSlice slice = PasteHelper.buildPage(base, remaining, candidate -> fitsOnPage(candidate, client),
                        pageIndex == BookPasteBookLimits.MAX_PAGE_COUNT - 1);
                boolean consumed = !slice.remainingText().equals(remaining);

                if (!slice.pageText().equals(base)) {
                    pages.set(pageIndex, slice.pageText());
                    changed = true;
                }

                if (consumed) {
                    pagesUsed++;
                    lastTouchedPage = pageIndex;
                }

                remaining = slice.remainingText();
                if (remaining.isEmpty()) {
                    break;
                }
            }

            if (!changed) {
                if (!remaining.isEmpty()) {
                client.keyboard.setClipboard(remaining);
            }
                sendStatus(client, pagesUsed, remaining);
                return true;
            }

            setFieldValue(screen, "currentSpread", lastTouchedPage / 2);
            setFieldValue(screen, "bookModified", true);
            invokeMethod(screen, "setTextBoxes", false);

            if (!remaining.isEmpty()) {
                client.keyboard.setClipboard(remaining);
            }
            sendStatus(client, pagesUsed, remaining);
            return true;
        } catch (ReflectiveOperationException | ClassCastException exception) {
            return false;
        }
    }

    private static void sendStatus(MinecraftClient client, int pagesUsed, String remaining) {
        if (client.player == null) {
            return;
        }

        client.player.sendMessage(PasteHelper.createStatusMessage(pagesUsed, remaining,
                candidate -> fitsOnPage(candidate, client)), true);
    }

    private static boolean isScholarScreen(Class<?> type) {
        Class<?> current = type;
        while (current != null) {
            if (SCHOLAR_SCREEN.equals(current.getName())) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private static boolean fitsOnPage(String candidate, MinecraftClient client) {
        if (candidate.length() > BookPasteBookLimits.MAX_PAGE_LENGTH) {
            return false;
        }

        try {
            Class<?> validator = Class.forName(SCHOLAR_VALIDATOR);
            for (Method method : validator.getDeclaredMethods()) {
                if (method.getName().equals("fitInDimensions") && Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 3) {
                    method.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Predicate<String> predicate = (Predicate<String>)method.invoke(null, client.textRenderer, PAGE_WIDTH, PAGE_HEIGHT);
                    return predicate.test(candidate);
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall back to vanilla-style line wrapping if Scholar internals are unavailable.
        }

        int lineCount = client.textRenderer.wrapLines(BookPasteTextCompat.literal(candidate), PAGE_WIDTH).size();
        return lineCount * client.textRenderer.fontHeight <= PAGE_HEIGHT;
    }

    private static Object getFieldValue(Object target, String name) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setFieldValue(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static void invokeMethod(Object target, String name, boolean arg) throws ReflectiveOperationException {
        Method method = findMethod(target.getClass(), name, boolean.class);
        method.setAccessible(true);
        method.invoke(target, arg);
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredMethod(name, parameterTypes);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }
}
