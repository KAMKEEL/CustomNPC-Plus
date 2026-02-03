package kamkeel.npcs.controllers;

import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphRemovePacket;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphSpawnPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.ITelegraph;
import noppes.npcs.api.ITelegraphInstance;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.ITelegraphHandler;
import noppes.npcs.scripted.ScriptTelegraph;
import noppes.npcs.scripted.ScriptTelegraphInstance;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side controller for managing telegraphs.
 * Provides factory methods for creating telegraphs and spawn methods for sending to clients.
 * Also manages saved telegraph presets.
 */
public class TelegraphController implements ITelegraphHandler {

    public static TelegraphController Instance;

    // Saved telegraph presets (name -> Telegraph)
    private HashMap<String, Telegraph> savedTelegraphs = new HashMap<>();

    // Active server-side telegraph instances (for removal tracking)
    private ConcurrentHashMap<String, TelegraphInstance> activeInstances = new ConcurrentHashMap<>();

    public TelegraphController() {
    }

    public static void init() {
        Instance = new TelegraphController();
        Instance.load();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTERNAL SPAWN METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Spawn a telegraph at a fixed position.
     *
     * @param telegraph The telegraph configuration
     * @param world     The world
     * @param x         X position
     * @param y         Y position
     * @param z         Z position
     * @param yaw       Rotation yaw (for directional telegraphs like LINE/CONE)
     * @return The spawned telegraph instance
     */
    public TelegraphInstance spawn(Telegraph telegraph, World world, double x, double y, double z, float yaw) {
        TelegraphInstance instance = new TelegraphInstance(telegraph, x, y, z, yaw);
        instance.setWorld(world);
        activeInstances.put(instance.getInstanceId(), instance);
        TelegraphSpawnPacket.sendToDimension(instance, world.provider.dimensionId);
        return instance;
    }

    /**
     * Spawn a telegraph at a position with default yaw.
     */
    public TelegraphInstance spawn(Telegraph telegraph, World world, double x, double y, double z) {
        return spawn(telegraph, world, x, y, z, 0);
    }

    /**
     * Spawn a telegraph that follows an entity.
     *
     * @param telegraph The telegraph configuration
     * @param entity    The entity to follow
     * @return The spawned telegraph instance
     */
    public TelegraphInstance spawn(Telegraph telegraph, Entity entity) {
        return spawn(telegraph, entity, 0);
    }

    /**
     * Spawn a telegraph that follows an entity with yaw.
     */
    public TelegraphInstance spawn(Telegraph telegraph, Entity entity, float yaw) {
        TelegraphInstance instance = new TelegraphInstance(telegraph, entity.posX, entity.posY, entity.posZ, yaw);
        instance.setWorld(entity.worldObj);
        instance.setEntityIdToFollow(entity.getEntityId());
        activeInstances.put(instance.getInstanceId(), instance);
        TelegraphSpawnPacket.sendToTracking(instance, entity);
        return instance;
    }

    /**
     * Spawn a telegraph to a specific player only.
     */
    public TelegraphInstance spawnToPlayer(Telegraph telegraph, EntityPlayerMP player, double x, double y, double z, float yaw) {
        TelegraphInstance instance = new TelegraphInstance(telegraph, x, y, z, yaw);
        instance.setWorld(player.worldObj);
        activeInstances.put(instance.getInstanceId(), instance);
        TelegraphSpawnPacket.send(instance, player);
        return instance;
    }

    /**
     * Spawn a telegraph to all players on the server.
     */
    public TelegraphInstance spawnToAll(Telegraph telegraph, World world, double x, double y, double z, float yaw) {
        TelegraphInstance instance = new TelegraphInstance(telegraph, x, y, z, yaw);
        instance.setWorld(world);
        activeInstances.put(instance.getInstanceId(), instance);
        TelegraphSpawnPacket.sendToAll(instance);
        return instance;
    }

    /**
     * Spawn a telegraph to all players tracking an entity.
     */
    public TelegraphInstance spawnToTracking(Telegraph telegraph, Entity entity, double x, double y, double z, float yaw) {
        TelegraphInstance instance = new TelegraphInstance(telegraph, x, y, z, yaw);
        instance.setWorld(entity.worldObj);
        activeInstances.put(instance.getInstanceId(), instance);
        TelegraphSpawnPacket.sendToTracking(instance, entity);
        return instance;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // REMOVAL METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Remove a telegraph by instance ID.
     */
    public void remove(String instanceId) {
        TelegraphInstance instance = activeInstances.remove(instanceId);
        if (instance != null && instance.getWorld() != null) {
            TelegraphRemovePacket.sendToDimension(instanceId, instance.getDimensionId());
        }
    }

    /**
     * Remove a telegraph instance.
     */
    public void remove(TelegraphInstance instance) {
        if (instance != null) {
            remove(instance.getInstanceId());
        }
    }

    /**
     * Remove a telegraph for a specific player only.
     */
    public void removeFromPlayer(String instanceId, EntityPlayerMP player) {
        activeInstances.remove(instanceId);
        TelegraphRemovePacket.send(instanceId, player);
    }

    /**
     * Get an active telegraph instance by ID.
     */
    public TelegraphInstance getActiveInstance(String instanceId) {
        return activeInstances.get(instanceId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRESET MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Save a telegraph preset (internal use).
     *
     * @param name      The name for the preset
     * @param telegraph The telegraph configuration to save
     */
    public void saveInternal(String name, Telegraph telegraph) {
        if (name == null || name.isEmpty() || telegraph == null) return;

        telegraph.setId(name);
        savedTelegraphs.put(name, telegraph);
        saveTelegraphFile(name, telegraph);
    }

    /**
     * Get a saved telegraph preset by name (internal use).
     *
     * @param name The preset name
     * @return The telegraph configuration, or null if not found
     */
    public Telegraph getInternal(String name) {
        return savedTelegraphs.get(name);
    }

    /**
     * Delete a saved telegraph preset.
     *
     * @param name The preset name
     */
    public void delete(String name) {
        Telegraph removed = savedTelegraphs.remove(name);
        if (removed != null) {
            File file = new File(getDir(), name + ".json");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Check if a preset exists.
     */
    public boolean has(String name) {
        return savedTelegraphs.containsKey(name);
    }

    /**
     * Get all saved preset names.
     */
    public String[] getSavedNames() {
        return savedTelegraphs.keySet().toArray(new String[0]);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERSISTENCE
    // ═══════════════════════════════════════════════════════════════════════════

    private File getDir() {
        return new File(CustomNpcs.getWorldSaveDirectory(), "telegraphs");
    }

    public void load() {
        savedTelegraphs.clear();
        LogWriter.info("Loading telegraph presets...");

        File dir = getDir();
        if (!dir.exists()) {
            dir.mkdirs();
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".json")) continue;
            try {
                Telegraph telegraph = new Telegraph();
                telegraph.readNBT(NBTJsonUtil.LoadFile(file));
                String name = file.getName().substring(0, file.getName().length() - 5);
                telegraph.setId(name);
                savedTelegraphs.put(name, telegraph);
            } catch (Exception e) {
                LogWriter.error("Error loading telegraph preset: " + file.getName(), e);
            }
        }

        LogWriter.info("Loaded " + savedTelegraphs.size() + " telegraph presets.");
    }

    private void saveTelegraphFile(String name, Telegraph telegraph) {
        try {
            File dir = getDir();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, name + ".json_new");
            File file2 = new File(dir, name + ".json");

            NBTJsonUtil.SaveFile(file, telegraph.writeNBT());
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
        } catch (Exception e) {
            LogWriter.error("Error saving telegraph preset: " + name, e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ITelegraphHandler INTERFACE IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public ITelegraph createCircle(float radius) {
        return new ScriptTelegraph(Telegraph.circle(radius));
    }

    @Override
    public ITelegraph createRing(float outerRadius, float innerRadius) {
        return new ScriptTelegraph(Telegraph.ring(outerRadius, innerRadius));
    }

    @Override
    public ITelegraph createLine(float length, float width) {
        return new ScriptTelegraph(Telegraph.line(length, width));
    }

    @Override
    public ITelegraph createCone(float length, float angle) {
        return new ScriptTelegraph(Telegraph.cone(length, angle));
    }

    @Override
    public ITelegraph createPoint() {
        return new ScriptTelegraph(Telegraph.point());
    }

    @Override
    public ITelegraph create(String type) {
        if (type == null) return null;
        try {
            TelegraphType telegraphType = TelegraphType.valueOf(type.toUpperCase());
            Telegraph telegraph;
            switch (telegraphType) {
                case CIRCLE:
                    telegraph = Telegraph.circle(3.0f);
                    break;
                case RING:
                    telegraph = Telegraph.ring(5.0f, 2.0f);
                    break;
                case LINE:
                    telegraph = Telegraph.line(5.0f, 2.0f);
                    break;
                case CONE:
                    telegraph = Telegraph.cone(5.0f, 45.0f);
                    break;
                case POINT:
                    telegraph = Telegraph.point();
                    break;
                default:
                    telegraph = Telegraph.circle(3.0f);
            }
            return new ScriptTelegraph(telegraph);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public ITelegraphInstance spawn(ITelegraph telegraph, IWorld world, double x, double y, double z) {
        return spawn(telegraph, world, x, y, z, 0);
    }

    @Override
    public ITelegraphInstance spawn(ITelegraph telegraph, IWorld world, double x, double y, double z, float yaw) {
        if (telegraph == null || world == null) return null;
        Telegraph internal = ((ScriptTelegraph) telegraph).getMCTelegraph();
        TelegraphInstance instance = spawn(internal, (World) world.getMCWorld(), x, y, z, yaw);
        return new ScriptTelegraphInstance(instance);
    }

    @Override
    public ITelegraphInstance spawn(ITelegraph telegraph, IEntity entity) {
        return spawn(telegraph, entity, 0);
    }

    @Override
    public ITelegraphInstance spawn(ITelegraph telegraph, IEntity entity, float yaw) {
        if (telegraph == null || entity == null) return null;
        Telegraph internal = ((ScriptTelegraph) telegraph).getMCTelegraph();
        TelegraphInstance instance = spawn(internal, (Entity) entity.getMCEntity(), yaw);
        return new ScriptTelegraphInstance(instance);
    }

    @Override
    public ITelegraph get(String name) {
        Telegraph telegraph = savedTelegraphs.get(name);
        return telegraph != null ? new ScriptTelegraph(new Telegraph(telegraph)) : null;
    }

    @Override
    public void save(String name, ITelegraph telegraph) {
        if (name == null || name.isEmpty() || telegraph == null) return;
        Telegraph internal = ((ScriptTelegraph) telegraph).getMCTelegraph();
        saveInternal(name, internal);
    }

    @Override
    public void remove(ITelegraphInstance instance) {
        if (instance != null) {
            remove(instance.getInstanceId());
        }
    }
}
