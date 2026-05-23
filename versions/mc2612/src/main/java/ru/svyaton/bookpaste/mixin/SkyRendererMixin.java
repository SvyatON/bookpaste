package ru.svyaton.bookpaste.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @Inject(method = "shouldRenderDarkDisc", at = @At("HEAD"), cancellable = true)
    private void bookpaste$preventCrashInSkyRenderer(CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance() == null || Minecraft.getInstance().player == null) {
            cir.setReturnValue(false);
        }
    }
}
