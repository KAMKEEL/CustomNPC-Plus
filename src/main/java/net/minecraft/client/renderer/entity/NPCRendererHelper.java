package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class NPCRendererHelper {

	public static ModelBase getMainModel(RendererLivingEntity render){
		return render.mainModel;
	}
	
	public static String getTexture(RendererLivingEntity render, Entity entity){
		ResourceLocation location = render.getEntityTexture(entity);
		return location.toString();
	}

	public static int shouldRenderPass(EntityLivingBase entity, int par2, float par3, RendererLivingEntity renderEntity) {
		return renderEntity.shouldRenderPass(entity, par2, par3);
	}

	public static void preRenderCallback(EntityLivingBase entity, float f,
			RendererLivingEntity renderEntity) {
		renderEntity.preRenderCallback(entity, f);
	}

	public static ModelBase getPassModel(RendererLivingEntity render) {
		return render.renderPassModel;
	}

	public static float handleRotationFloat(EntityLivingBase entity,
			float par2, RendererLivingEntity renderEntity) {
		return renderEntity.handleRotationFloat(entity, par2);
	}

	public static void renderEquippedItems(EntityLivingBase entity, float f,
			RendererLivingEntity renderEntity) {
		renderEntity.renderEquippedItems(entity, f);
	}
}
