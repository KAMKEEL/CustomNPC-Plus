package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiHoverText extends GuiScreen{
	private int x, y;
	public int id;

    protected static final ResourceLocation buttonTextures = new ResourceLocation("customnpcs:textures/gui/info.png");
	private String text;
	public GuiHoverText(int id, String text, int x, int y){
		this.text = text;
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	@Override
    public void drawScreen(int par1, int par2, float par3){
		GL11.glColor4f(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(buttonTextures);
        this.drawTexturedModalRect(this.x, this.y, 0, 0, 12, 12);
        
        if(inArea(x, y, 12, 12, par1, par2)){
	        List<String> lines = new ArrayList<String>();
	        lines.add(text);
	        this.drawHoveringText(lines, x + 8, y + 6, this.fontRendererObj);
	        GL11.glDisable(GL11.GL_LIGHTING);
        }
    }
	public boolean inArea(int x, int y, int width, int height, int mouseX, int mouseY){
		if(mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height)
			return false;
		return true;
	}
}
