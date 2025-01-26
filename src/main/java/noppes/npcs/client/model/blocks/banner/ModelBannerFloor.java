package noppes.npcs.client.model.blocks.banner;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBannerFloor extends ModelBase {
	private final ModelRenderer FloorBanner;
	private final ModelRenderer Leg4_r1;
	private final ModelRenderer Leg3_r1;
	private final ModelRenderer Leg2_r1;
	private final ModelRenderer Leg1_r1;
	private final ModelRenderer Point1_r1;

	public ModelBannerFloor() {
		textureWidth = 64;
		textureHeight = 64;

		FloorBanner = new ModelRenderer(this);
		FloorBanner.setRotationPoint(0.0F, 24.0F, 0.0F);
		FloorBanner.cubeList.add(new ModelBox(FloorBanner, 0, 0, -8.0F, -32.0F, -1.0F, 16, 3, 3, 0.0F));
		FloorBanner.cubeList.add(new ModelBox(FloorBanner, 0, 8, -6.0F, -36.0F, 0.5F, 4, 4, 0, 0.0F));
		FloorBanner.cubeList.add(new ModelBox(FloorBanner, 0, 13, -1.0F, -29.0F, -0.5F, 2, 26, 2, 0.0F));
		FloorBanner.cubeList.add(new ModelBox(FloorBanner, 18, 28, -6.0F, -2.0F, -5.0F, 12, 2, 11, 0.0F));
		FloorBanner.cubeList.add(new ModelBox(FloorBanner, 26, 19, -4.0F, -4.0F, -3.0F, 8, 2, 7, 0.0F));

		Leg4_r1 = new ModelRenderer(this);
		Leg4_r1.setRotationPoint(-3.5F, -2.1F, 0.5F);
		FloorBanner.addChild(Leg4_r1);
		setRotationAngle(Leg4_r1, 0.0F, 0.0F, 1.1781F);
		Leg4_r1.cubeList.add(new ModelBox(Leg4_r1, 10, 29, -1.0F, -3.0F, -1.0F, 2, 6, 2, 0.0F));

		Leg3_r1 = new ModelRenderer(this);
		Leg3_r1.setRotationPoint(0.0F, -2.1F, 4.0F);
		FloorBanner.addChild(Leg3_r1);
		setRotationAngle(Leg3_r1, 1.1781F, 0.0F, 0.0F);
		Leg3_r1.cubeList.add(new ModelBox(Leg3_r1, 10, 29, -1.0F, -3.0F, -1.0F, 2, 6, 2, 0.0F));

		Leg2_r1 = new ModelRenderer(this);
		Leg2_r1.setRotationPoint(0.0F, -2.1F, -3.0F);
		FloorBanner.addChild(Leg2_r1);
		setRotationAngle(Leg2_r1, -1.1781F, 0.0F, 0.0F);
		Leg2_r1.cubeList.add(new ModelBox(Leg2_r1, 10, 29, -1.0F, -3.0F, -1.0F, 2, 6, 2, 0.0F));

		Leg1_r1 = new ModelRenderer(this);
		Leg1_r1.setRotationPoint(3.5F, -2.1F, 0.5F);
		FloorBanner.addChild(Leg1_r1);
		setRotationAngle(Leg1_r1, 0.0F, 0.0F, -1.1781F);
		Leg1_r1.cubeList.add(new ModelBox(Leg1_r1, 10, 29, -1.0F, -3.0F, -1.0F, 2, 6, 2, 0.0F));

		Point1_r1 = new ModelRenderer(this);
		Point1_r1.setRotationPoint(4.0F, -34.0F, 0.5F);
		FloorBanner.addChild(Point1_r1);
		setRotationAngle(Point1_r1, 0.0F, 3.1416F, 0.0F);
		Point1_r1.cubeList.add(new ModelBox(Point1_r1, 0, 8, -2.0F, -2.0F, 0.0F, 4, 4, 0, 0.0F));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		FloorBanner.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
