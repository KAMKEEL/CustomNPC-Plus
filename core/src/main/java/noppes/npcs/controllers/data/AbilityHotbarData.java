package noppes.npcs.controllers.data;

import noppes.npcs.api.INbt;
import noppes.npcs.core.NBT;

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

    public INbt writeToNBT(INbt compound) {
        INbt tag = NBT.compound();
        tag.setInteger("slot", slot);
        tag.setString("abilityKey", abilityKey != null ? abilityKey : "");
        compound.setCompound("AbilityHotbar" + slot, tag);
        return compound;
    }

    public void readFromNBT(INbt compound) {
        abilityKey = compound.getString("abilityKey");
    }

    public void reset() {
        abilityKey = "";
    }

    public boolean isEmpty() {
        return abilityKey == null || abilityKey.isEmpty();
    }
}
