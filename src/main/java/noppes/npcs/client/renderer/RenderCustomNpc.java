package noppes.npcs.client.renderer;

import java.lang.reflect.Method;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.ModelRenderPassHelper;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class RenderCustomNpc extends RenderNPCHumanMale{

	private RendererLivingEntity renderEntity;
	private EntityLivingBase entity;
	
	private ModelRenderPassHelper renderpass = new ModelRenderPassHelper();
	
	public RenderCustomNpc() {
		super(new ModelMPM(0), new ModelMPM(1), new ModelMPM(0.5f));
	}
	
	@Override
    public void renderPlayer(EntityNPCInterface npcInterface, double d, double d1, double d2, float f, float f1){
		EntityCustomNpc npc = (EntityCustomNpc) npcInterface;
		entity = npc.modelData.getEntity(npc);
		ModelBase model = null;
		renderEntity = null;
		if(entity != null){
			renderEntity = (RendererLivingEntity) RenderManager.instance.getEntityRenderObject(entity);
			model = NPCRendererHelper.getMainModel(renderEntity);
			if(PixelmonHelper.isPixelmon(entity)){
				try {
					Class c = Class.forName("com.pixelmonmod.pixelmon.entities.pixelmon.Entity2HasModel");
					Method m = c.getMethod("getModel");
					model = (ModelBase) m.invoke(entity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(EntityList.getEntityString(entity).equals("doggystyle.Dog")){
				try {
					Method m = entity.getClass().getMethod("getBreed");
					Object breed = m.invoke(entity);
					m = breed.getClass().getMethod("getModel");
					model = (ModelBase) m.invoke(breed);
					model.getClass().getMethod("setPosition", int.class).invoke(model, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			renderPassModel = renderpass;
			renderpass.renderer = renderEntity;
			renderpass.entity = entity;
		}
		((ModelMPM)this.modelArmor).entityModel = model;
		((ModelMPM)this.modelArmor).entity = entity;

		((ModelMPM)this.modelArmorChestplate).entityModel = model;
		((ModelMPM)this.modelArmorChestplate).entity = entity;

		((ModelMPM)this.mainModel).entityModel = model;
		((ModelMPM)this.mainModel).entity = entity;
		
		super.renderPlayer(npc, d, d1, d2, f, f1);

    }    
	
	@Override
    protected void renderEquippedItems(EntityLivingBase entityliving, float f){
		if(renderEntity != null)
			NPCRendererHelper.renderEquippedItems(entity, f, renderEntity);
		else
			super.renderEquippedItems(entityliving, f);
    }

	@Override
    protected int shouldRenderPass(EntityLivingBase par1EntityLivingBase, int par2, float par3){
		if(renderEntity != null){
			return NPCRendererHelper.shouldRenderPass(entity, par2, par3, renderEntity);
		}
        return this.func_130006_a((EntityLiving)par1EntityLivingBase, par2, par3);
    }
	
	@Override
    protected void preRenderCallback(EntityLivingBase entityliving, float f){
		if(renderEntity != null){
			EntityNPCInterface npc = (EntityNPCInterface) entityliving;
			int size = npc.display.modelSize;
			if(entity instanceof EntityNPCInterface){
				((EntityNPCInterface)entity).display.modelSize = 5;
			}
			NPCRendererHelper.preRenderCallback(entity, f, renderEntity);
			npc.display.modelSize = size;
	        GL11.glScalef(0.2f * npc.display.modelSize, 0.2f * npc.display.modelSize, 0.2f * npc.display.modelSize);
		}
		else
			super.preRenderCallback(entityliving, f);
    }

	@Override
    protected float handleRotationFloat(EntityLivingBase par1EntityLivingBase, float par2){
		if(renderEntity != null){
			return NPCRendererHelper.handleRotationFloat(entity, par2, renderEntity);
		}
        return super.handleRotationFloat(par1EntityLivingBase, par2);
    }
}