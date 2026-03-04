package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import noppes.npcs.api.ability.type.IAbilityVortex;
import noppes.npcs.client.gui.builder.FieldDef;

import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vortex ability: Pulls targets toward the caster.
 * Can pull single target or AOE, with optional damage and stun on arrival.
 */
public class AbilityVortex extends Ability implements IAbilityVortex {

    private float pullRadius = 8.0f;
    private float pullStrength = 0.8f;
    private float damage = 0.0f;
    private float knockback = 0.0f;
    private boolean aoe = true;
    private boolean damageOnPull = false;
    private float pullDamage = 0.0f;

    // Runtime state — keyed by entity ID (not UUID, since cloned NPCs share UUIDs)
    private transient Map<Integer, PullState> pulledEntities;
    private transient boolean pullComplete = false;
    private transient int ticksSincePullDamage = 0;

    private static class PullState {
        double lastX, lastY, lastZ;
        int stuckTicks;

        PullState(EntityLivingBase entity) {
            this.lastX = entity.posX;
            this.lastY = entity.posY;
            this.lastZ = entity.posZ;
            this.stuckTicks = 0;
        }
    }

    private Map<Integer, PullState> getPulledEntities() {
        if (pulledEntities == null) {
            pulledEntities = new HashMap<>();
        }
        return pulledEntities;
    }

    public AbilityVortex() {
        this.typeId = "ability.cnpc.vortex";
        this.name = "Vortex";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 15.0f;
        this.lockMovement = LockMode.WINDUP_AND_ACTIVE;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.telegraphType = TelegraphType.CIRCLE;
        this.windUpSound = "mob.ghast.charge";
        this.activeSound = "mob.ghast.fireball";
        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/vortex.png",
                this::getActiveColor)
        };
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
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        getPulledEntities().clear();
        pullComplete = false;
        ticksSincePullDamage = 0;

        if (!isPreview() && !caster.worldObj.isRemote) {
            AxisAlignedBB box = caster.boundingBox.expand(pullRadius, pullRadius / 2, pullRadius);
            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = caster.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);

            if (aoe) {
                // ALL: pull every valid enemy in range
                for (EntityLivingBase entity : entities) {
                    if (entity == caster) continue;
                    if (entity.isDead) continue;
                    if (!AbilityTargetHelper.shouldAffect(caster, entity, TargetFilter.ENEMIES, false)) continue;

                    double dist = caster.getDistanceToEntity(entity);
                    if (dist <= pullRadius) {
                        getPulledEntities().put(entity.getEntityId(), new PullState(entity));
                    }
                }
            } else {
                // SINGULAR: pull one random target within range
                // NPC: pull aggro target if valid, otherwise random enemy
                // Player: pull random enemy in range
                if (!isPlayerCaster(caster) && target != null && !target.isDead) {
                    double dist = caster.getDistanceToEntity(target);
                    if (dist <= pullRadius) {
                        getPulledEntities().put(target.getEntityId(), new PullState(target));
                        return;
                    }
                }

                // Collect all valid enemies, then pick one at random
                List<EntityLivingBase> validTargets = new ArrayList<>();
                for (EntityLivingBase entity : entities) {
                    if (entity == caster) continue;
                    if (entity.isDead) continue;
                    if (!AbilityTargetHelper.shouldAffect(caster, entity, TargetFilter.ENEMIES, false)) continue;

                    double dist = caster.getDistanceToEntity(entity);
                    if (dist <= pullRadius) {
                        validTargets.add(entity);
                    }
                }
                if (!validTargets.isEmpty()) {
                    EntityLivingBase chosen = validTargets.get(caster.worldObj.rand.nextInt(validTargets.size()));
                    getPulledEntities().put(chosen.getEntityId(), new PullState(chosen));
                }
            }
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (isPreview()) {
            // No entities to pull in preview, just run animation for a duration
            if (tick >= 60) signalCompletion();
            return;
        }

        if (pullComplete || getPulledEntities().isEmpty()) {
            signalCompletion();
            return;
        }

        if (caster.worldObj.isRemote) return;

        // Safety cap: force-complete if active too long
        int maxActiveTicks = Math.max(40, (int)(pullRadius / pullStrength * 3));
        if (tick >= maxActiveTicks) {
            for (Map.Entry<Integer, PullState> entry : new HashMap<>(getPulledEntities()).entrySet()) {
                EntityLivingBase entity = findEntity(caster.worldObj, entry.getKey());
                if (entity != null && !entity.isDead) {
                    onTargetArrived(caster, entity, caster.worldObj);
                }
            }
            getPulledEntities().clear();
            pullComplete = true;
            signalCompletion();
            return;
        }

        double destX = caster.posX;
        double destY = caster.posY;
        double destZ = caster.posZ;

        boolean anyStillPulling = false;
        ticksSincePullDamage++;
        boolean shouldDealPullDamage = damageOnPull && pullDamage > 0 && ticksSincePullDamage >= 10;
        if (shouldDealPullDamage) {
            ticksSincePullDamage = 0;
        }

        for (Map.Entry<Integer, PullState> entry : new HashMap<>(getPulledEntities()).entrySet()) {
            int entityId = entry.getKey();
            PullState state = entry.getValue();

            EntityLivingBase entity = findEntity(caster.worldObj, entityId);
            if (entity == null || entity.isDead) {
                getPulledEntities().remove(entityId);
                continue;
            }

            double dx = destX - entity.posX;
            double dy = destY - entity.posY;
            double dz = destZ - entity.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist <= 1.5) {
                getPulledEntities().remove(entityId);
                onTargetArrived(caster, entity, caster.worldObj);
                continue;
            }

            // Stuck detection: check if entity made progress since last tick
            double lastDx = destX - state.lastX;
            double lastDy = destY - state.lastY;
            double lastDz = destZ - state.lastZ;
            double lastDist = Math.sqrt(lastDx * lastDx + lastDy * lastDy + lastDz * lastDz);
            double progress = lastDist - dist; // positive = entity moved closer

            double expectedProgress = Math.min(pullStrength, dist * 0.5);
            if (progress < expectedProgress * 0.3) {
                state.stuckTicks++;
            } else {
                state.stuckTicks = 0;
            }

            if (state.stuckTicks >= 5) {
                // Entity is stuck (wall, block, partial obstruction) - treat as arrived
                getPulledEntities().remove(entityId);
                onTargetArrived(caster, entity, caster.worldObj);
                continue;
            }

            anyStillPulling = true;

            // Record position BEFORE movement so next tick's stuck detection
            // can measure the actual progress (pull + AI movement combined)
            state.lastX = entity.posX;
            state.lastY = entity.posY;
            state.lastZ = entity.posZ;

            // Clamp speed to never exceed half the remaining distance, preventing overshoot/slingshot
            double maxSpeed = dist * 0.5;
            double effectiveSpeed = Math.min(pullStrength, maxSpeed);
            double factor = effectiveSpeed / dist;
            double motionX = dx * factor;
            double motionY = dy * factor * 0.5;
            double motionZ = dz * factor;

            if (entity instanceof EntityPlayer) {
                // Players: send velocity packet — client applies movement
                entity.motionX = motionX;
                entity.motionY = motionY;
                entity.motionZ = motionZ;
                entity.velocityChanged = true;
            } else {
                // NPCs/mobs: directly apply movement with collision detection.
                // Setting motionX alone is unreliable because the entity's AI tick
                // (moveFlying/moveEntityWithHeading) can override the velocity before
                // moveEntity runs, depending on entity tick order relative to the caster.
                entity.moveEntity(motionX, motionY, motionZ);
            }

            if (shouldDealPullDamage) {
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
     * Find an entity by entity ID.
     */
    private EntityLivingBase findEntity(World world, int entityId) {
        Entity entity = world.getEntityByID(entityId);
        if (entity instanceof EntityLivingBase) {
            return (EntityLivingBase) entity;
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
        nbt.setBoolean("damageOnPull", damageOnPull);
        nbt.setFloat("pullDamage", pullDamage);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.pullRadius = nbt.getFloat("pullRadius");
        this.pullStrength = nbt.getFloat("pullStrength");
        this.damage = nbt.getFloat("damage");
        this.knockback = nbt.getFloat("knockback");
        this.aoe = !nbt.hasKey("aoe") || nbt.getBoolean("aoe");
        this.damageOnPull = nbt.getBoolean("damageOnPull");
        this.pullDamage = nbt.getFloat("pullDamage");
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

    @Override
    public float getDisplayDamage() { return damage; }

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
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
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
            FieldDef.section("ability.section.pullDamage"),
            FieldDef.boolField("gui.enabled", this::isDamageOnPull, this::setDamageOnPull)
                .hover("ability.hover.dmgOnPull"),
            FieldDef.floatField("enchantment.damage", this::getPullDamage, this::setPullDamage)
                .visibleWhen(this::isDamageOnPull),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));
    }
}
