package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.svyaton.bookpaste.BookPasteBookLimits;

@Pseudo
@Mixin(targets = "io.github.mortuusars.scholar.client.gui.screen.edit.SpreadBookEditScreen", remap = false)
abstract class ScholarBookEditPageLimitMixin {
    @ModifyConstant(method = {"appendEmptyPage", "setBookContents", "/^lambda\\$insertEmptyPage\\$/"},
            constant = @Constant(intValue = 100), require = 0, remap = false)
    private int bookpaste$scholarEditPageLimit(int original) {
        return BookPasteBookLimits.pageLimit();
    }
}
