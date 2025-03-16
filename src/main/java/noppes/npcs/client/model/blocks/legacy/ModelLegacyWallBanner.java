package noppes.npcs.client.model.blocks.legacy;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelLegacyWallBanner extends ModelBase {
    // fields
    ModelRenderer MiddleStick;
    ModelRenderer StickDecoration;
    ModelRenderer TopDecoration;
    ModelRenderer FlagPole1;
    ModelRenderer FlagPole2;

    public ModelLegacyWallBanner() {

        MiddleStick = new ModelRenderer(this, 56, 0);
        MiddleStick.addBox(-1F, 0F, -1F, 2, 3, 2);
        MiddleStick.setRotationPoint(0F, -9F, 6.5F);

        StickDecoration = new ModelRenderer(this, 11, 12);
        StickDecoration.addBox(0F, 0F, 0F, 16, 3, 3);
        StickDecoration.setRotationPoint(-8F, -7.5F, 5F);

        TopDecoration = new ModelRenderer(this, 45, 19);
        TopDecoration.addBox(0F, 0F, 0F, 1, 1, 1);
        TopDecoration.setRotationPoint(-0.5F, -10F, 6F);

        FlagPole1 = new ModelRenderer(this, 45, 19);
        FlagPole1.addBox(0F, 0F, 0F, 1, 1, 1);
        FlagPole1.setRotationPoint(-7F, -6.5F, 4F);

        FlagPole2 = new ModelRenderer(this, 45, 19);
        FlagPole2.addBox(0F, 0F, 0F, 1, 1, 1);
        FlagPole2.setRotationPoint(6F, -6.5F, 4F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3,
                       float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        MiddleStick.render(f5);
        StickDecoration.render(f5);
        TopDecoration.render(f5);
        FlagPole1.render(f5);
        FlagPole2.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
