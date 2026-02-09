package com.mysticblazeyt.geartags.client;

import com.mysticblazeyt.geartags.client.enums.DurabilityDisplayMode;
import com.mysticblazeyt.geartags.client.enums.RenderMode;
import com.mysticblazeyt.geartags.client.enums.TextDisplayMode;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.item.ItemDisplayContext;

@Config(name = "geartags")
public class GearTagsConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    @Comment("Whether GearTags is Enabled")
    public boolean ENABLED = true;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether the Text has Shadow")
    public boolean TEXT_SHADOW = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker(allowAlpha = true)
    @Comment("The Default Color of the Text")
    public int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;

    @ConfigEntry.Gui.Tooltip
    @Comment("Whether the Durability Text changes Colors according to Durability")
    public boolean DYNAMIC_DURABILITY_TEXT = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @Comment("What should be displayed (Text, Icon or Both)")
    public RenderMode RENDER_MODE = RenderMode.BOTH;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @Comment("What text should be displayed (Count, Durability or Both)")
    public TextDisplayMode TEXT_DISPLAY_MODE = TextDisplayMode.BOTH;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @Comment("How should durability be displayed (Percentage or Number)")
    public DurabilityDisplayMode DURABILITY_DISPLAY_MODE = DurabilityDisplayMode.PERCENTAGE;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @Comment("Which Item Display Context should be used")
    public ItemDisplayContext ITEM_DISPLAY_CONTEXT = ItemDisplayContext.FIXED;

    @ConfigEntry.Gui.Tooltip
    @Comment("Z Spacing between Different Layers to Prevent Overlapping")
    public float Z_SPACING = 0.251f;

    @ConfigEntry.Gui.Tooltip
    @Comment("How far behind (the Item Icon) the Overlay Background Box is Rendered")
    public float OVERLAY_BOX_Z_SPACING = 0.0f;

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
    public int OVERLAY_BOX_HEIGHT = 16;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Space Between Items")
    public int ITEM_SPACING = 18;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Length of the Leading & Trailing Spaces")
    public int PADDING = 2;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Size of the Item Icon")
    public float ITEM_ICON_SCALE = 12f;

    @ConfigEntry.Gui.Tooltip
    @Comment("Horizontal offset of the item icon")
    public float ITEM_ICON_OFFSET_X = 8.5f;

    @ConfigEntry.Gui.Tooltip
    @Comment("Vertical offset of the item icon")
    public float ITEM_ICON_OFFSET_Y = 4.0f;

    @ConfigEntry.Gui.Tooltip
    @Comment("The Size of the Overlay Text")
    public float TEXT_SCALE = 0.055f;

    @ConfigEntry.Gui.Tooltip
    @Comment("Horizontal offset of the text")
    public float TEXT_OFFSET_X = 0.0f;

    @ConfigEntry.Gui.Tooltip
    @Comment("Vertical offset of the text")
    public float TEXT_OFFSET_Y = 2.0f;
}