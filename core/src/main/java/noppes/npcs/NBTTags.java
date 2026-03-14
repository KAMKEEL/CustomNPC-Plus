package noppes.npcs;

import noppes.npcs.core.NBT;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Core NBT serialization utilities for Map/Set/List collections.
 * Uses INbt/INbtList abstractions.
 *
 * NOTE: ItemStack-related methods, IScriptUnit methods, and raw NBT type methods
 * (nbtDoubleList, getIntAt) remain in the mc1710 version only since they require
 * direct MC types.
 */
public class NBTTags {

    public static int TAG_End = 0;
    public static int TAG_Byte = 1;
    public static int TAG_Short = 2;
    public static int TAG_Int = 3;
    public static int TAG_Long = 4;
    public static int TAG_Float = 5;
    public static int TAG_Double = 6;
    public static int TAG_Byte_Array = 7;
    public static int TAG_String = 8;
    public static int TAG_List = 9;
    public static int TAG_Compound = 10;
    public static int TAG_Int_Array = 11;

    // TODO: ItemStack methods stay in mc1710 version:
    // OLD: getItemStackList(NBTTagList) - uses NoppesUtilServer.readItem()
    // OLD: getItemStackArray(NBTTagList) - uses NoppesUtilServer.readItem()
    // OLD: nbtItemStackList(HashMap<Integer, ItemStack>) - uses NoppesUtilServer.writeItem()
    // OLD: nbtItemStackArray(ItemStack[]) - uses NoppesUtilServer.writeItem()

    public static ArrayList<int[]> getIntegerArraySet(INbtList tagList) {
        ArrayList<int[]> set = new ArrayList<int[]>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt compound = tagList.getCompound(i);
            set.add(compound.getIntArray("Array"));
        }
        return set;
    }

    public static HashMap<Integer, Boolean> getBooleanList(INbtList tagList) {
        HashMap<Integer, Boolean> list = new HashMap<Integer, Boolean>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getBoolean("Boolean"));
        }
        return list;
    }

    public static HashMap<Integer, Integer> getIntegerIntegerMap(INbtList tagList) {
        HashMap<Integer, Integer> list = new HashMap<Integer, Integer>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getInteger("Integer"));
        }
        return list;
    }

    public static HashMap<Integer, Float> getIntegerFloatMap(INbtList tagList) {
        HashMap<Integer, Float> list = new HashMap<Integer, Float>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getFloat("Float"));
        }
        return list;
    }

    public static HashMap<Integer, Double> getIntegerDoubleMap(INbtList tagList) {
        HashMap<Integer, Double> list = new HashMap<Integer, Double>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getDouble("Double"));
        }
        return list;
    }

    public static HashMap<Integer, Long> getIntegerLongMap(INbtList tagList) {
        HashMap<Integer, Long> list = new HashMap<Integer, Long>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getLong("Long"));
        }
        return list;
    }

    public static HashSet<String> getStringSet(INbtList tagList) {
        HashSet<String> list = new HashSet<>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.add(nbttagcompound.getString("String"));
        }
        return list;
    }

    public static HashMap<Integer, Byte> getIntegerByteMap(INbtList tagList) {
        HashMap<Integer, Byte> list = new HashMap<Integer, Byte>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getByte("Byte"));
        }
        return list;
    }

    public static INbtList nbtIntegerByteMap(Map<Integer, Byte> lines) {
        INbtList nbttaglist = NBT.list();
        if (lines == null)
            return nbttaglist;
        for (int slot : lines.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Slot", slot);
            nbttagcompound.setByte("Byte", lines.get(slot));
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtStringSet(HashSet<String> collection) {
        INbtList nbttaglist = NBT.list();
        if (collection == null)
            return nbttaglist;
        for (String slot : collection) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setString("String", slot);
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static HashSet<Integer> getIntegerSet(INbtList tagList) {
        HashSet<Integer> list = new HashSet<Integer>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.add(nbttagcompound.getInteger("Integer"));
        }
        return list;
    }

    public static INbtList nbtIntegerSet(HashSet<Integer> set) {
        INbtList nbttaglist = NBT.list();
        if (set == null)
            return nbttaglist;
        for (int slot : set) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Integer", slot);
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static HashMap<String, String> getStringStringMap(INbtList tagList) {
        HashMap<String, String> list = new HashMap<String, String>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getString("Slot"), nbttagcompound.getString("Value"));
        }
        return list;
    }

    public static HashMap<Integer, String> getIntegerStringMap(INbtList tagList) {
        HashMap<Integer, String> list = new HashMap<Integer, String>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getString("Value"));
        }
        return list;
    }

    public static HashMap<String, Integer> getStringIntegerMap(INbtList tagList) {
        HashMap<String, Integer> list = new HashMap<String, Integer>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getString("Slot"), nbttagcompound.getInteger("Value"));
        }
        return list;
    }

    public static HashMap<String, int[]> getStringIntegerArrayMap(INbtList tagList) {
        HashMap<String, int[]> list = new HashMap<String, int[]>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getString("Slot"), nbttagcompound.getIntArray("Value"));
        }
        return list;
    }

    public static HashMap<String, int[]> getStringIntegerArrayMap(INbtList tagList, int arrayLength) {
        HashMap<String, int[]> list = new HashMap<String, int[]>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            int[] a = nbttagcompound.getIntArray("Value");
            if (a.length != arrayLength) a = new int[arrayLength];
            list.put(nbttagcompound.getString("Slot"), a);
        }
        return list;
    }

    public static HashMap<String, Vector<String>> getVectorMap(INbtList tagList) {
        HashMap<String, Vector<String>> map = new HashMap<String, Vector<String>>();
        for (int i = 0; i < tagList.size(); i++) {
            Vector<String> values = new Vector<String>();
            INbt nbttagcompound = tagList.getCompound(i);
            INbtList list = nbttagcompound.getTagList("Values", 10);
            for (int j = 0; j < list.size(); j++) {
                INbt value = list.getCompound(j);
                values.add(value.getString("Value"));
            }

            map.put(nbttagcompound.getString("Key"), values);
        }
        return map;
    }


    public static List<String> getStringList(INbtList tagList) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            String line = nbttagcompound.getString("Line");
            list.add(line);
        }
        return list;
    }

    public static String[] getStringArray(INbtList tagList, int size) {
        String[] arr = new String[size];
        for (int i = 0; i < tagList.size(); i++) {
            INbt nbttagcompound = tagList.getCompound(i);
            String line = nbttagcompound.getString("Value");
            int slot = nbttagcompound.getInteger("Slot");
            arr[slot] = line;
        }
        return arr;
    }

    public static INbtList nbtIntegerArraySet(List<int[]> set) {
        INbtList nbttaglist = NBT.list();
        if (set == null)
            return nbttaglist;
        for (int[] arr : set) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setIntArray("Array", arr);
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtBooleanList(HashMap<Integer, Boolean> updatedSlots) {
        INbtList nbttaglist = NBT.list();
        if (updatedSlots == null)
            return nbttaglist;
        HashMap<Integer, Boolean> inventory2 = updatedSlots;
        for (Integer slot : inventory2.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Slot", slot);
            nbttagcompound.setBoolean("Boolean", inventory2.get(slot));

            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtIntegerIntegerMap(Map<Integer, Integer> lines) {
        INbtList nbttaglist = NBT.list();
        if (lines == null)
            return nbttaglist;
        for (int slot : lines.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Slot", slot);
            nbttagcompound.setInteger("Integer", lines.get(slot));
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtIntegerFloatMap(Map<Integer, Float> lines) {
        INbtList nbttaglist = NBT.list();
        if (lines == null)
            return nbttaglist;
        for (int slot : lines.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Slot", slot);
            nbttagcompound.setDouble("Float", lines.get(slot));
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtIntegerDoubleMap(Map<Integer, Double> lines) {
        INbtList nbttaglist = NBT.list();
        if (lines == null)
            return nbttaglist;
        for (int slot : lines.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Slot", slot);
            nbttagcompound.setDouble("Double", lines.get(slot));
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtIntegerLongMap(Map<Integer, Long> lines) {
        INbtList nbttaglist = NBT.list();
        if (lines == null)
            return nbttaglist;
        for (int slot : lines.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Slot", slot);
            nbttagcompound.setLong("Long", lines.get(slot));
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtVectorMap(Map<String, Vector<String>> map) {
        INbtList list = NBT.list();
        if (map == null)
            return list;
        for (String key : map.keySet()) {
            INbt compound = NBT.compound();
            compound.setString("Key", key);
            INbtList values = NBT.list();
            for (String value : map.get(key)) {
                INbt comp = NBT.compound();
                comp.setString("Value", value);
                values.addCompound(comp);
            }
            compound.setTagList("Values", values);
            list.addCompound(compound);
        }
        return list;
    }

    public static INbtList nbtStringStringMap(Map<String, String> map) {
        INbtList nbttaglist = NBT.list();
        if (map == null)
            return nbttaglist;
        for (String slot : map.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setString("Slot", slot);
            nbttagcompound.setString("Value", map.get(slot));

            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtStringIntegerMap(Map<String, Integer> map) {
        INbtList nbttaglist = NBT.list();
        if (map == null)
            return nbttaglist;
        for (String slot : map.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setString("Slot", slot);
            nbttagcompound.setInteger("Value", map.get(slot));

            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtStringIntegerArrayMap(Map<String, int[]> map) {
        INbtList nbttaglist = NBT.list();
        if (map == null)
            return nbttaglist;
        for (String slot : map.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setString("Slot", slot);
            nbttagcompound.setIntArray("Value", map.get(slot));

            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtIntegerStringMap(HashMap<Integer, String> map) {
        INbtList nbttaglist = NBT.list();
        if (map == null)
            return nbttaglist;
        for (int slot : map.keySet()) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Slot", slot);
            nbttagcompound.setString("Value", map.get(slot));

            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtStringArray(String[] list) {
        INbtList nbttaglist = NBT.list();
        if (list == null)
            return nbttaglist;
        for (int i = 0; i < list.length; i++) {
            if (list[i] == null)
                continue;
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setString("Value", list[i]);
            nbttagcompound.setInteger("Slot", i);
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    public static INbtList nbtStringList(List<String> list) {
        INbtList nbttaglist = NBT.list();
        for (String s : list) {
            INbt nbttagcompound = NBT.compound();
            nbttagcompound.setString("Line", s);
            nbttaglist.addCompound(nbttagcompound);
        }
        return nbttaglist;
    }

    // TODO: nbtDoubleList stays in mc1710 version - uses NBTTagDouble directly
    // OLD: public static NBTTagList nbtDoubleList(double... par1ArrayOfDouble)

    public static INbt NBTMerge(INbt data, INbt merge) {
        INbt compound = data.copy();
        Set<String> names = merge.getKeySet();
        for (String name : names) {
            int type = merge.getTagType(name);
            if (type == TAG_Compound)
                compound.setCompound(name, NBTMerge(compound.getCompound(name), merge.getCompound(name)));
            else {
                // For non-compound types, copy from merge.
                // We need to handle by type since INbt doesn't have a raw getTag/setTag.
                copyTag(compound, merge, name, type);
            }
        }
        return compound;
    }

    /**
     * Copy a single tag from source to dest by type.
     * Used by NBTMerge for non-compound types.
     */
    private static void copyTag(INbt dest, INbt source, String key, int type) {
        switch (type) {
            case 1: // Byte
                dest.setByte(key, source.getByte(key));
                break;
            case 2: // Short
                dest.setShort(key, source.getShort(key));
                break;
            case 3: // Int
                dest.setInteger(key, source.getInteger(key));
                break;
            case 4: // Long
                dest.setLong(key, source.getLong(key));
                break;
            case 5: // Float
                dest.setFloat(key, source.getFloat(key));
                break;
            case 6: // Double
                dest.setDouble(key, source.getDouble(key));
                break;
            case 7: // Byte Array
                dest.setByteArray(key, source.getByteArray(key));
                break;
            case 8: // String
                dest.setString(key, source.getString(key));
                break;
            case 9: // List
                dest.setTagList(key, source.getTagList(key, 0));
                break;
            case 10: // Compound (handled above in NBTMerge)
                dest.setCompound(key, source.getCompound(key));
                break;
            case 11: // Int Array
                dest.setIntArray(key, source.getIntArray(key));
                break;
        }
    }

    public static TreeMap<Long, String> GetLongStringMap(INbtList tagList) {
        TreeMap<Long, String> list = new TreeMap<>();

        for (int i = 0; i < tagList.size(); ++i) {
            INbt nbttagcompound = tagList.getCompound(i);
            list.put(nbttagcompound.getLong("Long"), nbttagcompound.getString("String"));
        }

        return list;
    }

    public static INbtList NBTLongStringMap(Map<Long, String> map) {
        INbtList nbttaglist = NBT.list();
        if (map == null) {
            return nbttaglist;
        } else {
            for (long slot : map.keySet()) {
                INbt nbttagcompound = NBT.compound();
                nbttagcompound.setLong("Long", slot);
                nbttagcompound.setString("String", map.get(slot));
                nbttaglist.addCompound(nbttagcompound);
            }

            return nbttaglist;
        }
    }

    // TODO: Script methods stay in mc1710 version - use IScriptHandler/IScriptUnit/NBTTagCompound directly
    // OLD: public static List<IScriptUnit> GetScriptOld(NBTTagList list, IScriptHandler handler)
    // OLD: public static List<IScriptUnit> GetScript(NBTTagCompound compound, IScriptHandler handler)
    // OLD: public static NBTTagList NBTScript(List<IScriptUnit> scripts)

    // TODO: getIntAt stays in mc1710 version - uses NBTTagInt.func_150287_d() directly
    // OLD: public static int getIntAt(NBTTagList tagList, int index)
}
