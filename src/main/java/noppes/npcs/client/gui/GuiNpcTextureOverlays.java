package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class GuiNpcTextureOverlays extends GuiNpcSelectionInterface{
    public GuiNpcTextureOverlays(EntityNPCInterface npc,GuiScreen parent){
    	super(npc, parent, npc.display.glowTexture.isEmpty()?"customnpcs:textures/overlays/":npc.display.glowTexture);
    	title = "Select Overlay";
    	this.parent = parent;
    }

    public void initGui(){
    	super.initGui();
        int index = npc.display.glowTexture.lastIndexOf("/");
        if(index > 0){
        	String asset = npc.display.glowTexture.substring(index + 1);
        	if(npc.display.glowTexture.equals(assets.getAsset(asset)))
        		slot.selected = asset;
        }
    }


    public void drawScreen(int i, int j, float f)
    {
    	int l = (width/2)-180;
    	int i1 =  (height/2) - 90;
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glEnable(2903 /*GL_COLOR_MATERIAL*/);
        GL11.glPushMatrix();
        GL11.glTranslatef(l + 33, i1 + 131, 50F);
        float f1 = (50F * 5) / npc.display.modelSize;
        GL11.glScalef(-f1, f1, f1);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        float f2 = npc.renderYawOffset;
        float f3 = npc.rotationYaw;
        float f4 = npc.rotationPitch;
        float f7 = npc.rotationYawHead;
        float f5 = (float)(l + 33) - i;
        float f6 = (float)((i1 + 131) - 50) - j;
        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-(float)Math.atan(f6 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
        npc.renderYawOffset = (float)Math.atan(f5 / 40F) * 20F;
        npc.rotationYaw = (float)Math.atan(f5 / 40F) * 40F;
        npc.rotationPitch = -(float)Math.atan(f6 / 40F) * 20F;
        npc.rotationYawHead = npc.rotationYaw;
        //npc.updateCloak();
        GL11.glTranslatef(0.0F, npc.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180F;
        RenderManager.instance.renderEntityWithPosYaw(npc, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        npc.renderYawOffset = f2;
        npc.rotationYaw = f3;
        npc.rotationPitch = f4;
        npc.rotationYawHead = f7;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);

        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        
        super.drawScreen(i, j, f);
    }
    
    @Override
    public void elementClicked(){
    	if(dataTextures.contains(slot.selected) && slot.selected != null){
    		npc.display.glowTexture = assets.getAsset(slot.selected);
    		npc.textureGlowLocation = null;
    	}
    }

	public void save() {
	}

	@Override
	public String[] getExtension() {
		return new String[]{"png"};
	}


}
