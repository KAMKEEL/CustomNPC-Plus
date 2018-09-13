package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMailboxUS extends ModelBase {
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

	public ModelMailboxUS() {
		textureWidth = 64;
		textureHeight = 128;

		Shape1 = new ModelRenderer(this, 0, 48);
		Shape1.addBox(0F, 0F, 0F, 16, 14, 16);
		Shape1.setRotationPoint(-8F, 8F, -8F);
		
		Shape2 = new ModelRenderer(this, 0, 79);
		Shape2.addBox(0F, 0F, 0F, 1, 2, 1);
		Shape2.setRotationPoint(-8F, 22F, -8F);
		
		Shape3 = new ModelRenderer(this, 5, 79);
		Shape3.addBox(0F, 0F, 0F, 1, 2, 1);
		Shape3.setRotationPoint(-8F, 22F, 7F);
		
		Shape4 = new ModelRenderer(this, 10, 79);
		Shape4.addBox(0F, 0F, 0F, 1, 2, 1);
		Shape4.setRotationPoint(7F, 22F, -8F);
		
		Shape5 = new ModelRenderer(this, 15, 79);
		Shape5.addBox(0F, 0F, 0F, 1, 2, 1);
		Shape5.setRotationPoint(7F, 22F, 7F);
		
		Shape6 = new ModelRenderer(this, 0, 14);
		Shape6.addBox(0F, 0F, 0F, 16, 3, 7);
		Shape6.setRotationPoint(-8F, 5F, 0F);
		
		Shape7 = new ModelRenderer(this, 0, 6);
		Shape7.addBox(0F, 0F, 0F, 16, 2, 6);
		Shape7.setRotationPoint(-8F, 3F, 0F);
		
		Shape8 = new ModelRenderer(this, 0, 0);
		Shape8.addBox(0F, 0F, 0F, 16, 1, 5);
		Shape8.setRotationPoint(-8F, 2F, 0F);
		
		Shape9 = new ModelRenderer(this, 0, 37);
		Shape9.addBox(0F, 0F, 0F, 1, 3, 7);
		Shape9.setRotationPoint(-8F, 5F, -7F);
		
		Shape10 = new ModelRenderer(this, 16, 37);
		Shape10.addBox(0F, 0F, 0F, 1, 3, 7);
		Shape10.setRotationPoint(7F, 5F, -7F);
		
		Shape11 = new ModelRenderer(this, 0, 29);
		Shape11.addBox(0F, 0F, 0F, 1, 2, 6);
		Shape11.setRotationPoint(-8F, 3F, -6F);
		
		Shape12 = new ModelRenderer(this, 14, 29);
		Shape12.addBox(0F, 0F, 0F, 1, 2, 6);
		Shape12.setRotationPoint(7F, 3F, -6F);
		
		Shape13 = new ModelRenderer(this, 0, 25);
		Shape13.addBox(0F, 0F, 0F, 16, 1, 3);
		Shape13.setRotationPoint(-8F, 2F, -3F);
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
	}

}
