package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.GuiNpcMobSpawner;
import noppes.npcs.controllers.data.Tag;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class GuiButtonScroll extends GuiCustomScroll
{
    private HashMap<Integer,GuiNpcButton> scrollButtons = new HashMap<Integer,GuiNpcButton>();
    public GuiButtonScroll(GuiScreen parent, int id)
    {
        super(parent, id);
    }

    @Override
    protected void drawItems()
    {
        for(int i = 0; i < list.size(); i++)
        {
            int j = 4;
            int k = (14 * i + 4) - scrollY;
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
                    fontRendererObj.drawString(text, j , k, 0xffffff);
                }
                else if(i == hover)
                    fontRendererObj.drawString(text, j , k, 0x00ff00);
                else
                    fontRendererObj.drawString(text, j , k, 0xffffff);
            }
        }
    }
}
