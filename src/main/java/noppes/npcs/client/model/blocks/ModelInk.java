package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelInk extends ModelBase {
	ModelRenderer InkMid;
	ModelRenderer InkTop;
	ModelRenderer InkBottom;
	ModelRenderer Shape1;
	ModelRenderer InkBottom2;

	public ModelInk() {
		InkMid = new ModelRenderer(this, 0, 25);
		InkMid.addBox(0F, 0F, 0F, 1, 1, 1);
		InkMid.setRotationPoint(5F, 21F, 3.5F);
		
		InkTop = new ModelRenderer(this, 0, 22);
		InkTop.addBox(0F, 0F, 0F, 2, 1, 2);
		InkTop.setRotationPoint(4.5F, 20F, 3F);
		
		InkBottom = new ModelRenderer(this, 3, 16);
		InkBottom.addBox(0F, 0F, 0F, 3, 1, 3);
		InkBottom.setRotationPoint(4F, 22F, 2.5F);
		
		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.addBox(0F, 0F, 0F, 0, 13, 3);
		Shape1.setRotationPoint(5.5F, 10F, 2.5F);
		
		InkBottom2 = new ModelRenderer(this, 0, 27);
		InkBottom2.addBox(0F, 0F, 0F, 3, 1, 3);
		InkBottom2.setRotationPoint(4F, 23F, 2.5F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Shape1.render(f5);
		InkMid.render(f5);
		InkTop.render(f5);
		InkBottom2.render(f5);
		InkBottom.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
