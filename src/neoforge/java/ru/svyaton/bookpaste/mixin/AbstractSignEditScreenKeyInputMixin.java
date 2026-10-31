package ru.svyaton.bookpaste.mixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.svyaton.bookpaste.BookPasteInputUtil;
import ru.svyaton.bookpaste.SignPasteCompat;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenKeyInputMixin {
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(int keyCode, int scancode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(Minecraft.getInstance()) && SignPasteCompat.handlePaste(this)) {
            cir.setReturnValue(true);
        }
    }
}
