package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelStool extends ModelBase {
	ModelRenderer Base;
	ModelRenderer Leg1;
	ModelRenderer Leg2;
	ModelRenderer Leg3;
	ModelRenderer Leg4;
	ModelRenderer Shape1;
	ModelRenderer Shape2;
	ModelRenderer Shape3;
	ModelRenderer Shape4;

	public ModelStool() {
		Base = new ModelRenderer(this, 9, 3);
		Base.addBox(-5F, 0F, -5F, 10, 1, 10);
		Base.setRotationPoint(0F, 16F, 0F);
		
		Leg1 = new ModelRenderer(this, 0, 12);
		Leg1.addBox(-1F, 0F, 0F, 2, 8, 1);
		Leg1.setRotationPoint(2F, 17F, 2F);
		setRotation(Leg1, 0.3316126F, 0.7853982F, 0F);
		
		Leg2 = new ModelRenderer(this, 0, 12);
		Leg2.addBox(-1F, 0F, 0F, 2, 8, 1);
		Leg2.setRotationPoint(2F, 17F, -2F);
		setRotation(Leg2, 0.3316126F, 2.356194F, -0.0081449F);
		
		Leg3 = new ModelRenderer(this, 0, 12);
		Leg3.addBox(-1F, 0F, 0F, 2, 8, 1);
		Leg3.setRotationPoint(-2F, 17F, 2F);
		setRotation(Leg3, 0.3316126F, -0.7853982F, 0F);
		
		Leg4 = new ModelRenderer(this, 0, 12);
		Leg4.addBox(-1F, 0F, 0F, 2, 8, 1);
		Leg4.setRotationPoint(-2F, 17F, -2F);
		setRotation(Leg4, 0.3316126F, -2.356194F, 0F);
		
		Shape1 = new ModelRenderer(this, 0, 11);
		Shape1.addBox(-3F, 0F, 0F, 6, 1, 1);
		Shape1.setRotationPoint(2.4F, 19F, 0F);
		setRotation(Shape1, 0F, 1.570796F, 0F);
		
		Shape2 = new ModelRenderer(this, 0, 11);
		Shape2.addBox(-3F, 0F, 0F, 6, 1, 1);
		Shape2.setRotationPoint(0F, 19F, 2.4F);
		
		Shape3 = new ModelRenderer(this, 0, 11);
		Shape3.addBox(-3F, 0F, 0F, 6, 1, 1);
		Shape3.setRotationPoint(0F, 19F, -3.4F);
		
		Shape4 = new ModelRenderer(this, 0, 11);
		Shape4.addBox(-3F, 0F, 0F, 6, 1, 1);
		Shape4.setRotationPoint(-3.4F, 19F, 0F);
		setRotation(Shape4, 0F, 1.570796F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Base.render(f5);
		Leg1.render(f5);
		Leg2.render(f5);
		Leg3.render(f5);
		Leg4.render(f5);
		Shape1.render(f5);
		Shape2.render(f5);
		Shape3.render(f5);
		Shape4.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}


}
