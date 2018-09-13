package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCouchCornerWool extends ModelBase {
	ModelRenderer Wool1;
	ModelRenderer Wool2;
	ModelRenderer Wool3;
	ModelRenderer Wool4;

	public ModelCouchCornerWool() {		
		Wool1 = new ModelRenderer(this, 11, 3);
		Wool1.addBox(0F, 0F, 0F, 13, 5, 2);
		Wool1.setRotationPoint(-7F, 16F, -8F);
		
		Wool2 = new ModelRenderer(this, 2, 4);
		Wool2.addBox(0F, 0F, 0F, 2, 10, 13);
		Wool2.setRotationPoint(-7F, 6F, -8F);
		
		Wool3 = new ModelRenderer(this, 14, 15);
		Wool3.addBox(0F, 0F, 0F, 15, 10, 2);
		Wool3.setRotationPoint(-7F, 6F, 5F);
		
		Wool4 = new ModelRenderer(this, 0, 45);
		Wool4.addBox(0F, 0F, 0F, 15, 5, 13);
		Wool4.setRotationPoint(-7F, 16F, -6F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Wool1.render(f5);
		Wool2.render(f5);
		Wool3.render(f5);
		Wool4.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
