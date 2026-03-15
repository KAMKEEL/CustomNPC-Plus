package noppes.npcs.scripted;

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
import noppes.npcs.wrapper.nbt.NBTListWrapper;
import noppes.npcs.wrapper.nbt.NBTWrapper;

import java.util.Iterator;
import java.util.Set;

public class ScriptNbt implements INbt {
    private NBTTagCompound compound;

    public ScriptNbt(NBTTagCompound compound) {
        this.compound = compound;
    }

    // ======================== Setters ========================

    @Override
    public void setString(String key, String value) {
        this.compound.setString(key, value);
    }

    @Override
    public void setInteger(String key, int value) {
        this.compound.setInteger(key, value);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        this.compound.setBoolean(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        this.compound.setDouble(key, value);
    }

    @Override
    public void setFloat(String key, float value) {
        this.compound.setFloat(key, value);
    }

    @Override
    public void setLong(String key, long value) {
        this.compound.setLong(key, value);
    }

    @Override
    public void setShort(String key, short value) {
        this.compound.setShort(key, value);
    }

    @Override
    public void setByte(String key, byte value) {
        this.compound.setByte(key, value);
    }

    @Override
    public void setByteArray(String key, byte[] value) {
        this.compound.setByteArray(key, value);
    }

    @Override
    public void setIntArray(String key, int[] value) {
        this.compound.setIntArray(key, value);
    }

    @Override
    public void setCompound(String key, INbt value) {
        if (value == null) {
            throw new CustomNPCsException("Value cant be null", new Object[0]);
        }
        this.compound.setTag(key, value.getMCNBT());
    }

    @Override
    public void setTagList(String key, INbtList list) {
        this.compound.setTag(key, (NBTTagList) list.getMCTagList());
    }

    @Override
    public void setList(String key, Object[] value) {
        NBTTagList list = new NBTTagList();
        for (Object nbt : value) {
            if (nbt instanceof INbt) {
                list.appendTag(((INbt) nbt).getMCNBT());
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
        this.compound.setTag(key, list);
    }

    // ======================== Getters ========================

    @Override
    public String getString(String key) {
        return this.compound.getString(key);
    }

    @Override
    public int getInteger(String key) {
        return this.compound.getInteger(key);
    }

    @Override
    public boolean getBoolean(String key) {
        return this.compound.getBoolean(key);
    }

    @Override
    public double getDouble(String key) {
        return this.compound.getDouble(key);
    }

    @Override
    public float getFloat(String key) {
        return this.compound.getFloat(key);
    }

    @Override
    public long getLong(String key) {
        return this.compound.getLong(key);
    }

    @Override
    public short getShort(String key) {
        return this.compound.getShort(key);
    }

    @Override
    public byte getByte(String key) {
        return this.compound.getByte(key);
    }

    @Override
    public byte[] getByteArray(String key) {
        return this.compound.getByteArray(key);
    }

    @Override
    public int[] getIntArray(String key) {
        return this.compound.getIntArray(key);
    }

    @Override
    public INbt getCompound(String key) {
        return new ScriptNbt(this.compound.getCompoundTag(key));
    }

    @Override
    public INbtList getTagList(String key, int type) {
        return new NBTListWrapper(this.compound.getTagList(key, type));
    }

    @Override
    public Object[] getList(String key, int type) {
        NBTTagList list = this.compound.getTagList(key, type);
        Object[] nbts = new Object[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) {
            int elemType = list.func_150303_d();
            if (elemType == 10) {
                nbts[i] = new ScriptNbt(list.getCompoundTagAt(i));
            } else if (elemType == 8) {
                nbts[i] = list.getStringTagAt(i);
            } else if (elemType == 6) {
                nbts[i] = list.func_150309_d(i);
            } else if (elemType == 5) {
                nbts[i] = list.func_150308_e(i);
            } else if (elemType == 3) {
                nbts[i] = Integer.parseInt(list.getStringTagAt(i));
            } else if (elemType == 11) {
                nbts[i] = list.func_150306_c(i);
            }
        }
        return nbts;
    }

    @Override
    public int getListType(String key) {
        NBTBase b = this.compound.getTag(key);
        if (b == null) {
            return 0;
        } else if (b.getId() != 9) {
            throw new CustomNPCsException("NBT tag " + key + " isn\'t a list", new Object[0]);
        }
        return ((NBTTagList) b).func_150303_d();
    }

    // ======================== Query ========================

    @Override
    public boolean hasKey(String key) {
        return this.compound.hasKey(key);
    }

    @Override
    public boolean hasKey(String key, int type) {
        return this.compound.hasKey(key, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getKeySet() {
        return (Set<String>) this.compound.func_150296_c();
    }

    @Override
    public void removeTag(String key) {
        this.compound.removeTag(key);
    }

    @Override
    public int getTagType(String key) {
        return this.compound.func_150299_b(key);
    }

    @Override
    public boolean isEmpty() {
        return this.compound.hasNoTags();
    }

    // ======================== Interop ========================

    @Override
    public void merge(INbt other) {
        NBTTagCompound otherTag = other.getMCNBT();
        for (Object keyObj : otherTag.func_150296_c()) {
            String key = (String) keyObj;
            this.compound.setTag(key, otherTag.getTag(key).copy());
        }
    }

    @Override
    public INbt copy() {
        return new ScriptNbt((NBTTagCompound) this.compound.copy());
    }

    @Override
    public NBTTagCompound getMCNBT() {
        return this.compound;
    }

    // ======================== Convenience ========================

    @Override
    public String toJsonString() {
        return NBTJsonUtil.Convert(this.compound);
    }

    @Override
    public boolean isEqual(INbt nbt) {
        return nbt != null && this.compound.equals(nbt.getMCNBT());
    }

    @Override
    public void clear() {
        Iterator<?> it = this.compound.func_150296_c().iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }
}
