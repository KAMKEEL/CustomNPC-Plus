//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.nbt.NBTTagCompound;

public interface INbt {
    void remove(String var1);

    boolean has(String var1);

    boolean getBoolean(String var1);

    void setBoolean(String var1, boolean var2);

    short getShort(String var1);

    void setShort(String var1, short var2);

    int getInteger(String var1);

    void setInteger(String var1, int var2);

    byte getByte(String var1);

    void setByte(String var1, byte var2);

    long getLong(String var1);

    void setLong(String var1, long var2);

    double getDouble(String var1);

    void setDouble(String var1, double var2);

    float getFloat(String var1);

    void setFloat(String var1, float var2);

    String getString(String var1);

    void setString(String var1, String var2);

    byte[] getByteArray(String var1);

    void setByteArray(String var1, byte[] var2);

    int[] getIntegerArray(String var1);

    void setIntegerArray(String var1, int[] var2);

    Object[] getList(String var1, int var2);

    int getListType(String var1);

    void setList(String var1, Object[] var2);

    INbt getCompound(String var1);

    void setCompound(String var1, INbt var2);

    String[] getKeys();

    int getType(String var1);

    NBTTagCompound getMCNBT();

    String toJsonString();

    boolean isEqual(INbt var1);

    void clear();
}
