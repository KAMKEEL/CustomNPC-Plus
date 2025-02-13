package noppes.npcs.client.model.blocks.lantern;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class LanternCeiling extends ModelBase {
    public final ModelRenderer Light;
    public final ModelRenderer Lantern;
    public final ModelRenderer Chain;
    private final ModelRenderer cube_r1;
    private final ModelRenderer cube_r2;

	public LanternCeiling() {
		textureWidth = 64;
		textureHeight = 32;

		Light = new ModelRenderer(this);
		Light.setRotationPoint(0.0F, 24.0F, 0.0F);
		Light.cubeList.add(new ModelBox(Light, 28, 8, -3.0F, -9.0F, -3.0F, 6, 7, 6, 0.0F));

		Lantern = new ModelRenderer(this);
		Lantern.setRotationPoint(0.0F, 24.0F, 0.0F);
		Lantern.cubeList.add(new ModelBox(Lantern, 28, 0, -3.0F, -2.0F, -3.0F, 6, 2, 6, 0.0F));
		Lantern.cubeList.add(new ModelBox(Lantern, 28, 21, -2.0F, -11.0F, -2.0F, 4, 2, 4, 0.0F));
        Lantern.cubeList.add(new ModelBox(Lantern, 46, 21, -5.0F, -11.0F, 0.0F, 3, 11, 0, 0.0F));
        Lantern.mirror = true;
        Lantern.cubeList.add(new ModelBox(Lantern, 46, 21, 2.0F, -11.0F, 0.0F, 3, 11, 0, 0.0F));
        Lantern.mirror = false;

		Chain = new ModelRenderer(this);
		Chain.setRotationPoint(0.0F, 24.0F, 0.0F);

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, -18.0F, 0.0F);
		Chain.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.0F, -0.7854F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 0, -1, 0.0F, 2.0F, -1.5F, 0, 5, 3, 0.0F));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(0.0F, -18.0F, 0.0F);
		Chain.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.0F, 0.7854F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 6, -1, 0.0F, 2.0F, -1.5F, 0, 5, 3, 0.0F));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		Light.render(f5);
		Lantern.render(f5);
		Chain.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
