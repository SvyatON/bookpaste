package ru.svyaton.bookpaste.mixin;

import net.minecraft.client.gui.screens.inventory.BookEditScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.svyaton.bookpaste.BookPasteBookLimits;

@Mixin(BookEditScreen.class)
abstract class UnlimitedBookEditScreenMixin {
    @ModifyConstant(method = "appendPageToBook", constant = @Constant(intValue = 100), require = 1, allow = 1)
    private int bookpaste$editorPageLimit(int original) {
        return BookPasteBookLimits.pageLimit();
    }
}
