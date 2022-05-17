package noppes.npcs.scripted.interfaces;

import noppes.npcs.NoppesUtilServer;
import noppes.npcs.scripted.ScriptWorld;
import noppes.npcs.scripted.interfaces.entity.IEntity;

public interface IParticle {

    void spawnOnEntity(IEntity entity);

    void spawnInWorld(ScriptWorld world);

    void spawnInWorld(ScriptWorld world, double x, double y, double z);

    void setFacePlayer(boolean facePlayer);
    boolean getFacePlayer();

    void setDirectory(String directory);
    String getDirectory();

    void setAmount(int amount);
    int getAmount();

    void setMaxAge(int maxAge);
    int getMaxAge();

    void setSize(int width, int height);
    int getWidth();
    int getHeight();

    void setOffset(int offsetX, int offsetY);
    int getOffsetX();
    int getOffsetY();

    void setAnim(int animRate, boolean animLoop, int animStart, int animEnd);
    int getAnimRate();
    boolean getAnimLoop();
    int getAnimStart();
    int getAnimEnd();

    void setPosition(double x, double y, double z);
    double getX();
    double getY();
    double getZ();

    void setMotion(double motionX, double motionY, double motionZ, float gravity);
    double getMotionX();
    double getMotionY();
    double getMotionZ();
    float getGravity();

    void setHEXColor(int HEXColor, int HEXColor2, float HEXColorRate, int HEXColorStart);
    int getHEXColor1();
    int getHEXColor2();
    float getHEXColorRate();
    int getHEXColorStart();

    void setAlpha(float alpha1, float alpha2, float alphaRate, int alphaRateStart);
    float getAlpha1();
    float getAlpha2();
    float getAlphaRate();
    int getAlphaRateStart();

    void setScale(float scale1, float scale2, float scaleRate, int scaleRateStart);
    float getScale1();
    float getScale2();
    float getScaleRate();
    int getScaleRateStart();

    void setRotationX(float rotationX1, float rotationX2, float rotationXRate, int rotationXRateStart);
    float getRotationX1();
    float getRotationX2();
    float getRotationXRate();
    int getRotationXRateStart();

    void setRotationY(float rotationY1, float rotationY2, float rotationYRate, int rotationYRateStart);
    float getRotationY1();
    float getRotationY2();
    float getRotationYRate();
    int getRotationYRateStart();

    void setRotationZ(float rotationZ1, float rotationZ2, float rotationZRate, int rotationZRateStart);
    float getRotationZ1();
    float getRotationZ2();
    float getRotationZRate();
    int getRotationZRateStart();
    
}
