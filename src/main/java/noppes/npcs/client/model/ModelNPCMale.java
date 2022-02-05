package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.EntityNPCInterface;

import org.lwjgl.opengl.GL11;

public class ModelNPCMale extends ModelBiped
{
    public boolean isDancing;
    public boolean isSleeping;
    
    public float animationTick;
    public float dancingTicks;

    public ModelNPCMale(float f)
    {
    	init(f, 0.0F);
    }
    @Override
    public void setLivingAnimations(EntityLivingBase par1EntityLiving, float f6, float f5, float par9){
    	animationTick += par9;
    	dancingTicks = CustomNpcs.ticks / 3.978873F;
    }
    public void init(float f, float f1)
    {
        heldItemLeft = 0;
        heldItemRight = 0;
        isSneak = false;
        aimedBow = false;
        bipedCloak = new ModelRenderer(this, 0, 0);
        bipedCloak.textureHeight = 32;
        bipedCloak.addBox(-5F, 0.0F, -1F, 10, 16, 1, f);
        
        bipedEars = new ModelRenderer(this, 24, 0);
        bipedEars.addBox(-3F, -6F, -1F, 6, 6, 1, f);
        
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8, f);
        bipedHead.setRotationPoint(0.0F, 0.0F + f1, 0.0F);
        
        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addBox(-4F, -8F, -4F, 8, 8, 8, f + 0.5F);
        bipedHeadwear.setRotationPoint(0.0F, 0.0F + f1, 0.0F);
        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0.0F, -2F, 8, 12, 4, f);
        bipedBody.setRotationPoint(0.0F, 0.0F + f1, 0.0F);
        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4, f);
        bipedRightArm.setRotationPoint(-5F, 2.0F + f1, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4, f);
        bipedLeftArm.setRotationPoint(5F, 2.0F + f1, 0.0F);
        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0.0F, -2F, 4, 12, 4, f);
        bipedRightLeg.setRotationPoint(-2F, 12F + f1, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-2F, 0.0F, -2F, 4, 12, 4, f);
        bipedLeftLeg.setRotationPoint(2.0F, 12F + f1, 0.0F);
        
    }

    @Override
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7){
        setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);

    	if(!isDancing){
	        bipedHead.render(par7);
	        bipedBody.render(par7);
	        bipedRightArm.render(par7);
	        bipedLeftArm.render(par7);
	        bipedRightLeg.render(par7);
	        bipedLeftLeg.render(par7);
	        bipedHeadwear.render(par7);
    	}
    	else{
    		renderHead(par1Entity, par7);
    		renderArms(par1Entity, par7);
    		renderBody(par1Entity, par7);
    		renderLegs(par1Entity, par7);
    	}
    }
    public void renderHead(Entity entityliving,float par7){
    	if(isDancing){
	        GL11.glPushMatrix();
	        GL11.glTranslatef((float)Math.sin(dancingTicks) * 0.075F, (float)Math.abs(Math.cos(dancingTicks)) * 0.125F - 0.02F, (float)(-Math.abs(Math.cos(dancingTicks))) * 0.075F);
	        bipedHead.render(par7);
	        bipedHeadwear.render(par7);
	        GL11.glPopMatrix();
    	}
    	else{        
    		bipedHead.render(par7);
    		bipedHeadwear.render(par7);
    	}
    }
    public void renderLeftArm(Entity entityliving,float par7){
    	if(isDancing){
	        GL11.glPushMatrix();
	        GL11.glTranslatef((float)Math.sin(dancingTicks) * 0.025F, (float)Math.abs(Math.cos(dancingTicks)) * 0.125F - 0.02F, 0.0F);
	        bipedLeftArm.render(par7);
	        GL11.glPopMatrix();
    	}
    	else{        
	        bipedLeftArm.render(par7);
    	}
    }
    public void renderArms(Entity entity,float par7){
    	renderLeftArm(entity,par7);
    	renderRightArm(entity,par7);
    }
    public void renderRightArm(Entity entityliving,float par7){
    	if(isDancing){
	        GL11.glPushMatrix();
	        GL11.glTranslatef((float)Math.sin(dancingTicks) * 0.025F, (float)Math.abs(Math.cos(dancingTicks)) * 0.125F - 0.02F, 0.0F);
	        bipedRightArm.render(par7);
	        GL11.glPopMatrix();
    	}
    	else{        
	        bipedRightArm.render(par7);
    	}
    }
    public void renderBody(Entity entityliving,float par7){
    	if(isDancing){
	        GL11.glPushMatrix();
	        GL11.glTranslatef((float)Math.sin(dancingTicks) * 0.015F, 0.0F, 0.0F);
	        bipedBody.render(par7);
	        GL11.glPopMatrix();
    	}
    	else{        
	        bipedBody.render(par7);
    	}
    }
    public void renderLegs(Entity entityliving,float par7){
        bipedRightLeg.render(par7);
        bipedLeftLeg.render(par7);
    }
    
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity)
    {
    	EntityNPCInterface npc = (EntityNPCInterface) entity;
		isRiding = npc.isRiding();
		
    	
    	if(isSneak && (npc.currentAnimation == EnumAnimation.CRAWLING || npc.currentAnimation == EnumAnimation.LYING))
    		isSneak = false;
        bipedHead.rotateAngleY = par4 / (180F / (float)Math.PI);
        bipedHead.rotateAngleX = par5 / (180F / (float)Math.PI);
        bipedHeadwear.rotateAngleY = bipedHead.rotateAngleY;
        bipedHeadwear.rotateAngleX = bipedHead.rotateAngleX;
        bipedRightArm.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float)Math.PI) * 2.0F * par2 * 0.5F;
        bipedLeftArm.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 2.0F * par2 * 0.5F;
        bipedRightArm.rotateAngleZ = 0.0F;
        bipedLeftArm.rotateAngleZ = 0.0F;
        bipedRightLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
        bipedLeftLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float)Math.PI) * 1.4F * par2;
        bipedRightLeg.rotateAngleY = 0.0F;
        bipedLeftLeg.rotateAngleY = 0.0F;
        
        if (isRiding)
        {
            bipedRightArm.rotateAngleX += -((float)Math.PI / 5F);
            bipedLeftArm.rotateAngleX += -((float)Math.PI / 5F);
            bipedRightLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            bipedLeftLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            bipedRightLeg.rotateAngleY = ((float)Math.PI / 10F);
            bipedLeftLeg.rotateAngleY = -((float)Math.PI / 10F);
        }

        if (heldItemLeft != 0)
        {
            bipedLeftArm.rotateAngleX = bipedLeftArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * (float)heldItemLeft;
        }

        if (heldItemRight != 0)
        {
            bipedRightArm.rotateAngleX = bipedRightArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * (float)heldItemRight;
        }

        bipedRightArm.rotateAngleY = 0.0F;
        bipedLeftArm.rotateAngleY = 0.0F;

        if (onGround > -9990F)
        {
            float f = onGround;
            bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f) * (float)Math.PI * 2.0F) * 0.2F;
            bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * 5F;
            bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * 5F;
            bipedLeftArm.rotationPointX = MathHelper.cos(bipedBody.rotateAngleY) * 5F;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;
            bipedLeftArm.rotateAngleY += bipedBody.rotateAngleY;
            bipedLeftArm.rotateAngleX += bipedBody.rotateAngleY;
            f = 1.0F - onGround;
            f *= f;
            f *= f;
            f = 1.0F - f;
            float f2 = MathHelper.sin(f * (float)Math.PI);
            float f4 = MathHelper.sin(onGround * (float)Math.PI) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
            bipedRightArm.rotateAngleX -= (double)f2 * 1.2D + (double)f4;
            bipedRightArm.rotateAngleY += bipedBody.rotateAngleY * 2.0F;
            bipedRightArm.rotateAngleZ = MathHelper.sin(onGround * (float)Math.PI) * -0.4F;
        }

        if (isSneak)
        {
            bipedBody.rotateAngleX = 0.5F;
            bipedRightLeg.rotateAngleX -= 0.0F;
            bipedLeftLeg.rotateAngleX -= 0.0F;
            bipedRightArm.rotateAngleX += 0.4F;
            bipedLeftArm.rotateAngleX += 0.4F;
            bipedRightLeg.rotationPointZ = 4F;
            bipedLeftLeg.rotationPointZ = 4F;
            bipedRightLeg.rotationPointY = 9F;
            bipedLeftLeg.rotationPointY = 9F;
            bipedHead.rotationPointY = 1.0F;
        }
        else
        {
            bipedBody.rotateAngleX = 0.0F;
            bipedRightLeg.rotationPointZ = 0.0F;
            bipedLeftLeg.rotationPointZ = 0.0F;
            bipedRightLeg.rotationPointY = 12F;
            bipedLeftLeg.rotationPointY = 12F;
            bipedHead.rotationPointY = 0.0F;
        }

        bipedRightArm.rotateAngleZ += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ -= MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(par3 * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(par3 * 0.067F) * 0.05F;

        if (aimedBow)
        {
            float f1 = 0.0F;
            float f3 = 0.0F;
            bipedRightArm.rotateAngleZ = 0.0F;
            bipedLeftArm.rotateAngleZ = 0.0F;
            bipedRightArm.rotateAngleY = -(0.1F - f1 * 0.6F) + bipedHead.rotateAngleY;
            bipedLeftArm.rotateAngleY = (0.1F - f1 * 0.6F) + bipedHead.rotateAngleY + 0.4F;
            bipedRightArm.rotateAngleX = -((float)Math.PI / 2F) + bipedHead.rotateAngleX;
            bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F) + bipedHead.rotateAngleX;
            bipedRightArm.rotateAngleX -= f1 * 1.2F - f3 * 0.4F;
            bipedLeftArm.rotateAngleX -= f1 * 1.2F - f3 * 0.4F;
            bipedRightArm.rotateAngleZ += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
            bipedLeftArm.rotateAngleZ -= MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
            bipedRightArm.rotateAngleX += MathHelper.sin(par3 * 0.067F) * 0.05F;
            bipedLeftArm.rotateAngleX -= MathHelper.sin(par3 * 0.067F) * 0.05F;
        }
    }

    public void renderEars(float f)
    {
        bipedEars.rotateAngleY = bipedHead.rotateAngleY;
        bipedEars.rotateAngleX = bipedHead.rotateAngleX;
        bipedEars.rotationPointX = 0.0F;
        bipedEars.rotationPointY = 0.0F;
        bipedEars.render(f);
    }

    public void setRotation(ModelRenderer model, float x, float y, float z)
    {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
    }
    
    public void renderCloak(float f)
    {
        bipedCloak.render(f);
    }

	public boolean isSleeping(Entity entity) {
		if(entity instanceof EntityPlayer && ((EntityPlayer)entity).isPlayerSleeping())
			return true;
		return ((EntityNPCInterface)entity).currentAnimation == EnumAnimation.LYING;
	}
}
