package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import noppes.npcs.ModelData;
import noppes.npcs.client.model.ModelMPM;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelPartInterface;

public class ModelBreasts extends ModelPartInterface {
	private Model2DRenderer breasts;
	private ModelRenderer breasts2;
	private ModelRenderer breasts3;

	public ModelBreasts(ModelMPM base) {
		super(base);

		breasts = new Model2DRenderer(base, 20f, 22, 8, 3, 64, 32);
		breasts.setRotationPoint(-3.6F, 5.2f, -3f);
		breasts.setScale(0.17f, 0.19f);
		breasts.setThickness(1);
		this.addChild(breasts);

		breasts2 = new ModelRenderer(base);
		this.addChild(breasts2);

		Model2DRenderer bottom = new Model2DRenderer(base, 20f, 22, 8, 4, 64, 32);
		bottom.setRotationPoint(-3.6F, 5f, -3.1f);
		bottom.setScale(0.225f, 0.20f);
		bottom.setThickness(2f);
		bottom.rotateAngleX = -(float) (Math.PI / 10);
		breasts2.addChild(bottom);

		breasts3 = new ModelRenderer(base);
		this.addChild(breasts3);

		Model2DRenderer right = new Model2DRenderer(base, 20f, 22, 3, 2, 64, 32);
		right.setRotationPoint(-3.8F, 5.3f, -3.6f);
		right.setScale(0.12f, 0.14f);
		right.setThickness(1.75f);
		breasts3.addChild(right);
		
		Model2DRenderer right2 = new Model2DRenderer(base, 20f, 22, 3, 1, 64, 32);
		right2.setRotationPoint(-3.8F, 4.1f, -3.14f);
		right2.setScale(0.06f, 0.07f);
		right2.setThickness(1.75f);
		right2.rotateAngleX = (float) (Math.PI / 9);
		breasts3.addChild(right2);
		
		Model2DRenderer right3 = new Model2DRenderer(base, 20f, 24, 3, 1, 64, 32);
		right3.setRotationPoint(-3.8F, 5.3f, -3.6f);
		right3.setScale(0.06f, 0.07f);
		right3.setThickness(1.75f);
		right3.rotateAngleX = (float) (-Math.PI / 9);
		breasts3.addChild(right3);
		
		Model2DRenderer right4 = new Model2DRenderer(base, 23f, 22, 1, 2, 64, 32);
		right4.setRotationPoint(-1.8f, 5.3f, -3.14f);
		right4.setScale(0.12f, 0.14f);
		right4.setThickness(1.75f);
		right4.rotateAngleY = (float) (Math.PI / 9);
		breasts3.addChild(right4);

		Model2DRenderer left = new Model2DRenderer(base, 25f, 22, 3, 2, 64, 32);
		left.setRotationPoint(0.8F, 5.3f, -3.6f);
		left.setScale(0.12f, 0.14f);
		left.setThickness(1.75f);
		breasts3.addChild(left);
		
		Model2DRenderer left2 = new Model2DRenderer(base, 25f, 22, 3, 1, 64, 32);
		left2.setRotationPoint(0.8F, 4.1f, -3.18f);
		left2.setScale(0.06f, 0.07f);
		left2.setThickness(1.75f);
		left2.rotateAngleX = (float) (Math.PI / 9);
		breasts3.addChild(left2);
		
		Model2DRenderer left3 = new Model2DRenderer(base, 25f, 24, 3, 1, 64, 32);
		left3.setRotationPoint(0.8F, 5.3f, -3.6f);
		left3.setScale(0.06f, 0.07f);
		left3.setThickness(1.75f);
		left3.rotateAngleX = (float) (-Math.PI / 9);
		breasts3.addChild(left3);
		
		Model2DRenderer left4 = new Model2DRenderer(base, 24f, 22, 1, 2, 64, 32);
		left4.setRotationPoint(0.8f, 5.3f, -3.6f);
		left4.setScale(0.12f, 0.14f);
		left4.setThickness(1.75f);
		left4.rotateAngleY = (float) (-Math.PI / 9);
		breasts3.addChild(left4);
	}

	@Override
	public void setRotationAngles(float par1, float par2, float par3,
			float par4, float par5, float par6, Entity entity) {

	}

	@Override
	public void initData(ModelData data) {
		this.isHidden = data.breasts == 0;
		breasts.isHidden = data.breasts != 1;
		breasts2.isHidden = data.breasts != 2;
		breasts3.isHidden = data.breasts != 3;
	}

}
