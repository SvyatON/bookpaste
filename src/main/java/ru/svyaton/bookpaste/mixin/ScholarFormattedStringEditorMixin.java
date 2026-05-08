package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.svyaton.bookpaste.ScholarCompat;

@Pseudo
@Mixin(targets = "io.github.mortuusars.scholar.client.gui.widget.textbox.text.FormattedStringEditor", remap = false)
abstract class ScholarFormattedStringEditorMixin {
    @Inject(method = "paste", at = @At("HEAD"), cancellable = true, remap = false)
    private void bookpaste$handleScholarPaste(boolean keepFormatting, CallbackInfo ci) {
        if (ScholarCompat.handlePaste()) {
            ci.cancel();
        }
    }
}

