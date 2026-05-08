package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import ru.svyaton.bookpaste.BookPasteScreenHandler;

@Mixin(EditBox.class)
abstract class EditBoxMixin {
    @Inject(method = "handleSpecialKey", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handleBookPaste(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (!bookpaste$isPasteShortcut(keyCode)) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof BookPasteScreenHandler handler && handler.bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }

    private static boolean bookpaste$isPasteShortcut(int keyCode) {
        return keyCode == InputUtil.GLFW_KEY_V && Screen.hasControlDown()
                || keyCode == InputUtil.GLFW_KEY_INSERT && Screen.hasShiftDown();
    }
}
