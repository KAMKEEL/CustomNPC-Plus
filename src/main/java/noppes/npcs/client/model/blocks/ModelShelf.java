package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelShelf extends ModelBase {
	public ModelRenderer SupportLeft2;
	ModelRenderer Top;
	public ModelRenderer SupportLeft1;
	public ModelRenderer SupportRight1;
	public ModelRenderer SupportRight2;

	public ModelShelf() {
		SupportLeft2 = new ModelRenderer(this, 0, 0);
		SupportLeft2.mirror = true;
		SupportLeft2.addBox(0F, 0F, 0F, 2, 10, 2);
		SupportLeft2.setRotationPoint(-7.498F, 9.5F, -0.5F);
		setRotation(SupportLeft2, 0.7853982F, 0F, 0F);
		
		Top = new ModelRenderer(this, 5, 0);
		Top.addBox(0F, 0F, 0F, 16, 2, 11);
		Top.setRotationPoint(-8F, 8F, -3F);
		
		SupportLeft1 = new ModelRenderer(this, 0, 0);
		SupportLeft1.mirror = true;
		SupportLeft1.addBox(0F, 0F, 0F, 2, 7, 2);
		SupportLeft1.setRotationPoint(-7.5F, 10F, 6F);
		
		SupportRight1 = new ModelRenderer(this, 0, 0);
		SupportRight1.addBox(0F, 0F, 0F, 2, 7, 2);
		SupportRight1.setRotationPoint(5.5F, 10F, 6F);
		
		SupportRight2 = new ModelRenderer(this, 0, 0);
		SupportRight2.addBox(0F, 0F, 0F, 2, 10, 2);
		SupportRight2.setRotationPoint(5.498F, 9.5F, -0.5F);
		setRotation(SupportRight2, 0.7853982F, 0F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		SupportLeft2.render(f5);
		Top.render(f5);
		SupportLeft1.render(f5);
		SupportRight1.render(f5);
		SupportRight2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
