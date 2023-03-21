package noppes.npcs.client.gui.util;

import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.GuiNpcMobSpawner;
import noppes.npcs.controllers.data.Tag;

import java.util.HashSet;

public class GuiCustomScrollCloner extends GuiCustomScroll {
    private final GuiNpcMobSpawner parent;

    public GuiCustomScrollCloner(GuiNpcMobSpawner parent, int id) {
        super(parent, id);
        this.parent = parent;
    }

    protected void drawItems()
    {
        int l = 0;
        for(int i = 0; i < list.size(); i++)
        {
            if (this.parent.tags.containsKey(list.get(i))) {
                HashSet<Tag> tags = this.parent.tags.get(list.get(i));
                boolean inFilter = false;
                for (Tag tag : tags) {
                    if (GuiNpcMobSpawner.filter.contains(tag.name)) {
                        inFilter = true;
                        break;
                    }
                }
                if (!inFilter) {
                    continue;
                }
            } else if (!GuiNpcMobSpawner.showNoTags) {
                continue;
            }

            int j = 4;
            int k = (14 * l + 4) - scrollY;
            l++;
            if(k >= 4 && k + 12 < ySize)
            {

                int xOffset = scrollHeight < ySize - 8?0:10;
                String displayString = StatCollector.translateToLocal(list.get(i));

                String text = "";
                float maxWidth = (xSize + xOffset - 8) * 0.8f;
                if(fontRendererObj.getStringWidth(displayString) > maxWidth){
                    for(int h = 0; h < displayString.length(); h++){
                        char c = displayString.charAt(h);
                        text += c;
                        if(fontRendererObj.getStringWidth(text) > maxWidth)
                            break;
                    }
                    if(displayString.length() > text.length())
                        text += "...";
                }
                else
                    text = displayString;
                if((multipleSelection && selectedList.contains(text)) || (!multipleSelection && selected == i)) {
                    drawVerticalLine(j-2, k-4, k + 10, 0xffffffff);
                    drawVerticalLine(j + xSize - 18 + xOffset, k - 4, k + 10, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k - 3 , 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k + 10 , 0xffffffff);
                    fontRendererObj.drawString(text, j , k, this.colors.getOrDefault(text, 0xffffff));
                } else if(i == hover) {
                    fontRendererObj.drawString(text, j, k, 0x00ff00);
                } else {
                    fontRendererObj.drawString(text, j, k, this.colors.getOrDefault(text, 0xffffff));
                }

                int tagStartX = j + fontRendererObj.getStringWidth(displayString) + 5;
                if (this.parent.tags.containsKey(list.get(i))) {
                    for (Tag tag : this.parent.tags.get(list.get(i))) {
                        if (!tag.getIsHidden()) {
                            fontRendererObj.drawString("[" + tag.name + "]", tagStartX, k, tag.color);
                            tagStartX += fontRendererObj.getStringWidth("[" + tag.name + "]") + 2;
                        }
                    }
                }
            }
        }
    }
}
