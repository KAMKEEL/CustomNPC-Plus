package noppes.npcs.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.data.IModelScalePart;
import noppes.npcs.util.ValueUtil;

public class ModelScalePart implements IModelScalePart {
    public float scaleX = 1, scaleY = 1, scaleZ = 1;

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setFloat("ScaleX", scaleX);
        compound.setFloat("ScaleY", scaleY);
        compound.setFloat("ScaleZ", scaleZ);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        scaleX = ValueUtil.clamp(compound.getFloat("ScaleX"), 0.5f, 1.5f);
        scaleY = ValueUtil.clamp(compound.getFloat("ScaleY"), 0.5f, 1.5f);
        scaleZ = ValueUtil.clamp(compound.getFloat("ScaleZ"), 0.5f, 1.5f);
    }

    public String toString() {
        return "ScaleX: " + scaleX + " - ScaleY: " + scaleY + " - ScaleZ: " + scaleZ;
    }

    public void setScale(float x, float y) {
        scaleZ = scaleX = x;
        scaleY = y;
    }

    public void setScale(float x, float y, float z) {
        scaleX = ValueUtil.clamp(x, 0.5f, 1.5f);
        scaleY = ValueUtil.clamp(y, 0.5f, 1.5f);
        scaleZ = ValueUtil.clamp(z, 0.5f, 1.5f);
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getScaleZ() {
        return scaleZ;
    }
}
