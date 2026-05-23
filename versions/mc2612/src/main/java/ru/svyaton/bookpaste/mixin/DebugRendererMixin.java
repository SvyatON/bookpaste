package ru.svyaton.bookpaste.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {

    @Inject(method = "emitGizmos", at = @At("HEAD"), cancellable = true)
    private void bookpaste$preventCrashInDebugRenderer(CallbackInfo ci) {
        if (Minecraft.getInstance() == null || Minecraft.getInstance().getConnection() == null) {
            ci.cancel();
        }
    }
}
