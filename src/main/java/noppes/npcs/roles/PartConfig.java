package noppes.npcs.roles;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IModelPart;

public class PartConfig implements IModelPart {
    public Object parent;

    public float rotationX = 0f;
    public float rotationY = 0f;
    public float rotationZ = 0f;
    public float pivotX = 0f;
    public float pivotY = 0f;
    public float pivotZ = 0f;

    public boolean fullAngles = false;
    public boolean animate = false;
    public float animRate = 1.0F;
    public boolean interpolate = true;
    public boolean disabled = false;

    // vvv Client-sided use vvv
    public float[] prevRotations = new float[]{0, 0, 0};
    public float[] prevPivots = new float[]{0, 0, 0};
    public float partialRotationTick = 0f;
    public float partialPivotTick = 0f;
    // ^^^ Client-sided use ^^^

    public PartConfig(Object parent) {
        this.parent = parent;
    }

    public NBTTagCompound writeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setFloat("RotationX", rotationX);
        compound.setFloat("RotationY", rotationY);
        compound.setFloat("RotationZ", rotationZ);
        compound.setFloat("PivotX", pivotX);
        compound.setFloat("PivotY", pivotY);
        compound.setFloat("PivotZ", pivotZ);

        compound.setBoolean("Disabled", disabled);
        compound.setBoolean("PuppetFullAngles", fullAngles);

        compound.setBoolean("PuppetInterpolate", interpolate);
        compound.setBoolean("PuppetAnimate", animate);
        compound.setFloat("PuppetAnimSpeed", animRate);
        return compound;
    }

    public void readNBT(NBTTagCompound compound) {
        rotationX = compound.getFloat("RotationX");
        rotationY = compound.getFloat("RotationY");
        rotationZ = compound.getFloat("RotationZ");
        pivotX = compound.getFloat("PivotX");
        pivotY = compound.getFloat("PivotY");
        pivotZ = compound.getFloat("PivotZ");

        disabled = compound.getBoolean("Disabled");
        fullAngles = compound.getBoolean("PuppetFullAngles");

        if (!compound.hasKey("PuppetInterpolate")) {
            interpolate = true;
        } else {
            interpolate = compound.getBoolean("PuppetInterpolate");
        }
        animate = compound.getBoolean("PuppetAnimate");
        animRate = compound.getFloat("PuppetAnimSpeed");
    }

    public void setEnabled(boolean bo) {
        this.disabled = !bo;
    }

    public boolean isEnabled() {
        return !this.disabled;
    }

    public void setAnimated(boolean animated) {
        this.animate = animated;
    }

    public boolean isAnimated() {
        return this.animate;
    }

    public void setInterpolated(boolean interpolate) {
        this.interpolate = interpolate;
    }

    public boolean isInterpolated() {
        return this.interpolate;
    }

    public void setFullAngles(boolean fullAngles) {
        this.fullAngles = fullAngles;
    }

    public boolean fullAngles() {
        return this.fullAngles;
    }

    public void setAnimRate(float animRate) {
        this.animRate = animRate;
    }

    public float getAnimRate() {
        return this.animRate;
    }

    public void setRotation(float rotationX, float rotationY, float rotationZ) {
        this.setRotationX(rotationX);
        this.setRotationY(rotationY);
        this.setRotationZ(rotationZ);
    }

    public void setRotationX(float rotation) {
        float f = rotation / 360f - 0.5f;
        if (this.getRotationX() != f && parent instanceof JobPuppet)
            ((JobPuppet) parent).npc.script.clientNeedsUpdate = true;
        this.rotationX = f;
    }

    public void setRotationY(float rotation) {
        float f = rotation / 360f - 0.5f;
        if (this.getRotationY() != f && parent instanceof JobPuppet)
            ((JobPuppet) parent).npc.script.clientNeedsUpdate = true;
        this.rotationY = f;
    }

    public void setRotationZ(float rotation) {
        float f = rotation / 360f - 0.5f;
        if (this.getRotationZ() != f && parent instanceof JobPuppet)
            ((JobPuppet) parent).npc.script.clientNeedsUpdate = true;
        this.rotationZ = f;
    }

    public float getRotationX() {
        return this.rotationX;
    }

    public float getRotationY() {
        return this.rotationY;
    }

    public float getRotationZ() {
        return this.rotationZ;
    }

    public void setOffset(float offsetX, float offsetY, float offsetZ) {
        this.setOffsetX(offsetX);
        this.setOffsetY(offsetY);
        this.setOffsetZ(offsetZ);
    }

    public void setOffsetX(float offset) {
        if (this.getOffsetX() != offset && parent instanceof JobPuppet)
            ((JobPuppet) parent).npc.script.clientNeedsUpdate = true;
        this.pivotX = offset;
    }

    public void setOffsetY(float offset) {
        if (this.getOffsetY() != offset && parent instanceof JobPuppet)
            ((JobPuppet) parent).npc.script.clientNeedsUpdate = true;
        this.pivotY = offset;
    }

    public void setOffsetZ(float offset) {
        if (this.getOffsetZ() != offset && parent instanceof JobPuppet)
            ((JobPuppet) parent).npc.script.clientNeedsUpdate = true;
        this.pivotZ = offset;
    }

    public float getOffsetX() {
        return this.pivotX;
    }

    public float getOffsetY() {
        return this.pivotY;
    }

    public float getOffsetZ() {
        return this.pivotZ;
    }
}
