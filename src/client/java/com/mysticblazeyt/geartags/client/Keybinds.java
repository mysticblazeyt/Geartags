package com.mysticblazeyt.geartags.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import static com.mysticblazeyt.geartags.client.GearTagsClient.CONFIG;
import static com.mysticblazeyt.geartags.client.GearTagsClient.client;

public class Keybinds {
    private static KeyBinding toggleKey;

    public static void register() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.geartags.toggle", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.Category.MISC));
    }

    public static void onTick() {
        if (toggleKey.wasPressed()) {
            if (client.player == null || client.world == null) return;
            CONFIG.ENABLED = !CONFIG.ENABLED;
            Text message = CONFIG.ENABLED ? Text.literal("GearTags: Enabled Overlay").formatted(Formatting.GREEN) : Text.literal("GearTags: Disabled Overlay").formatted(Formatting.RED);
            client.player.sendMessage(message, true);
        }
    }
}