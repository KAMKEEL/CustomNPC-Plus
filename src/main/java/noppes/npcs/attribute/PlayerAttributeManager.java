package noppes.npcs.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Central manager mapping player UUIDs to their attribute trackers.
 */
public class PlayerAttributeManager {
    private static final Map<UUID, PlayerAttributeTracker> trackers = new HashMap<>();

    public static PlayerAttributeTracker getTracker(UUID playerId) {
        return trackers.computeIfAbsent(playerId, id -> new PlayerAttributeTracker(id));
    }

    public static void removeTracker(UUID playerId) {
        trackers.remove(playerId);
    }

    /**
     * Should be called every 10 ticks for all online players.
     */
    public static void updateAllTrackers(Iterable<EntityPlayer> players) {
        for (EntityPlayer player : players) {
            PlayerAttributeTracker tracker = getTracker(player.getUniqueID());
            tracker.updateIfChanged(player);
        }
    }
}
