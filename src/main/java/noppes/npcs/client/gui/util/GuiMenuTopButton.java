package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

public class GuiMenuTopButton extends GuiNpcButton
{
	public static final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/menutopbutton.png");
    protected int height;
    public boolean active;
    public boolean hover = false;
    public boolean rotated = false;

    public IButtonListener listener;

    public GuiMenuTopButton(int i, int j, int k, String s)
    {
    	super(i,j,k,StatCollector.translateToLocal(s));
        active = false;

        width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayString) + 12;
        height = 20;
    }
    public GuiMenuTopButton(int i, GuiButton parent, String s)
    {
    	this(i, parent.xPosition + parent.width, parent.yPosition, s);
    }

    public GuiMenuTopButton(int i, GuiButton parent, String s,
    		IButtonListener listener) {
		this(i, parent, s);
		this.listener = listener;
	}

    @Override
    public int getHoverState(boolean flag)
    {
        byte byte0 = 1;
        if (active)
        {
            byte0 = 0;
        }
        else if (flag)
        {
            byte0 = 2;
        }
        return byte0;
    }

    @Override
    public void drawButton(Minecraft minecraft, int i, int j)
    {
        if (!getVisible())
        {
            return;
        }        
        GL11.glPushMatrix();
        minecraft.renderEngine.bindTexture(resource);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int height = this.height - (active?0:2);
        hover = i >= xPosition && j >= yPosition && i < xPosition + getWidth() && j < yPosition + height;
        int k = getHoverState(hover);
        drawTexturedModalRect(xPosition, yPosition, 0,  k * 20, getWidth() / 2, height);
        drawTexturedModalRect(xPosition + getWidth() / 2, yPosition, 200 - getWidth() / 2,  k * 20, getWidth() / 2, height);
        mouseDragged(minecraft, i, j);
        FontRenderer fontrenderer = minecraft.fontRenderer;
        if(rotated)
        	GL11.glRotatef(90, 1, 0, 0);
        if (active)
        {
            drawCenteredString(fontrenderer, displayString, xPosition + getWidth() / 2, yPosition + (height - 8) / 2, 0xffffa0);
        }
        else if (hover)
        {
            drawCenteredString(fontrenderer, displayString, xPosition + getWidth() / 2, yPosition + (height - 8) / 2, 0xffffa0);
        }
        else
        {
            drawCenteredString(fontrenderer, displayString, xPosition + getWidth() / 2, yPosition + (height - 8) / 2, 0xe0e0e0);
        }
        GL11.glPopMatrix();
    }

	@Override
    protected void mouseDragged(Minecraft minecraft, int i, int j)
    {
    }

    @Override
    public void mouseReleased(int i, int j)
    {
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j)
    {
    	boolean bo = !active && getVisible() && hover;
    	if(bo && listener != null){
    		listener.actionPerformed(this);
    		return false;
    	}
        return bo;
    }
}
