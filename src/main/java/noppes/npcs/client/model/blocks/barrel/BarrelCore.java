package noppes.npcs.client.model.blocks.barrel;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class BarrelCore extends ModelBase {
	private final ModelRenderer bb_main;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer cube_r3;
	private final ModelRenderer cube_r4;
	private final ModelRenderer cube_r5;

	public BarrelCore() {
        textureWidth = 64;
        textureHeight = 32;

        bb_main = new ModelRenderer(this);
        bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bb_main.addChild(cube_r1);
        setRotationAngle(cube_r1, 1.5708F, 0.0F, -1.5708F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 6, -6, 0.0F, -5.0F, -5.0F, 0, 10, 10, 0.0F));

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(0.0F, -14.5F, 0.0F);
        bb_main.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 0.0F, 1.5708F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 2, 2.5F, -5.5F, 5.5F, 8, 11, 0, 0.0F));
        cube_r2.cubeList.add(new ModelBox(cube_r2, 8, 2, 2.5F, -5.5F, -5.5F, 8, 11, 0, 0.0F));
        cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 3, -1.5F, -5.0F, -5.0F, 16, 10, 0, 0.0F));

        cube_r3 = new ModelRenderer(this);
        cube_r3.setRotationPoint(0.0F, -14.5F, 0.0F);
        bb_main.addChild(cube_r3);
        setRotationAngle(cube_r3, 1.5708F, 0.0F, 1.5708F);
        cube_r3.cubeList.add(new ModelBox(cube_r3, 8, 2, 2.5F, -5.5F, -5.5F, 8, 11, 0, 0.0F));
        cube_r3.cubeList.add(new ModelBox(cube_r3, 0, 2, 2.5F, -5.5F, 5.5F, 8, 11, 0, 0.0F));
        cube_r3.cubeList.add(new ModelBox(cube_r3, 6, -5, -1.5F, -5.0F, -5.0F, 0, 10, 10, 0.0F));
        cube_r3.cubeList.add(new ModelBox(cube_r3, 0, 5, -1.5F, -5.0F, -5.0F, 16, 10, 0, 0.0F));

        cube_r4 = new ModelRenderer(this);
        cube_r4.setRotationPoint(0.0F, -14.5F, 0.0F);
        bb_main.addChild(cube_r4);
        setRotationAngle(cube_r4, -1.5708F, 0.0F, 1.5708F);
        cube_r4.cubeList.add(new ModelBox(cube_r4, 0, 4, -1.5F, -5.0F, -5.0F, 16, 10, 0, 0.0F));

        cube_r5 = new ModelRenderer(this);
        cube_r5.setRotationPoint(0.0F, -8.0F, 5.0F);
        bb_main.addChild(cube_r5);
        setRotationAngle(cube_r5, 3.1416F, 0.0F, 1.5708F);
        cube_r5.cubeList.add(new ModelBox(cube_r5, 0, 3, -8.0F, -5.0F, 0.0F, 16, 10, 0, 0.0F));
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
