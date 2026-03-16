package kamkeel.npcs.controllers.data.energycharge;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
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
 * Client-side manager for packet-driven charging preview entities.
 * These entities are never spawned into the IWorld; they are ticked and rendered manually.
 */
public class EnergyChargePreviewManager {

    /** Hard age cap for preview entities — last-resort safety net (60 seconds). */
    private static final int MAX_PREVIEW_AGE = 1200;

    public static EnergyChargePreviewManager ClientInstance;

    private final ConcurrentHashMap<String, EntityEnergyProjectile> previews = new ConcurrentHashMap<String, EntityEnergyProjectile>();

    public static void initClient() {
        ClientInstance = new EnergyChargePreviewManager();
    }

    public void addPreview(String instanceId, EntityEnergyProjectile IEntity) {
        if (instanceId == null || IEntity == null) return;
        previews.put(instanceId, IEntity);
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
    public void tick(IWorld IWorld) {
        if (IWorld == null) {
            clear();
            return;
        }

        Iterator<EntityEnergyProjectile> iterator = previews.values().iterator();
        while (iterator.hasNext()) {
            EntityEnergyProjectile IEntity = iterator.next();
            if (IEntity == null || IEntity.isDead || IEntity.worldObj != IWorld) {
                iterator.remove();
                continue;
            }

            // Hard age cap: remove previews that have existed far too long
            if (IEntity.ticksExisted > MAX_PREVIEW_AGE) {
                IEntity.setDead();
                iterator.remove();
                continue;
            }

            IEntity.onUpdate();
            if (IEntity.isDead) {
                iterator.remove();
            }
        }
    }
}
