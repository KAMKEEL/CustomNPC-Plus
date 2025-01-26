package noppes.npcs.client.model.blocks.campfire;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCampfireLog extends ModelBase {
	private final ModelRenderer bb_main;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer cube_r3;

	public ModelCampfireLog() {
		textureWidth = 16;
		textureHeight = 16;

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);


		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(1.25F, -6.3F, 1.25F);
		bb_main.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.5449F, -1.0854F, -0.744F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 0, -1.0F, -4.0F, -1.0F, 3, 10, 3, 0.0F));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(-1.5F, -6.0F, -0.25F);
		bb_main.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.0F, 0.0F, 0.3927F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 0, -2.0F, -4.0F, -1.0F, 3, 10, 3, 0.0F));

		cube_r3 = new ModelRenderer(this);
		cube_r3.setRotationPoint(1.5F, -6.0F, -2.25F);
		bb_main.addChild(cube_r3);
		setRotationAngle(cube_r3, -0.3295F, 0.7268F, -0.4754F);
		cube_r3.cubeList.add(new ModelBox(cube_r3, 0, 0, -2.0F, -4.5F, -1.0F, 3, 10, 3, 0.0F));
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
