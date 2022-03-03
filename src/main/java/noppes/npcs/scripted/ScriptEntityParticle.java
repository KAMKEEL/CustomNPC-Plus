package noppes.npcs.scripted;

import noppes.npcs.NoppesUtilServer;
import noppes.npcs.scripted.entity.ScriptEntity;

/**
 * Created by luisc on 9/8/2021.
 */
public class ScriptEntityParticle {
    private String directory;
    private int HEXcolor = 0xFFFFFF;
    private int amount = 1;
    private int maxAge = 20;
    private double x = 0,y = 0,z = 0;
    private double motionX = 0,motionY = 0,motionZ = 0;
    private float gravity = 0;
    private float scale1 = 1.0f,scale2 = 1.0f,scaleRate = 0.0f;
    private float alpha1 = 1.0f,alpha2 = 1.0f,alphaRate = 0.0f;
    private int scaleRateStart = 0, alphaRateStart = 0;

    public float rotation1 = 0;
    public float rotation2 = 0;
    public float rotationRate = 0.0F;
    public int rotationRateStart = 0;

    public float rotationX1 = 0;
    public float rotationX2 = 0;
    public float rotationXRate = 0.0F;
    public int rotationXRateStart = 0;

    public float rotationY1 = 0;
    public float rotationY2 = 0;
    public float rotationYRate = 0.0F;
    public int rotationYRateStart = 0;

    public float rotationZ1 = 0;
    public float rotationZ2 = 0;
    public float rotationZRate = 0.0F;
    public int rotationZRateStart = 0;

    public ScriptEntityParticle(String directory){
        this.directory = directory;
    }

    public void spawnOnEntity(ScriptEntity entity){
        int entityID = entity.getMCEntity().getEntityId();
        NoppesUtilServer.spawnScriptedParticle(directory, HEXcolor, amount, maxAge,
                x, y, z,
                motionX, motionY, motionZ, gravity,
                scale1, scale2, scaleRate, scaleRateStart,
                alpha1, alpha2, alphaRate, alphaRateStart,
                rotation1, rotation2, rotationRate, rotationRateStart,
                rotationX1, rotationX2, rotationXRate, rotationXRateStart,
                rotationY1, rotationY2, rotationYRate, rotationYRateStart,
                rotationZ1, rotationZ2, rotationZRate, rotationZRateStart,
                entityID
        );
    }

    public String getDirectory() {
        return directory;
    }

    public int getHEXcolor() {
        return HEXcolor;
    }

    public int getAmount() {
        return amount;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }

    public double getMotionX() {
        return motionX;
    }
    public double getMotionY() {
        return motionY;
    }
    public double getMotionZ() {
        return motionZ;
    }
    public float getGravity() {
        return gravity;
    }

    public float getAlpha1() {
        return alpha1;
    }
    public float getAlpha2() {
        return alpha2;
    }
    public float getAlphaRate() {
        return alphaRate;
    }
    public int getAlphaRateStart() {
        return alphaRateStart;
    }

    public float getScale1() {
        return scale1;
    }
    public float getScale2() {
        return scale2;
    }
    public float getScaleRate() {
        return scaleRate;
    }
    public int getScaleRateStart() {
        return scaleRateStart;
    }

    public float getRotation1() {return rotation1;}
    public float getRotation2() {return rotation2;}
    public float getRotationRate() {return rotationRate;}
    public int getRotationRateStart() {return rotationRateStart;}

    public float getRotationX1() {return rotationX1;}
    public float getRotationX2() {return rotationX2;}
    public float getRotationXRate() {return rotationXRate;}
    public int getRotationXRateStart() {return rotationXRateStart;}

    public float getRotationY1() {return rotationY1;}
    public float getRotationY2() {return rotationY2;}
    public float getRotationYRate() {return rotationYRate;}
    public int getRotationYRateStart() {return rotationYRateStart;}

    public float getRotationZ1() {return rotationZ1;}
    public float getRotationZ2() {return rotationZ2;}
    public float getRotationZRate() {return rotationZRate;}
    public int getRotationZRateStart() {return rotationZRateStart;}

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setHEXColor(int HEXcolor) {
        this.HEXcolor = HEXcolor;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setPosition(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
    public void setZ(double z) {
        this.z = z;
    }

    public void setMotion(double motionX, double motionY, double motionZ, float gravity){
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.gravity = gravity;
    }
    public void setMotionX(double motionX) {
        this.motionX = motionX;
    }
    public void setMotionY(double motionY) {
        this.motionY = motionY;
    }
    public void setMotionZ(double motionZ) {
        this.motionZ = motionZ;
    }
    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public void setScale(float scale1, float scale2, float scaleRate, int scaleRateStart){
        this.scale1 = scale1;
        this.scale2 = scale2;
        this.scaleRate = scaleRate;
        this.scaleRateStart = scaleRateStart;
    }
    public void setScale1(float scale1) {
        this.scale1 = scale1;
    }
    public void setScale2(float scale2) {
        this.scale2 = scale2;
    }
    public void setScaleRate(float scaleRate) {
        this.scaleRate = scaleRate;
    }
    public void setScaleRateStart(int scaleRateStart) {
        this.scaleRateStart = scaleRateStart;
    }

    public void setAlpha(float alpha1, float alpha2, float alphaRate, int alphaRateStart){
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
        this.alphaRate = alphaRate;
        this.alphaRateStart = alphaRateStart;
    }
    public void setAlpha1(float alpha1) {
        this.alpha1 = alpha1;
    }
    public void setAlpha2(float alpha2) {
        this.alpha2 = alpha2;
    }
    public void setAlphaRate(float alphaRate) {
        this.alphaRate = alphaRate;
    }
    public void setAlphaRateStart(int alphaRateStart) {
        this.alphaRateStart = alphaRateStart;
    }

    public void setRotation(float rotation1, float rotation2, float rotationRate, int rotationRateStart){
        this.rotation1 = rotation1;
        this.rotation2 = rotation2;
        this.rotationRate = rotationRate;
        this.rotationRateStart = rotationRateStart;
    }
    public void setRotation1(float rotation1) {this.rotation1 = rotation1;}
    public void setRotation2(float rotation2) {this.rotation2 = rotation2;}
    public void setRotationRate(float rotationRate) {this.rotationRate = rotationRate;}
    public void setRotationRateStart(int rotationRateStart) {this.rotationRateStart = rotationRateStart;}

    public void setRotationX(float rotationX1, float rotationX2, float rotationXRate, int rotationXRateStart){
        this.rotationX1 = rotationX1;
        this.rotationX2 = rotationX2;
        this.rotationXRate = rotationXRate;
        this.rotationXRateStart = rotationXRateStart;
    }
    public void setRotationX1(float rotationX1) {this.rotationX1 = rotationX1;}
    public void setRotationX2(float rotationX2) {this.rotationX2 = rotationX2;}
    public void setRotationXRate(float rotationXRate) {this.rotationXRate = rotationXRate;}
    public void setRotationXRateStart(int rotationXRateStart) {this.rotationXRateStart = rotationXRateStart;}

    public void setRotationY(float rotationY1, float rotationY2, float rotationYRate, int rotationYRateStart){
        this.rotationY1 = rotationY1;
        this.rotationY2 = rotationY2;
        this.rotationYRate = rotationYRate;
        this.rotationYRateStart = rotationYRateStart;
    }
    public void setRotationY1(float rotationY1) {this.rotationY1 = rotationY1;}
    public void setRotationY2(float rotationY2) {this.rotationY2 = rotationY2;}
    public void setRotationYRate(float rotationYRate) {this.rotationYRate = rotationYRate;}
    public void setRotationYRateStart(int rotationYRateStart) {this.rotationYRateStart = rotationYRateStart;}

    public void setRotationZ(float rotationZ1, float rotationZ2, float rotationZRate, int rotationZRateStart){
        this.rotationZ1 = rotationZ1;
        this.rotationZ2 = rotationZ2;
        this.rotationZRate = rotationZRate;
        this.rotationZRateStart = rotationZRateStart;
    }
    public void setRotationZ1(float rotationZ1) {this.rotationZ1 = rotationZ1;}
    public void setRotationZ2(float rotationZ2) {this.rotationZ2 = rotationZ2;}
    public void setRotationZRate(float rotationZRate) {this.rotationZRate = rotationZRate;}
    public void setRotationZRateStart(int rotationZRateStart) {this.rotationZRateStart = rotationZRateStart;}
}
