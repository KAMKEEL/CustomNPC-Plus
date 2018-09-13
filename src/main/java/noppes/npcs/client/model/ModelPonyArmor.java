package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.entity.EntityNPCInterface;

public class ModelPonyArmor extends ModelBase
{

    private boolean rainboom;
    public ModelRenderer head;
    public ModelRenderer Body;
    public ModelRenderer BodyBack;
    public ModelRenderer rightarm;
    public ModelRenderer LeftArm;
    public ModelRenderer RightLeg;
    public ModelRenderer LeftLeg;
    public ModelRenderer rightarm2;
    public ModelRenderer LeftArm2;
    public ModelRenderer RightLeg2;
    public ModelRenderer LeftLeg2;
    public boolean isPegasus = false;
    public boolean isUnicorn = false;
    public boolean isSleeping = false;
    public boolean isFlying = false;
    public boolean isGlow = false;
    public boolean isSneak = false;
    public boolean aimedBow;
    public int heldItemRight;

    public ModelPonyArmor(float f)
    {
    	init(f,0.0f);
    }

    public void init(float strech,float f)
    {
        float f2 = 0.0F;
        float f3 = 0.0F;
        float f4 = 0.0F;
        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4F, -4F, -6F, 8, 8, 8, strech);
        head.setRotationPoint(f2, f3, f4);
        float f5 = 0.0F;
        float f6 = 0.0F;
        float f7 = 0.0F;
        Body = new ModelRenderer(this, 16, 16);
        Body.addBox(-4F, 4F, -2F, 8, 8, 4, strech);
        Body.setRotationPoint(f5, f6 + f, f7);
        BodyBack = new ModelRenderer(this, 0, 0);
        BodyBack.addBox(-4F, 4F, 6F, 8, 8, 8, strech);
        BodyBack.setRotationPoint(f5, f6 + f, f7);
        rightarm = new ModelRenderer(this, 0, 16);
        rightarm.addBox(-2F, 4F, -2F, 4, 12, 4, strech);
        rightarm.setRotationPoint(-3F, 8F + f, 0.0F);
        LeftArm = new ModelRenderer(this, 0, 16);
        LeftArm.mirror = true;
        LeftArm.addBox(-2F, 4F, -2F, 4, 12, 4, strech);
        LeftArm.setRotationPoint(3F, 8F + f, 0.0F);
        RightLeg = new ModelRenderer(this, 0, 16);
        RightLeg.addBox(-2F, 4F, -2F, 4, 12, 4, strech);
        RightLeg.setRotationPoint(-3F, 0.0F + f, 0.0F);
        LeftLeg = new ModelRenderer(this, 0, 16);
        LeftLeg.mirror = true;
        LeftLeg.addBox(-2F, 4F, -2F, 4, 12, 4, strech);
        LeftLeg.setRotationPoint(3F, 0.0F + f, 0.0F);
        rightarm2 = new ModelRenderer(this, 0, 16);
        rightarm2.addBox(-2F, 4F, -2F, 4, 12, 4, strech * 0.5F);
        rightarm2.setRotationPoint(-3F, 8F + f, 0.0F);
        LeftArm2 = new ModelRenderer(this, 0, 16);
        LeftArm2.mirror = true;
        LeftArm2.addBox(-2F, 4F, -2F, 4, 12, 4, strech * 0.5F);
        LeftArm2.setRotationPoint(3F, 8F + f, 0.0F);
        RightLeg2 = new ModelRenderer(this, 0, 16);
        RightLeg2.addBox(-2F, 4F, -2F, 4, 12, 4, strech * 0.5F);
        RightLeg2.setRotationPoint(-3F, 0.0F + f, 0.0F);
        LeftLeg2 = new ModelRenderer(this, 0, 16);
        LeftLeg2.mirror = true;
        LeftLeg2.addBox(-2F, 4F, -2F, 4, 12, 4, strech * 0.5F);
        LeftLeg2.setRotationPoint(3F, 0.0F + f, 0.0F);
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
    	EntityNPCInterface npc = (EntityNPCInterface) entity;
    	if(!isRiding)
    		isRiding = npc.currentAnimation == EnumAnimation.SITTING;
    	
    	if(isSneak && (npc.currentAnimation == EnumAnimation.CRAWLING || npc.currentAnimation == EnumAnimation.LYING))
    		isSneak = false;
        rainboom = false;
        float f6;
        float f7;
        if(isSleeping)
        {
            f6 = 1.4F;
            f7 = 0.1F;
        } else
        {
            f6 = f3 / 57.29578F;
            f7 = f4 / 57.29578F;
        }
        head.rotateAngleY = f6;
        head.rotateAngleX = f7;
        float f8;
        float f9;
        float f10;
        float f11;
        if(!isFlying || !isPegasus)
        {
            f8 = MathHelper.cos(f * 0.6662F + 3.141593F) * 0.6F * f1;
            f9 = MathHelper.cos(f * 0.6662F) * 0.6F * f1;
            f10 = MathHelper.cos(f * 0.6662F) * 0.3F * f1;
            f11 = MathHelper.cos(f * 0.6662F + 3.141593F) * 0.3F * f1;
            rightarm.rotateAngleY = 0.0F;
            LeftArm.rotateAngleY = 0.0F;
            RightLeg.rotateAngleY = 0.0F;
            LeftLeg.rotateAngleY = 0.0F;
            rightarm2.rotateAngleY = 0.0F;
            LeftArm2.rotateAngleY = 0.0F;
            RightLeg2.rotateAngleY = 0.0F;
            LeftLeg2.rotateAngleY = 0.0F;
        } else
        {
            if(f1 < 0.9999F)
            {
                rainboom = false;
                f8 = MathHelper.sin(0.0F - f1 * 0.5F);
                f9 = MathHelper.sin(0.0F - f1 * 0.5F);
                f10 = MathHelper.sin(f1 * 0.5F);
                f11 = MathHelper.sin(f1 * 0.5F);
            } else
            {
                rainboom = true;
                f8 = 4.712F;
                f9 = 4.712F;
                f10 = 1.571F;
                f11 = 1.571F;
            }
            rightarm.rotateAngleY = 0.2F;
            LeftArm.rotateAngleY = -0.2F;
            RightLeg.rotateAngleY = -0.2F;
            LeftLeg.rotateAngleY = 0.2F;
            rightarm2.rotateAngleY = 0.2F;
            LeftArm2.rotateAngleY = -0.2F;
            RightLeg2.rotateAngleY = -0.2F;
            LeftLeg2.rotateAngleY = 0.2F;
        }
        if(isSleeping)
        {
            f8 = 4.712F;
            f9 = 4.712F;
            f10 = 1.571F;
            f11 = 1.571F;
        }
        rightarm.rotateAngleX = f8;
        LeftArm.rotateAngleX = f9;
        RightLeg.rotateAngleX = f10;
        LeftLeg.rotateAngleX = f11;
        rightarm.rotateAngleZ = 0.0F;
        LeftArm.rotateAngleZ = 0.0F;
        rightarm2.rotateAngleX = f8;
        LeftArm2.rotateAngleX = f9;
        RightLeg2.rotateAngleX = f10;
        LeftLeg2.rotateAngleX = f11;
        rightarm2.rotateAngleZ = 0.0F;
        LeftArm2.rotateAngleZ = 0.0F;
        if(heldItemRight != 0 && !rainboom && !isUnicorn)
        {
            rightarm.rotateAngleX = rightarm.rotateAngleX * 0.5F - 0.3141593F;
            rightarm2.rotateAngleX = rightarm2.rotateAngleX * 0.5F - 0.3141593F;
        }
        float f12 = 0.0F;
        if(f5 > -9990F && !isUnicorn)
        {
            f12 = MathHelper.sin(MathHelper.sqrt_float(f5) * 3.141593F * 2.0F) * 0.2F;
        }
        Body.rotateAngleY = (float)((double)f12 * 0.20000000000000001D);
        BodyBack.rotateAngleY = (float)((double)f12 * 0.20000000000000001D);
        float f13 = MathHelper.sin(Body.rotateAngleY) * 5F;
        float f14 = MathHelper.cos(Body.rotateAngleY) * 5F;
        float f15 = 4F;
        if(isSneak && !isFlying)
        {
            f15 = 0.0F;
        }
        if(isSleeping)
        {
            f15 = 2.6F;
        }
        if(rainboom)
        {
            rightarm.rotationPointZ = f13 + 2.0F;
            rightarm2.rotationPointZ = f13 + 2.0F;
            LeftArm.rotationPointZ = (0.0F - f13) + 2.0F;
            LeftArm2.rotationPointZ = (0.0F - f13) + 2.0F;
        } else
        {
            rightarm.rotationPointZ = f13 + 1.0F;
            rightarm2.rotationPointZ = f13 + 1.0F;
            LeftArm.rotationPointZ = (0.0F - f13) + 1.0F;
            LeftArm2.rotationPointZ = (0.0F - f13) + 1.0F;
        }
        rightarm.rotationPointX = (0.0F - f14 - 1.0F) + f15;
        rightarm2.rotationPointX = (0.0F - f14 - 1.0F) + f15;
        LeftArm.rotationPointX = (f14 + 1.0F) - f15;
        LeftArm2.rotationPointX = (f14 + 1.0F) - f15;
        RightLeg.rotationPointX = (0.0F - f14 - 1.0F) + f15;
        RightLeg2.rotationPointX = (0.0F - f14 - 1.0F) + f15;
        LeftLeg.rotationPointX = (f14 + 1.0F) - f15;
        LeftLeg2.rotationPointX = (f14 + 1.0F) - f15;
        rightarm.rotateAngleY += Body.rotateAngleY;
        rightarm2.rotateAngleY += Body.rotateAngleY;
        LeftArm.rotateAngleY += Body.rotateAngleY;
        LeftArm2.rotateAngleY += Body.rotateAngleY;
        LeftArm.rotateAngleX += Body.rotateAngleY;
        LeftArm2.rotateAngleX += Body.rotateAngleY;
        rightarm.rotationPointY = 8F;
        LeftArm.rotationPointY = 8F;
        RightLeg.rotationPointY = 4F;
        LeftLeg.rotationPointY = 4F;
        rightarm2.rotationPointY = 8F;
        LeftArm2.rotationPointY = 8F;
        RightLeg2.rotationPointY = 4F;
        LeftLeg2.rotationPointY = 4F;
        if(f5 > -9990F && !isUnicorn)
        {
            float f16 = f5;
            f16 = 1.0F - f5;
            f16 *= f16 * f16;
            f16 = 1.0F - f16;
            float f21 = MathHelper.sin(f16 * 3.141593F);
            float f26 = MathHelper.sin(f5 * 3.141593F);
            float f30 = f26 * -(head.rotateAngleX - 0.7F) * 0.75F;
//            rightarm.rotateAngleX -= (double)f21 * 1.2D + (double)f30;
//            rightarm.rotateAngleY += Body.rotateAngleY * 2.0F;
//            rightarm.rotateAngleZ = f26 * -0.4F;
//            rightarm2.rotateAngleX -= (double)f21 * 1.2D + (double)f30;
//            rightarm2.rotateAngleY += Body.rotateAngleY * 2.0F;
//            rightarm2.rotateAngleZ = f26 * -0.4F;
        }
        if(isSneak && !isFlying)
        {
            float f17 = 0.4F;
            float f22 = 7F;
            float f27 = -4F;
            Body.rotateAngleX = f17;
            Body.rotationPointY = f22;
            Body.rotationPointZ = f27;
            BodyBack.rotateAngleX = f17;
            BodyBack.rotationPointY = f22;
            BodyBack.rotationPointZ = f27;
            RightLeg.rotateAngleX -= 0.0F;
            LeftLeg.rotateAngleX -= 0.0F;
            rightarm.rotateAngleX -= 0.4F;
            LeftArm.rotateAngleX -= 0.4F;
            RightLeg.rotationPointZ = 10F;
            LeftLeg.rotationPointZ = 10F;
            RightLeg.rotationPointY = 7F;
            LeftLeg.rotationPointY = 7F;
            RightLeg2.rotateAngleX -= 0.0F;
            LeftLeg2.rotateAngleX -= 0.0F;
            rightarm2.rotateAngleX -= 0.4F;
            LeftArm2.rotateAngleX -= 0.4F;
            RightLeg2.rotationPointZ = 10F;
            LeftLeg2.rotationPointZ = 10F;
            RightLeg2.rotationPointY = 7F;
            LeftLeg2.rotationPointY = 7F;
            float f31;
            float f33;
            float f35;
            if(isSleeping)
            {
                f31 = 2.0F;
                f33 = -1F;
                f35 = 1.0F;
            } else
            {
                f31 = 6F;
                f33 = -2F;
                f35 = 0.0F;
            }
            head.rotationPointY = f31;
            head.rotationPointZ = f33;
            head.rotationPointX = f35;
        } else
        {
            float f18 = 0.0F;
            float f23 = 0.0F;
            float f28 = 0.0F;
            Body.rotateAngleX = f18;
            Body.rotationPointY = f23;
            Body.rotationPointZ = f28;
            BodyBack.rotateAngleX = f18;
            BodyBack.rotationPointY = f23;
            BodyBack.rotationPointZ = f28;
            RightLeg.rotationPointZ = 10F;
            LeftLeg.rotationPointZ = 10F;
            RightLeg.rotationPointY = 8F;
            LeftLeg.rotationPointY = 8F;
            RightLeg2.rotationPointZ = 10F;
            LeftLeg2.rotationPointZ = 10F;
            RightLeg2.rotationPointY = 8F;
            LeftLeg2.rotationPointY = 8F;
            float f32 = MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
            float f34 = MathHelper.sin(f2 * 0.067F) * 0.05F;
            float f36 = 0.0F;
            float f37 = 0.0F;
            head.rotationPointY = f36;
            head.rotationPointZ = f37;
        }
//        if(isRiding)
//        {
//            float f19 = -10F;
//            float f24 = -10F;
//            head.rotationPointY = head.rotationPointY + f19;
//            head.rotationPointZ = head.rotationPointZ + f24;
//            Body.rotationPointY = Body.rotationPointY + f19;
//            Body.rotationPointZ = Body.rotationPointZ + f24;
//            BodyBack.rotationPointY = BodyBack.rotationPointY + f19;
//            BodyBack.rotationPointZ = BodyBack.rotationPointZ + f24;
//            LeftArm.rotationPointY = LeftArm.rotationPointY + f19;
//            LeftArm.rotationPointZ = LeftArm.rotationPointZ + f24;
//            rightarm.rotationPointY = rightarm.rotationPointY + f19;
//            rightarm.rotationPointZ = rightarm.rotationPointZ + f24;
//            LeftLeg.rotationPointY = LeftLeg.rotationPointY + f19;
//            LeftLeg.rotationPointZ = LeftLeg.rotationPointZ + f24;
//            RightLeg.rotationPointY = RightLeg.rotationPointY + f19;
//            RightLeg.rotationPointZ = RightLeg.rotationPointZ + f24;
//            LeftArm2.rotationPointY = LeftArm2.rotationPointY + f19;
//            LeftArm2.rotationPointZ = LeftArm2.rotationPointZ + f24;
//            rightarm2.rotationPointY = rightarm2.rotationPointY + f19;
//            rightarm2.rotationPointZ = rightarm2.rotationPointZ + f24;
//            LeftLeg2.rotationPointY = LeftLeg2.rotationPointY + f19;
//            LeftLeg2.rotationPointZ = LeftLeg2.rotationPointZ + f24;
//            RightLeg2.rotationPointY = RightLeg2.rotationPointY + f19;
//            RightLeg2.rotationPointZ = RightLeg2.rotationPointZ + f24;
//        }
        if(isSleeping)
        {
            rightarm.rotationPointZ = rightarm.rotationPointZ + 6F;
            LeftArm.rotationPointZ = LeftArm.rotationPointZ + 6F;
            RightLeg.rotationPointZ = RightLeg.rotationPointZ - 8F;
            LeftLeg.rotationPointZ = LeftLeg.rotationPointZ - 8F;
            rightarm.rotationPointY = rightarm.rotationPointY + 2.0F;
            LeftArm.rotationPointY = LeftArm.rotationPointY + 2.0F;
            RightLeg.rotationPointY = RightLeg.rotationPointY + 2.0F;
            LeftLeg.rotationPointY = LeftLeg.rotationPointY + 2.0F;
            rightarm2.rotationPointZ = rightarm2.rotationPointZ + 6F;
            LeftArm2.rotationPointZ = LeftArm2.rotationPointZ + 6F;
            RightLeg2.rotationPointZ = RightLeg2.rotationPointZ - 8F;
            LeftLeg2.rotationPointZ = LeftLeg2.rotationPointZ - 8F;
            rightarm2.rotationPointY = rightarm2.rotationPointY + 2.0F;
            LeftArm2.rotationPointY = LeftArm2.rotationPointY + 2.0F;
            RightLeg2.rotationPointY = RightLeg2.rotationPointY + 2.0F;
            LeftLeg2.rotationPointY = LeftLeg2.rotationPointY + 2.0F;
        }
        if(aimedBow && !isUnicorn)
        {
            float f20 = 0.0F;
            float f25 = 0.0F;
            rightarm.rotateAngleZ = 0.0F;
            rightarm.rotateAngleY = -(0.1F - f20 * 0.6F) + head.rotateAngleY;
            rightarm.rotateAngleX = 4.712F + head.rotateAngleX;
            rightarm.rotateAngleX -= f20 * 1.2F - f25 * 0.4F;
            float f29 = f2;
            rightarm.rotateAngleZ += MathHelper.cos(f29 * 0.09F) * 0.05F + 0.05F;
            rightarm.rotateAngleX += MathHelper.sin(f29 * 0.067F) * 0.05F;
            rightarm2.rotateAngleZ = 0.0F;
            rightarm2.rotateAngleY = -(0.1F - f20 * 0.6F) + head.rotateAngleY;
            rightarm2.rotateAngleX = 4.712F + head.rotateAngleX;
            rightarm2.rotateAngleX -= f20 * 1.2F - f25 * 0.4F;
            rightarm2.rotateAngleZ += MathHelper.cos(f29 * 0.09F) * 0.05F + 0.05F;
            rightarm2.rotateAngleX += MathHelper.sin(f29 * 0.067F) * 0.05F;
            rightarm.rotationPointZ = rightarm.rotationPointZ + 1.0F;
            rightarm2.rotationPointZ = rightarm2.rotationPointZ + 1.0F;
        }
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
    	float scale = f5;
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
            head.render(scale);
            Body.render(scale);
            BodyBack.render(scale);
            LeftArm.render(scale);
            rightarm.render(scale);
            LeftLeg.render(scale);
            RightLeg.render(scale);
            LeftArm2.render(scale);
            rightarm2.render(scale);
            LeftLeg2.render(scale);
            RightLeg2.render(scale);
        
    }

}
