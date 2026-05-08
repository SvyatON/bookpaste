package ru.svyaton.bookpaste.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.svyaton.bookpaste.BookPasteInputUtil;
import ru.svyaton.bookpaste.SignPasteCompat;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenKeyInputMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(MinecraftClient.getInstance()) && SignPasteCompat.handlePaste(this)) {
            cir.setReturnValue(true);
        }
    }
}
