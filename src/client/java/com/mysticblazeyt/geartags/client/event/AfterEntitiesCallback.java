package com.mysticblazeyt.geartags.client.event;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;

@FunctionalInterface
public interface AfterEntitiesCallback {
    void afterEntities(WorldRenderer worldRenderer, MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueue queue, float tickDelta);
}