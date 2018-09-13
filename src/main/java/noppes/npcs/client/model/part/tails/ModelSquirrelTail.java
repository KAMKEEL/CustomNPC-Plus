package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import noppes.npcs.client.model.ModelMPM;

public class ModelSquirrelTail extends ModelRenderer {

	private ModelMPM base;

	public ModelSquirrelTail(ModelMPM base) {
		super(base);
		this.base = base;

		textureWidth = 64;
		textureHeight = 32;

		ModelRenderer Shape1 = new ModelRenderer(base, 0, 0);
		Shape1.addBox(-1F, -1F, -1F, 2, 2, 3);
		Shape1.setRotationPoint(0F, -1F, 3F);
		setRotation(Shape1, 0F, 0F, 0F);
		this.addChild(Shape1);
		
		ModelRenderer Shape2 = new ModelRenderer(base, 0, 9);
		Shape2.addBox(-2F, -5F, -1F, 4, 5, 3);
		Shape2.setRotationPoint(0F, 0F, 1F);
		setRotation(Shape2, -0.37F, 0F, 0F);
		Shape1.addChild(Shape2);
		
		ModelRenderer Shape3 = new ModelRenderer(base, 0, 18);
		Shape3.addBox(-2.466667F, -6F, -1F, 5, 7, 3);
		Shape3.setRotationPoint(0F, -5F, 0F);
		setRotation(Shape3, 0.3f, 0F, 0F);
		Shape2.addChild(Shape3);
		
		ModelRenderer Shape4 = new ModelRenderer(base, 25, 0);
		Shape4.addBox(-3F, -0.6F, -1F, 6, 5, 3);
		Shape4.setRotationPoint(0F, -5F, 1F);
		setRotation(Shape4, 2.5F, 0F, 0F);
		Shape3.addChild(Shape4);
		
		ModelRenderer Shape5 = new ModelRenderer(base, 25, 10);
		Shape5.addBox(-3F, -2F, -1F, 6, 3, 5);
		Shape5.setRotationPoint(0F, 3.5F, 0F);
		setRotation(Shape5, -2.5F, 0F, 0F);
		Shape4.addChild(Shape5);
	}

	public void setRotationAngles(float par1, float par2, float par3,
			float par4, float par5, float par6, Entity entity) {

	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
