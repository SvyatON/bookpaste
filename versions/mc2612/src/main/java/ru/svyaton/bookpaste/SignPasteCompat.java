package ru.svyaton.bookpaste;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SignPasteCompat {
    private static final int SIGN_LINE_COUNT = 4;
    private static final int SIGN_LINE_WIDTH = 90;

    private SignPasteCompat() {
    }

    public static boolean handlePaste(Object screen) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.keyboardHandler == null) {
            return false;
        }

        String clipboard = client.keyboardHandler.getClipboard();
        if (clipboard == null || clipboard.trim().isEmpty()) {
            return false;
        }

        try {
            Field rowField = findField(screen.getClass(), "line", "currentRow", "field_40428", "field_3029");
            Field messagesField = findField(screen.getClass(), "messages", "text", "field_40425", "field_24285");
            String[] messages = (String[]) messagesField.get(screen);
            if (messages == null || messages.length == 0) {
                return false;
            }

            Method setter = findMethod(screen.getClass(), String.class, "setMessage", "setCurrentRowMessage", "method_49913", "method_45660", "method_16205");

            String remaining = PasteHelper.normalizeClipboard(clipboard);
            int startRow = clamp(rowField.getInt(screen), 0, Math.min(SIGN_LINE_COUNT, messages.length) - 1);
            int linesUsed = 0;
            int lastRow = startRow;

            for (int row = startRow; row < SIGN_LINE_COUNT && row < messages.length && !remaining.isEmpty(); row++) {
                String before = remaining;
                String base = messages[row] == null ? "" : messages[row];
                PasteHelper.PageSlice slice = PasteHelper.buildSignLine(base, remaining,
                        candidate -> client.font.width(candidate) <= SIGN_LINE_WIDTH,
                        row == SIGN_LINE_COUNT - 1);

                if (!slice.pageText().equals(base)) {
                    rowField.setInt(screen, row);
                    if (setter != null) {
                        setter.invoke(screen, slice.pageText());
                    } else {
                        messages[row] = slice.pageText();
                    }
                    lastRow = row;
                }

                remaining = slice.remainingText();
                if (!remaining.equals(before)) {
                    linesUsed++;
                    lastRow = row;
                }
            }

            rowField.setInt(screen, lastRow);
            if (!remaining.isEmpty()) {
                client.keyboardHandler.setClipboard(remaining);
            }

            if (client.player != null) {
                client.player.sendOverlayMessage(PasteHelper.createSignStatusMessage(linesUsed, remaining,
                        candidate -> client.font.width(candidate) <= SIGN_LINE_WIDTH));
            }
            return true;
        } catch (ReflectiveOperationException | ClassCastException ex) {
            return false;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Field findField(Class<?> type, String... names) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            for (String name : names) {
                try {
                    Field field = current.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                }
            }
            current = current.getSuperclass();
        }
        throw new NoSuchFieldException(String.join(", ", names));
    }

    private static Method findMethod(Class<?> type, Class<?> argument, String... names) {
        Class<?> current = type;
        while (current != null) {
            for (String name : names) {
                try {
                    Method method = current.getDeclaredMethod(name, argument);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ignored) {
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
