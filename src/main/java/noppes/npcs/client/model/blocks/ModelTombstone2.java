package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTombstone2 extends ModelBase {
	ModelRenderer Top;
	ModelRenderer mid;

	public ModelTombstone2() {
		Top = new ModelRenderer(this, 0, 0);
		Top.addBox(0F, 0F, 0F, 10, 1, 4);
		Top.setRotationPoint(-5F, 9F, -2F);

		mid = new ModelRenderer(this, 0, 0);
		mid.addBox(0F, 0F, 0F, 12, 14, 4);
		mid.setRotationPoint(-6F, 10F, -2F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Top.render(f5);
		mid.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
