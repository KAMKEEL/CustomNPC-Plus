package noppes.npcs.wrapper.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.util.NBTJsonUtil;

import java.util.Iterator;
import java.util.Set;

/**
 * 1.7.10 implementation of the unified INbt interface.
 * Thin wrapper that delegates to NBTTagCompound.
 */
public class NBTWrapper implements INbt {

    private final NBTTagCompound tag;

    public NBTWrapper(NBTTagCompound tag) {
        this.tag = tag;
    }

    /**
     * Direct access to the underlying MC tag.
     * For use within 1.7.10 code only — avoids casting from getMCNBT().
     */
    public NBTTagCompound getMCTag() {
        return tag;
    }

    // ======================== Setters ========================

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
    public void setCompound(String key, INbt compound) {
        tag.setTag(key, ((NBTWrapper) compound).getMCTag());
    }

    @Override
    public void setTagList(String key, INbtList list) {
        tag.setTag(key, (NBTTagList) list.getMCTagList());
    }

    // ======================== Getters ========================

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
    public INbt getCompound(String key) {
        return new NBTWrapper(tag.getCompoundTag(key));
    }

    @Override
    public INbtList getTagList(String key, int type) {
        return new NBTListWrapper(tag.getTagList(key, type));
    }

    @Override
    public Object[] getList(String key, int type) {
        NBTTagList list = tag.getTagList(key, type);
        Object[] result = new Object[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            int elemType = list.func_150303_d();
            if (elemType == 10) {
                result[i] = new NBTWrapper(list.getCompoundTagAt(i));
            } else if (elemType == 8) {
                result[i] = list.getStringTagAt(i);
            } else if (elemType == 6) {
                result[i] = list.func_150309_d(i);
            } else if (elemType == 5) {
                result[i] = list.func_150308_e(i);
            } else if (elemType == 3) {
                result[i] = Integer.parseInt(list.getStringTagAt(i));
            } else if (elemType == 11) {
                result[i] = list.func_150306_c(i);
            }
        }
        return result;
    }

    @Override
    public void setList(String key, Object[] value) {
        NBTTagList list = new NBTTagList();
        for (Object nbt : value) {
            if (nbt instanceof INbt) {
                list.appendTag((NBTTagCompound) ((INbt) nbt).getMCNBT());
            } else if (nbt instanceof String) {
                list.appendTag(new NBTTagString((String) nbt));
            } else if (nbt instanceof Double) {
                list.appendTag(new NBTTagDouble((Double) nbt));
            } else if (nbt instanceof Float) {
                list.appendTag(new NBTTagFloat((Float) nbt));
            } else if (nbt instanceof Integer) {
                list.appendTag(new NBTTagInt((Integer) nbt));
            } else if (nbt instanceof int[]) {
                list.appendTag(new NBTTagIntArray((int[]) nbt));
            }
        }
        tag.setTag(key, list);
    }

    @Override
    public int getListType(String key) {
        NBTBase b = tag.getTag(key);
        if (b == null) {
            return 0;
        } else if (b.getId() != 9) {
            return 0;
        }
        return ((NBTTagList) b).func_150303_d();
    }

    // ======================== Query ========================

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

    // ======================== Interop ========================

    @Override
    public void merge(INbt other) {
        NBTTagCompound otherTag = ((NBTWrapper) other).getMCTag();
        for (Object keyObj : otherTag.func_150296_c()) {
            String key = (String) keyObj;
            tag.setTag(key, otherTag.getTag(key).copy());
        }
    }

    @Override
    public INbt copy() {
        return new NBTWrapper((NBTTagCompound) tag.copy());
    }

    @Override
    public NBTTagCompound getMCNBT() {
        return tag;
    }

    // ======================== Convenience ========================

    @Override
    public String toJsonString() {
        return NBTJsonUtil.Convert(tag);
    }

    @Override
    public boolean isEqual(INbt nbt) {
        return nbt != null && tag.equals(nbt.getMCNBT());
    }

    @Override
    public void clear() {
        Iterator<?> it = tag.func_150296_c().iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NBTWrapper) {
            return tag.equals(((NBTWrapper) obj).tag);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }
}
