package noppes.npcs.client.model.animation;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import noppes.npcs.client.model.ModelMPM;

public class AniCrawling {

	public static void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity, ModelMPM model){

        model.bipedHead.rotateAngleZ = -par4 / (180F / (float)Math.PI);
        model.bipedHead.rotateAngleY = 0;
        model.bipedHead.rotateAngleX = -55 / (180F / (float)Math.PI);

        model.bipedHeadwear.rotateAngleX = model.bipedHead.rotateAngleX;
        model.bipedHeadwear.rotateAngleY = model.bipedHead.rotateAngleY;
        model.bipedHeadwear.rotateAngleZ = model.bipedHead.rotateAngleZ;
        
        if(par2 > 0.25)
        	par2 = 0.25f;
        float movement = MathHelper.cos(par1 * 0.8f + (float)Math.PI) * par2;

        model.bipedLeftArm.rotateAngleX = 180 / (180F / (float)Math.PI) - movement * 0.25f;
        model.bipedLeftArm.rotateAngleY = movement * -0.46f;
        model.bipedLeftArm.rotateAngleZ = movement * -0.2f;
        model.bipedLeftArm.rotationPointY = 2 - movement * 9.0F;
        
        model.bipedRightArm.rotateAngleX = 180 / (180F / (float)Math.PI) + movement * 0.25f;
        model.bipedRightArm.rotateAngleY = movement * -0.4f;
        model.bipedRightArm.rotateAngleZ = movement * -0.2f;
        model.bipedRightArm.rotationPointY = 2 + movement * 9.0F;

        model.bipedBody.rotateAngleY = movement * 0.1f;
        model.bipedBody.rotateAngleX = 0;
        model.bipedBody.rotateAngleZ = movement * 0.1f;
        
        model.bipedLeftLeg.rotateAngleX = movement * 0.1f;
        model.bipedLeftLeg.rotateAngleY = movement * 0.1f;
        model.bipedLeftLeg.rotateAngleZ = -7 / (180F / (float)Math.PI) - movement * 0.25f;
        model.bipedLeftLeg.rotationPointY = 10.4f + movement * 9.0F;
        model.bipedLeftLeg.rotationPointZ = movement * 0.6f - 0.01f;

        model.bipedRightLeg.rotateAngleX = movement * -0.1f;
        model.bipedRightLeg.rotateAngleY = movement * 0.1f;
        model.bipedRightLeg.rotateAngleZ = 7 / (180F / (float)Math.PI) - movement * 0.25f;
        model.bipedRightLeg.rotationPointY = 10.4f - movement * 9.0F;
        model.bipedRightLeg.rotationPointZ = movement * -0.6f - 0.01f;
	}
}
