package noppes.npcs.client.model.part.legs;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import noppes.npcs.ModelData;
import noppes.npcs.constants.EnumAnimation;

public class ModelMermaidLegs2 extends ModelRenderer {

	ModelRenderer Tail1;
	ModelRenderer Tail2;
	ModelRenderer Tail3;
	ModelRenderer Tail4;
	ModelRenderer Tail5;
	ModelRenderer Tail6;
	ModelRenderer Tail7;
	ModelRenderer Tail8;

    public boolean isRiding = false;
    public boolean isSneaking = false;
    public boolean isSleeping = false;
    public boolean isCrawling = false;

	public ModelMermaidLegs2(ModelBase base) {
		super(base);
		textureWidth = 64;
		textureHeight = 32;

		Tail1 = new ModelRenderer(base, 0, 18);
		Tail1.setTextureSize(64,32);
		Tail1.addBox(0F, 0F, 0F, 8, 6, 4);
		Tail1.setRotationPoint(-4F, 12F, -2F);
		setRotation(Tail1, 0.075F, 0F, 0F);

		Tail2 = new ModelRenderer(base, 0, 18);
		Tail2.setTextureSize(64,32);
		Tail2.addBox(0F, 0F, 0F, 6, 5, 3);
		Tail2.setRotationPoint(1F, 5.5F, 0.3F);
		setRotation(Tail2, 0.56F, 0F, 0F);
		Tail1.addChild(Tail2);

		Tail3 = new ModelRenderer(base, 0, 18);
		Tail3.setTextureSize(64,32);
		Tail3.addBox(0F, 0F, 0F, 5, 5, 2);
		Tail3.setRotationPoint(5.5F, 4F, 2.5F);
		setRotation(Tail3, -0.37818F, 3.141593F, 0F);
		Tail2.addChild(Tail3);

		Tail4 = new ModelRenderer(base, 0, 20);
		Tail4.setTextureSize(64,32);
		Tail4.addBox(0F, 0F, 0F, 4, 3, 1);
		Tail4.setRotationPoint(0.5F, 4.5F, 0.5F);
		setRotation(Tail4, -0.1f, 0, 0F);
		Tail3.addChild(Tail4);

		Tail5 = new ModelRenderer(base, 0, 20);
		Tail5.setTextureSize(64,32);
		Tail5.addBox(0F, 0F, 0F, 1, 3, 1);
		Tail5.setRotationPoint(-1F, 1.5F, 0F);
		setRotation(Tail5, 0, 0, 0F);
		Tail4.addChild(Tail5);

		Tail6 = new ModelRenderer(base, 0, 20);
		Tail6.setTextureSize(64,32);
		Tail6.addBox(0F, 0F, 0F, 1, 3, 1);
		Tail6.setRotationPoint(-2F, 3F, 0F);
		setRotation(Tail6, 0, 0, 0F);
		Tail4.addChild(Tail6);

		Tail7 = new ModelRenderer(base, 0, 20);
		Tail7.setTextureSize(64,32);
		Tail7.addBox(0F, 0F, 0F, 1, 3, 1);
		Tail7.setRotationPoint(4F, 1.5F, 0F);
		setRotation(Tail7, 0, 0, 0F);
		Tail4.addChild(Tail7);

		Tail8 = new ModelRenderer(base, 0, 20);
		Tail8.setTextureSize(64,32);
		Tail8.addBox(0F, 0F, 0F, 1, 3, 1);
		Tail8.setRotationPoint(5F, 3F, 0F);
		setRotation(Tail8, 0, 0, 0F);
		Tail4.addChild(Tail8);

	}

	@Override
	public void render(float f5) {
		if (isHidden || !showModel)
			return;
		Tail1.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6,
								  Entity entity) {
		Tail1.setRotationPoint(-4F, 12F, -2F);
		float ani = MathHelper.sin(par1 * 0.6662F);
		if (ani > 0.2)
			ani /= 3f;
		if(this.isSleeping || this.isCrawling){
			Tail1.rotateAngleX = Tail2.rotateAngleX = Tail3.rotateAngleX = Tail4.rotateAngleX = 0;
		}
		else{
			Tail1.rotateAngleX = 0.2F - ani * 0.2F * par2;
			Tail2.rotateAngleX = 0.56F - ani * 0.24F * par2;
			Tail3.rotateAngleX = -0.4F + ani * 0.24F * par2;
			Tail4.rotateAngleX = -0.1f + ani * 0.10F * par2;

			if(entity.isSneaking()){
				Tail1.setRotationPoint(-4F, 10F, 3F);
			}
		}

	}
}