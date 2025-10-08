package com.mysticblazeyt.geartags.client;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.client.render.OverlayTexture;
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
        Commands.register();
        Keybinds.register();

        WorldRenderEvents.LAST.register(context -> {
            float tickDelta = context.tickCounter().getTickProgress(true);
            renderItemHud(context.matrixStack(), context.camera().getPos(), tickDelta, context.consumers());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Keybinds.onTick();
            Util.onTick();
        });
    }

    private void renderItemHud(MatrixStack matrices, Vec3d cameraPos, float tickDelta, VertexConsumerProvider vertexConsumers) {
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
            matrices.translate(-(overlayWidth / 2f), 0, 0);
            fillRect(matrices, overlayWidth, CONFIG.OVERLAY_BOX_HEIGHT, vertexConsumers);
            matrices.pop();

            int startX = -(itemCount * CONFIG.ITEM_SPACING) / 2;
            for (int i = 0; i < displayItems.size(); i++) {
                matrices.push();
                matrices.translate(startX + i * CONFIG.ITEM_SPACING + CONFIG.ITEM_ICON_OFFSET_X, CONFIG.ITEM_ICON_OFFSET_Y, 0);

                matrices.push();
                matrices.scale(CONFIG.ITEM_ICON_SCALE, CONFIG.ITEM_ICON_SCALE, CONFIG.ITEM_ICON_SCALE);
                renderItemWithOverlay(displayItems.get(i), matrices, client.textRenderer, vertexConsumers);
                matrices.pop();

                matrices.pop();
            }
            matrices.pop();
        }
    }

    private void renderItemWithOverlay(ItemStack stack, MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers) {
        if (stack == null || stack.isEmpty()) return;
        World world = client.world;
        int light = 15728880;
        int overlay = OverlayTexture.DEFAULT_UV;

        // Render Item Icon
        client.getItemRenderer().renderItem(stack, ItemDisplayContext.FIXED, light, overlay, matrices, vertexConsumers, world, 0);

        if (stack.getCount() > 1) {
            String count = String.valueOf(stack.getCount());
            float textX = 17 - textRenderer.getWidth(count) + CONFIG.TEXT_OFFSET_X;
            float textY = CONFIG.TEXT_OFFSET_Y;
            int color = 0xFFFFFFFF;
            boolean shadow = CONFIG.TEXT_SHADOW;

            matrices.push();
            matrices.translate(0f, 0f, -CONFIG.Z_SPACING); // Fix Overlapping Items
            matrices.scale(CONFIG.TEXT_SCALE, CONFIG.TEXT_SCALE, CONFIG.TEXT_SCALE);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180)); // Fix Mirrored Text

            // Negative because Earlier Code Mirrored the Quad
            float adjustedX = -textX - textRenderer.getWidth(count);
            float adjustedY = -textY - textRenderer.fontHeight;

            OrderedText orderedText = Text.literal(count).asOrderedText();
            Matrix4f model = matrices.peek().getPositionMatrix();

            textRenderer.draw(orderedText, adjustedX, adjustedY, color, shadow, model, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            matrices.pop();
        }
        else if (stack.isDamageable() && stack.isDamaged()) {
            int max = stack.getMaxDamage();
            int current = max - stack.getDamage();
            float percent = (float) current / max;
            int percentInt = Math.round(percent * 100f);
            String perc = percentInt + "%";
            float textX = 17 - textRenderer.getWidth(perc) + CONFIG.TEXT_OFFSET_X;
            float textY = CONFIG.TEXT_OFFSET_Y;
            int color = getDurabilityColor(current, max);
            boolean shadow = CONFIG.TEXT_SHADOW;

            matrices.push();
            matrices.translate(0f, 0f, -CONFIG.Z_SPACING); // Fix Overlapping Items
            matrices.scale(CONFIG.TEXT_SCALE, CONFIG.TEXT_SCALE, CONFIG.TEXT_SCALE);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180)); // Fix Mirrored Text

            // Negative because Earlier Code Mirrored the Quad
            float adjustedX = -textX - textRenderer.getWidth(perc);
            float adjustedY = -textY - textRenderer.fontHeight;

            OrderedText orderedText = (Text.literal(perc)).asOrderedText();
            Matrix4f model = matrices.peek().getPositionMatrix();

            textRenderer.draw(orderedText, adjustedX, adjustedY, color, shadow, model, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            matrices.pop();
        }
    }

    private int getDurabilityColor(int current, int max) {
        float percent = (float) current / (float) max;
        int alpha = 0xFF << 24;

        if (!CONFIG.DYNAMIC_DURABILITY_TEXT) return 0xFFFFFFFF;

        if (percent >= 0.95f) return alpha | 0x00FF00;
        else if (percent >= 0.90f) return alpha | 0x19FF00;
        else if (percent >= 0.85f) return alpha | 0x33FF00;
        else if (percent >= 0.80f) return alpha | 0x4DFF00;
        else if (percent >= 0.75f) return alpha | 0x66FF00;
        else if (percent >= 0.70f) return alpha | 0x80FF00;
        else if (percent >= 0.65f) return alpha | 0x99FF00;
        else if (percent >= 0.60f) return alpha | 0xB2FF00;
        else if (percent >= 0.55f) return alpha | 0xCCFF00;
        else if (percent >= 0.50f) return alpha | 0xE5FF00;
        else if (percent >= 0.45f) return alpha | 0xFFFF00;
        else if (percent >= 0.40f) return alpha | 0xFFCC00;
        else if (percent >= 0.35f) return alpha | 0xFFA500;
        else if (percent >= 0.30f) return alpha | 0xFF8000;
        else if (percent >= 0.25f) return alpha | 0xFF6600;
        else if (percent >= 0.20f) return alpha | 0xFF4000;
        else if (percent >= 0.15f) return alpha | 0xFF1A00;
        else if (percent >= 0.10f) return alpha | 0xFF0000;
        else if (percent >= 0.05f) return alpha | 0xCC0000;
        else return alpha | 0x990000;
    }

    private double lerp(float delta, double start, double end) {
        return start + (end - start) * delta;
    }

    private void fillRect(MatrixStack matrices, int width, int height, VertexConsumerProvider vertexConsumers) {
        int alpha = CONFIG.OVERLAY_BOX_ALPHA;
        if (alpha <= 0) return; // skip drawing if alpha is 0

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());
        float a = MathHelper.clamp(alpha, 0, 100) / 100.0F; // convert percentage to 0.0-1.0
        float r = (1291845632 >> 16 & 255) / 255.0F;
        float g = (1291845632 >> 8 & 255) / 255.0F;
        float b = (1291845632 & 255) / 255.0F;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        vertexConsumer.vertex(matrix, 0, -4, 0).color(r, g, b, a);                    // Top-left
        vertexConsumer.vertex(matrix, 0, -4 + height, 0).color(r, g, b, a);           // Bottom-left
        vertexConsumer.vertex(matrix, width, -4 + height, 0).color(r, g, b, a);       // Bottom-right
        vertexConsumer.vertex(matrix, width, -4, 0).color(r, g, b, a);                // Top-right

        vertexConsumer.vertex(matrix, 0, -4, 0).color(r, g, b, a);                    // Top-left
        vertexConsumer.vertex(matrix, width, -4 + height, 0).color(r, g, b, a);       // Bottom-right
    }
}