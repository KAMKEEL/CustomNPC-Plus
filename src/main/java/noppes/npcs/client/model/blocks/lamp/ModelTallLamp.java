package noppes.npcs.client.model.blocks.lamp;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTallLamp extends ModelBase {
    public final ModelRenderer Shade;
    private final ModelRenderer cube_r1;
    private final ModelRenderer cube_r2;
    public final ModelRenderer Lamp;
    private final ModelRenderer cube_r3;
    private final ModelRenderer cube_r4;
    private final ModelRenderer cube_r5;
    public final ModelRenderer Light;

    public ModelTallLamp() {
        textureWidth = 64;
        textureHeight = 64;

        Shade = new ModelRenderer(this);
        Shade.setRotationPoint(0.0F, -6.0F, 7.0F);
        Shade.cubeList.add(new ModelBox(Shade, 0, 12, -6.0F, -2.0F, -13.0F, 12, 12, 0, 0.0F));
        Shade.cubeList.add(new ModelBox(Shade, -12, 0, -6.0F, -2.0F, -13.0F, 12, 0, 12, 0.0F));
        Shade.cubeList.add(new ModelBox(Shade, 0, 12, -6.0F, -2.0F, -1.0F, 12, 12, 0, 0.0F));

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(6.0F, 4.0F, -7.0F);
        Shade.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 1.5708F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 12, -6.0F, -6.0F, 0.0F, 12, 12, 0, 0.0F));

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(-6.0F, 4.0F, -7.0F);
        Shade.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 1.5708F, 0.0F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 0, 12, -6.0F, -6.0F, 0.0F, 12, 12, 0, 0.0F));

        Lamp = new ModelRenderer(this);
        Lamp.setRotationPoint(0.0F, 24.0F, 0.0F);
        Lamp.cubeList.add(new ModelBox(Lamp, 40, 22, -3.0F, -2.0F, -3.0F, 6, 2, 6, 0.0F));
        Lamp.cubeList.add(new ModelBox(Lamp, 32, 5, -1.0F, -25.0F, -1.0F, 2, 23, 2, 0.0F));
        Lamp.cubeList.add(new ModelBox(Lamp, 32, 0, -1.0F, -32.0F, -1.0F, 2, 3, 2, 0.0F));
        Lamp.cubeList.add(new ModelBox(Lamp, 22, 0, -4.0F, -32.0F, -1.0F, 3, 0, 2, 0.0F));

        cube_r3 = new ModelRenderer(this);
        cube_r3.setRotationPoint(0.0F, -32.0F, -2.5F);
        Lamp.addChild(cube_r3);
        setRotationAngle(cube_r3, 0.0F, -1.5708F, 0.0F);
        cube_r3.cubeList.add(new ModelBox(cube_r3, 22, 0, -1.5F, 0.0F, -1.0F, 3, 0, 2, 0.0F));

        cube_r4 = new ModelRenderer(this);
        cube_r4.setRotationPoint(0.0F, -32.0F, 2.5F);
        Lamp.addChild(cube_r4);
        setRotationAngle(cube_r4, 0.0F, 1.5708F, 0.0F);
        cube_r4.cubeList.add(new ModelBox(cube_r4, 22, 0, -1.5F, 0.0F, -1.0F, 3, 0, 2, 0.0F));

        cube_r5 = new ModelRenderer(this);
        cube_r5.setRotationPoint(2.5F, -32.0F, 0.0F);
        Lamp.addChild(cube_r5);
        setRotationAngle(cube_r5, -3.1416F, 0.0F, 0.0F);
        cube_r5.cubeList.add(new ModelBox(cube_r5, 22, 0, -1.5F, 0.0F, -1.0F, 3, 0, 2, 0.0F));

        Light = new ModelRenderer(this);
        Light.setRotationPoint(0.0F, 24.0F, 0.0F);
        Light.cubeList.add(new ModelBox(Light, 40, 14, -2.0F, -29.0F, -2.0F, 4, 4, 4, 0.0F));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
