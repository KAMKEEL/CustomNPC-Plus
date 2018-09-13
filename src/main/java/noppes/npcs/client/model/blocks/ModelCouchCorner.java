package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCouchCorner extends ModelBase {
	ModelRenderer Leg1;
	ModelRenderer Leg2;
	ModelRenderer Leg3;
	ModelRenderer Leg4;
	ModelRenderer Leg5;
	ModelRenderer Back;
	ModelRenderer Back2;
	ModelRenderer Bottom;
	ModelRenderer Bottom2;

	public ModelCouchCorner() {
		Leg1 = new ModelRenderer(this, 0, 0);
		Leg1.addBox(0F, 0F, 0F, 1, 1, 2);
		Leg1.setRotationPoint(7F, 23F, 6F);
		
		Leg2 = new ModelRenderer(this, 0, 0);
		Leg2.addBox(0F, 0F, 0F, 2, 1, 1);
		Leg2.setRotationPoint(4F, 23F, -8F);
		
		Leg3 = new ModelRenderer(this, 0, 0);
		Leg3.addBox(0F, 0F, 0F, 2, 1, 1);
		Leg3.setRotationPoint(-8F, 23F, -8F);
		
		Leg4 = new ModelRenderer(this, 0, 0);
		Leg4.addBox(0F, 0F, 0F, 2, 1, 2);
		Leg4.setRotationPoint(-8F, 23F, 6F);
		
		Leg5 = new ModelRenderer(this, 0, 0);
		Leg5.addBox(0F, 0F, 0F, 1, 1, 2);
		Leg5.setRotationPoint(7F, 23F, -6F);
		
		Back = new ModelRenderer(this, 1, 1);
		Back.addBox(0F, 0F, 0F, 1, 15, 15);
		Back.setRotationPoint(-8F, 6F, -8F);
		
		Back2 = new ModelRenderer(this, 14, 15);
		Back2.addBox(0F, 0F, 0F, 16, 15, 1);
		Back2.setRotationPoint(-8F, 6F, 7F);
		
		Bottom = new ModelRenderer(this, 4, 0);
		Bottom.addBox(0F, 0F, 0F, 16, 2, 14);
		Bottom.setRotationPoint(-8F, 21F, -6F);
		
		Bottom2 = new ModelRenderer(this, 0, 0);
		Bottom2.addBox(0F, 0F, 0F, 14, 2, 2);
		Bottom2.setRotationPoint(-8F, 21F, -8F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Leg1.render(f5);
		Leg2.render(f5);
		Leg3.render(f5);
		Leg4.render(f5);
		Leg5.render(f5);
		Back.render(f5);
		Back2.render(f5);
		Bottom.render(f5);
		Bottom2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
