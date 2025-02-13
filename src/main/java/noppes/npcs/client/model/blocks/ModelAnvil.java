package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelAnvil extends ModelBase {
    public final ModelRenderer Anvil;
    public final ModelRenderer Desk;
    public final ModelRenderer Bucket;
    private final ModelRenderer cube_r1;
    private final ModelRenderer cube_r2;
    public final ModelRenderer Lava;
    public final ModelRenderer Hammer;
    private final ModelRenderer Hammer_r1;
    public final ModelRenderer Mold;
    private final ModelRenderer cube_r3;
    private final ModelRenderer cube_r4;
    private final ModelRenderer cube_r5;
    private final ModelRenderer cube_r6;
    private final ModelRenderer cube_r7;
    private final ModelRenderer cube_r8;

    public ModelAnvil() {
        textureWidth = 64;
        textureHeight = 64;

        Anvil = new ModelRenderer(this);
        Anvil.setRotationPoint(0.0F, 24.0F, 0.0F);
        Anvil.cubeList.add(new ModelBox(Anvil, 0, 16, -6.0F, -4.0F, -8.0F, 12, 4, 12, 0.0F));
        Anvil.cubeList.add(new ModelBox(Anvil, 0, 32, -5.0F, -5.0F, -6.0F, 10, 1, 8, 0.0F));
        Anvil.cubeList.add(new ModelBox(Anvil, 0, 41, -4.0F, -10.0F, -4.0F, 8, 5, 4, 0.0F));
        Anvil.cubeList.add(new ModelBox(Anvil, 0, 0, -8.0F, -16.0F, -7.0F, 16, 6, 10, 0.0F));

        Desk = new ModelRenderer(this);
        Desk.setRotationPoint(0.0F, 24.0F, 0.0F);
        Desk.cubeList.add(new ModelBox(Desk, 52, 0, 5.0F, -14.0F, 5.0F, 3, 14, 3, 0.0F));
        Desk.cubeList.add(new ModelBox(Desk, 52, 0, -8.0F, -14.0F, 5.0F, 3, 14, 3, 0.0F));
        Desk.cubeList.add(new ModelBox(Desk, 0, 50, -8.0F, -16.0F, 3.0F, 16, 2, 5, 0.0F));

        Bucket = new ModelRenderer(this);
        Bucket.setRotationPoint(3.0F, 5.5F, 4.0F);
        setRotationAngle(Bucket, 0.0F, 0.3927F, 0.0F);
        Bucket.cubeList.add(new ModelBox(Bucket, 48, 17, -2.0F, 1.5F, -2.0F, 4, 1, 4, 0.0F));
        Bucket.cubeList.add(new ModelBox(Bucket, 54, 22, 1.5F, -2.5F, -2.0F, 1, 4, 4, 0.0F));
        Bucket.cubeList.add(new ModelBox(Bucket, 54, 22, -2.5F, -2.5F, -2.0F, 1, 4, 4, 0.0F));

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(0.0F, -0.5F, -2.0F);
        Bucket.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 1.5708F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 54, 22, -0.5F, -2.0F, -2.0F, 1, 4, 4, 0.0F));

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(0.0F, -0.5F, 2.0F);
        Bucket.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, -1.5708F, 0.0F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 54, 22, -0.5F, -2.0F, -2.0F, 1, 4, 4, 0.0F));

        Lava = new ModelRenderer(this);
        Lava.setRotationPoint(3.0F, 5.5F, 4.0F);
        setRotationAngle(Lava, 0.0F, 0.3927F, 0.0F);
        Lava.cubeList.add(new ModelBox(Lava, 48, 30, -2.0F, -1.5F, -2.0F, 4, 3, 4, 0.0F));

        Hammer = new ModelRenderer(this);
        Hammer.setRotationPoint(4.9909F, 6.5642F, -4.0337F);
        setRotationAngle(Hammer, 0.1327F, 0.0189F, 0.1303F);


        Hammer_r1 = new ModelRenderer(this);
        Hammer_r1.setRotationPoint(-0.2409F, 0.5358F, -0.4663F);
        Hammer.addChild(Hammer_r1);
        setRotationAngle(Hammer_r1, 2.9697F, -0.7703F, -2.8972F);
        Hammer_r1.cubeList.add(new ModelBox(Hammer_r1, 56, 45, -2.8377F, -0.7659F, -1.0002F, 3, 1, 1, 0.0F));
        Hammer_r1.cubeList.add(new ModelBox(Hammer_r1, 48, 37, 0.1623F, -1.7659F, -3.0002F, 3, 3, 5, 0.0F));

        Mold = new ModelRenderer(this);
        Mold.setRotationPoint(-3.2778F, 7.5556F, 1.5F);
        setRotationAngle(Mold, 0.0F, 0.7854F, 0.0F);
        Mold.cubeList.add(new ModelBox(Mold, -5, 59, -3.7222F, 0.3444F, -2.5F, 7, 0, 5, 0.0F));
        Mold.cubeList.add(new ModelBox(Mold, 14, 62, -3.7222F, -0.5556F, -2.5F, 7, 1, 1, 0.0F));
        Mold.cubeList.add(new ModelBox(Mold, 14, 62, -3.7222F, -0.5556F, 1.5F, 7, 1, 1, 0.0F));

        cube_r3 = new ModelRenderer(this);
        cube_r3.setRotationPoint(0.7778F, -0.0556F, -1.0F);
        Mold.addChild(cube_r3);
        setRotationAngle(cube_r3, 0.0F, 0.0F, 0.0F);
        cube_r3.cubeList.add(new ModelBox(cube_r3, 31, 62, 0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F));

        cube_r4 = new ModelRenderer(this);
        cube_r4.setRotationPoint(-1.2222F, -0.0556F, -1.0F);
        Mold.addChild(cube_r4);
        setRotationAngle(cube_r4, 0.0F, 0.0F, 0.0F);
        cube_r4.cubeList.add(new ModelBox(cube_r4, 25, 60, -1.5F, -0.5F, -0.5F, 3, 1, 1, 0.0F));

        cube_r5 = new ModelRenderer(this);
        cube_r5.setRotationPoint(0.7778F, -0.0556F, 1.0F);
        Mold.addChild(cube_r5);
        setRotationAngle(cube_r5, 0.0F, 0.0F, 0.0F);
        cube_r5.cubeList.add(new ModelBox(cube_r5, 31, 62, 0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F));

        cube_r6 = new ModelRenderer(this);
        cube_r6.setRotationPoint(-1.2222F, -0.0556F, 1.0F);
        Mold.addChild(cube_r6);
        setRotationAngle(cube_r6, 0.0F, 0.0F, 0.0F);
        cube_r6.cubeList.add(new ModelBox(cube_r6, 25, 60, -1.5F, -0.5F, -0.5F, 3, 1, 1, 0.0F));

        cube_r7 = new ModelRenderer(this);
        cube_r7.setRotationPoint(2.7778F, -0.0556F, 0.0F);
        Mold.addChild(cube_r7);
        setRotationAngle(cube_r7, 0.0F, -1.5708F, 0.0F);
        cube_r7.cubeList.add(new ModelBox(cube_r7, 15, 60, -1.5F, -0.5F, -0.5F, 3, 1, 1, 0.0F));

        cube_r8 = new ModelRenderer(this);
        cube_r8.setRotationPoint(-3.2222F, -0.0556F, 0.0F);
        Mold.addChild(cube_r8);
        setRotationAngle(cube_r8, 0.0F, -1.5708F, 0.0F);
        cube_r8.cubeList.add(new ModelBox(cube_r8, 15, 60, -1.5F, -0.5F, -0.5F, 3, 1, 1, 0.0F));
    }

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		Anvil.render(f5);
		Desk.render(f5);
		Bucket.render(f5);
		Hammer.render(f5);
		Mold.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
