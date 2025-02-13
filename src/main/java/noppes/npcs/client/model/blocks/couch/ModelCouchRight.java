package noppes.npcs.client.model.blocks.couch;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCouchRight extends ModelBase {
    public final ModelRenderer CouchBack;
    public final ModelRenderer Cussion;

    public ModelCouchRight() {
        textureWidth = 64;
        textureHeight = 64;

        CouchBack = new ModelRenderer(this);
        CouchBack.mirror = true;
        CouchBack.setRotationPoint(0.0F, 24.0F, 0.0F);
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, 6.0F, -2.0F, -8.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, -8.0F, -2.0F, -8.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, 6.0F, -2.0F, 6.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, -8.0F, -2.0F, 6.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, -8.0F, -3.0F, -8.0F, 14, 1, 15, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 51, -8.0F, -12.0F, 7.0F, 14, 10, 1, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 21, 6.0F, -6.0F, -6.0F, 2, 4, 10, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 22, 48, 6.0F, -12.0F, 4.0F, 2, 10, 4, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 24, 21, 6.0F, -12.0F, -8.0F, 2, 2, 12, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 14, 17, 7.0F, -10.0F, -6.0F, 0, 4, 10, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 40, 23, 6.0F, -10.0F, -8.0F, 2, 8, 2, 0.0F));

        Cussion = new ModelRenderer(this);
        Cussion.mirror = true;
        Cussion.setRotationPoint(0.0F, 24.0F, 0.0F);
        Cussion.cubeList.add(new ModelBox(Cussion, 2, 0, -8.0F, -7.0F, -8.0F, 14, 4, 15, 0.0F));
        Cussion.cubeList.add(new ModelBox(Cussion, 2, 19, -8.0F, -16.0F, 3.0F, 14, 9, 4, 0.0F));
        Cussion.cubeList.add(new ModelBox(Cussion, 2, 32, -8.0F, -16.0F, 7.0F, 14, 4, 1, 0.0F));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        CouchBack.render(f5);
        Cussion.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
