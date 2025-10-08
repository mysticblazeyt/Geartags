package com.mysticblazeyt.geartags.client;

import com.mojang.brigadier.Command;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screen.Screen;

import static com.mysticblazeyt.geartags.client.GearTagsClient.client;

public class Util {
    private static boolean openConfig = false;

    public static void onTick() {
        if(openConfig) {
            openConfigScreen(client.currentScreen);
            openConfig = false;
        }
    }

    public static Command<FabricClientCommandSource> queueOpenConfig() {
        return context -> {
            openConfig = true;
            return 1;
        };
    }

    public static void openConfigScreen(Screen parent) {
        Screen screen = AutoConfig.getConfigScreen(GearTagsConfig.class, parent).get();
        client.setScreen(screen);
    }
}