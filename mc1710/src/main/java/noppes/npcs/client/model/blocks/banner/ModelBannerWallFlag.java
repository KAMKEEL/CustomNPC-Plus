package noppes.npcs.client.model.blocks.banner;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBannerWallFlag extends ModelBase {
    public final ModelRenderer BannerFlag;
    public final float rotationX = 0;

    public ModelBannerWallFlag() {
        textureWidth = 32;
        textureHeight = 32;

        BannerFlag = new ModelRenderer(this);
        BannerFlag.setRotationPoint(0.0F, -8.0F, 5.0F);
        BannerFlag.cubeList.add(new ModelBox(BannerFlag, 2, 0, -7.0F, 0.4F, -0.05F, 14, 29, 0, 0.0F));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        // Render the flag
        BannerFlag.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
