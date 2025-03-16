package noppes.npcs.client.model.blocks.campfire;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCampfireStone extends ModelBase {
    private final ModelRenderer bb_main;
    private final ModelRenderer cube_r1;
    private final ModelRenderer cube_r2;
    private final ModelRenderer cube_r3;

    public ModelCampfireStone() {
        textureWidth = 16;
        textureHeight = 16;

        bb_main = new ModelRenderer(this);
        bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
        bb_main.cubeList.add(new ModelBox(bb_main, 18, 5, 4.0F, -2.0F, 1.0F, 3, 2, 4, 0.0F));
        bb_main.cubeList.add(new ModelBox(bb_main, 18, 2, -4.0F, -3.0F, -7.0F, 4, 3, 3, 0.0F));
        bb_main.cubeList.add(new ModelBox(bb_main, 17, 0, -3.0F, -3.0F, 5.0F, 4, 3, 3, 0.0F));
        bb_main.cubeList.add(new ModelBox(bb_main, 18, 2, -8.0F, -3.0F, -1.0F, 3, 3, 4, 0.0F));
        bb_main.cubeList.add(new ModelBox(bb_main, 18, 11, -6.0F, -2.0F, 3.0F, 3, 2, 3, 0.0F));
        bb_main.cubeList.add(new ModelBox(bb_main, 16, 0, -7.0F, -2.0F, -5.0F, 3, 2, 4, 0.0F));
        bb_main.cubeList.add(new ModelBox(bb_main, 18, 11, 4.0F, -2.0F, -6.0F, 3, 2, 3, 0.0F));

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(6.5F, -1.5F, -1.0F);
        bb_main.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, -1.5708F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 18, 2, -2.0F, -1.5F, -1.5F, 4, 3, 3, 0.0F));

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(2.5F, -1.5F, -6.0F);
        bb_main.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 1.5708F, 0.0F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 18, 5, -1.0F, -0.5F, -2.5F, 3, 2, 4, 0.0F));

        cube_r3 = new ModelRenderer(this);
        cube_r3.setRotationPoint(3.5F, -1.5F, 5.5F);
        bb_main.addChild(cube_r3);
        setRotationAngle(cube_r3, 0.0F, -1.5708F, 0.0F);
        cube_r3.cubeList.add(new ModelBox(cube_r3, 20, 1, -0.5F, -0.5F, -1.5F, 2, 2, 4, 0.0F));
    }

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
