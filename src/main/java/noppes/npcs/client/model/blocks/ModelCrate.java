package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCrate extends ModelBase {
	ModelRenderer sticky1;
	ModelRenderer sticky2;
	ModelRenderer sticky3;
	ModelRenderer sticky4;
	ModelRenderer core;
	ModelRenderer sticky1top;
	ModelRenderer sticky2top;
	ModelRenderer sticky3top;
	ModelRenderer sticky4top;
	ModelRenderer sidestick2;
	ModelRenderer sidestick3;
	ModelRenderer sidestick1;
	ModelRenderer sidestick4;
	ModelRenderer sidestuff2;
	ModelRenderer sidestuff1;
	ModelRenderer sidestuff3;
	ModelRenderer sidestuff4;
	ModelRenderer Shape1;
	ModelRenderer Shape2;

	public ModelCrate() {
		sticky1 = new ModelRenderer(this, 0, 0);
		sticky1.addBox(0F, 0F, 0F, 2, 2, 12);
		sticky1.setRotationPoint(6F, 22F, -6F);
		
		sticky2 = new ModelRenderer(this, 0, 0);
		sticky2.addBox(0F, 0F, 0F, 12, 2, 2);
		sticky2.setRotationPoint(-6F, 22F, -8F);
		
		sticky3 = new ModelRenderer(this, 0, 0);
		sticky3.addBox(0F, 0F, 0F, 2, 2, 12);
		sticky3.setRotationPoint(-8F, 22F, -6F);
		
		sticky4 = new ModelRenderer(this, 32, 0);
		sticky4.addBox(0F, 0F, 0F, 12, 2, 2);
		sticky4.setRotationPoint(-6F, 22F, 6F);
		
		core = new ModelRenderer(this, 0, 0);
		core.addBox(-8F, 0, -8F, 16, 16, 16, -1f);
		core.setRotationPoint(0, 8F, 0);
		
		sticky1top = new ModelRenderer(this, 0, 0);
		sticky1top.addBox(0F, 0F, 0F, 2, 2, 12);
		sticky1top.setRotationPoint(6F, 8F, -6F);
		
		sticky2top = new ModelRenderer(this, 0, 0);
		sticky2top.addBox(0F, 0F, 0F, 12, 2, 2);
		sticky2top.setRotationPoint(-6F, 8F, 6F);
		
		sticky3top = new ModelRenderer(this, 0, 0);
		sticky3top.addBox(0F, 0F, 0F, 2, 2, 12);
		sticky3top.setRotationPoint(-8F, 8F, -6F);
		
		sticky4top = new ModelRenderer(this, 0, 0);
		sticky4top.addBox(0F, 0F, 0F, 12, 2, 2);
		sticky4top.setRotationPoint(-6F, 8F, -8F);
		
		sidestick1 = new ModelRenderer(this, 0, 0);
		sidestick1.addBox(0F, 0F, 0F, 2, 16, 2);
		sidestick1.setRotationPoint(-8F, 8F, 6F);
		
		sidestick2 = new ModelRenderer(this, 0, 0);
		sidestick2.addBox(0F, 0F, 0F, 2, 16, 2);
		sidestick2.setRotationPoint(6F, 8F, 6F);
		
		sidestick3 = new ModelRenderer(this, 0, 0);
		sidestick3.addBox(0F, 0F, 0F, 2, 16, 2);
		sidestick3.setRotationPoint(-8F, 8F, -8F);
		
		sidestick4 = new ModelRenderer(this, 0, 0);
		sidestick4.addBox(0F, 0F, 0F, 2, 16, 2);
		sidestick4.setRotationPoint(6F, 8F, -8F);
		
		sidestuff1 = new ModelRenderer(this, 0, 0);
		sidestuff1.addBox(0F, 1F, 0F, 1, 18, 2);
		sidestuff1.setRotationPoint(6F, 8.5F, -6.5F);
		setRotation(sidestuff1, -0.7853982F, 1.570796F, 0F);
		
		sidestuff2 = new ModelRenderer(this, 0, 0);
		sidestuff2.addBox(0F, -1F, 0F, 1, 18, 2);
		sidestuff2.setRotationPoint(-7.5F, 9.5F, 5F);
		setRotation(sidestuff2, -0.7853982F, 0F, 0F);
		
		sidestuff3 = new ModelRenderer(this, 0, 0);
		sidestuff3.addBox(0F, 1F, 0F, 1, 18, 2);
		sidestuff3.setRotationPoint(7.5F, 8.5F, -6F);
		setRotation(sidestuff3, -0.7853982F, 3.141593F, 0F);
		
		sidestuff4 = new ModelRenderer(this, 0, 0);
		sidestuff4.addBox(0F, 1F, 0F, 1, 18, 2);
		sidestuff4.setRotationPoint(-6F, 8.5F, 6.5F);
		setRotation(sidestuff4, -0.7853982F, -1.570796F, 0F);
		
		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.addBox(0F, 0F, 0F, 18, 1, 2);
		Shape1.setRotationPoint(-5.5F, 22.5F, -7F);
		setRotation(Shape1, 0F, -0.7853982F, 0F);
		
		Shape2 = new ModelRenderer(this, 0, 0);
		Shape2.addBox(0F, 0F, 0F, 18, 1, 2);
		Shape2.setRotationPoint(-5.5F, 8.5F, -7F);
		setRotation(Shape2, 0F, -0.7853982F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		sticky1.render(f5);
		sticky2.render(f5);
		sticky3.render(f5);
		sticky4.render(f5);
		core.render(f5);
		sticky1top.render(f5);
		sticky2top.render(f5);
		sticky3top.render(f5);
		sticky4top.render(f5);

		sidestick1.render(f5);
		sidestick2.render(f5);
		sidestick3.render(f5);
		sidestick4.render(f5);
		sidestuff1.render(f5);
		sidestuff2.render(f5);
		sidestuff3.render(f5);
		sidestuff4.render(f5);
		Shape1.render(f5);
		Shape2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
