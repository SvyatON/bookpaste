package ru.svyaton.bookpaste.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bookpaste$preventCrashInTick(CallbackInfo ci) {
        if (this.minecraft == null || this.minecraft.player == null) {
            ci.cancel();
        }
    }
}
