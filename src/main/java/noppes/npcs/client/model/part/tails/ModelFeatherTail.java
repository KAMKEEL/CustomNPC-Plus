package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelFeatherTail extends ModelRenderer {
    ModelRenderer feather1;
    ModelRenderer feather2;
    ModelRenderer feather3;
    ModelRenderer feather4;
    ModelRenderer feather5;

    public ModelFeatherTail(ModelBiped base) {
        super(base);

        int x = 56;
        int y = 16;

        feather1 = new ModelRenderer(base, x, y);
        feather1.setTextureSize(64,32);
        feather1.addBox(-1.5F, 0F, 0F, 3, 8, 0);
        feather1.setRotationPoint(1F, -0.5F, 2F);
        setRotation(feather1, 1.482807F, 0.2602503F, 0.1487144F);
        addChild(feather1);

        feather2 = new ModelRenderer(base, x, y);
        feather2.setTextureSize(64,32);
        feather2.addBox(-1.5F, 0F, 0F, 3, 8, 0);
        feather2.setRotationPoint(0F, -0.5F, 1F);
        setRotation(feather2, 1.200559F, 0.3717861F, 0.1858931F);
        addChild(feather2);

        feather3 = new ModelRenderer(base, x, y);
        feather3.setTextureSize(64,32);
        feather3.mirror = true;
        feather3.addBox(-1.5F, -0.5F, 0F, 3, 8, 0);
        feather3.setRotationPoint(-1F, 0, 2F);
        setRotation(feather3, 1.256389F, -0.4089647F, -0.4833219F);
        addChild(feather3);

        feather4 = new ModelRenderer(base, x, y);
        feather4.setTextureSize(64,32);
        feather4.addBox(-1.5F, 0F, 0F, 3, 8, 0);
        feather4.setRotationPoint(0F, -0.5F, 2F);
        setRotation(feather4, 1.786329F, 0F, 0F);
        addChild(feather4);

        feather5 = new ModelRenderer(base, x, y);
        feather5.setTextureSize(64,32);
        feather5.mirror = true;
        feather5.addBox(-1.5F, 0F, 0F, 3, 8, 0);
        feather5.setRotationPoint(-1F, -0.5F, 2F);
        setRotation(feather5, 1.570073F, -0.2602503F, -0.2230717F);
        addChild(feather5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}

