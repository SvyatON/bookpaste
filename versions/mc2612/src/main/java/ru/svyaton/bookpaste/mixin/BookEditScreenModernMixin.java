package ru.svyaton.bookpaste.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import ru.svyaton.bookpaste.BookPasteBookEditAccess;
import ru.svyaton.bookpaste.BookPasteBookLimits;
import ru.svyaton.bookpaste.BookPasteInputUtil;
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
    private MultiLineEditBox page;

    @Shadow
    private boolean dirty;

    @Shadow
    private void updatePageContent() {
    }

    @Shadow
    private void updateButtonVisibility() {
    }

    @Shadow
    private void saveChanges() {
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePasteShortcut(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (BookPasteInputUtil.isPasteShortcutPressed(client) && this.bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean bookpaste$handleLargePaste() {
        Minecraft client = Minecraft.getInstance();
        String clipboard = client.keyboardHandler.getClipboard();
        if (clipboard == null || clipboard.isBlank()) {
            return false;
        }

        String normalizedClipboard = PasteHelper.normalizeClipboard(clipboard);
        PasteResult result = this.bookpaste$fillBook(normalizedClipboard, client);
        if (!result.handled()) {
            return false;
        }

        client.keyboardHandler.setClipboard(result.remaining());
        if (this.owner != null) {
            this.owner.sendOverlayMessage(this.bookpaste$createStatusMessage(result, client));
        }
        return true;
    }

    @Unique
    private PasteResult bookpaste$fillBook(String clipboard, Minecraft client) {
        if (this.pages.isEmpty()) {
            return new PasteResult(false, clipboard, 0);
        }

        List<String> workingPages = new ArrayList<>(this.pages);
        workingPages.set(this.currentPage, this.page.getValue());

        String remaining = clipboard;
        int pagesUsed = 0;
        boolean changed = false;
        int lastTouchedPage = this.currentPage;

        for (int pageIndex = this.currentPage; pageIndex < BookPasteBookLimits.MAX_PAGE_COUNT; pageIndex++) {
            if (pageIndex >= workingPages.size()) {
                workingPages.add("");
            }

            String base = workingPages.get(pageIndex);
            PasteHelper.PageSlice slice = PasteHelper.buildPage(base, remaining, candidate -> this.bookpaste$fitsOnPage(candidate, client),
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
            this.updatePageContent();
            this.dirty = true;
            this.updateButtonVisibility();
        }

        return new PasteResult(true, remaining, pagesUsed);
    }

    @Unique
    private boolean bookpaste$fitsOnPage(String candidate, Minecraft client) {
        if (candidate.length() > BookPasteBookLimits.MAX_PAGE_LENGTH) {
            return false;
        }

        if (candidate.isEmpty()) {
            return true;
        }

        int lineCount = client.font.split(Component.literal(candidate), BOOKPASTE_PAGE_WIDTH).size();
        return lineCount * client.font.lineHeight <= BOOKPASTE_PAGE_HEIGHT;
    }

    @Unique
    private Component bookpaste$createStatusMessage(PasteResult result, Minecraft client) {
        return PasteHelper.createStatusMessage(result.pagesAdded(), result.remaining(),
                candidate -> this.bookpaste$fitsOnPage(candidate, client));
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
        this.updatePageContent();
        this.dirty = true;
        this.updateButtonVisibility();
    }

    @Override
    public Player bookpaste$getPlayer() {
        return this.owner;
    }

    @Unique
    private record PasteResult(boolean handled, String remaining, int pagesAdded) {
    }
}
