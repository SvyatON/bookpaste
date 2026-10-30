package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
*///?}
//? if >=1.21.10 {
//? if <26.1 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
//?}
//?}
//? if <1.21.10 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;*/
//?}
import ru.svyaton.bookpaste.BookPasteInputUtil;
import ru.svyaton.bookpaste.BookPasteScreenHandler;

//? if >=26.2 {
/*@Mixin(EditBox.class)
abstract class EditBoxMixin {
    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handleBookPaste(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (!input.isPaste()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() instanceof BookPasteScreenHandler handler && handler.bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }
}*/
//?}
//? if >=26.1 {
//? if <26.2 {
/*@Mixin(EditBox.class)
abstract class EditBoxMixin {
    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handleBookPaste(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (!input.isPaste()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.screen instanceof BookPasteScreenHandler handler && handler.bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }
}*/
//?}
//?}
//? if >=1.21.10 {
//? if <26.1 {
@Mixin(TextFieldWidget.class)
abstract class EditBoxMixin {
    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyInput;)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handleBookPaste(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (!input.isPaste()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof BookPasteScreenHandler handler && handler.bookpaste$handleLargePaste()) {
            cir.setReturnValue(true);
        }
    }
}
//?}
//?}
//? if <1.21.10 {
/*@Mixin(TextFieldWidget.class)
abstract class EditBoxMixin {
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handleBookPaste(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (BookPasteInputUtil.isPasteShortcutPressed(client) && client.currentScreen instanceof BookPasteScreenHandler) {
            if (((BookPasteScreenHandler) client.currentScreen).bookpaste$handleLargePaste()) {
                cir.setReturnValue(true);
            }
        }
    }
}*/
//?}
