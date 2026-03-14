package noppes.npcs.wrapper.nbt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.platform.nbt.NBTFactory;

/**
 * 1.7.10 implementation of NBTFactory.
 */
public class NBTWrapperFactory implements NBTFactory {

    @Override
    public INbt createCompound() {
        return new NBTWrapper(new NBTTagCompound());
    }

    @Override
    public INbtList createList() {
        return new NBTListWrapper(new NBTTagList());
    }

    @Override
    public INbt wrap(Object mcNBTTagCompound) {
        if (!(mcNBTTagCompound instanceof NBTTagCompound)) {
            throw new IllegalArgumentException(
                "Expected NBTTagCompound, got " + mcNBTTagCompound.getClass().getName()
            );
        }
        return new NBTWrapper((NBTTagCompound) mcNBTTagCompound);
    }

    @Override
    public INbtList wrapList(Object mcNBTTagList) {
        if (!(mcNBTTagList instanceof NBTTagList)) {
            throw new IllegalArgumentException(
                "Expected NBTTagList, got " + mcNBTTagList.getClass().getName()
            );
        }
        return new NBTListWrapper((NBTTagList) mcNBTTagList);
    }
}
