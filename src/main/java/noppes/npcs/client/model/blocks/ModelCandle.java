package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCandle extends ModelBase {
	// fields
	ModelRenderer Base;
	ModelRenderer Bar1;
	ModelRenderer Bar2;
	ModelRenderer Bar3;
	ModelRenderer Bar4;
	ModelRenderer Wax;

	public ModelCandle() {
		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 4, 1, 4);
		Base.setRotationPoint(-2F, 23F, -2F);
		
		Bar1 = new ModelRenderer(this, 0, 0);
		Bar1.addBox(0F, 0F, 0F, 1, 1, 6);
		Bar1.setRotationPoint(-3F, 22F, -3F);
		
		Bar2 = new ModelRenderer(this, 0, 0);
		Bar2.addBox(0F, 0F, 0F, 1, 1, 6);
		Bar2.setRotationPoint(2F, 22F, -3F);
		
		Bar3 = new ModelRenderer(this, 0, 0);
		Bar3.addBox(0F, 0F, 0F, 4, 1, 1);
		Bar3.setRotationPoint(-2F, 22F, -3F);
		
		Bar4 = new ModelRenderer(this, 0, 0);
		Bar4.addBox(0F, 0F, 0F, 4, 1, 1);
		Bar4.setRotationPoint(-2F, 22F, 2F);
		
		Wax = new ModelRenderer(this, 16, 0);
		Wax.addBox(0F, 0F, 0F, 2, 4, 2);
		Wax.setRotationPoint(-1F, 19F, -1F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Base.render(f5);
		Bar1.render(f5);
		Bar2.render(f5);
		Bar3.render(f5);
		Bar4.render(f5);
		Wax.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
