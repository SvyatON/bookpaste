package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import ru.svyaton.bookpaste.BookPasteInputUtil;
import ru.svyaton.bookpaste.BookPasteScreenHandler;

@Mixin(EditBox.class)
abstract class EditBoxMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handleBookPaste(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (!BookPasteInputUtil.isPasteShortcutPressed(client)) {
            return;
        }

        if (client.screen instanceof BookPasteScreenHandler handler && handler.bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }
}
