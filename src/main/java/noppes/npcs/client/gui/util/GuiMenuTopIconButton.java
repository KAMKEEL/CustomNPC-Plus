package noppes.npcs.client.gui.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiMenuTopIconButton extends GuiMenuTopButton{
    private static final ResourceLocation resource = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    protected static RenderItem itemRender = new RenderItem();
    private ItemStack item;
    
	public GuiMenuTopIconButton(int i, int x, int y, String s, ItemStack item) {
		super(i, x, y, s);
		width = 28;
		height = 28;
		this.item = item;
	}
	
    public GuiMenuTopIconButton(int i, GuiButton parent, String s, ItemStack item){
    	super(i,parent, s);
		width = 28;
		height = 28;
		this.item = item;
    }

    public GuiMenuTopIconButton(int i, int x, int y, String s, IButtonListener listener, ItemStack item) {
		super(i, x, y, s);
		width = 28;
		height = 28;
		this.item = item;
		this.listener = listener;
	}

    public GuiMenuTopIconButton(int i, GuiButton parent, String s,
			IButtonListener listener, ItemStack item) {
		super(i, parent, s, listener);
		width = 28;
		height = 28;
		this.item = item;
	}

	@Override
    public void drawButton(Minecraft minecraft, int i, int j){
        if (!getVisible()){
            return;
        }
        if(item.getItem() == null)
        	item = new ItemStack(Blocks.dirt);
        hover = i >= xPosition && j >= yPosition && i < xPosition + getWidth() && j < yPosition + height;
        Minecraft mc = Minecraft.getMinecraft();
        if(hover && !active){
        	int x = i + mc.fontRenderer.getStringWidth(displayString);
            GL11.glTranslatef(x, yPosition + 2, 0);
        	drawHoveringText(Arrays.asList(new String[] {displayString}), 0, 0, mc.fontRenderer);
            GL11.glTranslatef(-x, -(yPosition + 2), 0);
        }
        mc.getTextureManager().bindTexture(resource);
        GL11.glPushMatrix();
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        this.drawTexturedModalRect(xPosition, yPosition + (active?2:0), 0, active?32:0, 28, 28);
        this.zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), item, xPosition + 6, yPosition + 10);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, mc.getTextureManager(), item, xPosition + 6, yPosition + 10);
        RenderHelper.disableStandardItemLighting(); 
        GL11.glDisable(GL11.GL_LIGHTING);
        itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
        GL11.glPopMatrix();

        
    }
    protected void drawHoveringText(List p_146283_1_, int p_146283_2_, int p_146283_3_, FontRenderer font)
    {
        if (!p_146283_1_.isEmpty())
        {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;
            Iterator iterator = p_146283_1_.iterator();

            while (iterator.hasNext())
            {
                String s = (String)iterator.next();
                int l = font.getStringWidth(s);

                if (l > k)
                {
                    k = l;
                }
            }

            int j2 = p_146283_2_ + 12;
            int k2 = p_146283_3_ - 12;
            int i1 = 8;

            if (p_146283_1_.size() > 1)
            {
                i1 += 2 + (p_146283_1_.size() - 1) * 10;
            }

            if (j2 + k > this.width)
            {
                j2 -= 28 + k;
            }

            if (k2 + i1 + 6 > this.height)
            {
                k2 = this.height - i1 - 6;
            }

            this.zLevel = 300.0F;
            itemRender.zLevel = 300.0F;
            int j1 = -267386864;
            this.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
            this.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
            this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
            this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
            this.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
            int k1 = 1347420415;
            int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
            this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
            this.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
            this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
            this.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);

            for (int i2 = 0; i2 < p_146283_1_.size(); ++i2)
            {
                String s1 = (String)p_146283_1_.get(i2);
                font.drawStringWithShadow(s1, j2, k2, -1);

                if (i2 == 0)
                {
                    k2 += 2;
                }

                k2 += 10;
            }

            this.zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }
}
