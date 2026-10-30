package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=26.1 {
/*import net.minecraft.client.Minecraft;*/
//?} else {
import net.minecraft.client.MinecraftClient;
//?}
import ru.svyaton.bookpaste.BookPasteInputUtil;
import ru.svyaton.bookpaste.ScribbleCompat;

@Pseudo
@Mixin(targets = "me.chrr.scribble.gui.edit.RichEditBox", remap = false)
abstract class ScribbleRichEditBoxMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, remap = false)
    private void bookpaste$handleScribblePaste(CallbackInfoReturnable<Boolean> cir) {
        //? if >=26.1 {
        /*if (BookPasteInputUtil.isPasteShortcutPressed(Minecraft.getInstance()) && ScribbleCompat.handlePaste()) {*/
        //?} else {
        if (BookPasteInputUtil.isPasteShortcutPressed(MinecraftClient.getInstance()) && ScribbleCompat.handlePaste()) {
        //?}
            cir.setReturnValue(true);
        }
    }
}
