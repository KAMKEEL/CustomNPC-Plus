package kamkeel.addon;

import net.minecraft.nbt.NBTTagCompound;

public interface DataDisplaySupport {

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound);
    public void readToNBT(NBTTagCompound nbttagcompound);

}
