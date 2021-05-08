package noppes.npcs.client.model.part.horns;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import noppes.npcs.client.model.ModelMPM;

public class ModelAntlerHorns extends ModelRenderer {

	public ModelAntlerHorns(ModelMPM base) {
		super(base);

		textureWidth = 64;
		textureHeight = 32;

		ModelRenderer right_base_horn = new ModelRenderer(base, 58, 20);
		right_base_horn.setTextureSize(64,32);
		right_base_horn.addBox(0F, -5F, 0F, 1, 6, 1);
		right_base_horn.setRotationPoint(-2.5F, -6F, -1F);
		setRotation(right_base_horn, 0F, 0F, -0.2F);
		addChild(right_base_horn);

		ModelRenderer right_horn1 = new ModelRenderer(base, 58, 20);
		right_horn1.setTextureSize(64,32);
		right_horn1.addBox(0F, -5F, 0F, 1, 5, 1);
		right_horn1.setRotationPoint(0F, -4F, 0F);
		setRotation(right_horn1, 1F, 0F, -1F);
		right_base_horn.addChild(right_horn1);

		ModelRenderer right_horn2 = new ModelRenderer(base, 58, 20);
		right_horn2.setTextureSize(64,32);
		right_horn2.addBox(0F, -4F, 0F, 1, 5, 1);
		right_horn2.setRotationPoint(-0F, -6F, -0F);
		setRotation(right_horn2, -0.5F, -0.5F, 0F);
		right_base_horn.addChild(right_horn2);

		ModelRenderer things1 = new ModelRenderer(base, 58, 20);
		things1.setTextureSize(64,32);
		things1.addBox(0F, -5F, 0F, 1, 5, 1);
		things1.setRotationPoint(0F, -3F, 1F);
		setRotation(things1, 2F, 0.5f, 0.5f);
		right_horn2.addChild(things1);

		ModelRenderer things2 = new ModelRenderer(base, 58, 20);
		things2.setTextureSize(64,32);
		things2.addBox(0F, -5F, 0F, 1, 5, 1);
		things2.setRotationPoint(0F, -3F, 1F);
		setRotation(things2, 2F, -0.5f, -0.5f);
		right_horn2.addChild(things2);

		ModelRenderer left_base_horn = new ModelRenderer(base, 58, 20);
		left_base_horn.setTextureSize(64,32);
		left_base_horn.addBox(0F, -5F, 0F, 1, 6, 1);
		left_base_horn.setRotationPoint(1.5F, -6F, -1F);
		setRotation(left_base_horn, 0F, 0F, 0.2F);
		addChild(left_base_horn);

		ModelRenderer left_horn1 = new ModelRenderer(base, 58, 20);
		left_horn1.setTextureSize(64,32);
		left_horn1.addBox(0F, -5F, 0F, 1, 5, 1);
		left_horn1.setRotationPoint(0F, -5F, 0F);
		setRotation(left_horn1, 1F, 0F, 1F);
		left_base_horn.addChild(left_horn1);

		ModelRenderer left_horn2 = new ModelRenderer(base, 58, 20);
		left_horn2.setTextureSize(64,32);
		left_horn2.addBox(0F, -4F, 0F, 1, 5, 1);
		left_horn2.setRotationPoint(0F, -6F, 1F);
		setRotation(left_horn2, -0.5F, 0.5F, 0F);
		left_base_horn.addChild(left_horn2);

		ModelRenderer things8 = new ModelRenderer(base, 58, 20);
		things8.setTextureSize(64,32);
		things8.addBox(0F, -5F, 0F, 1, 5, 1);
		things8.setRotationPoint(0F, -3F, 1F);
		setRotation(things8, 2F, -0.5f, -0.5f);
		left_horn2.addChild(things8);

		ModelRenderer things4 = new ModelRenderer(base, 58, 20);
		things4.setTextureSize(64,32);
		things4.addBox(0F, -5F, 0F, 1, 5, 1);
		things4.setRotationPoint(0F, -3F, 1F);
		setRotation(things4, 2F, 0.5f, 0.5f);
		left_horn2.addChild(things4);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
