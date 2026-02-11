package com.mysticblazeyt.geartags.client;

import com.mysticblazeyt.geartags.client.enums.DurabilityDisplayMode;
import com.mysticblazeyt.geartags.client.enums.RenderMode;
import com.mysticblazeyt.geartags.client.enums.TextDisplayMode;
import com.mysticblazeyt.geartags.client.event.AfterEntitiesEvents;
import com.mysticblazeyt.geartags.client.mixin.ItemRenderStateAccessor;
import com.mysticblazeyt.geartags.client.mixin.LayerRenderStateAccessor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class GearTagsClient implements ClientModInitializer {
    public static MinecraftClient client = MinecraftClient.getInstance();
    public static GearTagsConfig CONFIG;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(GearTagsConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(GearTagsConfig.class).getConfig();
        Keybinds.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Keybinds.onTick();
            Util.onTick();
        });

        AfterEntitiesEvents.AFTER_ENTITIES.register((worldRenderer, matrices, renderStates, queue, tickDelta) -> {
            Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
            VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
            renderItemHud(matrices, cameraPos, tickDelta, immediate, queue);
            immediate.draw();
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("geartags")
                .executes(Util.queueOpenConfig())
        ));
    }

    public static void renderItemHud(MatrixStack matrices, Vec3d cameraPos, float tickDelta, VertexConsumerProvider vertexConsumers, OrderedRenderCommandQueue queue) {
        if (!CONFIG.ENABLED) return;

        if (client.player == null || client.world == null) return;

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;

            ItemStack[] allSlots = new ItemStack[]{
                    player.getOffHandStack(),
                    player.getEquippedStack(EquipmentSlot.FEET),
                    player.getEquippedStack(EquipmentSlot.LEGS),
                    player.getEquippedStack(EquipmentSlot.CHEST),
                    player.getEquippedStack(EquipmentSlot.HEAD),
                    player.getMainHandStack()
            };

            List<ItemStack> displayItems = new ArrayList<>();
            for (ItemStack stack : allSlots) {
                if (stack != null && !stack.isEmpty() && stack.getItem() != Items.AIR) displayItems.add(stack);
            }
            int itemCount = displayItems.size();
            if (itemCount == 0) continue;

            int overlayWidth = itemCount * CONFIG.ITEM_SPACING + CONFIG.PADDING * 2;

            double px = lerp(tickDelta, player.lastX, player.getX());
            double py = lerp(tickDelta, player.lastY, player.getY());
            double pz = lerp(tickDelta, player.lastZ, player.getZ());

            double x = px - cameraPos.x;
            double y = py - cameraPos.y + player.getStandingEyeHeight() + CONFIG.OVERLAY_OFFSET_Y;
            double z = pz - cameraPos.z;

            matrices.push();
            matrices.translate(x, y, z);

            Camera camera = client.gameRenderer.getCamera();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

            matrices.scale(CONFIG.OVERLAY_SCALE, CONFIG.OVERLAY_SCALE, CONFIG.OVERLAY_SCALE);

            matrices.push();
            matrices.translate(-(overlayWidth / 2f), 0, CONFIG.OVERLAY_BOX_Z_SPACING);
            fillRect(matrices, overlayWidth, CONFIG.OVERLAY_BOX_HEIGHT, vertexConsumers);
            matrices.pop();

            int startX = -(itemCount * CONFIG.ITEM_SPACING) / 2;
            for (int i = 0; i < displayItems.size(); i++) {
                matrices.push();
                matrices.translate(startX + i * CONFIG.ITEM_SPACING + CONFIG.ITEM_ICON_OFFSET_X, CONFIG.ITEM_ICON_OFFSET_Y, 0);

                matrices.push();
                matrices.scale(CONFIG.ITEM_ICON_SCALE, CONFIG.ITEM_ICON_SCALE, CONFIG.ITEM_ICON_SCALE);
                renderItemWithOverlay(displayItems.get(i), matrices, client.textRenderer, vertexConsumers, queue);
                matrices.pop();

                matrices.pop();
            }
            matrices.pop();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static void renderItemWithOverlay(ItemStack stack, MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers, OrderedRenderCommandQueue queue) {
        if (stack == null || stack.isEmpty()) return;
        int light = 15728880;

        boolean renderIcon = CONFIG.RENDER_MODE == RenderMode.ICON_ONLY || CONFIG.RENDER_MODE == RenderMode.BOTH;
        boolean renderText = CONFIG.RENDER_MODE == RenderMode.TEXT_ONLY || CONFIG.RENDER_MODE == RenderMode.BOTH;

        if (renderIcon) {
            matrices.push();
            try {
                ItemModelManager itemModelManager = client.getItemModelManager();
                ItemRenderState renderState = new ItemRenderState();
                itemModelManager.clearAndUpdate(renderState, stack, CONFIG.ITEM_DISPLAY_CONTEXT, client.world, null, 0);
                ItemRenderStateAccessor renderStateAccessor = (ItemRenderStateAccessor) renderState;

                if (renderStateAccessor.getLayerCount() <= 0) return;

                ItemRenderState.LayerRenderState layerState = renderStateAccessor.getLayers()[0];
                LayerRenderStateAccessor layerAccessor = (LayerRenderStateAccessor) layerState;
                RenderLayer layer = layerAccessor.getRenderLayer();

                if (layer == null) renderState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
                else {
                    List<BakedQuad> quads = layerState.getQuads();

                    int[] tints = layerAccessor.getTints();
                    if (tints == null) tints = new int[]{-1};

                    ItemRenderState.Glint glint = layerAccessor.getGlint();

                    Transformation transform = layerAccessor.getTransform();
                    if (transform == null) return;

                    transform.apply(CONFIG.ITEM_DISPLAY_CONTEXT.isLeftHand(), matrices.peek());

                    ItemRenderer.renderItem(CONFIG.ITEM_DISPLAY_CONTEXT, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, tints, quads, layer, glint);
                }
            } finally {
                matrices.pop();
            }
        }

        if (renderText) {
            boolean renderCount = CONFIG.TEXT_DISPLAY_MODE == TextDisplayMode.COUNT_ONLY || CONFIG.TEXT_DISPLAY_MODE == TextDisplayMode.BOTH;
            boolean renderDurability = CONFIG.TEXT_DISPLAY_MODE == TextDisplayMode.DURABILITY_ONLY || CONFIG.TEXT_DISPLAY_MODE == TextDisplayMode.BOTH;
            if (stack.getCount() > 1 && renderCount) {
                String count = String.valueOf(stack.getCount());

                float width = textRenderer.getWidth(count);
                float height = textRenderer.fontHeight;
                float textX = -(width / 2) + CONFIG.TEXT_OFFSET_X;
                float textY = -(height / 2) + CONFIG.TEXT_OFFSET_Y;
                int color = CONFIG.DEFAULT_TEXT_COLOR;
                boolean shadow = CONFIG.TEXT_SHADOW;

                matrices.push();
                matrices.translate(0f, 0f, -CONFIG.Z_SPACING);
                matrices.scale(CONFIG.TEXT_SCALE, CONFIG.TEXT_SCALE, CONFIG.TEXT_SCALE);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));

                OrderedText orderedText = Text.literal(count).asOrderedText();
                Matrix4f model = matrices.peek().getPositionMatrix();

                textRenderer.draw(orderedText, textX, textY, color, shadow, model, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
                matrices.pop();
            } else if (stack.isDamageable() && stack.isDamaged() && renderDurability) {
                int max = stack.getMaxDamage();
                int current = max - stack.getDamage();
                String durability = getDurabilityString(current, max);

                float width = textRenderer.getWidth(durability);
                float height = textRenderer.fontHeight;
                float textX = -(width / 2) + CONFIG.TEXT_OFFSET_X;
                float textY = -(height / 2) + CONFIG.TEXT_OFFSET_Y;
                int color = getDurabilityColor(current, max);
                boolean shadow = CONFIG.TEXT_SHADOW;

                matrices.push();
                matrices.translate(0f, 0f, -CONFIG.Z_SPACING);
                matrices.scale(CONFIG.TEXT_SCALE, CONFIG.TEXT_SCALE, CONFIG.TEXT_SCALE);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));

                OrderedText orderedText = Text.literal(durability).asOrderedText();
                Matrix4f model = matrices.peek().getPositionMatrix();

                textRenderer.draw(orderedText, textX, textY, color, shadow, model, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
                matrices.pop();
            }
        }
    }

    private static String getDurabilityString(int current, int max) {
        if (CONFIG.DURABILITY_DISPLAY_MODE == DurabilityDisplayMode.NUMBER) return String.valueOf(current);
        if (CONFIG.DURABILITY_DISPLAY_MODE == DurabilityDisplayMode.FRACTION) return current + "/" + max;
        int percent = Math.round(((float) current / max) * 100f);
        return percent + "%" ;
    }

    private static int getDurabilityColor(int current, int max) {
        float percent = (float) current / (float) max;
        int alpha = 0xFF << 24;

        if (!CONFIG.DYNAMIC_DURABILITY_TEXT) return CONFIG.DEFAULT_TEXT_COLOR;

        if (percent >= 0.95f) return alpha | 0x00FF00;
        if (percent >= 0.90f) return alpha | 0x19FF00;
        if (percent >= 0.85f) return alpha | 0x33FF00;
        if (percent >= 0.80f) return alpha | 0x4DFF00;
        if (percent >= 0.75f) return alpha | 0x66FF00;
        if (percent >= 0.70f) return alpha | 0x80FF00;
        if (percent >= 0.65f) return alpha | 0x99FF00;
        if (percent >= 0.60f) return alpha | 0xB2FF00;
        if (percent >= 0.55f) return alpha | 0xCCFF00;
        if (percent >= 0.50f) return alpha | 0xE5FF00;
        if (percent >= 0.45f) return alpha | 0xFFFF00;
        if (percent >= 0.40f) return alpha | 0xFFCC00;
        if (percent >= 0.35f) return alpha | 0xFFA500;
        if (percent >= 0.30f) return alpha | 0xFF8000;
        if (percent >= 0.25f) return alpha | 0xFF6600;
        if (percent >= 0.20f) return alpha | 0xFF4000;
        if (percent >= 0.15f) return alpha | 0xFF1A00;
        if (percent >= 0.10f) return alpha | 0xFF0000;
        if (percent >= 0.05f) return alpha | 0xCC0000;
        return alpha | 0x990000;
    }

    private static void fillRect(MatrixStack matrices, int width, int height, VertexConsumerProvider vertexConsumers) {
        if (CONFIG.OVERLAY_BOX_ALPHA <= 0) return;

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayers.debugFilledBox());
        float a = CONFIG.OVERLAY_BOX_ALPHA / 100.0F;
        float r = (1291845632 >> 16 & 255) / 255.0F;
        float g = (1291845632 >> 8 & 255) / 255.0F;
        float b = (1291845632 & 255) / 255.0F;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        vertexConsumer.vertex(matrix, 0, -4, 0).color(r, g, b, a);                    // Top-left
        vertexConsumer.vertex(matrix, 0, -4 + height, 0).color(r, g, b, a);           // Bottom-left
        vertexConsumer.vertex(matrix, width, -4 + height, 0).color(r, g, b, a);       // Bottom-right
        vertexConsumer.vertex(matrix, width, -4, 0).color(r, g, b, a);                // Top-right
    }

    private static double lerp(float delta, double start, double end) {
        return start + (end - start) * delta;
    }
}