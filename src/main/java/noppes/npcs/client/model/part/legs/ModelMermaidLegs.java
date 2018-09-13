package noppes.npcs.client.model.part.legs;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelMermaidLegs extends ModelRenderer {

	ModelRenderer top;
	ModelRenderer middle;
	ModelRenderer bottom;
	ModelRenderer fin1;
	ModelRenderer fin2;

	public ModelMermaidLegs(ModelBase base) {
		super(base);
		textureWidth = 64;
		textureHeight = 32;

		top = new ModelRenderer(base, 0, 16);
		top.addBox(-2F, -2.5F, -2F, 8, 9, 4);
		top.setRotationPoint(-2F, 14F, 1F);
		setRotation(top, 0.26F, 0F, 0F);

		middle = new ModelRenderer(base, 28, 0);
		middle.addBox(0F, 0F, 0F, 7, 6, 4);
		middle.setRotationPoint(-1.5F, 6.5F, -1F);
		setRotation(middle, 0.86f, 0F, 0F);
		top.addChild(middle);

		bottom = new ModelRenderer(base, 24, 16);
		bottom.addBox(0F, 0F, 0F, 6, 7, 3);
		bottom.setRotationPoint(0.5F, 6F, 0.5f);
		setRotation(bottom, 0.15f, 0F, 0F);
		middle.addChild(bottom);

		fin1 = new ModelRenderer(base, 0, 0);
		fin1.addBox(0F, 0F, 0F, 5, 9, 1);
		fin1.setRotationPoint(0F, 4.5F, 1F);
		setRotation(fin1, 0.05f, 0, 0.5911399F);
		bottom.addChild(fin1);
		
		fin2 = new ModelRenderer(base, 0, 0);
		fin2.mirror = true;
		fin2.addBox(-5F, 0F, 0F, 5, 9, 1);
		fin2.setRotationPoint(6F, 4.5F, 1F);
		setRotation(fin2, 0.05f, 0, -0.591143F);
		bottom.addChild(fin2);

	}

	@Override
	public void render(float f5) {
		if (isHidden || !showModel)
			return;
		top.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float par1, float par2, float par3,
			float par4, float par5, float par6, Entity entity) {
		float ani = MathHelper.sin(par1 * 0.6662F);
		if(ani > 0.2)
			ani /= 3f;
		top.rotateAngleX = 0.26F - ani * 0.2F * par2;
		middle.rotateAngleX = 0.86f - ani * 0.24F * par2;
		bottom.rotateAngleX = 0.15f - ani * 0.28F * par2;
		fin2.rotateAngleX = fin1.rotateAngleX = 0.05f - ani * 0.35F * par2;
		
	}
}
