package com.mysticblazeyt.geartags.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "geartags")
public class GearTagsConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    @Comment("Whether GearTags is Enabled")
    public boolean ENABLED = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether the Text has Shadow")
    public boolean TEXT_SHADOW = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether the Durability Text changes Colors according to Durability")
    public boolean DYNAMIC_DURABILITY_TEXT = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Z Spacing between Different Layers to Prevent Overlapping")
    public float Z_SPACING = 0.15f;

    @ConfigEntry.Gui.Tooltip
    @Comment("How High (Above the Player) the Overlay should be Drawn")
    public float OVERLAY_OFFSET_Y = 1.3f;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Size of the Overlay")
    public float OVERLAY_SCALE = 0.04f;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Opacity of the Overlay Background Box")
    public int OVERLAY_BOX_ALPHA = 30;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Height of the Overlay Background Box")
    public int OVERLAY_BOX_HEIGHT = 12;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Space Between Items")
    public int ITEM_SPACING = 12;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Length of the Leading & Trailing Spaces")
    public int PADDING = 4;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Size of the Item Icon")
    public float ITEM_ICON_SCALE = 8f;

    @ConfigEntry.Gui.Tooltip
    @Comment("Horizontal offset of the item icon")
    public float ITEM_ICON_OFFSET_X = 5.0f;

    @ConfigEntry.Gui.Tooltip
    @Comment("Vertical offset of the item icon")
    public float ITEM_ICON_OFFSET_Y = 2.0f;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Size of the Overlay Text")
    public float TEXT_SCALE = 0.07f;

    @ConfigEntry.Gui.Tooltip
    @Comment("Horizontal offset of the text")
    public float TEXT_OFFSET_X = -10.0f;

    @ConfigEntry.Gui.Tooltip
    @Comment("Vertical offset of the text")
    public float TEXT_OFFSET_Y = -6.0f;
}