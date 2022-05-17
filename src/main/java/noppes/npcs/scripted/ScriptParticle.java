package noppes.npcs.scripted;

import noppes.npcs.NoppesUtilServer;
import noppes.npcs.scripted.interfaces.IParticle;
import noppes.npcs.scripted.interfaces.entity.IEntity;

public class ScriptParticle implements IParticle {
    private String directory;

    private int HEXColor = 0xFFFFFF;
    private int HEXColor2 = 0xFFFFFF;
    private float HEXColorRate = 0.0f;
    private int HEXColorStart = 0;

    private int amount = 1;
    private int maxAge = 20;
    private double x = 0,y = 0,z = 0;
    private double motionX = 0,motionY = 0,motionZ = 0;
    private float gravity = 0;
    private float scale1 = 1.0f,scale2 = 1.0f,scaleRate = 0.0f;
    private float alpha1 = 1.0f,alpha2 = 1.0f,alphaRate = 0.0f;
    private int scaleRateStart = 0, alphaRateStart = 0;

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

    public int width = -1, height = -1;
    public int offsetX = 0, offsetY = 0;

    public int animRate = 0;
    public boolean animLoop = true;
    public int animStart = 0, animEnd = -1;

    public boolean facePlayer = true;

    public ScriptParticle(String directory){
        this.directory = directory;
    }

    public void spawnOnEntity(IEntity entity){
        int entityID = entity.getMCEntity().getEntityId();
        NoppesUtilServer.spawnScriptedParticle(directory,
                HEXColor, HEXColor2, HEXColorRate, HEXColorStart,
                amount, maxAge,
                x, y, z,
                motionX, motionY, motionZ, gravity,
                scale1, scale2, scaleRate, scaleRateStart,
                alpha1, alpha2, alphaRate, alphaRateStart,
                rotationX1, rotationX2, rotationXRate, rotationXRateStart,
                rotationY1, rotationY2, rotationYRate, rotationYRateStart,
                rotationZ1, rotationZ2, rotationZRate, rotationZRateStart,
                facePlayer, width, height, offsetX, offsetY,
                animRate, animLoop, animStart, animEnd,
                entityID, entity.getWorld().getDimensionID()
        );
    }

    public void spawnInWorld(ScriptWorld world){
        NoppesUtilServer.spawnScriptedParticle(directory,
                HEXColor, HEXColor2, HEXColorRate, HEXColorStart,
                amount, maxAge,
                x, y, z,
                motionX, motionY, motionZ, gravity,
                scale1, scale2, scaleRate, scaleRateStart,
                alpha1, alpha2, alphaRate, alphaRateStart,
                rotationX1, rotationX2, rotationXRate, rotationXRateStart,
                rotationY1, rotationY2, rotationYRate, rotationYRateStart,
                rotationZ1, rotationZ2, rotationZRate, rotationZRateStart,
                facePlayer, width, height, offsetX, offsetY,
                animRate, animLoop, animStart, animEnd,
                -1, world.getDimensionID()
        );
    }

    public void spawnInWorld(ScriptWorld world, double x, double y, double z){
        NoppesUtilServer.spawnScriptedParticle(directory,
                HEXColor, HEXColor2, HEXColorRate, HEXColorStart,
                amount, maxAge,
                x, y, z,
                motionX, motionY, motionZ, gravity,
                scale1, scale2, scaleRate, scaleRateStart,
                alpha1, alpha2, alphaRate, alphaRateStart,
                rotationX1, rotationX2, rotationXRate, rotationXRateStart,
                rotationY1, rotationY2, rotationYRate, rotationYRateStart,
                rotationZ1, rotationZ2, rotationZRate, rotationZRateStart,
                facePlayer, width, height, offsetX, offsetY,
                animRate, animLoop, animStart, animEnd,
                -1, world.getDimensionID()
        );
    }

    public void setFacePlayer(boolean facePlayer) { this.facePlayer = facePlayer; }
    public boolean getFacePlayer() { return facePlayer; }

    public String getDirectory() {
        return directory;
    }
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
    public int getAmount() {
        return amount;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }
    public int getMaxAge() {
        return maxAge;
    }

    public void setSize(int width, int height){
        this.width = width;
        this.height = height;
    }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setOffset(int offsetX, int offsetY){
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }

    public void setAnim(int animRate, boolean animLoop, int animStart, int animEnd){
        this.animRate = animRate;
        this.animLoop = animLoop;
        this.animStart = animStart;
        this.animEnd = animEnd;
    }
    public int getAnimRate() { return animRate; }
    public boolean getAnimLoop() { return animLoop; }
    public int getAnimStart() { return animStart; }
    public int getAnimEnd() { return animEnd; }

    public void setPosition(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
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

    public void setMotion(double motionX, double motionY, double motionZ, float gravity){
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.gravity = gravity;
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

    public void setHEXColor(int HEXColor, int HEXColor2, float HEXColorRate, int HEXColorStart) {
        this.HEXColor = HEXColor;
        this.HEXColor2 = HEXColor2;
        this.HEXColorRate = HEXColorRate;
        this.HEXColorStart = HEXColorStart;
    }
    public int getHEXColor1() {
        return HEXColor;
    }
    public int getHEXColor2() {
        return HEXColor2;
    }
    public float getHEXColorRate() {
        return HEXColorRate;
    }
    public int getHEXColorStart() {
        return HEXColorStart;
    }

    public void setAlpha(float alpha1, float alpha2, float alphaRate, int alphaRateStart){
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
        this.alphaRate = alphaRate;
        this.alphaRateStart = alphaRateStart;
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

    public void setScale(float scale1, float scale2, float scaleRate, int scaleRateStart){
        this.scale1 = scale1;
        this.scale2 = scale2;
        this.scaleRate = scaleRate;
        this.scaleRateStart = scaleRateStart;
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

    public void setRotationX(float rotationX1, float rotationX2, float rotationXRate, int rotationXRateStart){
        this.rotationX1 = rotationX1;
        this.rotationX2 = rotationX2;
        this.rotationXRate = rotationXRate;
        this.rotationXRateStart = rotationXRateStart;
    }
    public float getRotationX1() {return rotationX1;}
    public float getRotationX2() {return rotationX2;}
    public float getRotationXRate() {return rotationXRate;}
    public int getRotationXRateStart() {return rotationXRateStart;}

    public void setRotationY(float rotationY1, float rotationY2, float rotationYRate, int rotationYRateStart){
        this.rotationY1 = rotationY1;
        this.rotationY2 = rotationY2;
        this.rotationYRate = rotationYRate;
        this.rotationYRateStart = rotationYRateStart;
    }
    public float getRotationY1() {return rotationY1;}
    public float getRotationY2() {return rotationY2;}
    public float getRotationYRate() {return rotationYRate;}
    public int getRotationYRateStart() {return rotationYRateStart;}

    public void setRotationZ(float rotationZ1, float rotationZ2, float rotationZRate, int rotationZRateStart){
        this.rotationZ1 = rotationZ1;
        this.rotationZ2 = rotationZ2;
        this.rotationZRate = rotationZRate;
        this.rotationZRateStart = rotationZRateStart;
    }
    public float getRotationZ1() {return rotationZ1;}
    public float getRotationZ2() {return rotationZ2;}
    public float getRotationZRate() {return rotationZRate;}
    public int getRotationZRateStart() {return rotationZRateStart;}

    @Deprecated
    public void setHEXColor(int HEXColor) {
        this.HEXColor = HEXColor;
    }
    @Deprecated
    public int getHEXColor() {
        return HEXColor;
    }
}
