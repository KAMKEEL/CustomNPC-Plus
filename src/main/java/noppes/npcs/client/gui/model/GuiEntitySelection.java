package noppes.npcs.client.gui.model;

import java.util.Collections;
import java.util.Vector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.ModelData;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCStringSlot;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.entity.EntityCustomNpc;

import org.lwjgl.opengl.GL11;

public class GuiEntitySelection extends GuiNPCInterface
{
	private GuiNPCStringSlot slot;
	private GuiCreationScreen parent;
	private Class<? extends EntityLivingBase> prevModel;
	private ModelData playerdata;
	private EntityCustomNpc npc;
	
    public GuiEntitySelection(GuiCreationScreen parent, ModelData playerdata, EntityCustomNpc npc)
    {
    	this.parent = parent;
    	this.playerdata = playerdata;
    	this.npc = npc;
    	drawDefaultBackground = false;
		prevModel = playerdata.getEntityClass();
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        Vector<String> list = new Vector<String>(parent.data.keySet());
        list.add("CustomNPC");
		Collections.sort(list,String.CASE_INSENSITIVE_ORDER);
        slot = new GuiNPCStringSlot(list,this,false,18);
        if(playerdata.getEntityClass() != null)
        	slot.selected = (String) EntityList.classToStringMapping.get(playerdata.getEntityClass());
        else{
        	slot.selected = "CustomNPC";
        }
        slot.registerScrollButtons(4, 5);
        
    	this.buttonList.add(new GuiNpcButton(2, width / 2 - 100, height - 44,98, 20, "gui.back"));
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
    	EntityLivingBase entity = playerdata.getEntity(npc);
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
        
        float scale = 1;
        if(entity.height > 2.4)
        	scale = 2 / entity.height;
        
        GL11.glScalef(-50 * scale, 50 * scale, 50 * scale);
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
        try{
            RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        }
        catch(Exception e){
        	playerdata.setEntityClass(null);
        }
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
    	try{
	    	playerdata.setEntityClass(parent.data.get(slot.selected));
	    	EntityLivingBase entity = playerdata.getEntity(npc);
	    	if(entity != null){
				RendererLivingEntity render = (RendererLivingEntity) RenderManager.instance.getEntityRenderObject(entity);
	    		npc.display.texture = NPCRendererHelper.getTexture(render,entity);
	    	}
	    	else{
	    		npc.display.texture = "customnpcs:textures/entity/humanmale/Steve.png";
	    	}
	    	npc.display.glowTexture = "";
			npc.textureLocation = null;
			npc.textureGlowLocation = null;
			npc.updateHitbox();
    	}
    	catch(Exception ex){
    		npc.display.texture = "customnpcs:textures/entity/humanmale/Steve.png";
    	}
    }
    public void doubleClicked(){
        close();
    }

    public void keyTyped(char par1, int par2)
    {
        if (par2 == 1)
        {
            close();
        }
    }
    public void close() {		
		this.mc.displayGuiScreen(parent);
	}

    
	protected void actionPerformed(GuiButton guibutton)
    {
		close();
    }

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}


}
