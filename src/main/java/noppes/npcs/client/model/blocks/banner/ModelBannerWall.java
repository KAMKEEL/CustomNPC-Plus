package noppes.npcs.client.model.blocks.banner;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBannerWall extends ModelBase {
    private final ModelRenderer bb_main;
    private final ModelRenderer cube_r1;

	public ModelBannerWall() {
        textureWidth = 64;
        textureHeight = 64;

        bb_main = new ModelRenderer(this);
        bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
        bb_main.cubeList.add(new ModelBox(bb_main, 0, 0, -8.0F, -32.0F, 5.0F, 16, 3, 3, 0.0F));
        bb_main.cubeList.add(new ModelBox(bb_main, 0, 8, -6.0F, -36.0F, 6.5F, 4, 4, 0, 0.0F));

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(4.0F, -34.0F, 6.5F);
        bb_main.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 3.1416F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 8, -2.0F, -2.0F, 0.0F, 4, 4, 0, 0.0F));}

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
