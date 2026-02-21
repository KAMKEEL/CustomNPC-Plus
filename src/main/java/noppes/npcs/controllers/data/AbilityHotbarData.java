package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class AbilityHotbarData {
    public static final String CHAIN_PREFIX = "chain:";
    public static final int TOTAL_SLOTS = 12;

    public int slot = -1;
    public String abilityKey = "";

    public AbilityHotbarData() {
    }

    public AbilityHotbarData(int slot) {
        this.slot = slot;
    }

    public boolean isChainKey() {
        return abilityKey != null && abilityKey.startsWith(CHAIN_PREFIX);
    }

    public String getResolveKey() {
        if (isChainKey()) return abilityKey.substring(CHAIN_PREFIX.length());
        return abilityKey;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("slot", slot);
        tag.setString("abilityKey", abilityKey != null ? abilityKey : "");
        compound.setTag("AbilityHotbar" + slot, tag);
        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        abilityKey = compound.getString("abilityKey");
    }

    public void reset() {
        abilityKey = "";
    }

    public boolean isEmpty() {
        return abilityKey == null || abilityKey.isEmpty();
    }
}
