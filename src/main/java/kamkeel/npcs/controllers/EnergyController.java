package kamkeel.npcs.controllers;

import kamkeel.npcs.controllers.data.energy.IEnergyExtender;
import kamkeel.npcs.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.api.IEnergyHandler;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.*;
import noppes.npcs.api.entity.IEnergyExplosion;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.entity.ScriptEnergyExplosion;

import java.util.ArrayList;
import java.util.List;

/**
 * Central controller for script-created energy entities and handler registration.
 * Provides factory methods for creating all energy entity types and routes
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
    public boolean fireOnEnergyDamage(Entity energyEntity, EntityLivingBase owner,
                                       EntityLivingBase target, float damage,
                                       float knockback, float knockbackUp,
                                       double kbDirX, double kbDirZ,
                                       float damageMultiplier,
                                       NBTTagCompound damageData) {
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
    public float fireModifyEnergyDamage(Entity energyEntity, EntityLivingBase owner,
                                         float baseDamage, NBTTagCompound damageData) {
        float damage = baseDamage;
        for (IEnergyExtender handler : extenders) {
            damage = handler.modifyEnergyDamage(energyEntity, owner, damage, damageData);
        }
        return damage;
    }

    // ═══════════════════════════════════════════════════════════════════
    // INTERNAL FACTORY METHODS (MC types)
    // ═══════════════════════════════════════════════════════════════════

    public EntityAbilityOrb createOrbInternal(World world, Entity owner, double x, double y, double z, float size) {
        EntityAbilityOrb entity = new EntityAbilityOrb(world);
        entity.setPosition(x, y, z);
        entity.setStartPosition(x, y, z);
        entity.setOwnerEntityId(owner.getEntityId());
        entity.setProjectileSize(size);
        return entity;
    }

    public EntityAbilityBeam createBeamInternal(World world, Entity owner, double x, double y, double z,
                                                 float beamWidth, float headSize) {
        EntityAbilityBeam entity = new EntityAbilityBeam(world);
        entity.setPosition(x, y, z);
        entity.setStartPosition(x, y, z);
        entity.setOwnerEntityId(owner.getEntityId());
        entity.setBeamWidth(beamWidth);
        entity.setHeadSize(headSize);
        return entity;
    }

    public EntityAbilityDisc createDiscInternal(World world, Entity owner, double x, double y, double z,
                                                 float radius, float thickness) {
        EntityAbilityDisc entity = new EntityAbilityDisc(world);
        entity.setPosition(x, y, z);
        entity.setStartPosition(x, y, z);
        entity.setOwnerEntityId(owner.getEntityId());
        entity.setDiscRadius(radius);
        entity.setDiscThickness(thickness);
        return entity;
    }

    public EntityAbilityLaser createLaserInternal(World world, Entity owner, double x, double y, double z,
                                                   float laserWidth) {
        EntityAbilityLaser entity = new EntityAbilityLaser(world);
        entity.setPosition(x, y, z);
        entity.setStartPosition(x, y, z);
        entity.setOwnerEntityId(owner.getEntityId());
        entity.setLaserWidth(laserWidth);
        return entity;
    }

    public EntityAbilityZone createHazardInternal(World world, Entity owner, double x, double y, double z) {
        EntityAbilityZone entity = new EntityAbilityZone(world);
        entity.initAsHazard(owner, x, y, z);
        return entity;
    }

    public EntityAbilityZone createTrapInternal(World world, Entity owner, double x, double y, double z) {
        EntityAbilityZone entity = new EntityAbilityZone(world);
        entity.initAsTrap(owner, x, y, z);
        return entity;
    }

    public EntityEnergySweeper createSweeperInternal(World world, Entity owner, double x, double y, double z) {
        EntityEnergySweeper entity = new EntityEnergySweeper(world);
        entity.setPosition(x, y, z);
        entity.setOwnerEntityId(owner.getEntityId());
        return entity;
    }

    public EntityEnergyPanel createPanelInternal(World world, Entity owner, double x, double y, double z) {
        EntityEnergyPanel entity = new EntityEnergyPanel(world);
        entity.setPosition(x, y, z);
        entity.setOwnerEntityId(owner.getEntityId());
        return entity;
    }

    public EntityEnergyExplosion createExplosionInternal(World world, Entity owner, double x, double y, double z, float radius) {
        return new EntityEnergyExplosion(world, owner, x, y, z, radius);
    }

    // ═══════════════════════════════════════════════════════════════════
    // IEnergyHandler (Script API methods)
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public IEnergyOrb createOrb(IWorld world, IEntity owner, double x, double y, double z, float size) {
        EntityAbilityOrb entity = createOrbInternal((World) world.getMCWorld(), owner.getMCEntity(), x, y, z, size);
        return (IEnergyOrb) NpcAPI.Instance().getIEntity(entity);
    }

    @Override
    public IEnergyBeam createBeam(IWorld world, IEntity owner, double x, double y, double z, float beamWidth, float headSize) {
        EntityAbilityBeam entity = createBeamInternal((World) world.getMCWorld(), owner.getMCEntity(), x, y, z, beamWidth, headSize);
        return (IEnergyBeam) NpcAPI.Instance().getIEntity(entity);
    }

    @Override
    public IEnergyDisc createDisc(IWorld world, IEntity owner, double x, double y, double z, float radius, float thickness) {
        EntityAbilityDisc entity = createDiscInternal((World) world.getMCWorld(), owner.getMCEntity(), x, y, z, radius, thickness);
        return (IEnergyDisc) NpcAPI.Instance().getIEntity(entity);
    }

    @Override
    public IEnergyLaser createLaser(IWorld world, IEntity owner, double x, double y, double z, float laserWidth) {
        EntityAbilityLaser entity = createLaserInternal((World) world.getMCWorld(), owner.getMCEntity(), x, y, z, laserWidth);
        return (IEnergyLaser) NpcAPI.Instance().getIEntity(entity);
    }

    @Override
    public IEnergyZone createHazard(IWorld world, IEntity owner, double x, double y, double z) {
        EntityAbilityZone entity = createHazardInternal((World) world.getMCWorld(), owner.getMCEntity(), x, y, z);
        return (IEnergyZone) NpcAPI.Instance().getIEntity(entity);
    }

    @Override
    public IEnergyZone createTrap(IWorld world, IEntity owner, double x, double y, double z) {
        EntityAbilityZone entity = createTrapInternal((World) world.getMCWorld(), owner.getMCEntity(), x, y, z);
        return (IEnergyZone) NpcAPI.Instance().getIEntity(entity);
    }

    @Override
    public IEnergySweeper createSweeper(IWorld world, IEntity owner, double x, double y, double z) {
        EntityEnergySweeper entity = createSweeperInternal((World) world.getMCWorld(), owner.getMCEntity(), x, y, z);
        return (IEnergySweeper) NpcAPI.Instance().getIEntity(entity);
    }

    @Override
    public IEnergyPanel createPanel(IWorld world, IEntity owner, double x, double y, double z) {
        EntityEnergyPanel entity = createPanelInternal((World) world.getMCWorld(), owner.getMCEntity(), x, y, z);
        return (IEnergyPanel) NpcAPI.Instance().getIEntity(entity);
    }

    @Override
    public IEnergyExplosion createExplosion(IWorld world, IEntity owner, double x, double y, double z, float radius) {
        EntityEnergyExplosion entity = createExplosionInternal((World) world.getMCWorld(), owner.getMCEntity(), x, y, z, radius);
        return new ScriptEnergyExplosion<>(entity);
    }
}
