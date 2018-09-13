package noppes.npcs.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.EntityNPCInterface;

public class ModelNPCGolem extends ModelNPCMale {

	private ModelRenderer bipedLowerBody;
	
    public ModelNPCGolem(float f)
    {
            super(f);
    }
    
    public void init(float f, float f1)
    {
    	super.init(f,f1);
        short short1 = 128;
        short short2 = 128;
        float f2 = -7.0F;
        this.bipedHead = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.bipedHead.setRotationPoint(0.0F, f2, -2.0F);
        this.bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8, 10, 8, f);
        this.bipedHead.setTextureOffset(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2, 4, 2, f);
        this.bipedHeadwear = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.bipedHeadwear.setRotationPoint(0.0F, f2, -2.0F);
        this.bipedHeadwear.setTextureOffset(0, 85).addBox(-4.0F, -12.0F, -5.5F, 8, 10, 8, f + 0.5F);
        this.bipedBody = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.bipedBody.setRotationPoint(0.0F, 0.0F + f2, 0.0F);
        this.bipedBody.setTextureOffset(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18, 12, 11, f + 0.2F);
        this.bipedBody.setTextureOffset(0, 21).addBox(-9.0F, -2.0F, -6.0F, 18, 8, 11, f);
        this.bipedLowerBody = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.bipedLowerBody.setRotationPoint(0.0F, 0.0F + f2, 0.0F);
        this.bipedLowerBody.setTextureOffset(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9, 5, 6, f + 0.5F);
        this.bipedLowerBody.setTextureOffset(30, 70).addBox(-4.5F, 6.0F, -3.0F, 9, 9, 6, f + 0.4F);
        this.bipedRightArm = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.bipedRightArm.setRotationPoint(0.0F, f2, 0.0F);        
        this.bipedRightArm.setTextureOffset(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4, 30, 6, f + 0.2F);
        this.bipedRightArm.setTextureOffset(80, 21).addBox(-13.0F, -2.5F, -3.0F, 4, 20, 6, f);
        this.bipedRightArm.setTextureOffset(100, 21).addBox(-13.0F, -2.5F, -3.0F, 4, 20, 6, f + 1.0F);
        this.bipedLeftArm = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.bipedLeftArm.setRotationPoint(0.0F, f2, 0.0F);
        this.bipedLeftArm.setTextureOffset(60, 58).addBox(9.0F, -2.5F, -3.0F, 4, 30, 6, f + 0.2F);
        this.bipedLeftArm.setTextureOffset(80, 58).addBox(9.0F, -2.5F, -3.0F, 4, 20, 6, f);
        this.bipedLeftArm.setTextureOffset(100, 58).addBox(9.0F, -2.5F, -3.0F, 4, 20, 6, f + 1.0F);
        this.bipedLeftLeg = (new ModelRenderer(this, 0, 22)).setTextureSize(short1, short2);
        this.bipedLeftLeg.setRotationPoint(-4.0F, 18.0F + f2, 0.0F);
        this.bipedLeftLeg.setTextureOffset(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, f);
        this.bipedRightLeg = (new ModelRenderer(this, 0, 22)).setTextureSize(short1, short2);
        this.bipedRightLeg.mirror = true;
        this.bipedRightLeg.setTextureOffset(60, 0).setRotationPoint(5.0F, 18.0F + f2, 0.0F);
        this.bipedRightLeg.addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, f);
    }
    
    @Override
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
    	super.render(par1Entity, par2, par3, par4, par5, par6, par7);
    	bipedLowerBody.render(par7);
    }

    @Override
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
        bipedLeftLeg.rotateAngleX = -1.5F * this.func_78172_a(par1, 13.0F) * par2;
        bipedRightLeg.rotateAngleX = 1.5F * this.func_78172_a(par1, 13.0F) * par2;
        bipedLeftLeg.rotateAngleY = 0.0F;
        bipedRightLeg.rotateAngleY = 0.0F;
        
        float f6 = MathHelper.sin(this.onGround * (float)Math.PI);
        float f7 = MathHelper.sin((16.0F - (1.0F - this.onGround) * (1.0F - this.onGround)) * (float)Math.PI);
        if (this.onGround > 0.0)
        {
	        this.bipedRightArm.rotateAngleZ = 0.0F;
	        this.bipedLeftArm.rotateAngleZ = 0.0F;
	        this.bipedRightArm.rotateAngleY = -(0.1F - f6 * 0.6F);
	        this.bipedLeftArm.rotateAngleY = 0.1F - f6 * 0.6F;
	        bipedRightArm.rotateAngleX = 0.0F;
	        bipedLeftArm.rotateAngleX = 0.0F;
	        this.bipedRightArm.rotateAngleX = -((float)Math.PI / 2F);
	        this.bipedLeftArm.rotateAngleX = -((float)Math.PI / 2F);
	        this.bipedRightArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
	        this.bipedLeftArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
        }
        else if (aimedBow)
        {
            float f1 = 0.0F;
            float f3 = 0.0F;
            bipedRightArm.rotateAngleZ = 0.0F;
            bipedRightArm.rotateAngleX = -((float)Math.PI / 2F) + bipedHead.rotateAngleX;
            bipedRightArm.rotateAngleX -= f1 * 1.2F - f3 * 0.4F;
            bipedRightArm.rotateAngleZ += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
            bipedRightArm.rotateAngleX += MathHelper.sin(par3 * 0.067F) * 0.05F;
            bipedLeftArm.rotateAngleX = (-0.2F - 1.5F * this.func_78172_a(par1, 13.0F)) * par2;
            bipedBody.rotateAngleY = -(0.1F - f1 * 0.6F) + bipedHead.rotateAngleY;
            bipedRightArm.rotateAngleY = -(0.1F - f1 * 0.6F) + bipedHead.rotateAngleY;
            bipedLeftArm.rotateAngleY = (0.1F - f1 * 0.6F) + bipedHead.rotateAngleY;
        }
        else
        {
	        bipedRightArm.rotateAngleX = (-0.2F + 1.5F * this.func_78172_a(par1, 13.0F)) * par2;
	        bipedLeftArm.rotateAngleX = (-0.2F - 1.5F * this.func_78172_a(par1, 13.0F)) * par2;
	        bipedBody.rotateAngleY = 0.0F;
	        bipedRightArm.rotateAngleY = 0.0F;
            bipedLeftArm.rotateAngleY = 0.0F;
            bipedRightArm.rotateAngleZ = 0.0F;
            bipedLeftArm.rotateAngleZ = 0.0F;
        }
        
        if (isRiding)
        {
            bipedRightArm.rotateAngleX += -((float)Math.PI / 5F);
            bipedLeftArm.rotateAngleX += -((float)Math.PI / 5F);
            bipedLeftLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            bipedRightLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            bipedLeftLeg.rotateAngleY = ((float)Math.PI / 10F);
            bipedRightLeg.rotateAngleY = -((float)Math.PI / 10F);
        }
    }
    
    private float func_78172_a(float par1, float par2)
    {
        return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
    }
}