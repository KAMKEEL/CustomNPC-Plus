package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelLongCrate extends ModelBase {
	// fields
	ModelRenderer Vertical1;
	ModelRenderer Horizontal1;
	ModelRenderer Cratebody;
	ModelRenderer Horizontal2;
	ModelRenderer Vertical3;
	ModelRenderer Vertical4;
	ModelRenderer Vertical2;

	public ModelLongCrate() {

		Vertical1 = new ModelRenderer(this, 80, 0);
		Vertical1.addBox(0F, 0F, 0F, 4, 13, 1);
		Vertical1.setRotationPoint(-12F, 11F, 8F);
		
		Horizontal1 = new ModelRenderer(this, 0, 0);
		Horizontal1.mirror = true;
		Horizontal1.addBox(0F, 0F, 0F, 4, 1, 18);
		Horizontal1.setRotationPoint(8F, 10F, -9F);
		
		Cratebody = new ModelRenderer(this, 8, 0);
		Cratebody.addBox(-16F, 0F, -8F, 32, 13, 16);
		Cratebody.setRotationPoint(0F, 11F, 0F);
		
		Horizontal2 = new ModelRenderer(this, 0, 0);
		Horizontal2.addBox(0F, 0F, 0F, 4, 1, 18);
		Horizontal2.setRotationPoint(-12F, 10F, -9F);
		
		Vertical3 = new ModelRenderer(this, 80, 0);
		Vertical3.addBox(0F, 0F, 0F, 4, 13, 1);
		Vertical3.setRotationPoint(-12F, 11F, -9F);
		
		Vertical4 = new ModelRenderer(this, 80, 0);
		Vertical4.mirror = true;
		Vertical4.addBox(0F, 0F, 0F, 4, 13, 1);
		Vertical4.setRotationPoint(8F, 11F, -9F);
		
		Vertical2 = new ModelRenderer(this, 80, 0);
		Vertical2.mirror = true;
		Vertical2.addBox(0F, 0F, 0F, 4, 13, 1);
		Vertical2.setRotationPoint(8F, 11F, 8F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Vertical1.render(f5);
		Horizontal1.render(f5);
		Cratebody.render(f5);
		Horizontal2.render(f5);
		Vertical3.render(f5);
		Vertical4.render(f5);
		Vertical2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
