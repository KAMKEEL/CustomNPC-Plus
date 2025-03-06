package noppes.npcs.client.model.blocks.lantern;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class LanternWall extends ModelBase {
    public final ModelRenderer Light;
    public final ModelRenderer Lantern;
    public final ModelRenderer Holder;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;

	public LanternWall() {
		textureWidth = 64;
		textureHeight = 32;

		Light = new ModelRenderer(this);
		Light.setRotationPoint(0.0F, 24.0F, 0.0F);
		Light.cubeList.add(new ModelBox(Light, 28, 8, -3.0F, -12.0F, -1.0F, 6, 7, 6, 0.0F));

		Lantern = new ModelRenderer(this);
		Lantern.setRotationPoint(0.0F, 24.0F, 0.0F);
		Lantern.cubeList.add(new ModelBox(Lantern, 28, 0, -3.0F, -5.0F, -1.0F, 6, 2, 6, 0.0F));
		Lantern.cubeList.add(new ModelBox(Lantern, 28, 21, -2.0F, -14.0F, 0.0F, 4, 2, 4, 0.0F));
		Lantern.cubeList.add(new ModelBox(Lantern, 46, 21, -5.0F, -14.0F, 2.0F, 3, 11, 0, 0.0F));
		Lantern.mirror = true;
        Lantern.cubeList.add(new ModelBox(Lantern, 46, 21, 2.0F, -14.0F, 2.0F, 3, 11, 0, 0.0F));
		Lantern.mirror = false;
        Lantern.cubeList.add(new ModelBox(Lantern, 28, 27, -3.0F, -16.0F, 2.0F, 6, 2, 0, 0.0F));

		Holder = new ModelRenderer(this);
		Holder.setRotationPoint(0.0F, 24.0F, 0.0F);


		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, -14.5F, 4.5F);
		Holder.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.0F, 3.1416F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 12, 0, 0.0F, -1.5F, -2.5F, 0, 3, 5, 0.0F));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(0.0F, -14.0F, 6.5F);
		Holder.addChild(cube_r2);
		setRotationAngle(cube_r2, -1.5708F, 0.0F, 0.0F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 12, 0, -2.0F, -1.5F, -2.0F, 4, 1, 4, 0.0F));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		Light.render(f5);
		Lantern.render(f5);
		Holder.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
