package kamkeel.npcs.controllers.data.telegraph;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active telegraph instances.
 * There's one instance per IWorld (client and server).
 */
public class TelegraphManager {

    /**
     * Client-side instance for rendering
     */
    public static TelegraphManager ClientInstance;

    private final ConcurrentHashMap<String, TelegraphInstance> telegraphs = new ConcurrentHashMap<>();

    public TelegraphManager() {
    }

    /**
     * Initialize the client-side manager.
     * Call from client proxy during mod init.
     */
    public static void initClient() {
        ClientInstance = new TelegraphManager();
    }

    // ═══════════════════════════════════════════════════════════════════
    // TELEGRAPH MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Add a new telegraph instance.
     */
    public void addTelegraph(TelegraphInstance instance) {
        telegraphs.put(instance.getInstanceId(), instance);
    }

    /**
     * Remove a telegraph by ID.
     */
    public void removeTelegraph(String instanceId) {
        telegraphs.remove(instanceId);
    }

    /**
     * Get a telegraph by ID.
     */
    public TelegraphInstance getTelegraph(String instanceId) {
        return telegraphs.get(instanceId);
    }

    /**
     * Get all active telegraphs for rendering.
     */
    public Collection<TelegraphInstance> getTelegraphs() {
        return telegraphs.values();
    }

    /**
     * Clear all telegraphs.
     */
    public void clear() {
        telegraphs.clear();
    }

    /**
     * Check if any telegraphs are active.
     */
    public boolean hasTelegraphs() {
        return !telegraphs.isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════
    // TICK
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Tick all telegraphs, removing expired ones.
     * Call from client tick handler.
     */
    public void tick(IWorld IWorld) {
        Iterator<TelegraphInstance> iterator = telegraphs.values().iterator();
        while (iterator.hasNext()) {
            TelegraphInstance instance = iterator.next();
            if (!instance.tick(IWorld)) {
                iterator.remove();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER METHODS FOR SPAWNING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Spawn a circle telegraph at a position.
     */
    public TelegraphInstance spawnCircle(double x, double y, double z, float radius, int durationTicks, int color) {
        Telegraph telegraph = Telegraph.circle(radius);
        telegraph.setDurationTicks(durationTicks);
        telegraph.setColor(color);
        telegraph.setWarningColor((color & 0x00FFFFFF) | 0xC0000000);

        TelegraphInstance instance = new TelegraphInstance(telegraph, x, y, z, 0);
        addTelegraph(instance);
        return instance;
    }

    /**
     * Spawn a line telegraph from a position in a direction.
     */
    public TelegraphInstance spawnLine(double x, double y, double z, float yaw, float length, float width, int durationTicks, int color) {
        Telegraph telegraph = Telegraph.line(length, width);
        telegraph.setDurationTicks(durationTicks);
        telegraph.setColor(color);
        telegraph.setWarningColor((color & 0x00FFFFFF) | 0xC0000000);

        TelegraphInstance instance = new TelegraphInstance(telegraph, x, y, z, yaw);
        addTelegraph(instance);
        return instance;
    }

    /**
     * Spawn a cone telegraph from a position in a direction.
     */
    public TelegraphInstance spawnCone(double x, double y, double z, float yaw, float length, float angle, int durationTicks, int color) {
        Telegraph telegraph = Telegraph.cone(length, angle);
        telegraph.setDurationTicks(durationTicks);
        telegraph.setColor(color);
        telegraph.setWarningColor((color & 0x00FFFFFF) | 0xC0000000);

        TelegraphInstance instance = new TelegraphInstance(telegraph, x, y, z, yaw);
        addTelegraph(instance);
        return instance;
    }
}
