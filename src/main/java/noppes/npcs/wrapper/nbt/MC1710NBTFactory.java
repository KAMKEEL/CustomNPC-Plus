package noppes.npcs.wrapper.nbt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;
import noppes.npcs.platform.nbt.NBTFactory;

/**
 * 1.7.10 implementation of NBTFactory.
 */
public class MC1710NBTFactory implements NBTFactory {

    @Override
    public INBTCompound createCompound() {
        return new MC1710NBTCompound(new NBTTagCompound());
    }

    @Override
    public INBTList createList() {
        return new MC1710NBTList(new NBTTagList());
    }

    @Override
    public INBTCompound wrap(Object mcNBTTagCompound) {
        if (!(mcNBTTagCompound instanceof NBTTagCompound)) {
            throw new IllegalArgumentException(
                "Expected NBTTagCompound, got " + mcNBTTagCompound.getClass().getName()
            );
        }
        return new MC1710NBTCompound((NBTTagCompound) mcNBTTagCompound);
    }

    @Override
    public INBTList wrapList(Object mcNBTTagList) {
        if (!(mcNBTTagList instanceof NBTTagList)) {
            throw new IllegalArgumentException(
                "Expected NBTTagList, got " + mcNBTTagList.getClass().getName()
            );
        }
        return new MC1710NBTList((NBTTagList) mcNBTTagList);
    }
}
