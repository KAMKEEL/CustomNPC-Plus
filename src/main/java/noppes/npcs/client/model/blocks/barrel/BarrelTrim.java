package noppes.npcs.client.model.blocks.barrel;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.7 - 1.12
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class BarrelTrim extends ModelBase {
	private final ModelRenderer bb_main;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;

	public BarrelTrim() {
		textureWidth = 32;
		textureHeight = 32;

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 7, 0.0F, -16.5F, -4.0F, 2, 1, 2, 0.0F));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, -8.0F, 0.0F);
		bb_main.addChild(cube_r1);
		setRotationAngle(cube_r1, -1.5708F, 0.0F, 1.5708F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 0, -6.0F, -6.0F, -6.0F, 2, 12, 12, 0.0F));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(0.0F, -3.0F, 0.0F);
		bb_main.addChild(cube_r2);
		setRotationAngle(cube_r2, 1.5708F, 0.0F, 1.5708F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 0, -1.0F, -6.0F, -6.0F, 2, 12, 12, 0.0F));
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
