package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.List;

public class GuiCustomScrollIcons extends GuiCustomScroll {
    public static final int ICON_NONE = 0;
    public static final int ICON_TAB = 1;
    public static final int ICON_FOLDER = 2;

    private static final int ICON_WIDTH = 14;
    private static final int FOLDER_COLOR = 0xFFDDAA00;
    private static final int TAB_COLOR = 0xFF6688AA;

    private List<Integer> iconTypes = new ArrayList<>();

    public GuiCustomScrollIcons(GuiScreen parent, int id) {
        super(parent, id);
    }

    public void setListWithIcons(List<String> names, List<Integer> icons) {
        this.setUnsortedList(names);
        this.iconTypes = new ArrayList<>(icons);
    }

    @Override
    protected void drawItems() {
        for (int i = 0; i < list.size(); i++) {
            int j = 4;
            int k = (14 * i + 4) - scrollY;
            if (k >= 4 && k + 12 < ySize) {
                int xOffset = scrollHeight < ySize - 8 ? 0 : 10;

                int iconType = (i < iconTypes.size()) ? iconTypes.get(i) : ICON_NONE;
                int textX = j;
                if (iconType != ICON_NONE) {
                    textX = j + ICON_WIDTH;
                }

                // Draw icon
                if (iconType == ICON_FOLDER) {
                    drawRect(j, k - 1, j + 10, k + 7, FOLDER_COLOR);
                } else if (iconType == ICON_TAB) {
                    drawRect(j, k - 1, j + 10, k + 7, TAB_COLOR);
                }

                // Draw text
                String displayString = StatCollector.translateToLocal(list.get(i));
                String text = "";
                float maxWidth = (xSize + xOffset - 8 - (iconType != ICON_NONE ? ICON_WIDTH : 0)) * 0.8f;
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

                if ((multipleSelection && selectedList.contains(list.get(i))) || (!multipleSelection && selected == i)) {
                    drawVerticalLine(j - 2, k - 4, k + 10, 0xffffffff);
                    drawVerticalLine(j + xSize - 18 + xOffset, k - 4, k + 10, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k - 3, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k + 10, 0xffffffff);
                    fontRendererObj.drawString(text, textX, k, 0xffffff);
                } else if (i == hover) {
                    fontRendererObj.drawString(text, textX, k, 0x00ff00);
                } else {
                    fontRendererObj.drawString(text, textX, k, 0xffffff);
                }
            }
        }
    }
}
