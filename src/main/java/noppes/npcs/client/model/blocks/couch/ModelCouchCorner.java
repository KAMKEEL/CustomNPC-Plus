package noppes.npcs.client.model.blocks.couch;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCouchCorner extends ModelBase {
    public final ModelRenderer CouchBack;
    private final ModelRenderer cube_r1;
    public final ModelRenderer Cussion;
    private final ModelRenderer cube_r2;
    private final ModelRenderer cube_r3;

    public ModelCouchCorner() {
        textureWidth = 64;
        textureHeight = 64;

        CouchBack = new ModelRenderer(this);
        CouchBack.setRotationPoint(0.0F, 24.0F, 0.0F);
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, -8.0F, -2.0F, -8.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, 6.0F, -2.0F, -8.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, -8.0F, -2.0F, 6.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 35, 6.0F, -2.0F, 6.0F, 2, 2, 2, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 1, 35, -8.0F, -3.0F, -8.0F, 15, 1, 15, 0.0F));
        CouchBack.cubeList.add(new ModelBox(CouchBack, 0, 51, -8.0F, -12.0F, 7.0F, 16, 10, 1, 0.0F));

        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(7.5F, -7.5F, -1.0F);
        CouchBack.addChild(cube_r1);
        setRotationAngle(cube_r1, 0.0F, 1.5708F, 0.0F);
        cube_r1.cubeList.add(new ModelBox(cube_r1, 1, 51, -8.0F, -4.5F, -0.5F, 15, 10, 1, 0.0F));

        Cussion = new ModelRenderer(this);
        Cussion.setRotationPoint(0.0F, 24.0F, 0.0F);
        Cussion.cubeList.add(new ModelBox(Cussion, 1, 37, -8.0F, -7.0F, -8.0F, 15, 4, 15, 0.0F));
        Cussion.cubeList.add(new ModelBox(Cussion, 0, 19, -8.0F, -16.0F, 3.0F, 15, 9, 4, 0.0F));
        Cussion.cubeList.add(new ModelBox(Cussion, 0, 32, -8.0F, -16.0F, 7.0F, 16, 4, 1, 0.0F));

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(7.5F, -14.0F, -1.0F);
        Cussion.addChild(cube_r2);
        setRotationAngle(cube_r2, 0.0F, 1.5708F, 0.0F);
        cube_r2.cubeList.add(new ModelBox(cube_r2, 1, 32, -8.0F, -2.0F, -0.5F, 15, 4, 1, 0.0F));

        cube_r3 = new ModelRenderer(this);
        cube_r3.setRotationPoint(5.5F, -11.5F, -3.5F);
        Cussion.addChild(cube_r3);
        setRotationAngle(cube_r3, 0.0F, 1.5708F, 0.0F);
        cube_r3.cubeList.add(new ModelBox(cube_r3, 5, 19, -6.5F, -4.5F, -2.5F, 11, 9, 4, 0.0F));
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
