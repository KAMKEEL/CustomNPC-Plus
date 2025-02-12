package noppes.npcs.client.model.blocks.couch;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCouchSingle extends ModelBase {
    public final ModelRenderer Cussion;
    public final ModelRenderer CouchBack;
    private final ModelRenderer ArmL;
    private final ModelRenderer ArmR;

    public ModelCouchSingle() {
        textureWidth = 64;
        textureHeight = 64;

        CouchBack = new ModelRenderer(this);
        CouchBack.setRotationPoint(0.0F, 24.0F, 0.0F);
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, -8.0F, -2.0F, -8.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, 6.0F, -2.0F, -8.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, -8.0F, -2.0F, 6.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, 6.0F, -2.0F, 6.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, -8.0F, -3.0F, -8.0F, 16, 1, 15, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 51, -8.0F, -12.0F, 7.0F, 16, 10, 1, 0.0F));

        ArmL = new ModelRenderer(this);
        ArmL.setRotationPoint(9.0F, -7.0F, 0.0F);
        CouchBack.addChild(ArmL);
        ArmL.mirror = true;
        ArmL.cubeList.add(new ModelBox(ArmL, 24, 21, -1.0F, -5.0F, -8.0F, 2, 2, 12, 0.0F));
        ArmL.cubeList.add(new ModelBox(ArmL, 14, 17, 0.0F, -3.0F, -6.0F, 0, 4, 10, 0.0F));
        ArmL.cubeList.add(new ModelBox(ArmL, 40, 23, -1.0F, -3.0F, -8.0F, 2, 8, 2, 0.0F));
        ArmL.cubeList.add(new ModelBox(ArmL, 0, 21, -1.0F, 1.0F, -6.0F, 2, 4, 10, 0.0F));
        ArmL.cubeList.add(new ModelBox(ArmL, 22, 48, -1.0F, -5.0F, 4.0F, 2, 10, 4, 0.0F));
        ArmL.mirror = false;

        ArmR = new ModelRenderer(this);
        ArmR.setRotationPoint(-9.0F, -7.0F, 0.0F);
        CouchBack.addChild(ArmR);
        ArmR.cubeList.add(new ModelBox(ArmR, 24, 21, -1.0F, -5.0F, -8.0F, 2, 2, 12, 0.0F));
        ArmR.cubeList.add(new ModelBox(ArmR, 14, 17, 0.0F, -3.0F, -6.0F, 0, 4, 10, 0.0F));
        ArmR.cubeList.add(new ModelBox(ArmR, 0, 21, -1.0F, 1.0F, -6.0F, 2, 4, 10, 0.0F));
        ArmR.cubeList.add(new ModelBox(ArmR, 40, 23, -1.0F, -3.0F, -8.0F, 2, 8, 2, 0.0F));
        ArmR.cubeList.add(new ModelBox(ArmR, 22, 48, -1.0F, -5.0F, 4.0F, 2, 10, 4, 0.0F));

        Cussion = new ModelRenderer(this);
        Cussion.setRotationPoint(0.0F, 24.0F, 0.0F);
        Cussion.cubeList.add(new ModelBox(Cussion, 0, 0, -8.0F, -7.0F, -8.0F, 16, 4, 15, 0.0F));
        Cussion.cubeList.add(new ModelBox(Cussion, 0, 19, -8.0F, -16.0F, 3.0F, 16, 9, 4, 0.0F));
        Cussion.cubeList.add(new ModelBox(Cussion, 0, 32, -8.0F, -16.0F, 7.0F, 16, 4, 1, 0.0F));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {}

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
