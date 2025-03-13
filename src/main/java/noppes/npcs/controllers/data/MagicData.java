package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IMagicData;

import java.util.HashMap;

public class MagicData implements IMagicData {
    private final HashMap<Integer, MagicEntry> magics = new HashMap<>();

    public void writeToNBT(NBTTagCompound compound) {
        NBTTagCompound magicData = new NBTTagCompound();
        for (int key : magics.keySet()) {
            magicData.setTag(String.valueOf(key), magics.get(key).writeToNBT());
        }
        compound.setTag("MagicData", magicData);
    }

    public void readToNBT(NBTTagCompound compound) {
        NBTTagCompound magicData = compound.getCompoundTag("MagicData");
        magics.clear();
        if (magicData == null)
            return;
        for (Object key : magicData.func_150296_c()) {
            int i = Integer.parseInt((String) key);
            MagicEntry entry = new MagicEntry();
            entry.readToNBT(magicData.getCompoundTag((String) key));
            magics.put(i, entry);
        }
    }

    public MagicEntry getMagic(int id) {
        return magics.get(id);
    }

    @Override
    public void removeMagic(int id) {
        magics.remove(id);
    }

    @Override
    public boolean hasMagic(int id) {
        return magics.containsKey(id);
    }

    public HashMap<Integer, MagicEntry> getMagics() {
        return magics;
    }

    @Override
    public void clear() {
        magics.clear();
    }

    @Override
    public boolean isEmpty() {
        return magics.isEmpty();
    }

    @Override
    public void addMagic(int id, float damage, float split) {
        MagicEntry entry = new MagicEntry();
        entry.damage = damage;
        entry.split = split;
        magics.put(id, entry);
    }

    @Override
    public float getMagicDamage(int id) {
        MagicEntry entry = magics.get(id);
        return entry == null ? 0 : entry.damage;
    }

    @Override
    public float getMagicSplit(int id) {
        MagicEntry entry = magics.get(id);
        return entry == null ? 0 : entry.split;
    }
}
