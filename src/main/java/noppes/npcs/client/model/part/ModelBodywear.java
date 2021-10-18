package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelScaleRenderer;

public class ModelBodywear extends ModelScaleRenderer {

    public ModelBodywear(ModelBase base) {
        super(base);

        float scaler = 0.81f;
        float thick = 0.65f;

        float x = -0.10f;
        float y = 12.76f;
        float z = -0.5f;

        float z1 = 0.27f;

        Model2DRenderer front = new Model2DRenderer(base, 20, 36, 8, 12, 64, 64);
        front.setRotationPoint(-4.588f, 0 + y, -2.302f + z);
        front.setScale(scaler);
        front.setThickness(thick);
        setRotation(front, 0, 0, 0);
        this.addChild(front);

        Model2DRenderer back = new Model2DRenderer(base, 32, 36, 8, 12, 64, 64);
        back.setRotationPoint(4.588f, 0 + y, 2.302f);
        back.setScale(scaler);
        back.setThickness(thick);
        setRotation(back, 0, (float) (Math.PI), 0);
        this.addChild(back);

        Model2DRenderer right = new Model2DRenderer(base, 28, 36, 4, 12, 64, 64);
        right.setRotationPoint(3.9425f, 0 + y, 2.29695f);
        right.setScale(scaler);
        right.setThickness(thick);
        setRotation(right, 0, (float) (Math.PI / 2f), 0);
        this.addChild(right);

        Model2DRenderer left = new Model2DRenderer(base, 16, 36, 4, 12, 64, 64);
        left.setRotationPoint(-3.9425f, 0 + y, -2.29695f + z);
        left.setScale(scaler);
        left.setThickness(thick);
        setRotation(left, 0, (float) (Math.PI / -2f), 0);
        this.addChild(left);


        Model2DRenderer top = new Model2DRenderer(base, 20, 32, 8, 4, 64, 64);
        top.setRotationPoint(-4.588f, -13.0f + y, -2.29695f);
        top.setScale(scaler / 3);
        top.setThickness(thick);
        setRotation(top, (float) (Math.PI / -2), 0, 0);
        this.addChild(top);

        Model2DRenderer bottom = new Model2DRenderer(base, 28, 32, 8, 4, 64, 64);
        bottom.setRotationPoint(-4.588f, -0.65f + y, -2.29695f);
        bottom.setScale(scaler / 3);
        bottom.setThickness(thick);
        setRotation(bottom, (float) (Math.PI / -2), 0, 0);
        this.addChild(bottom);
    }

    public void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
