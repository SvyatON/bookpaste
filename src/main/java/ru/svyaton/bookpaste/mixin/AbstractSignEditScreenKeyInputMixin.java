package ru.svyaton.bookpaste.mixin;
//? if >=26.1 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.input.KeyEvent;
*///?}
//? if >=1.21.10 {
//? if <26.1 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.input.KeyInput;
//?}
//?}
//? if >=1.19.3 {
//? if <1.21.10 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
//?}
//?}
//? if <1.19.3 {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;*/
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.svyaton.bookpaste.BookPasteInputUtil;
import ru.svyaton.bookpaste.SignPasteCompat;

//? if >=26.1 {
/*@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenKeyInputMixin {
    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(Minecraft.getInstance()) && SignPasteCompat.handlePaste(this)) {
            cir.setReturnValue(true);
        }
    }
}*/
//?}
//? if >=1.21.10 {
//? if <26.1 {
@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenKeyInputMixin {
    @Inject(method = "keyPressed(Lnet/minecraft/client/input/KeyInput;)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(MinecraftClient.getInstance()) && SignPasteCompat.handlePaste(this)) {
            cir.setReturnValue(true);
        }
    }
}
//?}
//?}
//? if >=1.19.3 {
//? if <1.21.10 {
/*@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenKeyInputMixin {
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(int keyCode, int scancode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(MinecraftClient.getInstance()) && SignPasteCompat.handlePaste(this)) {
            cir.setReturnValue(true);
        }
    }
}*/
//?}
//?}
//? if <1.19.3 {
/*@Mixin(SignEditScreen.class)
public abstract class AbstractSignEditScreenKeyInputMixin {
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    private void bookpaste$handlePaste(int keyCode, int scancode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (BookPasteInputUtil.isPasteShortcutPressed(MinecraftClient.getInstance()) && SignPasteCompat.handlePaste(this)) {
            cir.setReturnValue(true);
        }
    }
}*/
//?}
