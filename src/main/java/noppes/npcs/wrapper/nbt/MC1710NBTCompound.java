package noppes.npcs.wrapper.nbt;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;

import java.util.Set;

/**
 * 1.7.10 implementation of INBTCompound.
 * Thin wrapper that delegates to NBTTagCompound.
 */
public class MC1710NBTCompound implements INBTCompound {

    private final NBTTagCompound tag;

    public MC1710NBTCompound(NBTTagCompound tag) {
        this.tag = tag;
    }

    /**
     * Direct access to the underlying MC tag.
     * For use within 1.7.10 code only — avoids the cast from getUnderlyingTag().
     */
    public NBTTagCompound getMCTag() {
        return tag;
    }

    // --- Setters ---

    @Override
    public void setString(String key, String value) {
        tag.setString(key, value);
    }

    @Override
    public void setInteger(String key, int value) {
        tag.setInteger(key, value);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        tag.setBoolean(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        tag.setDouble(key, value);
    }

    @Override
    public void setFloat(String key, float value) {
        tag.setFloat(key, value);
    }

    @Override
    public void setLong(String key, long value) {
        tag.setLong(key, value);
    }

    @Override
    public void setShort(String key, short value) {
        tag.setShort(key, value);
    }

    @Override
    public void setByte(String key, byte value) {
        tag.setByte(key, value);
    }

    @Override
    public void setByteArray(String key, byte[] value) {
        tag.setByteArray(key, value);
    }

    @Override
    public void setIntArray(String key, int[] value) {
        tag.setIntArray(key, value);
    }

    @Override
    public void setCompound(String key, INBTCompound compound) {
        tag.setTag(key, ((MC1710NBTCompound) compound).getMCTag());
    }

    @Override
    public void setTag(String key, INBTCompound compound) {
        setCompound(key, compound);
    }

    @Override
    public void setList(String key, INBTList list) {
        tag.setTag(key, (NBTTagList) list.getUnderlyingTag());
    }

    // --- Getters ---

    @Override
    public String getString(String key) {
        return tag.getString(key);
    }

    @Override
    public int getInteger(String key) {
        return tag.getInteger(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return tag.getBoolean(key);
    }

    @Override
    public double getDouble(String key) {
        return tag.getDouble(key);
    }

    @Override
    public float getFloat(String key) {
        return tag.getFloat(key);
    }

    @Override
    public long getLong(String key) {
        return tag.getLong(key);
    }

    @Override
    public short getShort(String key) {
        return tag.getShort(key);
    }

    @Override
    public byte getByte(String key) {
        return tag.getByte(key);
    }

    @Override
    public byte[] getByteArray(String key) {
        return tag.getByteArray(key);
    }

    @Override
    public int[] getIntArray(String key) {
        return tag.getIntArray(key);
    }

    @Override
    public INBTCompound getCompound(String key) {
        return new MC1710NBTCompound(tag.getCompoundTag(key));
    }

    @Override
    public INBTList getList(String key, int type) {
        return new MC1710NBTList(tag.getTagList(key, type));
    }

    // --- Query ---

    @Override
    public boolean hasKey(String key) {
        return tag.hasKey(key);
    }

    @Override
    public boolean hasKey(String key, int type) {
        return tag.hasKey(key, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getKeySet() {
        return (Set<String>) tag.func_150296_c();
    }

    @Override
    public int getTagType(String key) {
        return tag.func_150299_b(key);
    }

    @Override
    public void removeTag(String key) {
        tag.removeTag(key);
    }

    @Override
    public boolean isEmpty() {
        return tag.hasNoTags();
    }

    // --- Interop ---

    @Override
    public void merge(INBTCompound other) {
        NBTTagCompound otherTag = ((MC1710NBTCompound) other).getMCTag();
        for (Object keyObj : otherTag.func_150296_c()) {
            String key = (String) keyObj;
            tag.setTag(key, otherTag.getTag(key).copy());
        }
    }

    @Override
    public INBTCompound copy() {
        return new MC1710NBTCompound((NBTTagCompound) tag.copy());
    }

    @Override
    public Object getUnderlyingTag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MC1710NBTCompound) {
            return tag.equals(((MC1710NBTCompound) obj).tag);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }
}
