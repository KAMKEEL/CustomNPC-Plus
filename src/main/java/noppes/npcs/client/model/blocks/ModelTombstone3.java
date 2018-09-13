package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTombstone3 extends ModelBase {
	ModelRenderer Bottom;
	ModelRenderer Piece5;
	ModelRenderer Piece2;
	ModelRenderer Piece1;
	ModelRenderer Piece4;
	ModelRenderer Piece3;
	ModelRenderer Piece7;

	public ModelTombstone3() {
		Bottom = new ModelRenderer(this, 0, 0);
		Bottom.addBox(0F, 0F, 0F, 12, 5, 4);
		Bottom.setRotationPoint(-6F, 19F, -2F);
		
		Piece5 = new ModelRenderer(this, 0, 0);
		Piece5.addBox(0F, 0F, 0F, 4, 1, 4);
		Piece5.setRotationPoint(-4F, 16F, -2F);
		
		Piece2 = new ModelRenderer(this, 0, 0);
		Piece2.addBox(0F, 0F, 0F, 4, 2, 4);
		Piece2.setRotationPoint(2F, 17F, -2F);
		
		Piece1 = new ModelRenderer(this, 0, 0);
		Piece1.addBox(0F, 0F, 0F, 6, 2, 4);
		Piece1.setRotationPoint(-5F, 17F, -2F);
		
		Piece4 = new ModelRenderer(this, 0, 0);
		Piece4.addBox(0F, 0F, 0F, 1, 3, 4);
		Piece4.setRotationPoint(-5F, 14F, -2F);
		
		Piece3 = new ModelRenderer(this, 0, 0);
		Piece3.addBox(0F, 0F, 0F, 3, 1, 4);
		Piece3.setRotationPoint(3F, 16F, -2F);
		
		Piece7 = new ModelRenderer(this, 0, 0);
		Piece7.addBox(0F, 0F, 0F, 1, 1, 4);
		Piece7.setRotationPoint(-4F, 15F, -2F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Bottom.render(f5);
		Piece5.render(f5);
		Piece2.render(f5);
		Piece1.render(f5);
		Piece4.render(f5);
		Piece3.render(f5);
		Piece7.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
