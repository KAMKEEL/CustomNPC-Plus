package noppes.npcs.client.model.blocks.candle;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCandleCeiling extends ModelBase {
    public final ModelRenderer Candle;
    private final ModelRenderer cube_r1;
    private final ModelRenderer cube_r2;
    private final ModelRenderer cube_r3;
    private final ModelRenderer cube_r4;
    private final ModelRenderer cube_r5;
    private final ModelRenderer cube_r6;
    private final ModelRenderer cube_r7;
    private final ModelRenderer cube_r8;
    public final ModelRenderer Holder;
    public final ModelRenderer Base;
    private final ModelRenderer cube_r9;
    private final ModelRenderer cube_r10;
    private final ModelRenderer cube_r11;
    public final ModelRenderer Chain;
    private final ModelRenderer cube_r12;
    private final ModelRenderer cube_r13;

    public ModelCandleCeiling() {
        textureWidth = 64;
        textureHeight = 32;

        Candle = new ModelRenderer(this);
        Candle.setRotationPoint(0.0F, 23.0F, 0.0F);
        Candle.cubeList.add(new ModelBox(Candle, 28, 1, -1.0F, -9.0F, 5.0F, 2, 5, 2, 0.0F));
        Candle.cubeList.add(new ModelBox(Candle, 28, 1, -7.0F, -9.0F, -1.0F, 2, 5, 2, 0.0F));
        Candle.cubeList.add(new ModelBox(Candle, 28, 1, 5.0F, -9.0F, -1.0F, 2, 5, 2, 0.0F));
        Candle.cubeList.add(new ModelBox(Candle, 28, 1, -1.0F, -9.0F, -7.0F, 2, 5, 2, 0.0F));

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(0.0F, -9.5F, -6.0F);
        Candle.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 2.3562F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(0.0F, -9.5F, -6.0F);
        Candle.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 0.7854F, 0.0F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        cube_r3 = new ModelRenderer(this);
        cube_r3.setRotationPoint(6.0F, -9.5F, 0.0F);
        Candle.addChild(cube_r3);
        setRotationAngle(cube_r3, 0.0F, 2.3562F, 0.0F);
        cube_r3.cubeList.add(new ModelBox(cube_r3, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        cube_r4 = new ModelRenderer(this);
        cube_r4.setRotationPoint(6.0F, -9.5F, 0.0F);
        Candle.addChild(cube_r4);
        setRotationAngle(cube_r4, 0.0F, 0.7854F, 0.0F);
        cube_r4.cubeList.add(new ModelBox(cube_r4, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        cube_r5 = new ModelRenderer(this);
        cube_r5.setRotationPoint(-6.0F, -9.5F, 0.0F);
        Candle.addChild(cube_r5);
        setRotationAngle(cube_r5, 0.0F, 2.3562F, 0.0F);
        cube_r5.cubeList.add(new ModelBox(cube_r5, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        cube_r6 = new ModelRenderer(this);
        cube_r6.setRotationPoint(-6.0F, -9.5F, 0.0F);
        Candle.addChild(cube_r6);
        setRotationAngle(cube_r6, 0.0F, 0.7854F, 0.0F);
        cube_r6.cubeList.add(new ModelBox(cube_r6, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        cube_r7 = new ModelRenderer(this);
        cube_r7.setRotationPoint(0.0F, -9.5F, 6.0F);
        Candle.addChild(cube_r7);
        setRotationAngle(cube_r7, 0.0F, 2.3562F, 0.0F);
        cube_r7.cubeList.add(new ModelBox(cube_r7, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        cube_r8 = new ModelRenderer(this);
        cube_r8.setRotationPoint(0.0F, -9.5F, 6.0F);
        Candle.addChild(cube_r8);
        setRotationAngle(cube_r8, 0.0F, 0.7854F, 0.0F);
        cube_r8.cubeList.add(new ModelBox(cube_r8, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        Holder = new ModelRenderer(this);
        Holder.setRotationPoint(0.0F, 24.0F, 0.0F);
        Holder.cubeList.add(new ModelBox(Holder, 12, 0, -2.0F, -5.0F, 4.0F, 4, 1, 4, 0.0F));
        Holder.cubeList.add(new ModelBox(Holder, 12, 0, -8.0F, -5.0F, -2.0F, 4, 1, 4, 0.0F));
        Holder.cubeList.add(new ModelBox(Holder, 12, 0, 4.0F, -5.0F, -2.0F, 4, 1, 4, 0.0F));
        Holder.cubeList.add(new ModelBox(Holder, 12, 0, -2.0F, -5.0F, -8.0F, 4, 1, 4, 0.0F));

        Base = new ModelRenderer(this);
        Base.setRotationPoint(0.0F, 24.0F, 0.0F);
        Base.cubeList.add(new ModelBox(Base, 12, 0, -2.0F, -2.0F, -2.0F, 4, 1, 4, 0.0F));
        Base.cubeList.add(new ModelBox(Base, 12, 0, 0.0F, -4.0F, 2.0F, 0, 3, 5, 0.0F));

        cube_r9 = new ModelRenderer(this);
        cube_r9.setRotationPoint(4.5F, -2.5F, 0.0F);
        Base.addChild(cube_r9);
        setRotationAngle(cube_r9, 0.0F, 1.5708F, 0.0F);
        cube_r9.cubeList.add(new ModelBox(cube_r9, 12, 0, 0.0F, -1.5F, -2.5F, 0, 3, 5, 0.0F));

        cube_r10 = new ModelRenderer(this);
        cube_r10.setRotationPoint(-7.0F, -2.5F, 0.0F);
        Base.addChild(cube_r10);
        setRotationAngle(cube_r10, 0.0F, -1.5708F, 0.0F);
        cube_r10.cubeList.add(new ModelBox(cube_r10, 12, 0, 0.0F, -1.5F, -5.0F, 0, 3, 5, 0.0F));

        cube_r11 = new ModelRenderer(this);
        cube_r11.setRotationPoint(0.0F, -2.5F, -4.5F);
        Base.addChild(cube_r11);
        setRotationAngle(cube_r11, 0.0F, 3.1416F, 0.0F);
        cube_r11.cubeList.add(new ModelBox(cube_r11, 12, 0, 0.0F, -1.5F, -2.5F, 0, 3, 5, 0.0F));

        Chain = new ModelRenderer(this);
        Chain.setRotationPoint(0.0F, 18.0F, 0.0F);


        cube_r12 = new ModelRenderer(this);
        cube_r12.setRotationPoint(0.0F, -3.0F, 0.0F);
        Chain.addChild(cube_r12);
        setRotationAngle(cube_r12, 0.0F, 0.7854F, 0.0F);
        cube_r12.cubeList.add(new ModelBox(cube_r12, 6, -1, 0.0F, -7.0F, -1.5F, 0, 14, 3, 0.0F));

        cube_r13 = new ModelRenderer(this);
        cube_r13.setRotationPoint(0.0F, -3.0F, 0.0F);
        Chain.addChild(cube_r13);
        setRotationAngle(cube_r13, 0.0F, -0.7854F, 0.0F);
        cube_r13.cubeList.add(new ModelBox(cube_r13, 0, -1, 0.0F, -7.0F, -1.5F, 0, 14, 3, 0.0F));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Candle.render(f5);
        Holder.render(f5);
        Base.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
