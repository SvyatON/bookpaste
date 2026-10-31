package ru.svyaton.bookpaste.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.svyaton.bookpaste.BookPasteInputUtil;
import ru.svyaton.bookpaste.BookPasteScreenHandler;

@Mixin(EditBox.class)
abstract class EditBoxMixin {
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handleBookPaste(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (BookPasteInputUtil.isPasteShortcutPressed(client) && client.screen instanceof BookPasteScreenHandler) {
            if (((BookPasteScreenHandler) client.screen).bookpaste$handleLargePaste()) {
                cir.setReturnValue(true);
            }
        }
    }
}
