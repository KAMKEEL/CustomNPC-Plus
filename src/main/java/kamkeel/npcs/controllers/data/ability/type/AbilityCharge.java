package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetFilter;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import noppes.npcs.api.ability.type.IAbilityCharge;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Charge ability: Rush attack where the caster charges in a line, damaging all targets hit.
 * Extends AbilityMovement for shared direction locking, stall detection, and velocity application.
 */
public class AbilityCharge extends AbilityMovement implements IAbilityCharge {

    // Type-specific parameters
    private float chargeSpeed = 0.8f;
    private float damage = 8.0f;
    private float knockback = 3.0f;
    private float hitWidth = 1.5f;

    // Type-specific runtime state
    private transient Set<Integer> hitEntities = new HashSet<>();

    public AbilityCharge() {
        this.typeId = "ability.cnpc.charge";
        this.name = "Charge";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.minRange = 4.0f;
        this.lockMovement = LockMovementType.WINDUP;
        this.cooldownTicks = 0;
        this.windUpTicks = 20;
        // LINE telegraph showing charge path
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        this.windUpSound = "mob.zombie.wood";
        this.activeSound = "mob.zombie.attack";
        this.windUpAnimationName = "Ability_Charge_Windup";
        this.activeAnimationName = "Ability_Charge_Active";
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    /**
     * During windup, NPCs face toward their target for animation purposes.
     * Players aim freely — the telegraph follows their look direction on the client,
     * and direction is locked at the end of windup in onExecute().
     */
    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (!isPlayerCaster(caster) && target != null) {
            lockDirectionToTarget(caster, target);
            enforceLockedRotation(caster);
        }
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        // Lock final direction at end of windup so it matches the telegraph
        lockDirection(caster, target);
        initMovement(caster, maxRange, chargeSpeed);
        hitEntities.clear();

        enforceLockedRotation(caster);
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (checkTimeout(tick)) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }

        if (movementDirection == null) {
            signalCompletion();
            return;
        }

        if (!isPreview()) {
            enforceLockedRotation(caster);
        }

        if (checkStall(caster, tick)) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }
        updatePrevPosition(caster);

        if (getDistanceTraveled(caster) >= maxRange) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }

        if (checkBlocked(caster, chargeSpeed)) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }

        // Move caster (flat: zero vertical motion)
        applyVelocityFlat(caster, chargeSpeed);

        // Server-side collision damage (skip in preview)
        if (!caster.worldObj.isRemote && !isPreview()) {
            AxisAlignedBB hitBox = caster.boundingBox.expand(hitWidth, hitWidth * 0.5, hitWidth);

            @SuppressWarnings("unchecked")
            List<Entity> entities = caster.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

            for (Entity entity : entities) {
                if (!(entity instanceof EntityLivingBase)) continue;
                if (entity == caster) continue;
                if (hitEntities.contains(entity.getEntityId())) continue;
                if (!AbilityTargetHelper.shouldAffect(caster, entity, TargetFilter.ENEMIES, false)) continue;

                EntityLivingBase livingEntity = (EntityLivingBase) entity;

                hitEntities.add(entity.getEntityId());

                boolean wasHit = applyAbilityDamageWithDirection(caster, livingEntity, damage, knockback,
                    movementDirection.xCoord, movementDirection.zCoord);

                if (wasHit) {
                    applyEffects(livingEntity);
                    caster.worldObj.playSoundAtEntity(livingEntity, "random.explode", 0.5f, 1.2f);
                }
            }
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        stopMomentum(caster);
        super.onComplete(caster, target);
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, net.minecraft.util.DamageSource source, float damage) {
        stopMomentum(caster);
        super.onInterrupt(caster, source, damage);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        hitEntities.clear();
    }

    @Override
    public void resetForBurst() {
        super.resetForBurst();
        hitEntities.clear();
    }

    @Override
    public float getTelegraphLength() {
        return maxRange;
    }

    @Override
    public float getTelegraphWidth() {
        return hitWidth * 2;
    }

    /**
     * Creates a LINE telegraph from caster position towards target.
     * Telegraph follows caster during windup (so caster can reposition before charging).
     * Direction is locked at creation based on target position.
     */
    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!isShowTelegraph() || getTelegraphType() == TelegraphType.NONE) {
            return null;
        }

        // NPC: telegraph points toward aggro target
        // Player: telegraph points in look direction
        float yaw;
        if (!isPlayerCaster(caster) && target != null) {
            double dx = target.posX - caster.posX;
            double dz = target.posZ - caster.posZ;
            yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            yaw = caster.rotationYaw;
        }

        // Create LINE telegraph
        Telegraph telegraph = Telegraph.line(getTelegraphLength(), getTelegraphWidth());
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        // Position at caster ground level, direction towards target
        double groundY = findGroundLevel(caster.worldObj, caster.posX, caster.posY, caster.posZ);
        TelegraphInstance instance = new TelegraphInstance(telegraph, caster.posX, groundY, caster.posZ, yaw);
        instance.setCasterEntityId(caster.getEntityId());
        // Telegraph follows caster during windup - allows caster to reposition
        instance.setEntityIdToFollow(caster.getEntityId());

        // For player casters, track their rotation so telegraph follows look direction
        if (isPlayerCaster(caster)) {
            instance.setTrackFollowedEntityYaw(true);
        }

        return instance;
    }

    @Override
    public int getMaxPreviewDuration() {
        return (int) Math.ceil(maxRange / chargeSpeed) + 5;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("chargeSpeed", chargeSpeed);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("hitWidth", hitWidth);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.chargeSpeed = nbt.getFloat("chargeSpeed");
        this.damage = nbt.getFloat("damage");
        this.knockback = nbt.getFloat("knockback");
        this.hitWidth = nbt.getFloat("hitWidth");
    }

    // Getters & Setters
    public float getChargeSpeed() {
        return chargeSpeed;
    }

    public void setChargeSpeed(float chargeSpeed) {
        this.chargeSpeed = chargeSpeed;
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

    public float getHitWidth() {
        return hitWidth;
    }

    public void setHitWidth(float hitWidth) {
        this.hitWidth = hitWidth;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.floatField("ability.chargeSpeed", this::getChargeSpeed, this::setChargeSpeed)
            ),
            FieldDef.row(
                FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback),
                FieldDef.floatField("ability.hitWidth", this::getHitWidth, this::setHitWidth)
            ),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));
    }
}
