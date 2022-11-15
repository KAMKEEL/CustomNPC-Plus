package noppes.npcs.scripted;

import net.minecraft.nbt.*;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.IParticle;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

import java.lang.reflect.Field;

public class ScriptParticle implements IParticle {
    public String directory;

    public int HEXColor = 0xFFFFFF;
    public int HEXColor2 = 0xFFFFFF;
    public float HEXColorRate = 0.0f;
    public int HEXColorStart = 0;

    public int amount = 1;
    public int maxAge = 20;
    public double x = 0,y = 0,z = 0;
    public double motionX = 0,motionY = 0,motionZ = 0;
    public float gravity = 0;
    public float scale1 = 20.0f,scale2 = 20.0f,scaleRate = 0.0f;
    public float alpha1 = 1.0f,alpha2 = 1.0f,alphaRate = 0.0f;
    public int scaleRateStart = 0, alphaRateStart = 0;

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
    public boolean glows = true;
    public boolean noClip = true;

    public ScriptParticle(String directory){
        this.directory = directory;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        ScriptParticle newParticle = new ScriptParticle("");

        //Iterate through fields, check if they're not equal to a new object, write to NBT
        for (Field field : ScriptParticle.class.getDeclaredFields()) {
            try {
                if (!field.get(this).equals(field.get(newParticle))) {
                    compound = writeNBTTag(compound, field.getType(), field.getName(), field.get(this));
                }
            } catch (Exception ignored) {}
        }

        return compound;
    }

    public NBTTagCompound writeNBTTag(NBTTagCompound compound, Class<?> c, String key, Object value) {
        if (c == boolean.class) {
            compound.setBoolean(key, (Boolean) value);
        }
        if (c == int.class) {
            compound.setInteger(key, (Integer) value);
        }
        if (c == double.class) {
            compound.setDouble(key, (Double) value);
        }
        if (c == float.class) {
            compound.setFloat(key, (Float) value);
        }
        if (c == String.class) {
            compound.setString(key, (String) value);
        }

        return compound;
    }

    private static Object readNBTTag(NBTTagCompound compound, String key) {
        if (compound.getTag(key) instanceof NBTTagByte) {
            return compound.getBoolean(key);
        }
        if (compound.getTag(key) instanceof NBTTagInt) {
            return compound.getInteger(key);
        }
        if (compound.getTag(key) instanceof NBTTagDouble) {
            return compound.getDouble(key);
        }
        if (compound.getTag(key) instanceof NBTTagFloat) {
            return compound.getFloat(key);
        }
        if (compound.getTag(key) instanceof NBTTagString) {
            return compound.getString(key);
        }

        return null;
    }

    public static ScriptParticle fromNBT(NBTTagCompound compound) {
        ScriptParticle particle = new ScriptParticle(compound.getString("directory"));

        //Iterate through fields, check if compound has a key equal to the field's name, and if so set the field equal to that value from NBT.
        for (Field field : ScriptParticle.class.getDeclaredFields()) {
            try {
                if (compound.hasKey(field.getName())) {
                    Object val = ScriptParticle.readNBTTag(compound,field.getName());
                    if (val != null) {
                        field.set(particle, val);
                    }
                }
            } catch (Exception ignored) {}
        }

        return particle;
    }

    public void spawn(IEntity entity){
        int entityID = entity.getMCEntity().getEntityId();

        NBTTagCompound compound = this.writeToNBT();
        compound.setInteger("EntityID", entityID);

        NoppesUtilServer.spawnScriptedParticle(compound, entity.getWorld().getDimensionID());
    }

    public void spawn(IWorld world){
        NBTTagCompound compound = this.writeToNBT();
        NoppesUtilServer.spawnScriptedParticle(compound, world.getDimensionID());
    }

    public void spawn(IWorld world, double x, double y, double z){
        NBTTagCompound compound = this.writeToNBT();
        NoppesUtilServer.spawnScriptedParticle(compound, world.getDimensionID());
    }

    public void spawnOnEntity(IEntity entity) {
        spawn(entity);
    }

    public void spawnInWorld(IWorld world) {
        spawn(world);
    }

    public void spawnInWorld(IWorld world, double x, double y, double z){
        spawn(world, x, y, z);
    }

    public void setGlows(boolean glows) { this.glows = glows; }
    public boolean getGlows() { return this.glows; }

    public void setNoClip(boolean noClip) { this.noClip = noClip; }
    public boolean getNoClip() { return this.noClip; }

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

    public void setPosition(IPos pos) {
        this.setPosition(pos.getX(),pos.getY(),pos.getZ());
    }
    public void getPos() {
        NpcAPI.Instance().getIPos(x,y,z);
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
