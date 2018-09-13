package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCouchWoodRight extends ModelBase {
	// fields
	ModelRenderer Shape1;
	ModelRenderer Shape2;
	ModelRenderer Shape3;
	ModelRenderer Shape4;
	ModelRenderer Shape5;
	ModelRenderer Shape6;
	ModelRenderer Shape7;
	ModelRenderer Shape8;
	ModelRenderer Shape9;
	ModelRenderer Shape10;
	ModelRenderer Shape11;
	ModelRenderer Shape12;
	ModelRenderer Shape13;
	ModelRenderer Shape14;
	ModelRenderer Shape15;
	ModelRenderer Shape16;
	ModelRenderer Shape17;
	ModelRenderer Shape18;

	public ModelCouchWoodRight() {
		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.addBox(0F, 0F, 0F, 2, 3, 10);
		Shape1.setRotationPoint(6F, 18F, -4F);

		Shape2 = new ModelRenderer(this, 0, 0);
		Shape2.addBox(0F, 0F, 0F, 1, 3, 2);
		Shape2.setRotationPoint(-8F, 21F, -6F);

		Shape3 = new ModelRenderer(this, 0, 0);
		Shape3.addBox(0F, 0F, 0F, 2, 4, 1);
		Shape3.setRotationPoint(6F, 14F, 0.5F);

		Shape4 = new ModelRenderer(this, 0, 0);
		Shape4.addBox(0F, 0F, 0F, 14, 2, 1);
		Shape4.setRotationPoint(-8F, 7F, 7F);

		Shape5 = new ModelRenderer(this, 0, 0);
		Shape5.addBox(0F, 0F, 0F, 14, 2, 2);
		Shape5.setRotationPoint(-8F, 19F, 6F);

		Shape6 = new ModelRenderer(this, 0, 0);
		Shape6.addBox(0F, 0F, 0F, 2, 1, 13);
		Shape6.setRotationPoint(6F, 13F, -7F);

		Shape7 = new ModelRenderer(this, 0, 0);
		Shape7.addBox(0F, 0F, 0F, 2, 1, 10);
		Shape7.setRotationPoint(-7F, 19F, -4F);

		Shape8 = new ModelRenderer(this, 0, 0);
		Shape8.addBox(0F, 0F, 0F, 1, 3, 2);
		Shape8.setRotationPoint(-8F, 21F, 6F);

		Shape9 = new ModelRenderer(this, 0, 0);
		Shape9.addBox(0F, 0F, 0F, 1, 1, 10);
		Shape9.setRotationPoint(5F, 19F, -4F);

		Shape10 = new ModelRenderer(this, 0, 0);
		Shape10.addBox(0F, 0F, 0F, 2, 10, 1);
		Shape10.setRotationPoint(-7F, 9F, 7F);

		Shape11 = new ModelRenderer(this, 0, 0);
		Shape11.addBox(0F, 0F, 0F, 14, 2, 2);
		Shape11.setRotationPoint(-8F, 19F, -6F);

		Shape12 = new ModelRenderer(this, 0, 0);
		Shape12.addBox(0F, 0F, 0F, 2, 1, 10);
		Shape12.setRotationPoint(-3F, 19F, -4F);

		Shape13 = new ModelRenderer(this, 0, 0);
		Shape13.addBox(0F, 0F, 0F, 2, 1, 10);
		Shape13.setRotationPoint(1F, 19F, -4F);

		Shape14 = new ModelRenderer(this, 0, 0);
		Shape14.addBox(0F, 0F, 0F, 2, 10, 1);
		Shape14.setRotationPoint(-3F, 9F, 7F);

		Shape15 = new ModelRenderer(this, 0, 0);
		Shape15.addBox(0F, 0F, 0F, 2, 10, 1);
		Shape15.setRotationPoint(1F, 9F, 7F);

		Shape16 = new ModelRenderer(this, 0, 0);
		Shape16.addBox(0F, 0F, 0F, 1, 10, 1);
		Shape16.setRotationPoint(5F, 9F, 7F);

		Shape17 = new ModelRenderer(this, 0, 0);
		Shape17.addBox(0F, 0F, 0F, 2, 10, 2);
		Shape17.setRotationPoint(6F, 14F, -6F);

		Shape18 = new ModelRenderer(this, 0, 0);
		Shape18.addBox(0F, 0F, 0F, 2, 18, 2);
		Shape18.setRotationPoint(6F, 6F, 6F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Shape1.render(f5);
		Shape2.render(f5);
		Shape3.render(f5);
		Shape4.render(f5);
		Shape5.render(f5);
		Shape6.render(f5);
		Shape7.render(f5);
		Shape8.render(f5);
		Shape9.render(f5);
		Shape10.render(f5);
		Shape11.render(f5);
		Shape12.render(f5);
		Shape13.render(f5);
		Shape14.render(f5);
		Shape15.render(f5);
		Shape16.render(f5);
		Shape17.render(f5);
		Shape18.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
