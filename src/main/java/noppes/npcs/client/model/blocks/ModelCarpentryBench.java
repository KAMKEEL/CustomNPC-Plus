package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCarpentryBench extends ModelBase {

    private final ModelRenderer blueprint;
    private final ModelRenderer chissle;
	private final ModelRenderer saw;
	private final ModelRenderer bb_main;

	public ModelCarpentryBench() {
		textureWidth = 64;
		textureHeight = 64;

		blueprint = new ModelRenderer(this);
		blueprint.setRotationPoint(-6.4564F, 9.3925F, -0.75F);
		setRotationAngle(blueprint, 0.0F, -0.3054F, 0.0F);
		blueprint.cubeList.add(new ModelBox(blueprint, 47, 27, -0.5436F, 0.5075F, -3.5F, 7, 0, 7, 0.0F));

        ModelRenderer blueprint_r1 = new ModelRenderer(this);
		blueprint_r1.setRotationPoint(-0.5436F, 0.5075F, 0.0F);
		blueprint.addChild(blueprint_r1);
		setRotationAngle(blueprint_r1, 0.0F, 0.0F, 0.7854F);
		blueprint_r1.cubeList.add(new ModelBox(blueprint_r1, 45, 27, -1.0F, 0.0F, -3.5F, 1, 0, 7, 0.0F));

        ModelRenderer blueprint_r2 = new ModelRenderer(this);
		blueprint_r2.setRotationPoint(-1.2507F, -0.1996F, 0.0F);
		blueprint.addChild(blueprint_r2);
		setRotationAngle(blueprint_r2, 0.0F, 0.0F, 1.9635F);
		blueprint_r2.cubeList.add(new ModelBox(blueprint_r2, 43, 27, -1.0F, 0.0F, -3.5F, 1, 0, 7, 0.0F));

		chissle = new ModelRenderer(this);
		chissle.setRotationPoint(1.356F, 18.6543F, -2.5F);
		setRotationAngle(chissle, 0.0F, -0.7854F, 0.0F);
		chissle.cubeList.add(new ModelBox(chissle, 17, 53, -0.356F, -0.6543F, -0.5F, 3, 1, 1, 0.0F));

        ModelRenderer cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(-1.606F, 0.3457F, 0.0F);
		chissle.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.0F, 0.0F, -0.3927F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 18, 52, -0.5F, 0.0F, -0.5F, 2, 0, 1, 0.0F));

		saw = new ModelRenderer(this);
		saw.setRotationPoint(-2.65F, 18.5F, -2.7F);
		setRotationAngle(saw, -3.096F, -0.3051F, 3.1241F);
		saw.cubeList.add(new ModelBox(saw, 16, 46, -1.6F, -0.5F, 0.7F, 1, 1, 1, 0.0F));
		saw.cubeList.add(new ModelBox(saw, 16, 48, 0.4F, -0.5F, 0.7F, 1, 1, 1, 0.0F));
		saw.cubeList.add(new ModelBox(saw, 8, 36, -1.6F, 0.0F, -9.3F, 4, 0, 9, 0.0F));
		saw.cubeList.add(new ModelBox(saw, 20, 46, -1.6F, -0.5F, -0.3F, 3, 1, 1, 0.0F));
		saw.cubeList.add(new ModelBox(saw, 20, 48, -1.6F, -0.5F, 1.7F, 3, 1, 1, 0.0F));

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.cubeList.add(new ModelBox(bb_main, 8, 50, 3.0F, -12.0F, -6.0F, 2, 12, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 50, -7.0F, -12.0F, -6.0F, 2, 12, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 36, -7.0F, -12.0F, 4.0F, 2, 12, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 8, 36, 3.0F, -12.0F, 4.0F, 2, 12, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 0, -9.0F, -14.0F, -7.0F, 16, 2, 14, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 16, -9.0F, -17.0F, 5.0F, 16, 3, 2, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 0, 24, -6.0F, -5.0F, -5.0F, 10, 1, 10, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 46, 17, 8.0F, -14.0F, -4.0F, 2, 3, 7, 0.0F));
		bb_main.cubeList.add(new ModelBox(bb_main, 29, 16, 6.0F, -12.0F, -4.0F, 2, 1, 7, 0.0F));

        ModelRenderer cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(10.5F, -12.5F, -0.5F);
		bb_main.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.7854F, 0.0F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 47, 17, -1.0F, -0.5F, -0.5F, 1, 1, 1, 0.0F));
		cube_r2.cubeList.add(new ModelBox(cube_r2, 41, 17, 0.0F, -0.5F, -1.5F, 1, 1, 3, 0.0F));

        ModelRenderer hammer_r1 = new ModelRenderer(this);
		hammer_r1.setRotationPoint(4.75F, -14.9F, 1.0F);
		bb_main.addChild(hammer_r1);
		setRotationAngle(hammer_r1, 2.9697F, -0.7703F, -2.8972F);
		hammer_r1.cubeList.add(new ModelBox(hammer_r1, 44, 39, -2.75F, -0.5F, -0.5F, 3, 1, 1, 0.0F));
		hammer_r1.cubeList.add(new ModelBox(hammer_r1, 52, 35, 0.25F, -1.0F, -2.0F, 2, 2, 4, 0.0F));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5,entity);
		blueprint.render(f5);
		chissle.render(f5);
		saw.render(f5);
		bb_main.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
