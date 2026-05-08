package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import ru.svyaton.bookpaste.BookPasteInputUtil;
import ru.svyaton.bookpaste.ScribbleCompat;

@Pseudo
@Mixin(targets = "me.chrr.scribble.gui.edit.RichEditBox", remap = false)
abstract class ScribbleRichEditBoxMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = false)
    private void bookpaste$handleScribblePaste(CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(MinecraftClient.getInstance()) && ScribbleCompat.handlePaste()) {
            cir.setReturnValue(true);
        }
    }
}
