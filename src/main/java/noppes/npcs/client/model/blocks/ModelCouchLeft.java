package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCouchLeft extends ModelBase {
	ModelRenderer Leg1;
	ModelRenderer Leg2;
	ModelRenderer Leg3;
	ModelRenderer Leg4;
	ModelRenderer Back;
	ModelRenderer Side;
	ModelRenderer Bottom;

	public ModelCouchLeft() {
		Leg1 = new ModelRenderer(this, 0, 0);
		Leg1.addBox(0F, 0F, 0F, 2, 1, 2);
		Leg1.setRotationPoint(-8F, 23F, 6F);
		
		Leg2 = new ModelRenderer(this, 0, 0);
		Leg2.addBox(0F, 0F, 0F, 1, 1, 2);
		Leg2.setRotationPoint(7F, 23F, -6F);
		
		Leg3 = new ModelRenderer(this, 0, 0);
		Leg3.addBox(0F, 0F, 0F, 2, 1, 2);
		Leg3.setRotationPoint(-8F, 23F, -6F);
		
		Leg4 = new ModelRenderer(this, 0, 0);
		Leg4.addBox(0F, 0F, 0F, 1, 1, 2);
		Leg4.setRotationPoint(7F, 23F, 6F);
		
		Back = new ModelRenderer(this, 1, 15);
		Back.addBox(0F, 0F, 0F, 14, 15, 1);
		Back.setRotationPoint(-6F, 6F, 7F);
		
		Side = new ModelRenderer(this, 1, 28);
		Side.addBox(0F, 0F, 0F, 2, 11, 14);
		Side.setRotationPoint(-8F, 12F, -6F);
		
		Bottom = new ModelRenderer(this, 3, 1);
		Bottom.addBox(0F, 0F, 0F, 14, 2, 14);
		Bottom.setRotationPoint(-6F, 21F, -6F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Leg1.render(f5);
		Leg2.render(f5);
		Leg3.render(f5);
		Leg4.render(f5);
		Back.render(f5);
		Side.render(f5);
		Bottom.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
