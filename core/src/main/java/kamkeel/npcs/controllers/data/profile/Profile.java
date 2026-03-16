package kamkeel.npcs.controllers.data.profile;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IProfile;
import noppes.npcs.api.handler.data.ISlot;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.NoppesUtilServer;

public class Profile implements IProfile {
    public IPlayer player;
    public int currentSlotId;
    private final Map<Integer, ISlot> slots = new HashMap<>();
    public Map<Integer, Long> sharedQuestTimestamps = new HashMap<>();
    private boolean locked = false;

    public Profile(IPlayer player, INbt compound) {
        this.player = player;
        if (compound.hasKey("CurrentSlotId")) {
            this.currentSlotId = compound.getInteger("CurrentSlotId");
        } else {
            this.currentSlotId = 0;
        }

        if (compound.hasKey("Slots")) {
            INbt slotsCompound = compound.getCompoundTag("Slots");
            Set<String> keys = slotsCompound.func_150296_c();
            for (String key : keys) {
                try {
                    int slotId = Integer.parseInt(key);
                    INbt slotNBT = slotsCompound.getCompoundTag(key);
                    ISlot slot = Slot.fromNBT(slotId, slotNBT);
                    slots.put(slotId, slot);
                } catch (NumberFormatException e) {
                    // Skip keys that are not valid slot IDs.
                }
            }
        }

        if (compound.hasKey("SharedQuestTimestamps")) {
            INbtList list = compound.getTagList("SharedQuestTimestamps", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                INbt entry = list.getCompoundTagAt(i);
                sharedQuestTimestamps.put(entry.getInteger("Quest"), entry.getLong("Date"));
            }
        }
    }

    public Profile(IPlayer player) {
        this.player = player;
        this.currentSlotId = 0;
        // Create a default slot.
        Slot defaultSlot = new Slot(0, "Default Slot");
        defaultSlot.setLastLoaded(System.currentTimeMillis());
        slots.put(0, defaultSlot);
    }

    @Override
    public IPlayer getPlayer() {
        return NoppesUtilServer.getIPlayer(player);
    }

    @Override
    public int getCurrentSlotId() {
        return currentSlotId;
    }

    @Override
    public Map<Integer, ISlot> getSlots() {
        return slots;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public INbt writeToNBT() {
        INbt compound = new INbt();
        if (player != null)
            compound.setString("Name", player.getCommandSenderName());
        compound.setInteger("CurrentSlotId", currentSlotId);
        INbt slotsCompound = new INbt();
        for (Map.Entry<Integer, ISlot> entry : slots.entrySet()) {
            // Temporary slots (typically using negative IDs) are kept in memory
            // only and skipped here so they never persist to disk.
            if (entry.getValue().isTemporary())
                continue;
            slotsCompound.setTag(String.valueOf(entry.getKey()), entry.getValue().toNBT());
        }
        compound.setTag("Slots", slotsCompound);

        INbtList questList = new INbtList();
        for (Map.Entry<Integer, Long> entry : sharedQuestTimestamps.entrySet()) {
            INbt questEntry = new INbt();
            questEntry.setInteger("Quest", entry.getKey());
            questEntry.setLong("Date", entry.getValue());
            questList.appendTag(questEntry);
        }
        compound.setTag("SharedQuestTimestamps", questList);

        return compound;
    }
}
