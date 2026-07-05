package ru.svyaton.bookpaste;

import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SignPasteCompat {
    private static final int SIGN_LINE_COUNT = 4;
    private static final int SIGN_LINE_WIDTH = 90;

    private SignPasteCompat() {
    }

    public static boolean handlePaste(Object screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.keyboard == null) {
            return false;
        }

        String clipboard = client.keyboard.getClipboard();
        if (clipboard == null || clipboard.trim().isEmpty()) {
            return false;
        }

        try {
            Field rowField = findField(screen.getClass(), int.class, "currentRow", "line", "field_3029", "field_40428");
            Field messagesField = findField(screen.getClass(), String[].class, "messages", "text", "field_24285", "field_40425");
            String[] messages = (String[]) messagesField.get(screen);
            if (messages == null || messages.length == 0) {
                return false;
            }

            Method setter = findMethod(screen.getClass(), String.class,
                    "setCurrentRowMessage", "setMessage", "method_49913", "method_45660", "method_16205");

            String remaining = PasteHelper.normalizeClipboard(clipboard);
            int startRow = clamp(rowField.getInt(screen), 0, Math.min(SIGN_LINE_COUNT, messages.length) - 1);
            int linesUsed = 0;
            int lastRow = startRow;

            for (int row = startRow; row < SIGN_LINE_COUNT && row < messages.length && !remaining.isEmpty(); row++) {
                String before = remaining;
                String base = messages[row] == null ? "" : messages[row];
                PasteHelper.PageSlice slice = PasteHelper.buildSignLine(base, remaining,
                        candidate -> client.textRenderer.getWidth(candidate) <= SIGN_LINE_WIDTH,
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
                client.keyboard.setClipboard(remaining);
            }

            if (client.player != null) {
                client.player.sendMessage(PasteHelper.createSignStatusMessage(linesUsed, remaining,
                        candidate -> client.textRenderer.getWidth(candidate) <= SIGN_LINE_WIDTH), true);
            }
            return true;
        } catch (ReflectiveOperationException | ClassCastException ex) {
            return false;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Field findField(Class<?> type, Class<?> expectedType, String... names) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            for (String name : names) {
                try {
                    Field field = current.getDeclaredField(name);
                    if (expectedType == null || expectedType.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        return field;
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }
            current = current.getSuperclass();
        }
        throw new NoSuchFieldException(String.join(", ", names) + " of type " + (expectedType != null ? expectedType.getName() : "any"));
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
