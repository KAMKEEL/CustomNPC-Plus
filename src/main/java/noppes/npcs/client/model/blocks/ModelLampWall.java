package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelLampWall extends ModelBase {
	ModelRenderer Base;
	ModelRenderer Top1;
	ModelRenderer Top2;
	ModelRenderer Top3;
	ModelRenderer Shape2;
	ModelRenderer Shape3;
	ModelRenderer Shape4;
	ModelRenderer Shape5;

	public ModelLampWall() {
		Base = new ModelRenderer(this, 0, 6);
		Base.addBox(0F, 0F, 0F, 4, 7, 4);
		Base.setRotationPoint(-2F, 14F, 1F);
		
		Top1 = new ModelRenderer(this, 0, 0);
		Top1.addBox(0F, 0F, 0F, 5, 1, 5);
		Top1.setRotationPoint(-2.5F, 14F, 0.5F);
		
		Top2 = new ModelRenderer(this, 0, 0);
		Top2.addBox(0F, 0F, 0F, 4, 1, 4);
		Top2.setRotationPoint(-2F, 13.5F, 1F);
		
		Top3 = new ModelRenderer(this, 0, 0);
		Top3.addBox(0F, 0F, 0F, 3, 1, 3);
		Top3.setRotationPoint(-1.5F, 13F, 1.5F);
		
		Shape2 = new ModelRenderer(this, 0, 0);
		Shape2.addBox(0F, 0F, 0F, 1, 1, 3);
		Shape2.setRotationPoint(-0.5F, 11F, 3.5F);
		
		Shape3 = new ModelRenderer(this, 0, 0);
		Shape3.addBox(0F, 0F, 0F, 3, 3, 1);
		Shape3.setRotationPoint(0F, 9.5F, 6.5F);
		setRotation(Shape3, 0F, 0F, 0.7853982F);
		
		Shape4 = new ModelRenderer(this, 0, 0);
		Shape4.addBox(0F, 0F, 0F, 1, 3, 1);
		Shape4.setRotationPoint(-0.5F, 10.5F, 2.5F);
		
		Shape5 = new ModelRenderer(this, 0, 0);
		Shape5.addBox(0F, 0F, 0F, 4, 4, 1);
		Shape5.setRotationPoint(0F, 8.7F, 7F);
		setRotation(Shape5, 0F, 0F, 0.7853982F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Base.render(f5);
		Top1.render(f5);
		Top2.render(f5);
		Top3.render(f5);
		Shape2.render(f5);
		Shape3.render(f5);
		Shape4.render(f5);
		Shape5.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}


}
