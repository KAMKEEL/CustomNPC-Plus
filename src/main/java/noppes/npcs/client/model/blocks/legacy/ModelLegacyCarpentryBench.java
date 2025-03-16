package noppes.npcs.client.model.blocks.legacy;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelLegacyCarpentryBench extends ModelBase {
    // fields
    ModelRenderer Leg1;
    ModelRenderer Leg2;
    ModelRenderer Leg3;
    ModelRenderer Leg4;
    ModelRenderer Bottom_plate;
    ModelRenderer Desktop;
    ModelRenderer Backboard;
    ModelRenderer Vice_Jaw1;
    ModelRenderer Vice_Jaw2;
    ModelRenderer Vice_Base1;
    ModelRenderer Vice_Base2;
    ModelRenderer Vice_Crank;
    ModelRenderer Vice_Screw;
    ModelRenderer Blueprint;

    public ModelLegacyCarpentryBench() {
        this.textureWidth = 128;
        this.textureHeight = 64;

        Leg1 = new ModelRenderer(this, 0, 0);
        Leg1.addBox(0F, 0F, 0F, 2, 14, 2);
        Leg1.setRotationPoint(6F, 10F, 5F);

        Leg2 = new ModelRenderer(this, 0, 0);
        Leg2.addBox(0F, 0F, 0F, 2, 14, 2);
        Leg2.setRotationPoint(6F, 10F, -5F);

        Leg3 = new ModelRenderer(this, 0, 0);
        Leg3.addBox(0F, 0F, 0F, 2, 14, 2);
        Leg3.setRotationPoint(-8F, 10F, 5F);

        Leg4 = new ModelRenderer(this, 0, 0);
        Leg4.addBox(0F, 0F, 0F, 2, 14, 2);
        Leg4.setRotationPoint(-8F, 10F, -5F);

        Bottom_plate = new ModelRenderer(this, 0, 24);
        Bottom_plate.addBox(0F, 0F, 0F, 14, 1, 10);
        Bottom_plate.setRotationPoint(-7F, 21F, -4F);
        Bottom_plate.setTextureSize(130, 64);

        Desktop = new ModelRenderer(this, 0, 3);
        Desktop.addBox(0F, 0F, 0F, 18, 2, 13);
        Desktop.setRotationPoint(-9F, 9F, -6F);

        Backboard = new ModelRenderer(this, 0, 18);
        Backboard.addBox(-1F, 0F, 0F, 18, 5, 1);
        Backboard.setRotationPoint(-8F, 7F, 7F);

        Vice_Jaw1 = new ModelRenderer(this, 54, 18);
        Vice_Jaw1.addBox(0F, 0F, 0F, 3, 2, 1);
        Vice_Jaw1.setRotationPoint(3F, 6F, -8F);

        Vice_Jaw2 = new ModelRenderer(this, 54, 21);
        Vice_Jaw2.addBox(0F, 0F, 0F, 3, 2, 1);
        Vice_Jaw2.setRotationPoint(3F, 6F, -6F);

        Vice_Base1 = new ModelRenderer(this, 38, 30);
        Vice_Base1.addBox(0F, 0F, 0F, 3, 1, 3);
        Vice_Base1.setRotationPoint(3F, 8F, -5F);

        Vice_Base2 = new ModelRenderer(this, 38, 25);
        Vice_Base2.addBox(0F, 0F, 0F, 1, 2, 2);
        Vice_Base2.setRotationPoint(4F, 7F, -5F);

        Vice_Crank = new ModelRenderer(this, 54, 24);
        Vice_Crank.addBox(0F, 0F, 0F, 1, 5, 1);
        Vice_Crank.setRotationPoint(6F, 6F, -9F);

        Vice_Screw = new ModelRenderer(this, 44, 25);
        Vice_Screw.addBox(0F, 0F, 0F, 1, 1, 4);
        Vice_Screw.setRotationPoint(4F, 8F, -8F);

        Blueprint = new ModelRenderer(this, 31, 18);
        Blueprint.addBox(0F, 0F, 0F, 8, 0, 7);
        Blueprint.setRotationPoint(0F, 9F, 1F);
        setRotation(Blueprint, 0.3271718F, 0.1487144F, 0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Leg1.render(f5);
        Leg2.render(f5);
        Leg3.render(f5);
        Leg4.render(f5);
        Bottom_plate.render(f5);
        Desktop.render(f5);
        Backboard.render(f5);
        Vice_Jaw1.render(f5);
        Vice_Jaw2.render(f5);
        Vice_Base1.render(f5);
        Vice_Base2.render(f5);
        Vice_Crank.render(f5);
        Vice_Screw.render(f5);
        Blueprint.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }


}
