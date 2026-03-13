package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.data.IMagicData;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.core.NBT;

import java.util.HashMap;

public class MagicData implements IMagicData {
    private final HashMap<Integer, MagicEntry> magics = new HashMap<>();

    public void writeToNBT(INBTCompound compound) {
        INBTCompound magicData = NBT.compound();
        for (int key : magics.keySet()) {
            magicData.setCompound(String.valueOf(key), magics.get(key).writeToNBT());
        }
        compound.setCompound("MagicData", magicData);
    }

    public void readToNBT(INBTCompound compound) {
        INBTCompound magicData = compound.getCompound("MagicData");
        magics.clear();
        if (magicData == null)
            return;
        for (String key : magicData.getKeySet()) {
            int i = Integer.parseInt(key);
            MagicEntry entry = new MagicEntry();
            entry.readToNBT(magicData.getCompound(key));
            magics.put(i, entry);
        }
    }

    public MagicEntry getMagic(int id) {
        return magics.get(id);
    }

    public void removeMagic(int id) {
        magics.remove(id);
    }

    public boolean hasMagic(int id) {
        return magics.containsKey(id);
    }

    public HashMap<Integer, MagicEntry> getMagics() {
        return magics;
    }

    public void clear() {
        magics.clear();
    }

    public boolean isEmpty() {
        return magics.isEmpty();
    }

    public void addMagic(int id, float damage, float split) {
        MagicEntry entry = new MagicEntry();
        entry.damage = damage;
        entry.split = split;
        magics.put(id, entry);
    }

    public float getMagicDamage(int id) {
        MagicEntry entry = magics.get(id);
        return entry == null ? 0 : entry.damage;
    }

    public float getMagicSplit(int id) {
        MagicEntry entry = magics.get(id);
        return entry == null ? 0 : entry.split;
    }

    public MagicData copy() {
        MagicData copy = new MagicData();
        for (java.util.Map.Entry<Integer, MagicEntry> e : magics.entrySet()) {
            MagicEntry entryCopy = new MagicEntry();
            entryCopy.damage = e.getValue().damage;
            entryCopy.split = e.getValue().split;
            copy.magics.put(e.getKey(), entryCopy);
        }
        return copy;
    }
}
