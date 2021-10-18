//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

import java.util.Iterator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.scripted.interfaces.INbt;
import noppes.npcs.util.NBTJsonUtil;

public class ScriptNbt implements INbt {
    private NBTTagCompound compound;

    public ScriptNbt(NBTTagCompound compound) {
        this.compound = compound;
    }

    public void remove(String key) {
        this.compound.removeTag(key);
    }

    public boolean has(String key) {
        return this.compound.hasKey(key);
    }

    public boolean getBoolean(String key) {
        return this.compound.getBoolean(key);
    }

    public void setBoolean(String key, boolean value) {
        this.compound.setBoolean(key, value);
    }

    public short getShort(String key) {
        return this.compound.getShort(key);
    }

    public void setShort(String key, short value) {
        this.compound.setShort(key, value);
    }

    public int getInteger(String key) {
        return this.compound.getInteger(key);
    }

    public void setInteger(String key, int value) {
        this.compound.setInteger(key, value);
    }

    public byte getByte(String key) {
        return this.compound.getByte(key);
    }

    public void setByte(String key, byte value) {
        this.compound.setByte(key, value);
    }

    public long getLong(String key) {
        return this.compound.getLong(key);
    }

    public void setLong(String key, long value) {
        this.compound.setLong(key, value);
    }

    public double getDouble(String key) {
        return this.compound.getDouble(key);
    }

    public void setDouble(String key, double value) {
        this.compound.setDouble(key, value);
    }

    public float getFloat(String key) {
        return this.compound.getFloat(key);
    }

    public void setFloat(String key, float value) {
        this.compound.setFloat(key, value);
    }

    public String getString(String key) {
        return this.compound.getString(key);
    }

    public void setString(String key, String value) {
        this.compound.setString(key, value);
    }

    public byte[] getByteArray(String key) {
        return this.compound.getByteArray(key);
    }

    public void setByteArray(String key, byte[] value) {
        this.compound.setByteArray(key, value);
    }

    public int[] getIntegerArray(String key) {
        return this.compound.getIntArray(key);
    }

    public void setIntegerArray(String key, int[] value) {
        this.compound.setIntArray(key, value);
    }

    public Object[] getList(String key, int type) {
        NBTTagList list = this.compound.getTagList(key, type);
        Object[] nbts = new Object[list.tagCount()];

        for(int i = 0; i < list.tagCount(); ++i) {
            if(list.func_150303_d() == 10) {
                nbts[i] = new ScriptNbt(list.getCompoundTagAt(i));
            } else if(list.func_150303_d() == 8) {
                nbts[i] = list.getStringTagAt(i);
            } else if(list.func_150303_d() == 6) {
                nbts[i] = Double.valueOf(list.func_150309_d(i));
            } else if(list.func_150303_d() == 5) {
                nbts[i] = Float.valueOf(list.func_150308_e(i));
            } else if(list.func_150303_d() == 3) {
                nbts[i] = Integer.valueOf(list.getStringTagAt(i));
            } else if(list.func_150303_d() == 11) {
                nbts[i] = list.func_150306_c(i);
            }
        }

        return nbts;
    }

    public int getListType(String key) {
        NBTBase b = this.compound.getTag(key);
        if(b == null) {
            return 0;
        } else if(b.getId() != 9) {
            throw new CustomNPCsException("NBT tag " + key + " isn\'t a list", new Object[0]);
        } else {
            return ((NBTTagList)b).func_150303_d();
        }
    }

    public void setList(String key, Object[] value) {
        NBTTagList list = new NBTTagList();
        Object[] var4 = value;
        int var5 = value.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Object nbt = var4[var6];
            if(nbt instanceof INbt) {
                list.appendTag(((INbt) nbt).getMCNBT());
            } else if(nbt instanceof String) {
                list.appendTag(new NBTTagString((String) nbt));
            } else if(nbt instanceof Double) {
                list.appendTag(new NBTTagDouble(((Double) nbt).doubleValue()));
            } else if(nbt instanceof Float) {
                list.appendTag(new NBTTagFloat(((Float) nbt).floatValue()));
            } else if(nbt instanceof Integer) {
                list.appendTag(new NBTTagInt(((Integer) nbt).intValue()));
            } else if(nbt instanceof int[]) {
                list.appendTag(new NBTTagIntArray((int[]) ((int[]) nbt)));
            }
        }

        this.compound.setTag(key, list);
    }

    public INbt getCompound(String key) {
        return new ScriptNbt(this.compound.getCompoundTag(key));
    }

    public void setCompound(String key, INbt value) {
        if(value == null) {
            throw new CustomNPCsException("Value cant be null", new Object[0]);
        } else {
            this.compound.setTag(key, value.getMCNBT());
        }
    }

    public String[] getKeys() {
        return (String[])this.compound.func_150296_c().toArray(new String[this.compound.func_150296_c().size()]);
    }

    public int getType(String key) {
        return this.compound.func_150299_b(key);
    }

    public NBTTagCompound getMCNBT() {
        return this.compound;
    }

    public String toJsonString() {
        return NBTJsonUtil.Convert(this.compound);
    }

    public boolean isEqual(INbt nbt) {
        return nbt == null?false:this.compound.equals(nbt.getMCNBT());
    }

    public void clear() {
        Iterator var1 = this.compound.func_150296_c().iterator();

        while(var1.hasNext()) {
            String name = (String)var1.next();
            this.compound.removeTag(name);
        }

    }
}
