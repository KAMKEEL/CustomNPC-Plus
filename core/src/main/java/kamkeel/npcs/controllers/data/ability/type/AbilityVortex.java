package kamkeel.npcs.controllers.data.ability.type;

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
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import noppes.npcs.api.ability.type.IAbilityVortex;
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

    // Runtime state — keyed by IEntity ID (not UUID, since cloned NPCs share UUIDs)
    private transient Map<Integer, PullState> pulledEntities;
    private transient boolean pullComplete = false;
    private transient int ticksSincePullDamage = 0;

    private static class PullState {
        double lastX, lastY, lastZ;
        int stuckTicks;

        PullState(IEntityLivingBase IEntity) {
            this.lastX = IEntity.posX;
            this.lastY = IEntity.posY;
            this.lastZ = IEntity.posZ;
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
    public void onExecute(IEntityLivingBase caster, IEntityLivingBase target) {
        getPulledEntities().clear();
        pullComplete = false;
        ticksSincePullDamage = 0;

        if (!isPreview() && !caster.worldObj.isRemote) {
            IBoundingBox box = caster.boundingBox.expand(pullRadius, pullRadius / 2, pullRadius);
            @SuppressWarnings("unchecked")
            List<IEntityLivingBase> entities = caster.worldObj.getEntitiesWithinAABB(IEntityLivingBase.class, box);

            if (aoe) {
                // ALL: pull every valid enemy in range
                for (IEntityLivingBase IEntity : entities) {
                    if (IEntity == caster) continue;
                    if (IEntity.isDead) continue;
                    if (!AbilityTargetHelper.shouldAffect(caster, IEntity, TargetFilter.ENEMIES, false)) continue;

                    double dist = caster.getDistanceToEntity(IEntity);
                    if (dist <= pullRadius) {
                        getPulledEntities().put(IEntity.getEntityId(), new PullState(IEntity));
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
                List<IEntityLivingBase> validTargets = new ArrayList<>();
                for (IEntityLivingBase IEntity : entities) {
                    if (IEntity == caster) continue;
                    if (IEntity.isDead) continue;
                    if (!AbilityTargetHelper.shouldAffect(caster, IEntity, TargetFilter.ENEMIES, false)) continue;

                    double dist = caster.getDistanceToEntity(IEntity);
                    if (dist <= pullRadius) {
                        validTargets.add(IEntity);
                    }
                }
                if (!validTargets.isEmpty()) {
                    IEntityLivingBase chosen = validTargets.get(caster.worldObj.rand.nextInt(validTargets.size()));
                    getPulledEntities().put(chosen.getEntityId(), new PullState(chosen));
                }
            }
        }
    }

    @Override
    public void onActiveTick(IEntityLivingBase caster, IEntityLivingBase target, int tick) {
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
                IEntityLivingBase IEntity = findEntity(caster.worldObj, entry.getKey());
                if (IEntity != null && !IEntity.isDead) {
                    onTargetArrived(caster, IEntity, caster.worldObj);
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

            IEntityLivingBase IEntity = findEntity(caster.worldObj, entityId);
            if (IEntity == null || IEntity.isDead) {
                getPulledEntities().remove(entityId);
                continue;
            }

            double dx = destX - IEntity.posX;
            double dy = destY - IEntity.posY;
            double dz = destZ - IEntity.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist <= 1.5) {
                getPulledEntities().remove(entityId);
                onTargetArrived(caster, IEntity, caster.worldObj);
                continue;
            }

            // Stuck detection: check if IEntity made progress since last tick
            double lastDx = destX - state.lastX;
            double lastDy = destY - state.lastY;
            double lastDz = destZ - state.lastZ;
            double lastDist = Math.sqrt(lastDx * lastDx + lastDy * lastDy + lastDz * lastDz);
            double progress = lastDist - dist; // positive = IEntity moved closer

            double expectedProgress = Math.min(pullStrength, dist * 0.5);
            if (progress < expectedProgress * 0.3) {
                state.stuckTicks++;
            } else {
                state.stuckTicks = 0;
            }

            if (state.stuckTicks >= 5) {
                // IEntity is stuck (wall, block, partial obstruction) - treat as arrived
                getPulledEntities().remove(entityId);
                onTargetArrived(caster, IEntity, caster.worldObj);
                continue;
            }

            anyStillPulling = true;

            // Record position BEFORE movement so next tick's stuck detection
            // can measure the actual progress (pull + AI movement combined)
            state.lastX = IEntity.posX;
            state.lastY = IEntity.posY;
            state.lastZ = IEntity.posZ;

            // Clamp speed to never exceed half the remaining distance, preventing overshoot/slingshot
            double maxSpeed = dist * 0.5;
            double effectiveSpeed = Math.min(pullStrength, maxSpeed);
            double factor = effectiveSpeed / dist;
            double motionX = dx * factor;
            double motionY = dy * factor * 0.5;
            double motionZ = dz * factor;

            if (IEntity instanceof IPlayer) {
                // Players: send velocity packet — client applies movement
                IEntity.motionX = motionX;
                IEntity.motionY = motionY;
                IEntity.motionZ = motionZ;
                IEntity.velocityChanged = true;
            } else {
                // NPCs/mobs: directly apply movement with collision detection.
                // Setting motionX alone is unreliable because the IEntity's AI tick
                // (moveFlying/moveEntityWithHeading) can override the velocity before
                // moveEntity runs, depending on IEntity tick order relative to the caster.
                IEntity.moveEntity(motionX, motionY, motionZ);
            }

            if (shouldDealPullDamage) {
                applyAbilityDamage(caster, IEntity, pullDamage * 0.5f, 0);
            }
        }

        if (!anyStillPulling && getPulledEntities().isEmpty()) {
            pullComplete = true;
            signalCompletion();
        }
    }

    private void onTargetArrived(IEntityLivingBase caster, IEntityLivingBase IEntity, IWorld IWorld) {
        // Apply damage with scripted event support
        boolean wasHit = applyAbilityDamage(caster, IEntity, damage, knockback * 0.5f);

        // Only apply effects if hit wasn't cancelled
        if (wasHit) {
            applyEffects(IEntity);
        }
    }

    /**
     * Find an IEntity by IEntity ID.
     */
    private IEntityLivingBase findEntity(IWorld IWorld, int entityId) {
        IEntity IEntity = IWorld.getEntityByID(entityId);
        if (IEntity instanceof IEntityLivingBase) {
            return (IEntityLivingBase) IEntity;
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
    public void writeTypeNBT(INbt nbt) {
        nbt.setFloat("pullRadius", pullRadius);
        nbt.setFloat("pullStrength", pullStrength);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setBoolean("aoe", aoe);
        nbt.setBoolean("damageOnPull", damageOnPull);
        nbt.setFloat("pullDamage", pullDamage);
    }

    @Override
    public void readTypeNBT(INbt nbt) {
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

    @ClientOnly
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
