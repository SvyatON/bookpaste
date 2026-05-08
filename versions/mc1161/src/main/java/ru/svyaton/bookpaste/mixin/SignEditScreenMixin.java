package ru.svyaton.bookpaste.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.svyaton.bookpaste.SignPasteCompat;

@Mixin(SignEditScreen.class)
public abstract class SignEditScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (bookpaste$isPasteShortcut(keyCode) && SignPasteCompat.handlePaste(this)) {
            cir.setReturnValue(true);
        }
    }

    private static boolean bookpaste$isPasteShortcut(int keyCode) {
        return (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_V)
                || (Screen.hasShiftDown() && keyCode == GLFW.GLFW_KEY_INSERT);
    }
}
