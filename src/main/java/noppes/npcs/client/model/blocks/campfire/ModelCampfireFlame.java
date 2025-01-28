package noppes.npcs.client.model.blocks.campfire;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCampfireFlame extends ModelBase {
    private final ModelRenderer[] frames;
    private static int frameDuration = 2;

    public ModelCampfireFlame() {
        textureWidth = 16;
        textureHeight = 144;

        frames = new ModelRenderer[9];

        // Create frames and add them to the array
        frames[0] = new ModelRenderer(this);
        frames[0].setRotationPoint(0.0F, 16.5F, 0.0F);
        frames[0].cubeList.add(new ModelBox(frames[0], 0, 0, -8.0F, -8.5F, 0.0F, 16, 16, 0, 0.0F));

        ModelRenderer cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(0.0F, 0.0F, 0.0F);
        frames[0].addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, -1.5708F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 0, 0, -8.0F, -8.5F, 0.0F, 16, 16, 0, 0.0F));

        // Repeat the above for all frames, incrementing texture offsets for each frame
        for (int i = 1; i < 9; i++) {
            frames[i] = new ModelRenderer(this);
            frames[i].setRotationPoint(0.0F, 16.5F, 0.0F);
            frames[i].cubeList.add(new ModelBox(frames[i], 0, i * 16 + 1, -8.0F, -8.5F, 0.0F, 16, 16, 0, 0.0F));

            ModelRenderer cube_r = new ModelRenderer(this);
            cube_r.setRotationPoint(0.0F, 0.0F, 0.0F);
            frames[i].addChild(cube_r);
            setRotationAngle(cube_r, 0.0F, -1.5708F, 0.0F);
            cube_r.cubeList.add(new ModelBox(cube_r, 0, i * 16 + 1, -8.0F, -8.5F, 0.0F, 16, 16, 0, 0.0F));
        }
    }

    @Override
    public void render(Entity entity, float world_time, float f1, float f2, float f3, float f4, float f5) {
        // Calculate the current frame directly using modulo arithmetic
        int frameIndex = ((int) world_time / frameDuration) % frames.length;

        // Render only the current frame
        frames[frameIndex].render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
