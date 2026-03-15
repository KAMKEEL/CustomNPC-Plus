package noppes.npcs.client.model.blocks.lantern;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class LanternFloor extends ModelBase {
    public final ModelRenderer Light;
    public final ModelRenderer Lantern;

    public LanternFloor() {
        textureWidth = 64;
        textureHeight = 32;

        Light = new ModelRenderer(this);
        Light.setRotationPoint(0.0F, 24.0F, 0.0F);
        Light.cubeList.add(new ModelBox(Light, 28, 8, -3.0F, -9.0F, -3.0F, 6, 7, 6, 0.0F));

        Lantern = new ModelRenderer(this);
        Lantern.setRotationPoint(0.0F, 24.0F, 0.0F);
        Lantern.cubeList.add(new ModelBox(Lantern, 28, 0, -3.0F, -2.0F, -3.0F, 6, 2, 6, 0.0F));
        Lantern.cubeList.add(new ModelBox(Lantern, 28, 21, -2.0F, -11.0F, -2.0F, 4, 2, 4, 0.0F));
        Lantern.cubeList.add(new ModelBox(Lantern, 46, 21, -5.0F, -11.0F, 0.0F, 3, 11, 0, 0.0F));
        Lantern.mirror = true;
        Lantern.cubeList.add(new ModelBox(Lantern, 46, 21, 2.0F, -11.0F, 0.0F, 3, 11, 0, 0.0F));
        Lantern.mirror = false;
        Lantern.cubeList.add(new ModelBox(Lantern, 28, 27, -3.0F, -13.0F, 0.0F, 6, 2, 0, 0.0F));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        Light.render(f5);
        Lantern.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
