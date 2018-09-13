package noppes.npcs.client.gui.util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public abstract class GuiNPCInterface2 extends GuiNPCInterface
{
	private ResourceLocation background = new ResourceLocation("customnpcs:textures/gui/menubg.png");
	private GuiNpcMenu menu;
	
    public GuiNPCInterface2(EntityNPCInterface npc){
    	this(npc, -1);
    }
    public GuiNPCInterface2(EntityNPCInterface npc, int activeMenu){
    	super(npc);
    	xSize = 420;
    	ySize = 200;
    	menu = new GuiNpcMenu(this, activeMenu, npc);
    	
    }
    @Override
    public void initGui(){
    	super.initGui();
        menu.initGui(guiLeft, guiTop, xSize);
    }
    

    @Override
    public void mouseClicked(int i, int j, int k){
    	super.mouseClicked(i, j, k);
    	if(!hasSubGui())
	    	menu.mouseClicked(i, j, k);
    }   
	     
    public void delete(){
    	npc.delete();
        displayGuiScreen(null);
        mc.setIngameFocus();
    }
    public abstract void save();
    
    @Override
    public void drawScreen(int i, int j, float f)
    {
    	if(drawDefaultBackground)
    		drawDefaultBackground(); //drawDefaultBackground
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 200, 220);
        drawTexturedModalRect(guiLeft + xSize-230, guiTop, 26, 0, 230, 220);
        menu.drawElements(getFontRenderer(), i, j, mc, f);
        
        boolean bo = drawDefaultBackground;
        drawDefaultBackground = false;
        super.drawScreen(i, j, f);
        drawDefaultBackground = bo;
    }
}
