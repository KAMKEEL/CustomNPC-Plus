package kamkeel.npcs.controllers.data.energycharge;

import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.data.energycharge.EnergyChargeSpawnPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Server-side tracker for active charging preview visuals.
 * Stores enough data to resend {@link EnergyChargeSpawnPacket} to late-joining players
 * who enter tracking range after the initial broadcast.
 * <p>
 * Keyed by caster entity ID so {@code PlayerEvent.StartTracking} can look up
 * active charges for a newly tracked entity.
 */
public class EnergyChargeTracker {
    public static final EnergyChargeTracker Instance = new EnergyChargeTracker();

    public static class ChargeEntry {
        public final String instanceId;
        public final String entityClassName;
        public final NBTTagCompound spawnNbt;
        public final int casterEntityId;
        public final int startTick;

        public ChargeEntry(String instanceId, String entityClassName,
                           NBTTagCompound spawnNbt, int casterEntityId, int startTick) {
            this.instanceId = instanceId;
            this.entityClassName = entityClassName;
            this.spawnNbt = spawnNbt;
            this.casterEntityId = casterEntityId;
            this.startTick = startTick;
        }
    }

    private final Map<Integer, List<ChargeEntry>> activeCharges = new HashMap<>();

    public void add(ChargeEntry entry) {
        List<ChargeEntry> list = activeCharges.get(entry.casterEntityId);
        if (list == null) {
            list = new ArrayList<>();
            activeCharges.put(entry.casterEntityId, list);
        }
        list.add(entry);
    }

    public void remove(String instanceId, int casterEntityId) {
        List<ChargeEntry> list = activeCharges.get(casterEntityId);
        if (list == null) return;
        Iterator<ChargeEntry> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().instanceId.equals(instanceId)) {
                it.remove();
                break;
            }
        }
        if (list.isEmpty()) {
            activeCharges.remove(casterEntityId);
        }
    }

    public void removeAllForCaster(int casterEntityId) {
        activeCharges.remove(casterEntityId);
    }

    /**
     * Send all active charges for a caster to a specific player.
     * Adjusts ChargeTick in the NBT to reflect elapsed time so the
     * late-joining client starts at the correct visual progress.
     */
    public void sendToPlayer(int casterEntityId, EntityPlayerMP player, int currentWorldTick) {
        List<ChargeEntry> entries = activeCharges.get(casterEntityId);
        if (entries == null || entries.isEmpty()) return;

        for (ChargeEntry entry : entries) {
            int elapsed = currentWorldTick - entry.startTick;
            int chargeDuration = entry.spawnNbt.hasKey("ChargeDuration")
                ? entry.spawnNbt.getInteger("ChargeDuration") : 0;

            // Charge already complete — server remove packet should follow shortly
            if (chargeDuration > 0 && elapsed >= chargeDuration) {
                continue;
            }

            NBTTagCompound adjustedNbt = (NBTTagCompound) entry.spawnNbt.copy();
            if (elapsed > 0) {
                adjustedNbt.setInteger("ChargeTick", elapsed);
            }

            PacketHandler.Instance.sendToPlayer(
                new EnergyChargeSpawnPacket(entry.instanceId, entry.entityClassName, adjustedNbt),
                player
            );
        }
    }

    public boolean hasCharges(int casterEntityId) {
        List<ChargeEntry> list = activeCharges.get(casterEntityId);
        return list != null && !list.isEmpty();
    }

    public void clear() {
        activeCharges.clear();
    }
}
