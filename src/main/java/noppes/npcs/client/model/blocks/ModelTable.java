package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTable extends ModelBase {
    public final ModelRenderer Shape3;
    public final ModelRenderer Shape4;
    private final ModelRenderer cube_r1;
    public final ModelRenderer Shape1;
    private final ModelRenderer cube_r2;
    private final ModelRenderer cube_r3;
    public final ModelRenderer Shape5;
    private final ModelRenderer cube_r4;
    public final ModelRenderer Table;

    public ModelTable() {
        textureWidth = 64;
        textureHeight = 32;

        Shape3 = new ModelRenderer(this);
        Shape3.setRotationPoint(-6.0F, 24.0F, 6.0F);
        Shape3.cubeList.add(new ModelBox(Shape3, 8, 19, 6.0F, -13.0F, -12.0F, 5, 5, 0, 0.0F));
        Shape3.cubeList.add(new ModelBox(Shape3, 0, 0, 11.0F, -13.0F, -13.0F, 2, 13, 2, 0.0F));
        Shape3.cubeList.add(new ModelBox(Shape3, 8, 14, 12.0F, -13.0F, -11.0F, 0, 5, 5, 0.0F));

        Shape4 = new ModelRenderer(this);
        Shape4.setRotationPoint(-6.0F, 24.0F, 6.0F);
        Shape4.cubeList.add(new ModelBox(Shape4, 8, 19, 6.0F, -13.0F, 0.0F, 5, 5, 0, 0.0F));
        Shape4.cubeList.add(new ModelBox(Shape4, 0, 0, 11.0F, -13.0F, -1.0F, 2, 13, 2, 0.0F));

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(12.0F, -10.5F, -3.5F);
        Shape4.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 3.1416F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 8, 14, 0.0F, -2.5F, -2.5F, 0, 5, 5, 0.0F));

        Shape1 = new ModelRenderer(this);
        Shape1.setRotationPoint(-6.0F, 24.0F, 6.0F);
        Shape1.cubeList.add(new ModelBox(Shape1, 0, 0, -1.0F, -13.0F, -1.0F, 2, 13, 2, 0.0F));

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(0.0F, -10.5F, -3.5F);
        Shape1.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 3.1416F, 0.0F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 8, 14, 0.0F, -2.5F, -2.5F, 0, 5, 5, 0.0F));

        cube_r3 = new ModelRenderer(this);
        cube_r3.setRotationPoint(3.5F, -10.5F, 0.0F);
        Shape1.addChild(cube_r3);
        setRotationAngle(cube_r3, 0.0F, 3.1416F, 0.0F);
        cube_r3.cubeList.add(new ModelBox(cube_r3, 8, 19, -2.5F, -2.5F, 0.0F, 5, 5, 0, 0.0F));

        Shape5 = new ModelRenderer(this);
        Shape5.setRotationPoint(-6.0F, 24.0F, 6.0F);
        Shape5.cubeList.add(new ModelBox(Shape5, 0, 0, -1.0F, -13.0F, -13.0F, 2, 13, 2, 0.0F));
        Shape5.cubeList.add(new ModelBox(Shape5, 8, 14, 0.0F, -13.0F, -11.0F, 0, 5, 5, 0.0F));

        cube_r4 = new ModelRenderer(this);
        cube_r4.setRotationPoint(3.5F, -10.5F, -12.0F);
        Shape5.addChild(cube_r4);
        setRotationAngle(cube_r4, 0.0F, 3.1416F, 0.0F);
        cube_r4.cubeList.add(new ModelBox(cube_r4, 8, 19, -2.5F, -2.5F, 0.0F, 5, 5, 0, 0.0F));

        Table = new ModelRenderer(this);
        Table.setRotationPoint(-6.0F, 24.0F, 6.0F);
        Table.cubeList.add(new ModelBox(Table, 0, 0, -2.0F, -16.0F, -14.0F, 16, 3, 16, 0.0F));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        Shape3.render(f5);
        Shape4.render(f5);
        Shape1.render(f5);
        Shape5.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
