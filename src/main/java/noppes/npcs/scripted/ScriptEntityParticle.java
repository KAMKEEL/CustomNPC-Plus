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

    public ScriptEntityParticle(String directory){
        this.directory = directory;
    }

    public void spawnOnEntity(ScriptEntity entity){
        int entityID = entity.getMCEntity().getEntityId();
        NoppesUtilServer.spawnScriptedParticle(entity.getMCEntity(), directory, HEXcolor, amount, maxAge,
                x, y, z,
                motionX, motionY, motionZ, gravity,
                scale1, scale2, scaleRate, scaleRateStart,
                alpha1, alpha2, alphaRate, alphaRateStart,
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

    public float getScale1() {
        return scale1;
    }

    public float getScale2() {
        return scale2;
    }

    public float getScaleRate() {
        return scaleRate;
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

    public int getScaleRateStart() {
        return scaleRateStart;
    }

    public int getAlphaRateStart() {
        return alphaRateStart;
    }

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
}
