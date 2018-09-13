package noppes.npcs.client.gui.model;

import java.util.Collections;
import java.util.Vector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.ModelData;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.controllers.Preset;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCStringSlot;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.entity.EntityCustomNpc;

import org.lwjgl.opengl.GL11;

public class GuiPresetSelection extends GuiNPCInterface
{
	private GuiNPCStringSlot slot;
	private GuiCreationScreen parent;
	private NBTTagCompound prevData;
	private ModelData playerdata;
	private EntityCustomNpc npc;
	
    public GuiPresetSelection(GuiCreationScreen parent, ModelData playerdata)
    {
    	this.parent = parent;
    	this.playerdata = playerdata;
    	prevData = playerdata.writeToNBT();
    	drawDefaultBackground = false;
		npc = new EntityCustomNpc(Minecraft.getMinecraft().theWorld);
		npc.modelData = playerdata.copy();
		
		PresetController.instance.load();
    }
    
    public void initGui()
    {
        super.initGui();
        Vector<String> list = new Vector<String>();
        for(Preset preset : PresetController.instance.presets.values())
        	list.add(preset.name);
        
		Collections.sort(list,String.CASE_INSENSITIVE_ORDER);
        slot = new GuiNPCStringSlot(list,this,false,18);
        slot.registerScrollButtons(4, 5);

    	this.buttonList.add(new GuiNpcButton(2, width / 2 - 100, height - 44,98, 20, "Back"));
    	this.buttonList.add(new GuiNpcButton(3, width / 2 + 2, height - 44,98, 20, "Load"));
    	this.buttonList.add(new GuiNpcButton(4, width / 2 - 49, height - 22,98, 20, "Remove"));
    }


    public void drawScreen(int i, int j, float f)
    {
    	EntityLivingBase entity = npc.modelData.getEntity(npc);
    	if(entity == null)
    		entity = this.npc;
    	else
    		EntityUtil.Copy(npc, entity);
    	
    	int l = (width/2)-180;
    	int i1 =  (height/2) - 90;
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glEnable(2903 /*GL_COLOR_MATERIAL*/);
        GL11.glPushMatrix();
        GL11.glTranslatef(l + 33, i1 + 131, 50F);
        GL11.glScalef(-50, 50, 50);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f7 = entity.rotationYawHead;
        float f5 = (float)(l + 33) - i;
        float f6 = (float)((i1 + 131) - 50) - j;
        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-(float)Math.atan(f6 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float)Math.atan(f5 / 40F) * 20F;
        entity.rotationYaw = (float)Math.atan(f5 / 40F) * 40F;
        entity.rotationPitch = -(float)Math.atan(f6 / 40F) * 20F;
        entity.rotationYawHead = entity.rotationYaw;
        GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180F;
        RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        entity.renderYawOffset = f2;
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.rotationYawHead = f7;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);

        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    	slot.drawScreen(i, j, f);
        super.drawScreen(i, j, f);
    }
    public void elementClicked(){
    	Preset preset = PresetController.instance.getPreset(slot.selected);
    	npc.modelData.readFromNBT(preset.data.writeToNBT());
    }
    public void doubleClicked(){
		playerdata.readFromNBT(npc.modelData.writeToNBT());
        close();
    }

    @Override
    public void keyTyped(char par1, int par2)
    {
        if (par2 == 1)
        {
            close();
        }
    }
	public void close() {		
		mc.displayGuiScreen(parent);
	}

	public FontRenderer getFontRenderer() {
		return this.fontRendererObj;
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
    {
		GuiNpcButton guibutton = (GuiNpcButton) button;
		if(guibutton.id == 2){
			close();
		}
		if(guibutton.id == 3){
			playerdata.readFromNBT(npc.modelData.writeToNBT());
			close();
		}
		if(guibutton.id == 4){
			PresetController.instance.removePreset(slot.selected);
	        Vector<String> list = new Vector<String>();
	        for(Preset preset : PresetController.instance.presets.values())
	        	list.add(preset.name);
	        
			Collections.sort(list,String.CASE_INSENSITIVE_ORDER);
			slot.setList(list);
			slot.selected = "";
		}
    }

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}


}
