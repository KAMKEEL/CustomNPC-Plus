package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import noppes.npcs.controllers.TagController;
import noppes.npcs.controllers.data.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * Scroll list that renders CNPC tags next to item names.
 * Used in directory/fullscreen GUIs for Forms, Auras, Outlines, etc.
 */
public class GuiCustomScrollTagged extends GuiCustomScroll {
    private HashMap<String, HashSet<UUID>> itemTagMap = new HashMap<>();
    private boolean showTags = true;

    public GuiCustomScrollTagged(GuiScreen parent, int id) {
        super(parent, id);
    }

    public void setItemTagMap(HashMap<String, HashSet<UUID>> tagMap) {
        this.itemTagMap = tagMap != null ? tagMap : new HashMap<>();
    }

    public HashMap<String, HashSet<UUID>> getItemTagMap() {
        return itemTagMap;
    }

    public void setShowTags(boolean show) {
        this.showTags = show;
    }

    @Override
    protected void drawItems() {
        for (int i = 0; i < list.size(); i++) {
            int j = 4;
            int k = (14 * i + 4) - scrollY;
            if (k >= 4 && k + 12 < ySize) {
                int xOffset = scrollHeight < ySize - 8 ? 0 : 10;
                String rawEntry = list.get(i);
                String displayString = StatCollector.translateToLocal(rawEntry);

                String text = "";
                float maxWidth = showTags && itemTagMap.containsKey(rawEntry)
                    ? (xSize + xOffset - 8) * 0.5f
                    : (xSize + xOffset - 8) * 0.8f;

                if (fontRendererObj.getStringWidth(displayString) > maxWidth) {
                    for (int h = 0; h < displayString.length(); h++) {
                        char c = displayString.charAt(h);
                        text += c;
                        if (fontRendererObj.getStringWidth(text) > maxWidth)
                            break;
                    }
                    if (displayString.length() > text.length())
                        text += "...";
                } else {
                    text = displayString;
                }

                Integer customColor = colors.get(rawEntry);
                if (customColor != null) {
                    if (!text.isEmpty())
                        fontRendererObj.drawString(text, j, k, customColor);
                } else if ((multipleSelection && selectedList.contains(text)) || (!multipleSelection && selected == i)) {
                    drawVerticalLine(j - 2, k - 4, k + 10, 0xffffffff);
                    drawVerticalLine(j + xSize - 18 + xOffset, k - 4, k + 10, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k - 3, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k + 10, 0xffffffff);
                    fontRendererObj.drawString(text, j, k, 0xffffff);
                } else if (i == hover) {
                    fontRendererObj.drawString(text, j, k, 0x00ff00);
                } else {
                    fontRendererObj.drawString(text, j, k, 0xffffff);
                }

                // Draw tags after item name
                if (showTags && itemTagMap.containsKey(rawEntry)) {
                    int tagStartX = j + fontRendererObj.getStringWidth(text) + 3;
                    int maxX = j + xSize - 18 + xOffset;
                    HashSet<UUID> tagUUIDs = itemTagMap.get(rawEntry);
                    TagController tc = TagController.getInstance();
                    if (tc != null && tagUUIDs != null) {
                        for (UUID uuid : tagUUIDs) {
                            Tag tag = tc.getTagFromUUID(uuid);
                            if (tag != null && !tag.hideTag) {
                                String tagText = "[" + tag.name + "]";
                                int tagWidth = fontRendererObj.getStringWidth(tagText);
                                if (tagStartX + tagWidth > maxX) break;
                                fontRendererObj.drawString(tagText, tagStartX, k, tag.color);
                                tagStartX += tagWidth + 3;
                            }
                        }
                    }
                }
            }
        }
    }
}
