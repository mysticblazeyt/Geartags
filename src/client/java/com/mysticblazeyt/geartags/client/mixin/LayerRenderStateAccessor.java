package com.mysticblazeyt.geartags.client.mixin;

import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.json.Transformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderState.LayerRenderState.class)
public interface LayerRenderStateAccessor {
    @Accessor("renderLayer")
    RenderLayer getRenderLayer();

    @Accessor("tints")
    int[] getTints();

    @Accessor("glint")
    ItemRenderState.Glint getGlint();

    @Accessor("transform")
    Transformation getTransform();
}