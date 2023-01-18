package noppes.npcs.client.model.part;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.ModelPartInterface;
import noppes.npcs.client.model.util.ModelPlaneRenderer;
import org.lwjgl.opengl.GL11;

public class ModelSkirt extends ModelPartInterface {
	private ModelPlaneRenderer Shape1;
	public ModelSkirt(ModelMPM base) {
		super(base);
		float pi = (float) (Math.PI / 5);

		textureWidth = 64;
		textureHeight = 32;

		Shape1 = new ModelPlaneRenderer(base, 58, 18);
		Shape1.setTextureSize(64,32);
		Shape1.addSidePlane(0, 0, 0, 9, 2);
		
		ModelPlaneRenderer part1 = new ModelPlaneRenderer(base, 58, 18);
		part1.setTextureSize(64,32);
		part1.addSidePlane(2, 0, 0, 9, 2);
		part1.rotateAngleY = -(float) (Math.PI/2);
		Shape1.addChild(part1);

		Shape1.setRotationPoint(2.4F, 8.8F, 0F);
		setRotation(Shape1, 0.3F, -0.2f, -0.2F);
	}
    public void render(float par1)
    {
		if (this.isHidden || !this.showModel)
			return;
    	GL11.glPushMatrix();
    	GL11.glScalef(1.7f, 1.04f, 1.6f);
    	super.render(par1);
    	GL11.glPopMatrix();
    }
	public void renderParts(float par1) {
		for(int i = 0; i < 10; i++){
			GL11.glRotatef(36, 0, 1, 0);
			Shape1.render(par1);
		}
	}

	public void setRotationAngles(float par1, float par2, float par3,
			float par4, float par5, float par6, Entity entity) {
		setRotation(Shape1, 0.3F, -0.2f, -0.2F);
    	Shape1.rotateAngleX += base.bipedLeftArm.rotateAngleX * 0.04f;
    	Shape1.rotateAngleZ += base.bipedLeftArm.rotateAngleX * 0.06f;
        //this.Shape1.rotateAngleX -= MathHelper.sin(par3 * 0.067F) * 0.05F;
        this.Shape1.rotateAngleZ -= MathHelper.cos(par3 * 0.09F) * 0.04F - 0.05F;
	}

	@Override
	public void initData(ModelData data) {
		ModelPartData config = data.getPartData("skirt");
		if(config == null)
		{
			isHidden = true;
			return;
		}
		color = config.color;
		isHidden = false;
		location = (ResourceLocation) config.getResource();
	}
}
