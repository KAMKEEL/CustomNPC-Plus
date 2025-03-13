package noppes.npcs.client.gui.util;

import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.GuiNpcMobSpawner;
import noppes.npcs.controllers.data.Tag;

import java.util.UUID;

public class GuiCustomScrollCloner extends GuiCustomScroll {
    private final GuiNpcMobSpawner parent;

    public GuiCustomScrollCloner(GuiNpcMobSpawner parent, int id) {
        super(parent, id);
        this.parent = parent;
    }

    protected void drawItems() {
        int l = 0;
        for (int i = 0; i < list.size(); i++) {
            int j = 4;
            int k = (14 * l + 4) - scrollY;
            l++;
            if (k >= 4 && k + 12 < ySize) {

                int xOffset = scrollHeight < ySize - 8 ? 0 : 10;
                String displayString = StatCollector.translateToLocal(list.get(i));

                String text = "";
                float maxWidth = (xSize + xOffset - 8) * 0.8f;
                if (fontRendererObj.getStringWidth(displayString) > maxWidth) {
                    for (int h = 0; h < displayString.length(); h++) {
                        char c = displayString.charAt(h);
                        text += c;
                        if (fontRendererObj.getStringWidth(text) > maxWidth)
                            break;
                    }
                    if (displayString.length() > text.length())
                        text += "...";
                } else
                    text = displayString;
                if ((multipleSelection && selectedList.contains(text)) || (!multipleSelection && selected == i)) {
                    drawVerticalLine(j - 2, k - 4, k + 10, 0xffffffff);
                    drawVerticalLine(j + xSize - 18 + xOffset, k - 4, k + 10, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k - 3, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k + 10, 0xffffffff);
                    fontRendererObj.drawString(text, j, k, this.colors.getOrDefault(text, 0xffffff));
                } else if (i == hover) {
                    fontRendererObj.drawString(text, j, k, 0x00ff00);
                } else {
                    fontRendererObj.drawString(text, j, k, this.colors.getOrDefault(text, 0xffffff));
                }

                if ((this.parent.getShowingClones() == 0 || this.parent.getShowingClones() == 2) && this.parent.tagMap != null) {
                    int tagStartX = j + fontRendererObj.getStringWidth(displayString) + 3;
                    if (GuiNpcMobSpawner.displayTags == 0 || GuiNpcMobSpawner.displayTags == 1) {
                        if (this.parent.tagMap.hasClone(list.get(i))) {
                            for (UUID tagUUID : this.parent.tagMap.getUUIDsList(list.get(i))) {
                                Tag tag = GuiNpcMobSpawner.tags.get(tagUUID);
                                if (tag != null) {
                                    if (GuiNpcMobSpawner.displayTags == 1 || !tag.getIsHidden()) {
                                        fontRendererObj.drawString("[" + tag.name + "]", tagStartX, k, tag.color);
                                        tagStartX += fontRendererObj.getStringWidth("[" + tag.name + "]") + 3;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
