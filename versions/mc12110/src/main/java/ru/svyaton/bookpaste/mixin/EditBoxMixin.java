package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.input.KeyInput;
import ru.svyaton.bookpaste.BookPasteScreenHandler;

@Mixin(EditBox.class)
abstract class EditBoxMixin {
    @Inject(method = "handleSpecialKey", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handleBookPaste(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (!input.isPaste()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof BookPasteScreenHandler handler && handler.bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }
}
