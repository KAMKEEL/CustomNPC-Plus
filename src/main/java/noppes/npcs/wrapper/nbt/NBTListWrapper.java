package noppes.npcs.wrapper.nbt;

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;

/**
 * 1.7.10 implementation of INbtList.
 * Thin wrapper that delegates to NBTTagList.
 */
public class NBTListWrapper implements INbtList {

    private final NBTTagList tag;

    public NBTListWrapper(NBTTagList tag) {
        this.tag = tag;
    }

    /**
     * Direct access to the underlying MC tag.
     * For use within 1.7.10 code only.
     */
    public NBTTagList getMCTag() {
        return tag;
    }

    // --- Getters ---

    @Override
    public int size() {
        return tag.tagCount();
    }

    @Override
    public INbt getCompound(int index) {
        return new NBTWrapper(tag.getCompoundTagAt(index));
    }

    @Override
    public String getString(int index) {
        return tag.getStringTagAt(index);
    }

    @Override
    public int getInt(int index) {
        return Integer.parseInt(tag.getStringTagAt(index));
    }

    @Override
    public double getDouble(int index) {
        return tag.func_150309_d(index);
    }

    @Override
    public float getFloat(int index) {
        return tag.func_150308_e(index);
    }

    @Override
    public int[] getIntArray(int index) {
        return tag.func_150306_c(index);
    }

    @Override
    public int getElementType() {
        return tag.func_150303_d();
    }

    // --- Mutators ---

    @Override
    public void addCompound(INbt compound) {
        tag.appendTag(((NBTWrapper) compound).getMCTag());
    }

    @Override
    public void addString(String value) {
        tag.appendTag(new NBTTagString(value));
    }

    @Override
    public void addInt(int value) {
        tag.appendTag(new NBTTagInt(value));
    }

    @Override
    public void addDouble(double value) {
        tag.appendTag(new NBTTagDouble(value));
    }

    @Override
    public void remove(int index) {
        tag.removeTag(index);
    }

    // --- Interop ---

    @Override
    public Object getMCTagList() {
        return tag;
    }
}
