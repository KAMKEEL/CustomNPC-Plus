package noppes.npcs.scripted.interfaces;

import noppes.npcs.NoppesUtilServer;
import noppes.npcs.scripted.ScriptWorld;
import noppes.npcs.scripted.interfaces.entity.IEntity;

public interface IParticle {

    void spawnOnEntity(IEntity entity);

    void spawnInWorld(ScriptWorld world);

    void spawnInWorld(ScriptWorld world, double x, double y, double z);

    void setSize(int width, int height);
    int getWidth();
    int getHeight();

    void setOffset(int offsetX, int offsetY);
    int getOffsetX();
    int getOffsetY();

    void setAnim(int animRate, boolean animLoop, int animStart, int animEnd);
    void setAnimRate(int animRate);
    int getAnimRate();
    void setAnimLoop(boolean animLoop);
    boolean getAnimLoop();
    void setAnimStart(int animStart);
    int getAnimStart();
    void setAnimEnd(int animEnd);
    int getAnimEnd();

    boolean getFacePlayer();
    void setFacePlayer(boolean facePlayer);

    String getDirectory();

    int getAmount();

    int getMaxAge();

    double getX();
    double getY();
    double getZ();

    double getMotionX();
    double getMotionY();
    double getMotionZ();
    float getGravity();

    int getHEXColor1();
    int getHEXColor2();
    float getHEXColorRate();
    int getHEXColorStart();

    float getAlpha1();
    float getAlpha2();
    float getAlphaRate();
    int getAlphaRateStart();

    float getScale1();
    float getScale2();
    float getScaleRate();
    int getScaleRateStart();

    float getRotationX1();
    float getRotationX2();
    float getRotationXRate();
    int getRotationXRateStart();

    float getRotationY1();
    float getRotationY2();
    float getRotationYRate();
    int getRotationYRateStart();

    float getRotationZ1();
    float getRotationZ2();
    float getRotationZRate();
    int getRotationZRateStart();

    void setDirectory(String directory);

    void setAmount(int amount);

    void setMaxAge(int maxAge);

    void setPosition(double x, double y, double z);
    void setX(double x);
    void setY(double y);
    void setZ(double z);

    void setMotion(double motionX, double motionY, double motionZ, float gravity);
    void setMotionX(double motionX);
    void setMotionY(double motionY);
    void setMotionZ(double motionZ);
    void setGravity(float gravity);

    void setHEXColor(int HEXColor, int HEXColor2, float HEXColorRate, int HEXColorStart);

    void setAlpha(float alpha1, float alpha2, float alphaRate, int alphaRateStart);

    void setScale(float scale1, float scale2, float scaleRate, int scaleRateStart);

    void setRotationX(float rotationX1, float rotationX2, float rotationXRate, int rotationXRateStart);

    void setRotationY(float rotationY1, float rotationY2, float rotationYRate, int rotationYRateStart);

    void setRotationZ(float rotationZ1, float rotationZ2, float rotationZRate, int rotationZRateStart);
    
}
