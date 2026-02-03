package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import noppes.npcs.api.ability.type.IAbilityVortex;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Vortex ability: Pulls targets toward the caster.
 * Can pull single target or AOE, with optional damage and stun on arrival.
 */
public class AbilityVortex extends Ability implements IAbilityVortex {

    private float pullRadius = 8.0f;
    private float pullStrength = 0.8f;
    private float damage = 0.0f;
    private float knockback = 0.0f;
    private boolean aoe = false;
    private int maxTargets = 5;
    private boolean damageOnPull = false;
    private float pullDamage = 0.0f;

    // Runtime state
    private transient Set<UUID> pulledEntities;
    private transient boolean pullComplete = false;
    private transient int ticksSincePullDamage = 0;

    private Set<UUID> getPulledEntities() {
        if (pulledEntities == null) {
            pulledEntities = new HashSet<>();
        }
        return pulledEntities;
    }

    public AbilityVortex() {
        this.typeId = "ability.cnpc.vortex";
        this.name = "Vortex";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 15.0f;
        this.lockMovement = LockMovementType.WINDUP_AND_ACTIVE;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.telegraphType = TelegraphType.CIRCLE;
        this.windUpSound = "mob.ghast.charge";
        this.activeSound = "mob.ghast.fireball";
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AOE_SELF};
    }

    @Override
    public float getTelegraphRadius() {
        return pullRadius;
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        getPulledEntities().clear();
        pullComplete = false;
        ticksSincePullDamage = 0;

        if (aoe) {
            AxisAlignedBB box = caster.boundingBox.expand(pullRadius, pullRadius / 2, pullRadius);
            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);

            int count = 0;
            for (EntityLivingBase entity : entities) {
                if (entity == caster) continue;
                if (entity.isDead) continue;

                double dist = caster.getDistanceToEntity(entity);
                if (dist <= pullRadius) {
                    getPulledEntities().add(entity.getUniqueID());
                    count++;
                    if (count >= maxTargets) break;
                }
            }
        } else {
            // Single target mode - still check pullRadius
            if (target != null && !target.isDead) {
                double dist = caster.getDistanceToEntity(target);
                if (dist <= pullRadius) {
                    getPulledEntities().add(target.getUniqueID());
                }
            }
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (pullComplete || getPulledEntities().isEmpty()) {
            signalCompletion();
            return;
        }

        double destX = caster.posX;
        double destY = caster.posY;
        double destZ = caster.posZ;

        boolean anyStillPulling = false;
        ticksSincePullDamage++;

        for (UUID uuid : new HashSet<>(getPulledEntities())) {
            EntityLivingBase entity = findEntity(caster, world, uuid);
            if (entity == null || entity.isDead) {
                getPulledEntities().remove(uuid);
                continue;
            }

            double dx = destX - entity.posX;
            double dy = destY - entity.posY;
            double dz = destZ - entity.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist <= 1.0f) {
                getPulledEntities().remove(uuid);
                onTargetArrived(caster, entity, world);
                continue;
            }

            anyStillPulling = true;

            double factor = pullStrength / dist;
            double nextX = dx * factor;
            double nextY = dy * factor * 0.5;
            double nextZ = dz * factor;

            AxisAlignedBB nextBox = entity.boundingBox.copy().offset(nextX, nextY, nextZ);
            if (!world.getCollidingBoundingBoxes(entity, nextBox).isEmpty()) {
                getPulledEntities().remove(uuid);
                continue;
            }

            entity.motionX = nextX;
            entity.motionY = nextY;
            entity.motionZ = nextZ;
            entity.velocityChanged = true;

            if (damageOnPull && pullDamage > 0 && ticksSincePullDamage >= 10) {
                ticksSincePullDamage = 0;
                entity.hurtResistantTime = 0;
                applyAbilityDamage(caster, entity, pullDamage * 0.5f, 0);
            }
        }

        if (!anyStillPulling && getPulledEntities().isEmpty()) {
            pullComplete = true;
            signalCompletion();
        }
    }

    private void onTargetArrived(EntityLivingBase caster, EntityLivingBase entity, World world) {
        // Apply damage with scripted event support
        boolean wasHit = applyAbilityDamage(caster, entity, damage, knockback * 0.5f);

        // Only apply effects if hit wasn't cancelled
        if (wasHit) {
            applyEffects(entity);
        }
    }

    /**
     * Find an entity by UUID within the vortex pull area.
     * Uses AABB search instead of iterating all loaded entities.
     */
    @SuppressWarnings("unchecked")
    private EntityLivingBase findEntity(EntityLivingBase caster, World world, UUID uuid) {
        // Search within pull radius (entities being pulled should be within this area)
        AxisAlignedBB searchBox = caster.boundingBox.expand(pullRadius, pullRadius / 2, pullRadius);
        List<EntityLivingBase> nearbyEntities = world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

        for (EntityLivingBase entity : nearbyEntities) {
            if (entity.getUniqueID().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public void cleanup() {
        getPulledEntities().clear();
        pullComplete = false;
        ticksSincePullDamage = 0;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("pullRadius", pullRadius);
        nbt.setFloat("pullStrength", pullStrength);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setBoolean("aoe", aoe);
        nbt.setInteger("maxTargets", maxTargets);
        nbt.setBoolean("damageOnPull", damageOnPull);
        nbt.setFloat("pullDamage", pullDamage);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.pullRadius = nbt.hasKey("pullRadius") ? nbt.getFloat("pullRadius") : 8.0f;
        this.pullStrength = nbt.hasKey("pullStrength") ? nbt.getFloat("pullStrength") : 0.8f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 0.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 0.0f;
        this.aoe = nbt.hasKey("aoe") && nbt.getBoolean("aoe");
        this.maxTargets = nbt.hasKey("maxTargets") ? nbt.getInteger("maxTargets") : 5;
        this.damageOnPull = nbt.hasKey("damageOnPull") && nbt.getBoolean("damageOnPull");
        this.pullDamage = nbt.hasKey("pullDamage") ? nbt.getFloat("pullDamage") : 0.0f;
    }

    // Getters & Setters
    public float getPullRadius() {
        return pullRadius;
    }

    public void setPullRadius(float pullRadius) {
        this.pullRadius = pullRadius;
    }

    public float getPullStrength() {
        return pullStrength;
    }

    public void setPullStrength(float pullStrength) {
        this.pullStrength = pullStrength;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getKnockback() {
        return knockback;
    }

    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }

    public boolean isAoe() {
        return aoe;
    }

    public void setAoe(boolean aoe) {
        this.aoe = aoe;
    }

    public int getMaxTargets() {
        return maxTargets;
    }

    public void setMaxTargets(int maxTargets) {
        this.maxTargets = maxTargets;
    }

    public boolean isDamageOnPull() {
        return damageOnPull;
    }

    public void setDamageOnPull(boolean damageOnPull) {
        this.damageOnPull = damageOnPull;
    }

    public float getPullDamage() {
        return pullDamage;
    }

    public void setPullDamage(float pullDamage) {
        this.pullDamage = pullDamage;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<FieldDef> getFieldDefinitions() {
        return Arrays.asList(
            FieldDef.row(
                FieldDef.floatField("ability.pullRadius", this::getPullRadius, this::setPullRadius),
                FieldDef.floatField("ability.pullStrength", this::getPullStrength, this::setPullStrength)
            ),
            FieldDef.section("ability.section.damage"),
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback)
            ),
            FieldDef.section("ability.section.aoe"),
            FieldDef.boolField("gui.enabled", this::isAoe, this::setAoe)
                .hover("ability.hover.aoe"),
            FieldDef.intField("ability.maxTargets", this::getMaxTargets, this::setMaxTargets)
                .visibleWhen(this::isAoe),
            FieldDef.section("ability.section.pullDamage"),
            FieldDef.boolField("gui.enabled", this::isDamageOnPull, this::setDamageOnPull)
                .hover("ability.hover.dmgOnPull"),
            FieldDef.floatField("enchantment.damage", this::getPullDamage, this::setPullDamage)
                .visibleWhen(this::isDamageOnPull),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        );
    }
}
