package noppes.npcs.client.model.animation;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import noppes.npcs.client.model.ModelMPM;

public class AniHug {

	public static void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity, ModelMPM base){

        float f6 = MathHelper.sin(base.onGround * 3.141593F);
        float f7 = MathHelper.sin((1.0F - (1.0F - base.onGround) * (1.0F - base.onGround)) * 3.141593F);
        base.bipedRightArm.rotateAngleZ = 0.0F;
        base.bipedLeftArm.rotateAngleZ = 0.0F;
        base.bipedRightArm.rotateAngleY = -(0.1F - f6 * 0.6F);
        base.bipedLeftArm.rotateAngleY = 0.1F;
        base.bipedRightArm.rotateAngleX = -1.570796F;
        base.bipedLeftArm.rotateAngleX = -1.570796F;
        base.bipedRightArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
        //bipedLeftArm.rotateAngleX -= f6 * 1.2F - f7 * 0.4F;
        base.bipedRightArm.rotateAngleZ += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
        base.bipedLeftArm.rotateAngleZ -= MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
        base.bipedRightArm.rotateAngleX += MathHelper.sin(par3 * 0.067F) * 0.05F;
        base.bipedLeftArm.rotateAngleX -= MathHelper.sin(par3 * 0.067F) * 0.05F;
	}
}
