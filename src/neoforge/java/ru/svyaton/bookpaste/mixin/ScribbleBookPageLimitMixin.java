package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.svyaton.bookpaste.BookPasteBookLimits;

@Pseudo
@Mixin(targets = "me.chrr.scribble.screen.ScribbleBookEditScreen", remap = false)
abstract class ScribbleBookPageLimitMixin {
    @ModifyConstant(method = {"canInsertPages", "updateCurrentPages"}, constant = @Constant(intValue = 100), require = 0, remap = false)
    private int bookpaste$scribblePageLimit(int original) {
        return BookPasteBookLimits.pageLimit();
    }
}
