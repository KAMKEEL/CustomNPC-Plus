package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
public class PlayerAbilityHotbarData {
    public AbilityHotbarData[] slots = new AbilityHotbarData[AbilityHotbarData.TOTAL_SLOTS];

    public PlayerAbilityHotbarData() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new AbilityHotbarData(i);
        }
    }

    public AbilityHotbarData getSlot(int index) {
        if (index < 0 || index >= slots.length) return null;
        return slots[index];
    }

    public void setSlot(int index, String abilityKey) {
        if (index < 0 || index >= slots.length) return;
        slots[index].abilityKey = abilityKey != null ? abilityKey : "";
    }

    public void clearSlot(int index) {
        if (index < 0 || index >= slots.length) return;
        slots[index].reset();
    }

    public boolean hasAnyAbilities() {
        for (AbilityHotbarData slot : slots) {
            if (!slot.isEmpty()) return true;
        }
        return false;
    }

    public void writeToNBT(INbt compound) {
        for (int i = 0; i < slots.length; i++) {
            slots[i].writeToNBT(new NBTWrapper(compound));
        }
    }

    public void readFromNBT(INbt compound) {
        for (int i = 0; i < slots.length; i++) {
            INbt slotTag = compound.getCompoundTag("AbilityHotbar" + i);
            slots[i].readFromNBT(new NBTWrapper(slotTag));
        }
        validateSlots();
    }

    public void validateSlots() {
        if (AbilityController.Instance == null) return;
        // Only validate on the server - the client trusts server-sent data.
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) return;
        for (AbilityHotbarData slot : slots) {
            if (slot.isEmpty()) continue;
            boolean valid;
            if (slot.isChainKey()) {
                valid = AbilityController.Instance.canResolveChainedAbility(slot.getResolveKey());
            } else {
                valid = AbilityController.Instance.canResolveAbility(slot.abilityKey);
                // Also check that resolved ability allows player usage
                if (valid) {
                    Ability ability = AbilityController.Instance.resolveAbility(slot.abilityKey);
                    if (ability != null && !ability.getAllowedBy().allowsPlayer()) {
                        valid = false;
                    }
                }
            }
            if (!valid) {
                slot.reset();
            }
        }
    }

    /**
     * Sync hotbar data only to the client.
     * Used after server confirms a client hotbar edit.
     */
    public void syncToClient(IPlayer player) {
        if (player instanceof IPlayer) {
            AbilityHotbarSyncPacket.sendToPlayer((IPlayer) player);
        }
    }
}
