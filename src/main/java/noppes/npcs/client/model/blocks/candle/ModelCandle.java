package noppes.npcs.client.model.blocks.candle;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCandle extends ModelBase {
    public final ModelRenderer Candle;
    private final ModelRenderer cube_r1;
    private final ModelRenderer cube_r2;
    public final ModelRenderer Holder;

    public ModelCandle() {
        textureWidth = 64;
        textureHeight = 32;

        Candle = new ModelRenderer(this);
        Candle.setRotationPoint(0.0F, 23.0F, 0.0F);
        Candle.cubeList.add(new ModelBox(Candle, 28, 1, -1.0F, -5.0F, -1.0F, 2, 5, 2, 0.0F));

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(0.0F, -5.5F, 0.0F);
        Candle.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 2.3562F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(0.0F, -5.5F, 0.0F);
        Candle.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 0.7854F, 0.0F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 31, -1, 0.0F, -0.5F, -0.5F, 0, 1, 1, 0.0F));

        Holder = new ModelRenderer(this);
        Holder.setRotationPoint(0.0F, 24.0F, 0.0F);
        Holder.cubeList.add(new ModelBox(Holder, 12, 0, -2.0F, -1.0F, -2.0F, 4, 1, 4, 0.0F));
        Holder.cubeList.add(new ModelBox(Holder, 36, 0, 2.0F, -2.0F, -3.0F, 1, 1, 6, 0.0F));
        Holder.cubeList.add(new ModelBox(Holder, 36, 0, -3.0F, -2.0F, -3.0F, 1, 1, 6, 0.0F));
        Holder.cubeList.add(new ModelBox(Holder, 36, 7, -2.0F, -2.0F, -3.0F, 4, 1, 1, 0.0F));
        Holder.cubeList.add(new ModelBox(Holder, 36, 7, -2.0F, -2.0F, 2.0F, 4, 1, 1, 0.0F));
        Holder.cubeList.add(new ModelBox(Holder, 12, 11, 2.0F, -4.0F, 0.0F, 3, 3, 0, 0.0F));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Candle.render(f5);
        Holder.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
