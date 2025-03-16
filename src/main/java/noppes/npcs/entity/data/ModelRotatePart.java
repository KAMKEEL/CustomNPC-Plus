package noppes.npcs.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.data.IModelRotatePart;
import noppes.npcs.util.ValueUtil;

public class ModelRotatePart implements IModelRotatePart {
    public float rotationX = 0f, rotationY = 0f, rotationZ = 0f;
    public boolean disabled = false;

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("Disabled", disabled);
        compound.setFloat("RotationX", rotationX);
        compound.setFloat("RotationY", rotationY);
        compound.setFloat("RotationZ", rotationZ);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        disabled = compound.getBoolean("Disabled");
        rotationX = ValueUtil.clamp(compound.getFloat("RotationX"), -0.5f, 0.5f);
        rotationY = ValueUtil.clamp(compound.getFloat("RotationY"), -0.5f, 0.5f);
        rotationZ = ValueUtil.clamp(compound.getFloat("RotationZ"), -0.5f, 0.5f);
    }

    public void setRotation(float x, float y, float z) {
        this.rotationX = ValueUtil.clamp(x, -0.5f, 0.5f);
        this.rotationY = ValueUtil.clamp(y, -0.5f, 0.5f);
        this.rotationZ = ValueUtil.clamp(z, -0.5f, 0.5f);
    }

    public float getRotateX() {
        return this.rotationX;
    }

    public float getRotateY() {
        return this.rotationY;
    }

    public float getRotateZ() {
        return this.rotationZ;
    }

    public void disabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean disabled() {
        return this.disabled;
    }
}
