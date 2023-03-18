package noppes.npcs.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.data.IModelScale;
import noppes.npcs.api.entity.data.IModelScalePart;
import noppes.npcs.util.ValueUtil;

public class ModelScale implements IModelScale {
    public ModelScalePart head = new ModelScalePart();
    public ModelScalePart body = new ModelScalePart();
    public ModelScalePart arms = new ModelScalePart();
    public ModelScalePart legs = new ModelScalePart();

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("HeadConfig", head.writeToNBT());
        compound.setTag("BodyConfig", body.writeToNBT());
        compound.setTag("ArmsConfig", arms.writeToNBT());
        compound.setTag("LegsConfig", legs.writeToNBT());
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        head.readFromNBT(compound.getCompoundTag("HeadConfig"));
        body.readFromNBT(compound.getCompoundTag("BodyConfig"));
        arms.readFromNBT(compound.getCompoundTag("ArmsConfig"));
        legs.readFromNBT(compound.getCompoundTag("LegsConfig"));
    }

    @Override
    public IModelScalePart getPart(int part) {
        switch (ValueUtil.clamp(part,0,3)) {
            case 0:
                return head;
            case 1:
                return body;
            case 2:
                return arms;
            case 3:
                return legs;
        }
        return null;
    }
}
