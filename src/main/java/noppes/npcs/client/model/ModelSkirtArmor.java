package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.util.ModelPlaneRenderer;

import org.lwjgl.opengl.GL11;

public class ModelSkirtArmor extends ModelBiped {
	private ModelPlaneRenderer Shape1;
	public ModelSkirtArmor() {
		float pi = (float) (Math.PI / 5);

		Shape1 = new ModelPlaneRenderer(this, 4, 20);
		Shape1.addSidePlane(0, 0, 0, 9, 2);
		
		ModelPlaneRenderer part1 = new ModelPlaneRenderer(this, 6, 20);
		part1.addSidePlane(2, 0, 0, 9, 2);
		part1.rotateAngleY = -(float) (Math.PI/2);
		Shape1.addChild(part1);

		Shape1.setRotationPoint(2.4F, 8.8F, 0F);
		setRotation(Shape1, 0.3F, -0.2f, -0.2F);
	}

	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        this.setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);

    	GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, this.bipedRightLeg.rotationPointZ * par7);
    	GL11.glScalef(1.6f, 1.04f, 1.6f);
		for(int i = 0; i < 10; i++){
			GL11.glRotatef(36, 0, 1, 0);
			Shape1.render(par7);
		}
    	GL11.glPopMatrix();
    }
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity)
    {
		setRotation(Shape1, 0.3F, -0.2f, -0.2F);
    	isSneak = par7Entity.isSneaking();
    	super.setRotationAngles(par1, par2, par3, par4, par5, par6, par7Entity);
    	Shape1.rotateAngleX += this.bipedLeftArm.rotateAngleX * 0.02f;
    	Shape1.rotateAngleZ += this.bipedLeftArm.rotateAngleX * 0.06f;
        //this.Shape1.rotateAngleX -= MathHelper.sin(par3 * 0.067F) * 0.05F;
        this.Shape1.rotateAngleZ -= MathHelper.cos(par3 * 0.09F) * 0.02F - 0.05F;
    }
}
