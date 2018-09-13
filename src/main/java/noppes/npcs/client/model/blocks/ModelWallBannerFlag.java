package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelWallBannerFlag extends ModelBase {
	ModelRenderer Flag;

	public ModelWallBannerFlag() {
		textureWidth = 32;
		textureHeight = 32;
		Flag = new ModelRenderer(this, 0, 0);
		Flag.addBox(0F, 0F, 0F, 15, 27, 0);
		Flag.setRotationPoint(-7.5F, -7F, 4.5F);
		Flag.setTextureSize(32, 32);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3,
			float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		Flag.render(f5);
	}

}
