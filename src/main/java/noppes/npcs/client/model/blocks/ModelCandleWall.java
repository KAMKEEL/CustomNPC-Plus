package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCandleWall extends ModelBase {
	ModelRenderer Base;
	ModelRenderer Bar1;
	ModelRenderer Bar2;
	ModelRenderer Bar3;
	ModelRenderer Bar4;
	ModelRenderer Wax;
	ModelRenderer Wall2;
	ModelRenderer Wall1;
	ModelRenderer Bar5;
	ModelRenderer Bar6;

	public ModelCandleWall() {
		Base = new ModelRenderer(this, 0, 0);
		Base.addBox(0F, 0F, 0F, 4, 1, 4);
		Base.setRotationPoint(-2F, 13F, -4F);
		
		Bar1 = new ModelRenderer(this, 0, 0);
		Bar1.addBox(0F, 0F, 0F, 1, 1, 6);
		Bar1.setRotationPoint(-3F, 12F, -5F);
		
		Bar2 = new ModelRenderer(this, 0, 0);
		Bar2.addBox(0F, 0F, 0F, 1, 1, 6);
		Bar2.setRotationPoint(2F, 12F, -5F);
		
		Bar3 = new ModelRenderer(this, 0, 0);
		Bar3.addBox(0F, 0F, 0F, 4, 1, 1);
		Bar3.setRotationPoint(-2F, 12F, -5F);
		
		Bar4 = new ModelRenderer(this, 0, 0);
		Bar4.addBox(0F, 0F, 0F, 4, 1, 1);
		Bar4.setRotationPoint(-2F, 12F, 0F);
		
		Wax = new ModelRenderer(this, 16, 0);
		Wax.addBox(0F, 0F, 0F, 2, 4, 2);
		Wax.setRotationPoint(-1F, 9F, -3F);
		
		Wall2 = new ModelRenderer(this, 0, 0);
		Wall2.addBox(0F, 0F, 0F, 3, 3, 1);
		Wall2.setRotationPoint(0F, 13.7F, -7.5F);
		setRotation(Wall2, 0F, 0F, 0.7853982F);
		
		Wall1 = new ModelRenderer(this, 0, 0);
		Wall1.addBox(0F, 0F, 0F, 4, 4, 1);
		Wall1.setRotationPoint(0F, 13F, -8F);
		setRotation(Wall1, 0F, 0F, 0.7853982F);
		
		Bar5 = new ModelRenderer(this, 0, 0);
		Bar5.addBox(0F, 0F, 0F, 1, 2, 1);
		Bar5.setRotationPoint(-0.5F, 13.5F, -2.5F);
		
		Bar6 = new ModelRenderer(this, 0, 0);
		Bar6.addBox(0F, 0F, 0F, 1, 1, 5);
		Bar6.setRotationPoint(-0.5F, 15.5F, -6.5F);
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
		Wall2.render(f5);
		Wall1.render(f5);
		Bar5.render(f5);
		Bar6.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
