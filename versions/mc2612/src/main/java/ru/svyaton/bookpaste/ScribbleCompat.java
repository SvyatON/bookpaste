package ru.svyaton.bookpaste;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ScribbleCompat {
    private static final int PAGE_WIDTH = 114;
    private static final int PAGE_HEIGHT = 128;
    private static boolean pasteShortcutWasDown;

    private ScribbleCompat() {
    }

    public static void tick(Minecraft client) {
        boolean pasteShortcutDown = BookPasteInputUtil.isPasteShortcutPressed(client);
        if (pasteShortcutDown && !pasteShortcutWasDown) {
            handlePaste();
        }
        pasteShortcutWasDown = pasteShortcutDown;
    }

    public static boolean handlePaste() {
        Minecraft client = Minecraft.getInstance();
        if (!(client.screen instanceof Screen)) {
            return false;
        }

        Screen screen = (Screen)client.screen;
        if (!(screen instanceof BookPasteBookEditAccess) || !isScribbleScreen(screen)) {
            return false;
        }

        BookPasteBookEditAccess access = (BookPasteBookEditAccess)screen;

        String clipboard = client.keyboardHandler.getClipboard();
        if (clipboard == null || clipboard.isBlank()) {
            return false;
        }

        try {
            List<String> workingPages = new ArrayList<>(access.bookpaste$getPages());
            int currentPage = access.bookpaste$getCurrentPage();
            while (workingPages.size() <= currentPage) {
                workingPages.add("");
            }

            String currentPageContent = getCurrentPageContent(screen);
            if (currentPageContent != null) {
                workingPages.set(currentPage, currentPageContent);
            }

            String remaining = PasteHelper.normalizeClipboard(clipboard);
            int pagesUsed = 0;
            boolean changed = false;
            int lastTouchedPage = currentPage;

            for (int pageIndex = currentPage; pageIndex < BookPasteBookLimits.MAX_PAGE_COUNT; pageIndex++) {
                while (workingPages.size() <= pageIndex) {
                    workingPages.add("");
                }

                String base = workingPages.get(pageIndex);
                PasteHelper.PageSlice slice = PasteHelper.buildPage(base, remaining,
                        candidate -> fitsOnPage(candidate, client),
                        pageIndex == BookPasteBookLimits.MAX_PAGE_COUNT - 1);
                boolean consumed = !slice.remainingText().equals(remaining);

                if (!slice.pageText().equals(base)) {
                    workingPages.set(pageIndex, slice.pageText());
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

            if (changed) {
                access.bookpaste$setPagesAndCurrentPage(workingPages, lastTouchedPage);
                setOptionalFieldValue(screen, "scribble$dirty", true);
            }

            client.keyboardHandler.setClipboard(remaining);
            sendStatus(access, client, pagesUsed, remaining);
            return true;
        } catch (ReflectiveOperationException | ClassCastException exception) {
            return false;
        }
    }

    private static void sendStatus(BookPasteBookEditAccess access, Minecraft client, int pagesUsed, String remaining) {
        if (access.bookpaste$getPlayer() == null) {
            return;
        }

        access.bookpaste$getPlayer().sendOverlayMessage(PasteHelper.createStatusMessage(pagesUsed, remaining,
                candidate -> fitsOnPage(candidate, client)));
    }

    private static boolean isScribbleScreen(Screen screen) {
        return hasMethod(screen.getClass(), "scribble$history$getRichEditBox");
    }

    private static boolean fitsOnPage(String candidate, Minecraft client) {
        if (candidate.length() > BookPasteBookLimits.MAX_PAGE_LENGTH) {
            return false;
        }

        if (candidate.isEmpty()) {
            return true;
        }

        String visibleText = stripFormattingCodes(candidate);
        int lineCount = client.font.split(Component.literal(visibleText), PAGE_WIDTH).size();
        return lineCount * client.font.lineHeight <= PAGE_HEIGHT;
    }

    private static String stripFormattingCodes(String text) {
        StringBuilder builder = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '\u00A7' && i + 1 < text.length()) {
                i++;
                continue;
            }
            builder.append(current);
        }

        return builder.toString();
    }

    private static String getCurrentPageContent(Object screen) throws ReflectiveOperationException {
        Object richEditBox = invokeMethodWithReturn(screen, "scribble$history$getRichEditBox");
        if (richEditBox == null) {
            return null;
        }

        Object richText = invokeMethodWithReturn(richEditBox, "getRichText");
        if (richText == null) {
            return null;
        }

        return (String)invokeMethodWithReturn(richText, "getAsFormattedString");
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

    private static void setOptionalFieldValue(Object target, String name, Object value) {
        try {
            setFieldValue(target, name, value);
        } catch (ReflectiveOperationException ignored) {
        }
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

    private static void invokeMethod(Object target, String name) throws ReflectiveOperationException {
        Method method = findMethod(target.getClass(), name);
        method.setAccessible(true);
        method.invoke(target);
    }

    private static Object invokeMethodWithReturn(Object target, String name) throws ReflectiveOperationException {
        Method method = findMethod(target.getClass(), name);
        method.setAccessible(true);
        return method.invoke(target);
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

    private static boolean hasMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            findMethod(type, name, parameterTypes);
            return true;
        } catch (NoSuchMethodException exception) {
            return false;
        }
    }
}
