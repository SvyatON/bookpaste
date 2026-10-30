package ru.svyaton.bookpaste;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//? if >=26.1 {
/*import net.minecraft.client.Minecraft;*/
//?} else {
import net.minecraft.client.MinecraftClient;
//?}
//? if >=26.1 {
/*import net.minecraft.client.gui.screens.Screen;*/
//?} else {
import net.minecraft.client.gui.screen.Screen;
//?}
//? if >=26.1 {
/*import net.minecraft.network.chat.Component;*/
//?}
//? if >=1.19 {
//? if <26.1 {
import net.minecraft.text.Text;
//?}
//?}
//? if <1.19 {
/*import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;*/
//?}

public final class ScribbleCompat {
    private static final int PAGE_WIDTH = 114;
    private static final int PAGE_HEIGHT = 128;
    private static boolean pasteShortcutWasDown;

    private ScribbleCompat() {
    }

    //? if >=26.1 {
    /*public static void tick(Minecraft client) {*/
    //?} else {
    public static void tick(MinecraftClient client) {
    //?}
        boolean pasteShortcutDown = BookPasteInputUtil.isPasteShortcutPressed(client);
        if (pasteShortcutDown && !pasteShortcutWasDown) {
            handlePaste();
        }
        pasteShortcutWasDown = pasteShortcutDown;
    }

    public static boolean handlePaste() {
        //? if >=26.1 {
        /*Minecraft client = Minecraft.getInstance();*/
        //?} else {
        MinecraftClient client = MinecraftClient.getInstance();
        //?}
        //? if >=26.2 {
        /*if (!(client.gui.screen() instanceof Screen)) {*/
        //?}
        //? if >=26.1 {
//? if <26.2 {
        /*if (!(client.screen instanceof Screen)) {*/
        //?}
        //?}
        //? if <26.1 {
        if (!(client.currentScreen instanceof Screen)) {
        //?}
            return false;
        }

        //? if >=26.2 {
        /*Screen screen = (Screen)client.gui.screen();*/
        //?}
        //? if >=26.1 {
//? if <26.2 {
        /*Screen screen = (Screen)client.screen;*/
        //?}
        //?}
        //? if <26.1 {
        Screen screen = (Screen)client.currentScreen;
        //?}

        //? if >=26.1 {
        /*String clipboard = client.keyboardHandler.getClipboard();*/
        //?} else {
        String clipboard = client.keyboard.getClipboard();
        //?}
        if (clipboard == null || clipboard.trim().isEmpty()) {
            return false;
        }

        try {
            BookPasteBookEditAccess access = (screen instanceof BookPasteBookEditAccess) ? (BookPasteBookEditAccess) screen : null;
            boolean isScribble = isScribbleScreen(screen);
            if (!isScribble) {
                return false;
            }

            List<String> workingPages;
            int currentPage;

            if (access != null) {
                List<String> cachedPages = ScribblePageAccess.readLegacyCache(screen, access.bookpaste$getPages().size());
                workingPages = cachedPages != null ? cachedPages : new ArrayList<>(access.bookpaste$getPages());
                currentPage = access.bookpaste$getCurrentPage();
            } else {
                workingPages = getScribblePages(screen);
                currentPage = ScribblePageAccess.focusedPage(screen);
            }

            while (workingPages.size() <= currentPage) {
                workingPages.add("");
            }

            String currentPageContent = getCurrentPageContent(screen, currentPage);
            if (currentPageContent != null) {
                workingPages.set(currentPage, currentPageContent);
            }

            String remaining = PasteHelper.normalizeClipboard(clipboard);
            int pagesUsed = 0;
            boolean changed = false;
            int lastTouchedPage = currentPage;

            int pageLimit = BookPasteBookLimits.pageLimit();
            for (int pageIndex = currentPage; pageIndex < pageLimit; pageIndex++) {
                while (workingPages.size() <= pageIndex) {
                    workingPages.add("");
                }

                String base = workingPages.get(pageIndex);
                PasteHelper.PageSlice slice = PasteHelper.buildPage(base, remaining,
                        candidate -> fitsOnPage(candidate, client),
                        pageIndex == pageLimit - 1);
                String nextRemaining = slice.remainingText();
                boolean consumed = !nextRemaining.equals(remaining);
                if (consumed && !nextRemaining.isEmpty()) {
                    nextRemaining = ScribblePageAccess.continueFormatting(slice.pageText(), nextRemaining);
                    consumed = !nextRemaining.equals(remaining);
                }
                if (!consumed && base.isEmpty()) {
                    break;
                }

                if (!slice.pageText().equals(base)) {
                    workingPages.set(pageIndex, slice.pageText());
                    changed = true;
                }

                if (consumed) {
                    pagesUsed++;
                    lastTouchedPage = pageIndex;
                }

                remaining = nextRemaining;
                if (remaining.isEmpty()) {
                    break;
                }
            }

            if (changed) {
                if (access != null) {
                    List<String> originalPages = new ArrayList<>(access.bookpaste$getPages());
                    int originalPage = access.bookpaste$getCurrentPage();
                    ScribblePageAccess.LegacyReplacement replacement = ScribblePageAccess.stageLegacy(screen, workingPages, originalPages);
                    try {
                        replacement.commit();
                        setOptionalFieldValue(screen, "scribble$dirty", true);
                        access.bookpaste$setPagesAndCurrentPage(workingPages, lastTouchedPage);
                        if (hasMethod(screen.getClass(), "scribble$history$switchPage", int.class)) {
                            Method switchPage = findMethod(screen.getClass(), "scribble$history$switchPage", int.class);
                            switchPage.setAccessible(true);
                            switchPage.invoke(screen, lastTouchedPage);
                        }
                    } catch (ReflectiveOperationException | RuntimeException exception) {
                        try {
                            replacement.rollback();
                            access.bookpaste$setPagesAndCurrentPage(originalPages, originalPage);
                        } catch (ReflectiveOperationException | RuntimeException restoreFailure) {
                            exception.addSuppressed(restoreFailure);
                        }
                        throw exception;
                    }
                } else {
                    ScribblePageAccess.replacePages(screen, workingPages, lastTouchedPage);
                }
            }

            //? if >=26.1 {
            /*client.keyboardHandler.setClipboard(remaining);*/
            //?} else {
            client.keyboard.setClipboard(remaining);
            //?}
            sendStatus(client, pagesUsed, remaining);
            pasteShortcutWasDown = true;
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            java.util.logging.Logger.getLogger("BookPaste").log(java.util.logging.Level.WARNING, "Could not paste into Scribble", exception);
            return false;
        }
    }

    private static List<String> getScribblePages(Screen screen) throws ReflectiveOperationException {
        return ScribblePageAccess.readPages(screen);
    }

    //? if >=26.1 {
    /*private static void sendStatus(Minecraft client, int pagesUsed, String remaining) {*/
    //?} else {
    private static void sendStatus(MinecraftClient client, int pagesUsed, String remaining) {
    //?}
        if (client.player == null) {
            return;
        }
        BookPasteMessages.show(client, PasteHelper.createStatusMessage(pagesUsed, remaining,
                candidate -> fitsOnPage(candidate, client)));
    }

    private static boolean isScribbleScreen(Screen screen) {
        if (screen.getClass().getName().contains("ScribbleBookEditScreen")) {
            return true;
        }
        try {
            findField(screen.getClass(), "pages");
            if (screen.getClass().getName().contains("me.chrr.scribble")) {
                return true;
            }
        } catch (NoSuchFieldException ignored) {
        }
        try {
            findField(screen.getClass(), "richPages");
            if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("scribble")) return true;
        } catch (NoSuchFieldException ignored) {
        }
        try {
            findField(screen.getClass(), "synchronizedPages");
            return true;
        } catch (NoSuchFieldException e) {
            return hasMethod(screen.getClass(), "scribble$history$getRichEditBox");
        }
    }

    //? if >=26.1 {
    /*private static boolean fitsOnPage(String candidate, Minecraft client) {*/
    //?} else {
    private static boolean fitsOnPage(String candidate, MinecraftClient client) {
    //?}
        if (candidate.length() > 1024) {
            return false;
        }

        if (candidate.isEmpty()) {
            return true;
        }

        String visibleText = stripFormattingCodes(candidate);
        Object formatted = candidate.indexOf('\u00a7') >= 0 ? ScribblePageAccess.parseFormattedText(candidate) : null;
        //? if >=26.1 {
        /*net.minecraft.network.chat.FormattedText text = formatted instanceof net.minecraft.network.chat.FormattedText
                ? (net.minecraft.network.chat.FormattedText) formatted : Component.literal(visibleText);
        int lineCount = client.font.split(text, PAGE_WIDTH).size();
        return lineCount * client.font.lineHeight <= PAGE_HEIGHT;*/
    //?}
    //? if >=1.19 {
    //? if <26.1 {
    net.minecraft.text.StringVisitable text = formatted instanceof net.minecraft.text.StringVisitable
            ? (net.minecraft.text.StringVisitable) formatted : Text.literal(visibleText);
    int lineCount = client.textRenderer.wrapLines(text, PAGE_WIDTH).size();
    return lineCount * client.textRenderer.fontHeight <= PAGE_HEIGHT;
    //?}
    //?}
    //? if >=1.16.5 && <1.19 {
    /*net.minecraft.text.StringVisitable text = formatted instanceof net.minecraft.text.StringVisitable
            ? (net.minecraft.text.StringVisitable) formatted : new LiteralText(visibleText);
    int lineCount = client.textRenderer.wrapLines(text, PAGE_WIDTH).size();
    return lineCount * client.textRenderer.fontHeight <= PAGE_HEIGHT;*/
    //?}
    //? if <1.16.5 {
    /*net.minecraft.text.StringRenderable text = formatted instanceof net.minecraft.text.StringRenderable
            ? (net.minecraft.text.StringRenderable) formatted : new LiteralText(visibleText);
    int lineCount = client.textRenderer.wrapLines(text, PAGE_WIDTH).size();
    return lineCount * client.textRenderer.fontHeight <= PAGE_HEIGHT;*/
    //?}
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

    private static String getCurrentPageContent(Object screen, int currentPage) throws ReflectiveOperationException {
        if (hasMethod(screen.getClass(), "scribble$history$getRichEditBox")) {
            Object editBox = invokeMethodWithReturn(screen, "scribble$history$getRichEditBox");
            if (editBox != null) {
                return ScribblePageAccess.formattedText(invokeMethodWithReturn(editBox, "getRichText"));
            }
        }
        if (screen instanceof BookPasteBookEditAccess) {
            List<String> cached = ScribblePageAccess.readLegacyCache(screen,
                    ((BookPasteBookEditAccess) screen).bookpaste$getPages().size());
            if (cached != null && currentPage < cached.size()) return cached.get(currentPage);
        }
        if (screen.getClass().getName().contains("ScribbleBookEditScreen")) {
            List<String> pages = ScribblePageAccess.readPages(screen);
            if (currentPage < pages.size()) return pages.get(currentPage);
        }
        return null;
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
