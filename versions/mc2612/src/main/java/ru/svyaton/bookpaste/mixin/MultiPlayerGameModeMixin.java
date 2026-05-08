package ru.svyaton.bookpaste.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "ensureHasSentCarriedItem", at = @At("HEAD"), cancellable = true)
    private void bookpaste$preventCrashWhenKicked(CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) {
            ci.cancel();
        }
    }
}
