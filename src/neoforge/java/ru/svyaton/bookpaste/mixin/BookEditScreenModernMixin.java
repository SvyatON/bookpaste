package ru.svyaton.bookpaste.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.svyaton.bookpaste.BookPasteBookEditAccess;
import ru.svyaton.bookpaste.BookPasteInputUtil;
import ru.svyaton.bookpaste.BookPasteMessages;
import ru.svyaton.bookpaste.BookPasteScreenHandler;
import ru.svyaton.bookpaste.PasteHelper;

@Mixin(BookEditScreen.class)
abstract class BookEditScreenModernMixin implements BookPasteScreenHandler, BookPasteBookEditAccess {
    @Unique
    private static final int BOOKPASTE_PAGE_WIDTH = 114;

    @Unique
    private static final int BOOKPASTE_PAGE_HEIGHT = 128;

    @Shadow
    private int currentPage;

    @Shadow
    private List<String> pages;

    @Shadow
    private Player owner;

    @Shadow
    private boolean isModified;

    @Shadow
    private String getCurrentPageText() {
        throw new AssertionError();
    }

    @Shadow
    private void setCurrentPageText(String content) {
    }

    @Shadow
    private void clearDisplayCacheAfterPageChange() {
    }

    @Shadow
    private void updateButtonVisibility() {
    }

    @Shadow
    private void saveChanges(boolean sign) {
    }

    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(int keyCode, int scancode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(Minecraft.getInstance()) && bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean bookpaste$handleLargePaste() {
        if (ru.svyaton.bookpaste.ScribbleCompat.handlePaste()) return true;

        Minecraft client = Minecraft.getInstance();
        String clipboard = client.keyboardHandler.getClipboard();

        if (clipboard == null || clipboard.trim().isEmpty()) {
            return false;
        }

        String normalizedClipboard = PasteHelper.normalizeClipboard(clipboard);
        PasteResult result = bookpaste$fillBook(normalizedClipboard, client);
        if (!result.handled()) {
            return false;
        }

        client.keyboardHandler.setClipboard(result.remaining());

        BookPasteMessages.show(client, bookpaste$createStatusMessage(result, client));

        return true;
    }

    @Unique
    private PasteResult bookpaste$fillBook(String clipboard, Object client) {
        if (this.pages.isEmpty()) {
            return new PasteResult(false, clipboard, 0);
        }

        List<String> workingPages = new ArrayList<>(this.pages);

        String currentContent = this.getCurrentPageText();
        workingPages.set(this.currentPage, currentContent == null ? "" : currentContent);

        String remaining = clipboard;
        int pagesUsed = 0;
        boolean changed = false;
        int lastTouchedPage = this.currentPage;

        int pageLimit = ru.svyaton.bookpaste.BookPasteBookLimits.pageLimit();
        for (int pageIndex = this.currentPage; pageIndex < pageLimit; pageIndex++) {
            if (pageIndex >= workingPages.size()) {
                workingPages.add("");
            }

            String base = workingPages.get(pageIndex);
            PasteHelper.PageSlice slice = PasteHelper.buildPage(base, remaining, candidate -> bookpaste$fitsOnPage(candidate),
                    pageIndex == pageLimit - 1);
            boolean consumedText = !slice.remainingText().equals(remaining);
            if (!consumedText && base.isEmpty()) {
                break;
            }

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

            this.isModified = true;
            this.setCurrentPageText(this.pages.get(this.currentPage));
            this.updateButtonVisibility();
            this.saveChanges(false);

        }

        return new PasteResult(true, remaining, pagesUsed);
    }

    @Unique
    private boolean bookpaste$fitsOnPage(String candidate) {
        if (candidate.length() > 1024) {
            return false;
        }

        if (candidate.isEmpty()) {
            return true;
        }

        Minecraft client = Minecraft.getInstance();
        int lineCount = client.font.split(Component.literal(candidate), BOOKPASTE_PAGE_WIDTH).size();
        return lineCount * client.font.lineHeight <= BOOKPASTE_PAGE_HEIGHT;
    }

    @Unique

    private Component bookpaste$createStatusMessage(PasteResult result, Minecraft client) {
        return PasteHelper.createStatusMessage(result.pagesAdded(), result.remaining(),
                candidate -> bookpaste$fitsOnPage(candidate));
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

        this.isModified = true;
        this.setCurrentPageText(this.pages.get(this.currentPage));
        this.clearDisplayCacheAfterPageChange();
        this.updateButtonVisibility();
        this.saveChanges(false);
    }

    @Override
    public Player bookpaste$getPlayer() {
        return this.owner;
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

        public boolean handled() {
            return this.handled;
        }

        public String remaining() {
            return this.remaining;
        }

        public int pagesAdded() {
            return this.pagesAdded;
        }
    }
}
