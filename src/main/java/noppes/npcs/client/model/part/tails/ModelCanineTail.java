package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCanineTail extends ModelRenderer {
	ModelRenderer Base_1;
	ModelRenderer BaseB_1;
	ModelRenderer Mid_1;
	ModelRenderer Mid_2;
	ModelRenderer MidB_1;
	ModelRenderer End_1;

	public ModelCanineTail(ModelBiped base) {
		super(base);

		Base_1 = new ModelRenderer(base, 56, 16);
		Base_1.addBox(-1F, 0F, -3F, 2, 3, 2);
		Base_1.setRotationPoint(0F, 1F, -1.2F);
		setRotation(Base_1, -0.4490659F, 3.141593F, 0F);
		addChild(Base_1);

		BaseB_1 = new ModelRenderer(base, 56, 16);
		BaseB_1.addBox(-0.5F, 0F, -1.5F, 1, 3, 1);
		Base_1.addChild(BaseB_1);

		Mid_1 = new ModelRenderer(base, 56, 20);
		Mid_1.addBox(-1F, 3F, -2.8F, 2, 2, 2);
		setRotation(Mid_1, -0.16F, 0F, 0F);
		Base_1.addChild(Mid_1);

		Mid_2 = new ModelRenderer(base, 56, 22);
		Mid_2.addBox(-1.5F, 5F, -1.5F, 3, 6, 3);
		Mid_2.setRotationPoint(0, 0, -1.5f);
	    setRotation(Mid_2, -0F, 0F, 0F);
		Mid_1.addChild(Mid_2);

		ModelRenderer Mid_2b = new ModelRenderer(base, 56, 22);
		Mid_2b.addBox(-1.5F, 5F, -1.5F, 3, 6, 3);
	    setRotation(Mid_2b, -0F, (float)Math.PI, 0F);
	    Mid_2.addChild(Mid_2b);

		MidB_1 = new ModelRenderer(base, 56, 20);
		MidB_1.addBox(-0.5F, 3F, -1F, 1, 2, 1);
		Mid_1.addChild(MidB_1);

		End_1 = new ModelRenderer(base, 56, 29);
		End_1.addBox(-1F, 10.7F, -1F, 2, 1, 2);
		Mid_2.addChild(End_1);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
		Base_1.rotateAngleX = -0.5490659F - f1 * 0.7f;

		Base_1.rotateAngleY = 3.141593F + rotateAngleY * 0.1f;
		Mid_1.rotateAngleY = rotateAngleY * 0.2f;
		Mid_2.rotateAngleY = rotateAngleY * 0.2f;
	}

}
