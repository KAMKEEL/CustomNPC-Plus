package noppes.npcs.wrapper.nbt;

import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;

/**
 * 1.7.10 implementation of INBTList.
 * Thin wrapper that delegates to NBTTagList.
 */
public class MC1710NBTList implements INBTList {

    private final NBTTagList tag;

    public MC1710NBTList(NBTTagList tag) {
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
    public INBTCompound getCompound(int index) {
        return new MC1710NBTCompound(tag.getCompoundTagAt(index));
    }

    @Override
    public String getString(int index) {
        return tag.getStringTagAt(index);
    }

    @Override
    public int getInt(int index) {
        // 1.7.10 doesn't have a direct getIntAt on NBTTagList.
        // Parse from the string representation or use the compound approach.
        // In practice, int lists are stored as compounds with "Slot" keys.
        // For direct int tag lists, use getStringTagAt and parse.
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
    public void addCompound(INBTCompound compound) {
        tag.appendTag(((MC1710NBTCompound) compound).getMCTag());
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
    public Object getUnderlyingTag() {
        return tag;
    }
}
