package com.mysticblazeyt.geartags.client.mixin;

import com.mysticblazeyt.geartags.client.event.AfterEntitiesEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "pushEntityRenders", at = @At("TAIL"))
    private void onAfterEntities(MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueue queue, CallbackInfo ci) {
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
        AfterEntitiesEvents.AFTER_ENTITIES.invoker().afterEntities((WorldRenderer)(Object)this, matrices, renderStates, queue, tickDelta);
    }
}