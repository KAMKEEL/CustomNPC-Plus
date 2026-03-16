package kamkeel.npcs.controllers;

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
import kamkeel.npcs.entity.EntityAbilityZone;
import kamkeel.npcs.entity.EntityEnergySweeper;
import kamkeel.npcs.entity.EntityEnergyExplosion;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.energy.IEnergyExtender;
import noppes.npcs.api.IEnergyHandler;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.*;
import noppes.npcs.api.entity.IEnergyExplosion;
import java.util.ArrayList;
import java.util.List;

/**
 * Central controller for script-created energy entities and handler registration.
 * Provides factory methods for creating all energy IEntity types and routes
 * damage through registered handlers when entities have customDamageData
 * but no sourceAbility.
 */
public class EnergyController implements IEnergyHandler {

    public static EnergyController Instance = new EnergyController();

    private final List<IEnergyExtender> extenders = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════════
    // HANDLER REGISTRATION
    // ═══════════════════════════════════════════════════════════════════

    public void registerExtender(IEnergyExtender handler) {
        extenders.add(handler);
    }

    public List<IEnergyExtender> getExtenders() {
        return extenders;
    }

    // ═══════════════════════════════════════════════════════════════════
    // DAMAGE ROUTING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Fire onEnergyDamage on all handlers. Chain of responsibility — first true wins.
     */
    public boolean fireOnEnergyDamage(IEntity energyEntity, IEntityLivingBase owner,
                                       IEntityLivingBase target, float damage,
                                       float knockback, float knockbackUp,
                                       double kbDirX, double kbDirZ,
                                       float damageMultiplier,
                                       INbt damageData) {
        for (IEnergyExtender handler : extenders) {
            if (handler.onEnergyDamage(energyEntity, owner, target, damage,
                knockback, knockbackUp, kbDirX, kbDirZ, damageMultiplier, damageData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fire modifyEnergyDamage on all handlers. Cumulative — each handler's output feeds the next.
     */
    public float fireModifyEnergyDamage(IEntity energyEntity, IEntityLivingBase owner,
                                         float baseDamage, INbt damageData) {
        float damage = baseDamage;
        for (IEnergyExtender handler : extenders) {
            damage = handler.modifyEnergyDamage(energyEntity, owner, damage, damageData);
        }
        return damage;
    }

    // ═══════════════════════════════════════════════════════════════════
    // INTERNAL FACTORY METHODS (MC types)
    // ═══════════════════════════════════════════════════════════════════

    public EntityAbilityOrb createOrbInternal(IWorld IWorld, IEntity owner, double x, double y, double z, float size) {
        EntityAbilityOrb IEntity = new EntityAbilityOrb(IWorld);
        IEntity.setPosition(x, y, z);
        IEntity.setStartPosition(x, y, z);
        IEntity.setOwnerEntityId(owner.getEntityId());
        IEntity.setProjectileSize(size);
        return IEntity;
    }

    public EntityAbilityBeam createBeamInternal(IWorld IWorld, IEntity owner, double x, double y, double z,
                                                 float beamWidth, float headSize) {
        EntityAbilityBeam IEntity = new EntityAbilityBeam(IWorld);
        IEntity.setPosition(x, y, z);
        IEntity.setStartPosition(x, y, z);
        IEntity.setOwnerEntityId(owner.getEntityId());
        IEntity.setBeamWidth(beamWidth);
        IEntity.setHeadSize(headSize);
        return IEntity;
    }

    public EntityAbilityDisc createDiscInternal(IWorld IWorld, IEntity owner, double x, double y, double z,
                                                 float radius, float thickness) {
        EntityAbilityDisc IEntity = new EntityAbilityDisc(IWorld);
        IEntity.setPosition(x, y, z);
        IEntity.setStartPosition(x, y, z);
        IEntity.setOwnerEntityId(owner.getEntityId());
        IEntity.setDiscRadius(radius);
        IEntity.setDiscThickness(thickness);
        return IEntity;
    }

    public EntityAbilityLaser createLaserInternal(IWorld IWorld, IEntity owner, double x, double y, double z,
                                                   float laserWidth) {
        EntityAbilityLaser IEntity = new EntityAbilityLaser(IWorld);
        IEntity.setPosition(x, y, z);
        IEntity.setStartPosition(x, y, z);
        IEntity.setOwnerEntityId(owner.getEntityId());
        IEntity.setLaserWidth(laserWidth);
        return IEntity;
    }

    public EntityAbilityZone createHazardInternal(IWorld IWorld, IEntity owner, double x, double y, double z) {
        EntityAbilityZone IEntity = new EntityAbilityZone(IWorld);
        IEntity.initAsHazard(owner, x, y, z);
        return IEntity;
    }

    public EntityAbilityZone createTrapInternal(IWorld IWorld, IEntity owner, double x, double y, double z) {
        EntityAbilityZone IEntity = new EntityAbilityZone(IWorld);
        IEntity.initAsTrap(owner, x, y, z);
        return IEntity;
    }

    public EntityEnergySweeper createSweeperInternal(IWorld IWorld, IEntity owner, double x, double y, double z) {
        EntityEnergySweeper IEntity = new EntityEnergySweeper(IWorld);
        IEntity.setPosition(x, y, z);
        IEntity.setOwnerEntityId(owner.getEntityId());
        return IEntity;
    }

    public EntityEnergyPanel createPanelInternal(IWorld IWorld, IEntity owner, double x, double y, double z) {
        EntityEnergyPanel IEntity = new EntityEnergyPanel(IWorld);
        IEntity.setPosition(x, y, z);
        IEntity.setOwnerEntityId(owner.getEntityId());
        return IEntity;
    }

    public EntityEnergyExplosion createExplosionInternal(IWorld IWorld, IEntity owner, double x, double y, double z, float radius) {
        return new EntityEnergyExplosion(IWorld, owner, x, y, z, radius);
    }

    // ═══════════════════════════════════════════════════════════════════
    // IEnergyHandler (Script API methods)
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public IEnergyOrb createOrb(IWorld IWorld, IEntity owner, double x, double y, double z, float size) {
        EntityAbilityOrb IEntity = createOrbInternal((IWorld) IWorld.getMCWorld(), owner.getMCEntity(), x, y, z, size);
        return (IEnergyOrb) NpcAPI.Instance().getIEntity(IEntity);
    }

    @Override
    public IEnergyBeam createBeam(IWorld IWorld, IEntity owner, double x, double y, double z, float beamWidth, float headSize) {
        EntityAbilityBeam IEntity = createBeamInternal((IWorld) IWorld.getMCWorld(), owner.getMCEntity(), x, y, z, beamWidth, headSize);
        return (IEnergyBeam) NpcAPI.Instance().getIEntity(IEntity);
    }

    @Override
    public IEnergyDisc createDisc(IWorld IWorld, IEntity owner, double x, double y, double z, float radius, float thickness) {
        EntityAbilityDisc IEntity = createDiscInternal((IWorld) IWorld.getMCWorld(), owner.getMCEntity(), x, y, z, radius, thickness);
        return (IEnergyDisc) NpcAPI.Instance().getIEntity(IEntity);
    }

    @Override
    public IEnergyLaser createLaser(IWorld IWorld, IEntity owner, double x, double y, double z, float laserWidth) {
        EntityAbilityLaser IEntity = createLaserInternal((IWorld) IWorld.getMCWorld(), owner.getMCEntity(), x, y, z, laserWidth);
        return (IEnergyLaser) NpcAPI.Instance().getIEntity(IEntity);
    }

    @Override
    public IEnergyZone createHazard(IWorld IWorld, IEntity owner, double x, double y, double z) {
        EntityAbilityZone IEntity = createHazardInternal((IWorld) IWorld.getMCWorld(), owner.getMCEntity(), x, y, z);
        return (IEnergyZone) NpcAPI.Instance().getIEntity(IEntity);
    }

    @Override
    public IEnergyZone createTrap(IWorld IWorld, IEntity owner, double x, double y, double z) {
        EntityAbilityZone IEntity = createTrapInternal((IWorld) IWorld.getMCWorld(), owner.getMCEntity(), x, y, z);
        return (IEnergyZone) NpcAPI.Instance().getIEntity(IEntity);
    }

    @Override
    public IEnergySweeper createSweeper(IWorld IWorld, IEntity owner, double x, double y, double z) {
        EntityEnergySweeper IEntity = createSweeperInternal((IWorld) IWorld.getMCWorld(), owner.getMCEntity(), x, y, z);
        return (IEnergySweeper) NpcAPI.Instance().getIEntity(IEntity);
    }

    @Override
    public IEnergyPanel createPanel(IWorld IWorld, IEntity owner, double x, double y, double z) {
        EntityEnergyPanel IEntity = createPanelInternal((IWorld) IWorld.getMCWorld(), owner.getMCEntity(), x, y, z);
        return (IEnergyPanel) NpcAPI.Instance().getIEntity(IEntity);
    }

    @Override
    public IEnergyExplosion createExplosion(IWorld IWorld, IEntity owner, double x, double y, double z, float radius) {
        EntityEnergyExplosion IEntity = createExplosionInternal((IWorld) IWorld.getMCWorld(), owner.getMCEntity(), x, y, z, radius);
        return new ScriptEnergyExplosion<>(IEntity);
    }
}
