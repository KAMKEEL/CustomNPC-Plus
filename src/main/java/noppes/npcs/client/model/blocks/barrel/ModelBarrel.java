package noppes.npcs.client.model.blocks.barrel;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

public class ModelBarrel extends ModelBase {
	private final ModelRenderer Wall;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;
	private final ModelRenderer cube_r3;
	private final ModelRenderer Trim;
	private final ModelRenderer Base;

	public ModelBarrel() {
		textureWidth = 64;
		textureHeight = 32;

		Wall = new ModelRenderer(this);
		Wall.setRotationPoint(0.0F, 24.0F, 0.0F);
		Wall.cubeList.add(new ModelBox(Wall, 0, 0, 5.0F, -16.0F, 5.0F, 1, 16, 1, 0.0F));
		Wall.cubeList.add(new ModelBox(Wall, 0, 0, 5.0F, -16.0F, -6.0F, 1, 16, 1, 0.0F));
		Wall.cubeList.add(new ModelBox(Wall, 0, 0, -6.0F, -16.0F, -6.0F, 1, 16, 1, 0.0F));
		Wall.cubeList.add(new ModelBox(Wall, 0, 0, -6.0F, -16.0F, 5.0F, 1, 16, 1, 0.0F));

		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(-6.5F, -8.0F, 0.0F);
		Wall.addChild(cube_r1);
		setRotationAngle(cube_r1, 1.5708F, 0.0F, 0.0F);
		cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 0, -0.5F, -6.0F, -8.0F, 1, 12, 16, 0.0F));

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(0.0F, -8.0F, 6.5F);
		Wall.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.0F, 0.0F, 1.5708F);
		cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 14, -8.0F, -6.0F, -0.5F, 16, 12, 1, 0.0F));
		cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 14, -8.0F, -6.0F, -13.5F, 16, 12, 1, 0.0F));

		cube_r3 = new ModelRenderer(this);
		cube_r3.setRotationPoint(6.5F, -8.0F, 0.0F);
		Wall.addChild(cube_r3);
		setRotationAngle(cube_r3, -1.5708F, 0.0F, 0.0F);
		cube_r3.cubeList.add(new ModelBox(cube_r3, 0, 0, -0.5F, -6.0F, -8.0F, 1, 12, 16, 0.0F));

		Trim = new ModelRenderer(this);
		Trim.setRotationPoint(0.0F, 24.0F, 0.0F);
		Trim.cubeList.add(new ModelBox(Trim, 4, 28, -6.5F, -5.0F, 5.5F, 13, 2, 2, 0.0F));
		Trim.cubeList.add(new ModelBox(Trim, 34, 17, 5.5F, -5.0F, -6.5F, 2, 2, 13, 0.0F));
		Trim.cubeList.add(new ModelBox(Trim, 4, 28, -6.5F, -5.0F, -7.5F, 13, 2, 2, 0.0F));
		Trim.cubeList.add(new ModelBox(Trim, 34, 17, -7.5F, -5.0F, -6.5F, 2, 2, 13, 0.0F));
		Trim.cubeList.add(new ModelBox(Trim, 4, 28, -6.5F, -13.0F, -7.5F, 13, 2, 2, 0.0F));
		Trim.cubeList.add(new ModelBox(Trim, 4, 28, -6.5F, -13.0F, 5.5F, 13, 2, 2, 0.0F));
		Trim.cubeList.add(new ModelBox(Trim, 34, 17, -7.5F, -13.0F, -6.5F, 2, 2, 13, 0.0F));
		Trim.cubeList.add(new ModelBox(Trim, 34, 17, 5.5F, -13.0F, -6.5F, 2, 2, 13, 0.0F));

		Base = new ModelRenderer(this);
		Base.setRotationPoint(0.0F, 24.0F, 0.0F);
		Base.cubeList.add(new ModelBox(Base, 16, 2, -6.0F, -15.0F, -6.0F, 12, 1, 12, 0.0F));
		Base.cubeList.add(new ModelBox(Base, 16, 2, -6.0F, -2.0F, -6.0F, 12, 1, 12, 0.0F));
	}

    public void renderBase(float f5){
        Base.render(f5);
    }

    public void renderTrim(float f5){
        Trim.render(f5);
    }

    public void renderWall(float f5){
        Wall.render(f5);
    }

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
