package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.svyaton.bookpaste.ScholarCompat;

@Pseudo
@Mixin(targets = "io.github.mortuusars.scholar.client.gui.screen.SpreadBookScreen", remap = false)
abstract class ScholarBookPageLimitMixin {
    @ModifyConstant(method = "getPageCount", constant = @Constant(intValue = 100), require = 0, remap = false)
    private int bookpaste$scholarPageLimit(int original) {
        return ScholarCompat.pageLimit(this, original);
    }
}
