package noppes.npcs.client.gui.custom.interfaces;

import net.minecraft.nbt.NBTTagCompound;

public interface IDataHolder extends IGuiComponent {
    NBTTagCompound toNBT();
}
