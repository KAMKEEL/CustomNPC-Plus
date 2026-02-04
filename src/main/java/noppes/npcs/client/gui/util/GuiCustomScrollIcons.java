package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiCustomScrollIcons extends GuiCustomScroll {
    public static final int ICON_NONE = 0;
    public static final int ICON_TAB = 1;
    public static final int ICON_FOLDER = 2;

    private static final ResourceLocation TAB_TEXTURE = new ResourceLocation("customnpcs", "textures/gui/cloner/tab.png");
    private static final ResourceLocation FOLDER_TEXTURE = new ResourceLocation("customnpcs", "textures/gui/cloner/folder.png");

    private static final int ICON_RENDER_SIZE = 10;
    private static final int ICON_PADDING = 14;

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
        Minecraft mc = Minecraft.getMinecraft();
        for (int i = 0; i < list.size(); i++) {
            int j = 4;
            int k = (14 * i + 4) - scrollY;
            if (k >= 4 && k + 12 < ySize) {
                int xOffset = scrollHeight < ySize - 8 ? 0 : 10;

                int iconType = (i < iconTypes.size()) ? iconTypes.get(i) : ICON_NONE;
                int textX = j;
                if (iconType != ICON_NONE) {
                    textX = j + ICON_PADDING;
                }

                // Draw icon texture
                if (iconType == ICON_FOLDER || iconType == ICON_TAB) {
                    ResourceLocation tex = (iconType == ICON_FOLDER) ? FOLDER_TEXTURE : TAB_TEXTURE;
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.getTextureManager().bindTexture(tex);
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    drawFullTexture(j, k - 1, ICON_RENDER_SIZE, ICON_RENDER_SIZE);
                    GL11.glDisable(GL11.GL_BLEND);
                }

                // Draw text
                String displayString = StatCollector.translateToLocal(list.get(i));
                String text = "";
                float maxWidth = (xSize + xOffset - 8 - (iconType != ICON_NONE ? ICON_PADDING : 0)) * 0.8f;
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

                int itemColor = this.colors.getOrDefault(list.get(i), 0xffffff);

                if ((multipleSelection && selectedList.contains(list.get(i))) || (!multipleSelection && selected == i)) {
                    drawVerticalLine(j - 2, k - 4, k + 10, 0xffffffff);
                    drawVerticalLine(j + xSize - 18 + xOffset, k - 4, k + 10, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k - 3, 0xffffffff);
                    drawHorizontalLine(j - 2, j + xSize - 18 + xOffset, k + 10, 0xffffffff);
                    fontRendererObj.drawString(text, textX, k, itemColor);
                } else if (i == hover) {
                    fontRendererObj.drawString(text, textX, k, 0x00ff00);
                } else {
                    fontRendererObj.drawString(text, textX, k, itemColor);
                }
            }
        }
    }

    /**
     * Draw a full texture (UV 0,0 to 1,1) scaled to the given width/height.
     * Works with any texture size (not just 256x256).
     */
    private void drawFullTexture(int x, int y, int w, int h) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + h, 0, 0, 1);
        tessellator.addVertexWithUV(x + w, y + h, 0, 1, 1);
        tessellator.addVertexWithUV(x + w, y, 0, 1, 0);
        tessellator.addVertexWithUV(x, y, 0, 0, 0);
        tessellator.draw();
    }
}
