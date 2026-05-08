package ru.svyaton.bookpaste.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import ru.svyaton.bookpaste.BookPasteBookLimits;
import ru.svyaton.bookpaste.BookPasteBookEditAccess;
import ru.svyaton.bookpaste.BookPasteTextCompat;
import ru.svyaton.bookpaste.BookPasteScreenHandler;
import ru.svyaton.bookpaste.PasteHelper;

@Mixin(BookEditScreen.class)
abstract class BookEditScreenLegacyMixin implements BookPasteScreenHandler, BookPasteBookEditAccess {
    @Unique
    private static final int BOOKPASTE_PAGE_WIDTH = 114;

    @Unique
    private static final int BOOKPASTE_PAGE_HEIGHT = 128;

    @Shadow
    private int currentPage;

    @Shadow
    private List<String> pages;

    @Shadow
    private PlayerEntity player;

    @Shadow
    private boolean dirty;

    @Shadow
    private String getCurrentPageContent() {
        return "";
    }

    @Shadow
    private void setPageContent(String newContent) {
    }

    @Shadow
    private void finalizeBook(boolean signBook) {
    }

    @Shadow
    private void updateButtons() {
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void bookpaste$onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (bookpaste$isPasteShortcut(keyCode) && this.bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean bookpaste$handleLargePaste() {
        MinecraftClient client = MinecraftClient.getInstance();
        String clipboard = client.keyboard.getClipboard();
        if (clipboard == null || clipboard.trim().isEmpty()) {
            return false;
        }

        String normalizedClipboard = PasteHelper.normalizeClipboard(clipboard);
        PasteResult result = bookpaste$fillBook(normalizedClipboard, client);
        if (!result.handled()) {
            return false;
        }

        if (!result.remaining().isEmpty()) {
            client.keyboard.setClipboard(result.remaining());
        }
        this.player.sendMessage(bookpaste$createStatusMessage(result, client), true);
        return true;
    }

    @Unique
    private PasteResult bookpaste$fillBook(String clipboard, MinecraftClient client) {
        if (this.pages.isEmpty()) {
            return new PasteResult(false, clipboard, 0);
        }

        List<String> workingPages = new ArrayList<>(this.pages);
        workingPages.set(this.currentPage, this.getCurrentPageContent());

        String remaining = clipboard;
        int pagesUsed = 0;
        boolean changed = false;
        int lastTouchedPage = this.currentPage;

        for (int pageIndex = this.currentPage; pageIndex < BookPasteBookLimits.MAX_PAGE_COUNT; pageIndex++) {
            if (pageIndex >= workingPages.size()) {
                workingPages.add("");
            }

            String base = workingPages.get(pageIndex);
            PasteHelper.PageSlice slice = PasteHelper.buildPage(base, remaining, candidate -> bookpaste$fitsOnPage(candidate, client),
                    pageIndex == BookPasteBookLimits.MAX_PAGE_COUNT - 1);
            boolean consumedText = !slice.remainingText().equals(remaining);

            if (!slice.pageText().equals(base)) {
                workingPages.set(pageIndex, slice.pageText());
                changed = true;
            }

            if (consumedText) {
                pagesUsed++;
                lastTouchedPage = pageIndex;
            }

            remaining = slice.remainingText();
            if (remaining.isEmpty()) {
                break;
            }
        }

        if (changed) {
            this.pages.clear();
            this.pages.addAll(workingPages);
            this.currentPage = lastTouchedPage;
            this.setPageContent(workingPages.get(this.currentPage));
            this.dirty = true;
            this.updateButtons();
            this.finalizeBook(false);
        }

        return new PasteResult(true, remaining, pagesUsed);
    }

    @Unique
    private boolean bookpaste$fitsOnPage(String candidate, MinecraftClient client) {
        if (candidate.length() > BookPasteBookLimits.MAX_PAGE_LENGTH) {
            return false;
        }

        if (candidate.isEmpty()) {
            return true;
        }

        int lineCount = client.textRenderer.wrapLines(BookPasteTextCompat.literal(candidate), BOOKPASTE_PAGE_WIDTH).size();
        return lineCount * client.textRenderer.fontHeight <= BOOKPASTE_PAGE_HEIGHT;
    }

    @Unique
    private Text bookpaste$createStatusMessage(PasteResult result, MinecraftClient client) {
        return PasteHelper.createStatusMessage(result.pagesAdded(), result.remaining(),
                candidate -> bookpaste$fitsOnPage(candidate, client));
    }

    @Override
    public List<String> bookpaste$getPages() {
        return this.pages;
    }

    @Override
    public int bookpaste$getCurrentPage() {
        return this.currentPage;
    }

    @Override
    public void bookpaste$setPagesAndCurrentPage(List<String> pages, int currentPage) {
        this.pages.clear();
        this.pages.addAll(pages);
        this.currentPage = currentPage;
        this.setPageContent(this.pages.get(this.currentPage));
        this.dirty = true;
        this.updateButtons();
        this.finalizeBook(false);
    }

    @Override
    public PlayerEntity bookpaste$getPlayer() {
        return this.player;
    }

    @Unique
    private static boolean bookpaste$isPasteShortcut(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_V && BookEditScreen.hasControlDown()
                || keyCode == GLFW.GLFW_KEY_INSERT && BookEditScreen.hasShiftDown();
    }

    @Unique
    private static final class PasteResult {
        private final boolean handled;
        private final String remaining;
        private final int pagesAdded;

        private PasteResult(boolean handled, String remaining, int pagesAdded) {
            this.handled = handled;
            this.remaining = remaining;
            this.pagesAdded = pagesAdded;
        }

        private boolean handled() {
            return this.handled;
        }

        private String remaining() {
            return this.remaining;
        }

        private int pagesAdded() {
            return this.pagesAdded;
        }
    }
}
