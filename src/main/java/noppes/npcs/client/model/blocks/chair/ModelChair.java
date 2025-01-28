package noppes.npcs.client.model.blocks.chair;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelChair extends ModelBase {
	private final ModelRenderer bb_main;

	public ModelChair(){
		textureWidth = 64;
		textureHeight = 32;

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 17, 4.0F, -6.0F, 4.0F, 2, 6, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 17, -6.0F, -6.0F, 4.0F, 2, 6, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 17, -6.0F, -6.0F, -6.0F, 2, 6, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 17, 4.0F, -6.0F, -6.0F, 2, 6, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 0, -6.0F, -8.0F, -6.0F, 12, 2, 12, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 8, 17, 4.0F, -20.0F, 4.0F, 2, 12, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 8, 17, -6.0F, -20.0F, 4.0F, 2, 12, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 16, 18, -4.0F, -19.0F, 5.0F, 8, 11, 0, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 16, 8, 5.0F, -4.0F, -4.0F, 0, 2, 8, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 16, 16, -4.0F, -4.0F, -5.0F, 8, 2, 0, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 16, 8, -5.0F, -4.0F, -4.0F, 0, 2, 8, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 16, 16, -4.0F, -4.0F, 5.0F, 8, 2, 0, 0.0F));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		bb_main.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
