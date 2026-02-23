package kamkeel.npcs.controllers.data.energycharge;

import kamkeel.npcs.entity.EntityEnergyProjectile;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side manager for packet-driven charging preview entities.
 * These entities are never spawned into the world; they are ticked and rendered manually.
 */
public class EnergyChargePreviewManager {

    public static EnergyChargePreviewManager ClientInstance;

    private final ConcurrentHashMap<String, EntityEnergyProjectile> previews = new ConcurrentHashMap<String, EntityEnergyProjectile>();

    public static void initClient() {
        ClientInstance = new EnergyChargePreviewManager();
    }

    public void addPreview(String instanceId, EntityEnergyProjectile entity) {
        if (instanceId == null || entity == null) return;
        previews.put(instanceId, entity);
    }

    public void removePreview(String instanceId) {
        if (instanceId == null) return;
        previews.remove(instanceId);
    }

    public Collection<EntityEnergyProjectile> getPreviews() {
        return previews.values();
    }

    public boolean hasPreviews() {
        return !previews.isEmpty();
    }

    public void clear() {
        previews.clear();
    }

    /**
     * Tick all preview entities and remove invalid/expired entries.
     */
    public void tick(World world) {
        if (world == null) {
            clear();
            return;
        }

        Iterator<EntityEnergyProjectile> iterator = previews.values().iterator();
        while (iterator.hasNext()) {
            EntityEnergyProjectile entity = iterator.next();
            if (entity == null || entity.isDead || entity.worldObj != world) {
                iterator.remove();
                continue;
            }
            entity.onUpdate();
            if (entity.isDead) {
                iterator.remove();
            }
        }
    }
}
