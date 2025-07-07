package kamkeel.npcs.controllers.data.profile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IProfile;
import noppes.npcs.api.handler.data.ISlot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Profile implements IProfile {
    public EntityPlayer player;
    public int currentSlotId;
    private final Map<Integer, ISlot> slots = new HashMap<>();
    private boolean locked = false;

    public Profile(EntityPlayer player, NBTTagCompound compound) {
        this.player = player;
        if (compound.hasKey("CurrentSlotId")) {
            this.currentSlotId = compound.getInteger("CurrentSlotId");
        } else {
            this.currentSlotId = 0;
        }

        if (compound.hasKey("Slots")) {
            NBTTagCompound slotsCompound = compound.getCompoundTag("Slots");
            Set<String> keys = slotsCompound.func_150296_c();
            for (String key : keys) {
                try {
                    int slotId = Integer.parseInt(key);
                    NBTTagCompound slotNBT = slotsCompound.getCompoundTag(key);
                    ISlot slot = Slot.fromNBT(slotId, slotNBT);
                    slots.put(slotId, slot);
                } catch (NumberFormatException e) {
                    // Skip keys that are not valid slot IDs.
                }
            }
        }
    }

    public Profile(EntityPlayer player) {
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
    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        if (player != null)
            compound.setString("Name", player.getCommandSenderName());
        compound.setInteger("CurrentSlotId", currentSlotId);
        NBTTagCompound slotsCompound = new NBTTagCompound();
        for (Map.Entry<Integer, ISlot> entry : slots.entrySet()) {
            // Temporary slots (typically using negative IDs) are kept in memory
            // only and skipped here so they never persist to disk.
            if (entry.getValue().isTemporary())
                continue;
            slotsCompound.setTag(String.valueOf(entry.getKey()), entry.getValue().toNBT());
        }
        compound.setTag("Slots", slotsCompound);
        return compound;
    }
}
