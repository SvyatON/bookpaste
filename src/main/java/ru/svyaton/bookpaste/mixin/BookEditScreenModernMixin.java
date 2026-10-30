package ru.svyaton.bookpaste.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;*/
//?}
//? if >=1.21.10 {
//? if <26.1 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
//?}
//?}
//? if >=1.21.8 {
//? if <1.21.10 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;*/
//?}
//?}
//? if >=1.19 {
//? if <1.21.8 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;*/
//?}
//?}
//? if <1.19 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;*/
//?}
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

    //? if >=26.1 {
    /*@Shadow
    private Player owner;

    @Shadow
    private MultiLineEditBox page;*/
    //?}
    //? if >=1.21.10 {
    //? if <26.1 {
    @Shadow
    private PlayerEntity player;

    @Shadow
    private EditBoxWidget editBox;
    //?}
    //?}
    //? if >=26.1 {
    /*@Shadow
    private void updatePageContent() {
    }

    @Shadow
    private void updateButtonVisibility() {
    }

    @Shadow
    private void saveChanges() {
    }*/
    //?}
    //? if >=1.21.8 && <26.1 {
    //? if <1.21.10 {
    /*@Shadow
    private PlayerEntity player;

    @Shadow
    private EditBoxWidget editBox;*/
    //?}
    //?}
    //? if <1.21.8 {
    /*@Shadow
    private PlayerEntity player;

    @Shadow
    private boolean dirty;

    @Shadow
    private String getCurrentPageContent() {
        throw new AssertionError();
    }

    @Shadow
    private void setPageContent(String content) {
    }

    @Shadow
    private void updateButtons() {
    }

    @Shadow
    private void finalizeBook(boolean sign) {
    }*/
    //?}

    //? if >=1.21.8 && <26.1 {
    @Shadow
    private void updatePage() {
    }

    @Shadow
    private void updatePreviousPageButtonVisibility() {
    }

    @Shadow
    private void finalizeBook() {
    }
    //?}

    //? if >=26.1 {
    /*@Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(Minecraft.getInstance()) && bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }*/
    //?}
    //? if >=1.21.10 {
    //? if <26.1 {
    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyInput;)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(MinecraftClient.getInstance()) && bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }
    //?}
    //?}
    //? if <1.21.10 {
    /*@Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(int keyCode, int scancode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(MinecraftClient.getInstance()) && bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }*/
    //?}

    @Override
    public boolean bookpaste$handleLargePaste() {
        if (ru.svyaton.bookpaste.ScribbleCompat.handlePaste()) return true;
        //? if >=26.1 {
        /*Minecraft client = Minecraft.getInstance();
        String clipboard = client.keyboardHandler.getClipboard();*/
        //?} else {
        MinecraftClient client = MinecraftClient.getInstance();
        String clipboard = client.keyboard.getClipboard();
        //?}
        if (clipboard == null || clipboard.trim().isEmpty()) {
            return false;
        }

        String normalizedClipboard = PasteHelper.normalizeClipboard(clipboard);
        PasteResult result = bookpaste$fillBook(normalizedClipboard, client);
        if (!result.handled()) {
            return false;
        }

        //? if >=26.1 {
        /*client.keyboardHandler.setClipboard(result.remaining());*/
        //?} else {
        client.keyboard.setClipboard(result.remaining());
        //?}
        BookPasteMessages.show(client, bookpaste$createStatusMessage(result, client));

        return true;
    }

    @Unique
    private PasteResult bookpaste$fillBook(String clipboard, Object client) {
        if (this.pages.isEmpty()) {
            return new PasteResult(false, clipboard, 0);
        }

        List<String> workingPages = new ArrayList<>(this.pages);
        //? if >=26.1 {
        /*workingPages.set(this.currentPage, this.page.getValue());*/
        //?}
        //? if >=1.21.8 {
        //? if <26.1 {
        workingPages.set(this.currentPage, this.editBox.getText());
        //?}
        //?}
        //? if <1.21.8 {
        /*String currentContent = this.getCurrentPageContent();
        workingPages.set(this.currentPage, currentContent == null ? "" : currentContent);*/
        //?}

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
            //? if <1.21.8 {
            /*this.dirty = true;
            this.setPageContent(this.pages.get(this.currentPage));
            this.updateButtons();
            this.finalizeBook(false);*/
            //?}
            //? if >=26.1 {
            /*this.updatePageContent();
            this.updateButtonVisibility();
            this.saveChanges();*/
            //?}
            //? if >=1.21.8 && <26.1 {
            this.updatePage();
            this.updatePreviousPageButtonVisibility();
            this.finalizeBook();
            //?}
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

        //? if >=26.1 {
        /*Minecraft client = Minecraft.getInstance();
        int lineCount = client.font.split(Component.literal(candidate), BOOKPASTE_PAGE_WIDTH).size();
        return lineCount * client.font.lineHeight <= BOOKPASTE_PAGE_HEIGHT;*/
        //?}
        //? if >=1.19 {
        //? if <26.1 {
        MinecraftClient client = MinecraftClient.getInstance();
        int lineCount = client.textRenderer.wrapLines(Text.literal(candidate), BOOKPASTE_PAGE_WIDTH).size();
        return lineCount * client.textRenderer.fontHeight <= BOOKPASTE_PAGE_HEIGHT;
        //?}
        //?}
        //? if <1.19 {
        /*MinecraftClient client = MinecraftClient.getInstance();
        int lineCount = client.textRenderer.wrapLines(new LiteralText(candidate), BOOKPASTE_PAGE_WIDTH).size();
        return lineCount * client.textRenderer.fontHeight <= BOOKPASTE_PAGE_HEIGHT;*/
        //?}
    }

    @Unique
    //? if >=26.1 {
    /*private Component bookpaste$createStatusMessage(PasteResult result, Minecraft client) {
        return PasteHelper.createStatusMessage(result.pagesAdded(), result.remaining(),
                candidate -> bookpaste$fitsOnPage(candidate));
    }*/
    //?} else {
    private Text bookpaste$createStatusMessage(PasteResult result, MinecraftClient client) {
        return PasteHelper.createStatusMessage(result.pagesAdded(), result.remaining(),
                candidate -> bookpaste$fitsOnPage(candidate));
    }
    //?}

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
        //? if <1.21.8 {
        /*this.dirty = true;
        this.setPageContent(this.pages.get(this.currentPage));
        this.updateButtons();
        this.finalizeBook(false);*/
        //?}
        //? if >=26.1 {
        /*this.updatePageContent();
        this.updateButtonVisibility();
        this.saveChanges();*/
        //?}
        //? if >=1.21.8 && <26.1 {
        this.updatePage();
        this.updatePreviousPageButtonVisibility();
        this.finalizeBook();
        //?}
    }

    //? if >=26.1 {
    /*@Override
    public Player bookpaste$getPlayer() {
        return this.owner;
    }*/
    //?} else {
    @Override
    public PlayerEntity bookpaste$getPlayer() {
        return this.player;
    }
    //?}

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
