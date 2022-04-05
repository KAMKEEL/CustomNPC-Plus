

package noppes.npcs.client.model.part;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import noppes.npcs.client.model.util.Model2DRenderer;
import noppes.npcs.client.model.util.ModelScaleRenderer;

public class ModelLimbWear extends ModelScaleRenderer {
    public ModelLimbWear(ModelBase base, String limb, String side, String type) {
        super(base);

        float thick = 0.55f;
        switch (limb){
            case "arm":
                switch(type) {
                    case "Steve":
                        switch (side) {
                            case "right":
                                rightArm(base, thick);
                                break;
                            case "left":
                                leftArm(base, thick);
                                break;
                        }
                        break;
                    case "Alex":
                        switch (side) {
                            case "right":
                                rightArmAlex(base, thick);
                                break;
                            case "left":
                                leftArmAlex(base, thick);
                                break;
                        }
                        break;
                }
                break;
            case "leg":
                switch (side){
                    case "right":
                        rightLeg(base,thick);
                        break;
                    case "left":
                        leftLeg(base,thick);
                        break;
                }
                break;
        }


    }

    public void rightArmAlex(ModelBase base, float thick){
        float x = 0.95f;
        float y = -2.0f;

        Model2DRenderer front = new Model2DRenderer(base, 44, 36, 3, 12, 64, 64);
        front.setRotationPoint(-3.39375f+x, 12.535f+y, -2.58f);
        front.setScale(0.96f,0.8175f);
        front.setThickness(thick);
        setRotation(front, 0, 0, 0);
        this.addChild(front);

        Model2DRenderer back = new Model2DRenderer(base, 51, 36, 3, 12, 64, 64);
        back.setRotationPoint(0.44f+x, 12.535f+y, 2.58f);
        back.setScale(0.96f,0.8175f);
        back.setThickness(thick);
        setRotation(back, 0, (float)(Math.PI), 0);
        this.addChild(back);

        Model2DRenderer right = new Model2DRenderer(base, 47, 36, 4, 12, 64, 64);
        right.setRotationPoint(0.45f+x, 12.55f+y, -2.55f);
        right.setScale(0.96f,0.82f);
        right.setThickness(thick);
        setRotation(right, 0, (float)(Math.PI/-2f), 0);
        this.addChild(right);

        Model2DRenderer left = new Model2DRenderer(base, 40, 36, 4, 12, 64, 64);
        left.setRotationPoint(-3.405f+x, 12.55f+y, 2.55f);
        left.setScale(0.96f,0.82f);
        left.setThickness(thick);
        setRotation(left, 0, (float)(Math.PI/2f), 0);
        this.addChild(left);

        Model2DRenderer top = new Model2DRenderer(base, 44, 32, 3, 4, 64, 64);
        top.setRotationPoint(-3.39375f+x, -0.6f+y, -2.55f);
        top.setScale(0.32f);
        top.setThickness(thick);
        setRotation(top, (float)(Math.PI / -2), 0, 0);
        this.addChild(top);

        Model2DRenderer bottom = new Model2DRenderer(base, 47, 32, 3, 4, 64, 64);
        bottom.setRotationPoint(-3.39375f+x, 12.0f+y, -2.545f);
        bottom.setScale(0.32f);
        bottom.setThickness(thick);
        setRotation(bottom, (float)(Math.PI / -2), 0, 0);
        this.addChild(bottom);
    }

    public void leftArmAlex(ModelBase base,float thick){
        float x = 1.95f;
        float y = -2.0f;

        Model2DRenderer front = new Model2DRenderer(base, 44+8, 36+16, 3, 12, 64, 64);
        front.setRotationPoint(-3.39375f+x, 12.535f+y, -2.58f);
        front.setScale(0.96f,0.8175f);
        front.setThickness(thick);
        setRotation(front, 0, 0, 0);
        this.addChild(front);

        Model2DRenderer back = new Model2DRenderer(base, 51+8, 36+16, 3, 12, 64, 64);
        back.setRotationPoint(0.44f+x, 12.535f+y, 2.58f);
        back.setScale(0.96f,0.8175f);
        back.setThickness(thick);
        setRotation(back, 0, (float)(Math.PI), 0);
        this.addChild(back);

        Model2DRenderer right = new Model2DRenderer(base, 47+8, 36+16, 4, 12, 64, 64);
        right.setRotationPoint(0.45f+x, 12.55f+y, -2.55f);
        right.setScale(0.96f,0.82f);
        right.setThickness(thick);
        setRotation(right, 0, (float)(Math.PI/-2f), 0);
        this.addChild(right);

        Model2DRenderer left = new Model2DRenderer(base, 40+8, 36+16, 4, 12, 64, 64);
        left.setRotationPoint(-3.405f+x, 12.55f+y, 2.55f);
        left.setScale(0.96f,0.82f);
        left.setThickness(thick);
        setRotation(left, 0, (float)(Math.PI/2f), 0);
        this.addChild(left);

        Model2DRenderer top = new Model2DRenderer(base, 44+8, 32+16, 3, 4, 64, 64);
        top.setRotationPoint(-3.39375f+x, -0.6f+y, -2.55f);
        top.setScale(0.32f);
        top.setThickness(thick);
        setRotation(top, (float)(Math.PI / -2), 0, 0);
        this.addChild(top);

        Model2DRenderer bottom = new Model2DRenderer(base, 47+8, 32+16, 3, 4, 64, 64);
        bottom.setRotationPoint(-3.39375f+x, 12.0f+y, -2.545f);
        bottom.setScale(0.32f);
        bottom.setThickness(thick);
        setRotation(bottom, (float)(Math.PI / -2), 0, 0);
        this.addChild(bottom);
    }

    public void rightArm(ModelBase base, float thick){
        float x = 0.95f;
        float y = -2.0f;

        Model2DRenderer front = new Model2DRenderer(base, 44, 36, 4, 12, 64, 64);
        front.setRotationPoint(-4.52f+x, 12.535f+y, -2.58f);
        front.setScale(0.96f,0.8175f);
        front.setThickness(thick);
        setRotation(front, 0, 0, 0);
        this.addChild(front);

        Model2DRenderer back = new Model2DRenderer(base, 52, 36, 4, 12, 64, 64);
        back.setRotationPoint(0.6f+x, 12.535f+y, 2.58f);
        back.setScale(0.96f,0.8175f);
        back.setThickness(thick);
        setRotation(back, 0, (float)(Math.PI), 0);
        this.addChild(back);

        Model2DRenderer right = new Model2DRenderer(base, 48, 36, 4, 12, 64, 64);
        right.setRotationPoint(0.62f+x, 12.55f+y, -2.55f);
        right.setScale(0.96f,0.82f);
        right.setThickness(thick);
        setRotation(right, 0, (float)(Math.PI/-2f), 0);
        this.addChild(right);

        Model2DRenderer left = new Model2DRenderer(base, 40, 36, 4, 12, 64, 64);
        left.setRotationPoint(-4.56f+x, 12.55f+y, 2.55f);
        left.setScale(0.96f,0.82f);
        left.setThickness(thick);
        setRotation(left, 0, (float)(Math.PI/2f), 0);
        this.addChild(left);

        Model2DRenderer top = new Model2DRenderer(base, 44, 32, 4, 4, 64, 64);
        top.setRotationPoint(-4.525f+x, -0.6f+y, -2.55f);
        top.setScale(0.32f);
        top.setThickness(thick);
        setRotation(top, (float)(Math.PI / -2), 0, 0);
        this.addChild(top);

        Model2DRenderer bottom = new Model2DRenderer(base, 48, 32, 4, 4, 64, 64);
        bottom.setRotationPoint(-4.5f+x, 12.0f+y, -2.525f);
        bottom.setScale(0.32f);
        bottom.setThickness(thick);
        setRotation(bottom, (float)(Math.PI / -2), 0, 0);
        this.addChild(bottom);
    }

    public void leftArm(ModelBase base,float thick){
        float x = 2.95f;
        float y = -2.0f;

        Model2DRenderer front = new Model2DRenderer(base, 52, 52, 4, 12, 64, 64);
        front.setRotationPoint(-4.52f+x, 12.535f+y, -2.58f);
        front.setScale(0.96f,0.8175f);
        front.setThickness(thick);
        setRotation(front, 0, 0, 0);
        this.addChild(front);

        Model2DRenderer back = new Model2DRenderer(base, 60, 52, 4, 12, 64, 64);
        back.setRotationPoint(0.6f+x, 12.535f+y, 2.58f);
        back.setScale(0.96f,0.8175f);
        back.setThickness(thick);
        setRotation(back, 0, (float)(Math.PI), 0);
        this.addChild(back);

        Model2DRenderer right = new Model2DRenderer(base, 56, 52, 4, 12, 64, 64);
        right.setRotationPoint(0.62f+x, 12.55f+y, -2.55f);
        right.setScale(0.96f,0.82f);
        right.setThickness(thick);
        setRotation(right, 0, (float)(Math.PI/-2f), 0);
        this.addChild(right);

        Model2DRenderer left = new Model2DRenderer(base, 48, 52, 4, 12, 64, 64);
        left.setRotationPoint(-4.56f+x, 12.55f+y, 2.55f);
        left.setScale(0.96f,0.82f);
        left.setThickness(thick);
        setRotation(left, 0, (float)(Math.PI/2f), 0);
        this.addChild(left);


        Model2DRenderer top = new Model2DRenderer(base, 52, 48, 4, 4, 64, 64);
        top.setRotationPoint(-4.525f+x, -0.6f+y, -2.55f);
        top.setScale(0.32f);
        top.setThickness(thick);
        setRotation(top, (float)(Math.PI / -2), 0, 0);
        this.addChild(top);

        Model2DRenderer bottom = new Model2DRenderer(base, 56, 48, 4, 4, 64, 64);
        bottom.setRotationPoint(-4.56f+x, 12.0f+y, -2.55f);
        bottom.setScale(0.32f);
        bottom.setThickness(thick);
        setRotation(bottom, (float)(Math.PI / -2), 0, 0);
        this.addChild(bottom);
    }

    public void rightLeg(ModelBase base, float thick){
        float x = 1.8f;
        float y = 0.0f;

        Model2DRenderer front = new Model2DRenderer(base, 4, 36, 4, 12, 64, 64);
        front.setRotationPoint(-4.52f+x, 12.535f+y, -2.58f);
        front.setScale(0.96f,0.8175f);
        front.setThickness(thick);
        setRotation(front, 0, 0, 0);
        this.addChild(front);

        Model2DRenderer back = new Model2DRenderer(base, 12, 36, 4, 12, 64, 64);
        back.setRotationPoint(0.6f+x, 12.535f+y, 2.58f);
        back.setScale(0.96f,0.8175f);
        back.setThickness(thick);
        setRotation(back, 0, (float)(Math.PI), 0);
        this.addChild(back);

        Model2DRenderer right = new Model2DRenderer(base, 8, 36, 4, 12, 64, 64);
        right.setRotationPoint(0.62f+x, 12.55f+y, -2.55f);
        right.setScale(0.96f,0.82f);
        right.setThickness(thick);
        setRotation(right, 0, (float)(Math.PI/-2f), 0);
        this.addChild(right);

        Model2DRenderer left = new Model2DRenderer(base, 0, 36, 4, 12, 64, 64);
        left.setRotationPoint(-4.53f+x, 12.55f+y, 2.55f);
        left.setScale(0.96f,0.82f);
        left.setThickness(thick);
        setRotation(left, 0, (float)(Math.PI/2f), 0);
        this.addChild(left);


        Model2DRenderer top = new Model2DRenderer(base, 4, 32, 4, 4, 64, 64);
        top.setRotationPoint(-4.525f+x, -0.6f+y, -2.55f);
        top.setScale(0.32f);
        top.setThickness(thick);
        setRotation(top, (float)(Math.PI / -2), 0, 0);
        this.addChild(top);

        Model2DRenderer bottom = new Model2DRenderer(base, 8, 32, 4, 4, 64, 64);
        bottom.setRotationPoint(-4.5f+x, 12.11f+y, -2.525f);
        bottom.setScale(0.32f);
        bottom.setThickness(thick);
        setRotation(bottom, (float)(Math.PI / -2), 0, 0);
        this.addChild(bottom);
    }

    public void leftLeg(ModelBase base,float thick){
        float x = 2.05f;
        float y = 0.0f;

        Model2DRenderer front = new Model2DRenderer(base, 4, 52, 4, 12, 64, 64);
        front.setRotationPoint(-4.52f+x, 12.535f+y, -2.58f);
        front.setScale(0.96f,0.8175f);
        front.setThickness(thick);
        setRotation(front, 0, 0, 0);
        this.addChild(front);

        Model2DRenderer back = new Model2DRenderer(base, 12, 52, 4, 12, 64, 64);
        back.setRotationPoint(0.6f+x, 12.535f+y, 2.58f);
        back.setScale(0.96f,0.8175f);
        back.setThickness(thick);
        setRotation(back, 0, (float)(Math.PI), 0);
        this.addChild(back);

        Model2DRenderer right = new Model2DRenderer(base, 8, 52, 4, 12, 64, 64);
        right.setRotationPoint(0.62f+x, 12.55f+y, -2.55f);
        right.setScale(0.96f,0.82f);
        right.setThickness(thick);
        setRotation(right, 0, (float)(Math.PI/-2f), 0);
        this.addChild(right);

        Model2DRenderer left = new Model2DRenderer(base, 0, 52, 4, 12, 64, 64);
        left.setRotationPoint(-4.53f+x, 12.55f+y, 2.55f);
        left.setScale(0.96f,0.82f);
        left.setThickness(thick);
        setRotation(left, 0, (float)(Math.PI/2f), 0);
        this.addChild(left);


        Model2DRenderer top = new Model2DRenderer(base, 4, 48, 4, 4, 64, 64);
        top.setRotationPoint(-4.525f+x, -0.6f+y, -2.55f);
        top.setScale(0.32f);
        top.setThickness(thick);
        setRotation(top, (float)(Math.PI / -2), 0, 0);
        this.addChild(top);

        Model2DRenderer bottom = new Model2DRenderer(base, 8, 48, 4, 4, 64, 64);
        bottom.setRotationPoint(-4.5f+x, 12.11f+y, -2.525f);
        bottom.setScale(0.32f);
        bottom.setThickness(thick);
        setRotation(bottom, (float)(Math.PI / -2), 0, 0);
        this.addChild(bottom);
    }

    public void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
