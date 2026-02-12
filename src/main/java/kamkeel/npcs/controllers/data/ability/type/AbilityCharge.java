package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import noppes.npcs.api.ability.type.IAbilityCharge;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Charge ability: Rush attack where NPC charges in a line, damaging all targets hit.
 */
public class AbilityCharge extends Ability implements IAbilityCharge {

    // Type-specific parameters
    private float chargeSpeed = 0.8f;
    private float damage = 8.0f;
    private float knockback = 3.0f;
    private float hitWidth = 1.5f;

    // Runtime state (transient)
    private transient double startX, startY, startZ;
    private transient double prevTickX, prevTickZ;
    private transient Vec3 chargeDirection;
    private transient Set<Integer> hitEntities = new HashSet<>();
    private transient float lockedYaw;
    private transient int maxActiveTicks;

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
        this.activeAnimationName = "Ability_Active_Windup";
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public boolean hasAbilityMovement() {
        return true; // This ability moves the NPC
    }

    /**
     * Called on the first tick of windup - lock direction here so it matches telegraph.
     */
    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (tick == 1) {
            // Lock direction on first windup tick (same time telegraph is created)
            lockChargeDirection(caster, target);
        }
        // Keep caster facing the locked direction during windup
        enforceLockedRotation(caster);
    }

    /**
     * Locks the charge direction. Called once at windup start — direction won't change even if target moves.
     * NPC: charges toward aggro target.
     * Player: charges in look direction.
     */
    private void lockChargeDirection(EntityLivingBase caster, EntityLivingBase target) {
        if (!isPlayerCaster(caster) && target != null) {
            // NPC: charge toward aggro target
            double dx = target.posX - caster.posX;
            double dz = target.posZ - caster.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0) {
                chargeDirection = Vec3.createVectorHelper(dx / len, 0, dz / len);
            } else {
                float yaw = (float) Math.toRadians(caster.rotationYaw);
                chargeDirection = Vec3.createVectorHelper(-Math.sin(yaw), 0, Math.cos(yaw));
            }
        } else {
            // Player: charge in look direction
            float yaw = (float) Math.toRadians(caster.rotationYaw);
            chargeDirection = Vec3.createVectorHelper(-Math.sin(yaw), 0, Math.cos(yaw));
        }
        lockedYaw = (float) Math.toDegrees(Math.atan2(-chargeDirection.xCoord, chargeDirection.zCoord));
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        // Initialize charge - direction was already locked during windup
        startX = caster.posX;
        startY = caster.posY;
        startZ = caster.posZ;
        prevTickX = caster.posX;
        prevTickZ = caster.posZ;
        hitEntities.clear();

        // Safety timeout: expected ticks + buffer to prevent infinite charge
        maxActiveTicks = chargeSpeed > 0 ? (int)(maxRange / chargeSpeed) + 10 : 10;

        // If direction wasn't set during windup (shouldn't happen), set it now
        if (chargeDirection == null) {
            lockChargeDirection(caster, target);
        }

        enforceLockedRotation(caster);
    }

    private void enforceLockedRotation(EntityLivingBase caster) {
        caster.rotationYaw = lockedYaw;
        caster.rotationYawHead = lockedYaw;
        caster.prevRotationYaw = lockedYaw;
        caster.prevRotationYawHead = lockedYaw;
        caster.renderYawOffset = lockedYaw;
        caster.prevRenderYawOffset = lockedYaw;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Safety timeout or missing state: force-complete to prevent stuck NPC
        if (!isPreview() && (chargeDirection == null || tick > maxActiveTicks)) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }

        if (chargeDirection == null) {
            signalCompletion();
            return;
        }

        // Enforce rotation every tick
        if (!isPreview()) {
            enforceLockedRotation(caster);
        }

        // Stall detection: if entity hasn't moved since last tick, it's stuck against a wall
        if (!isPreview() && tick > 1) {
            double dx = caster.posX - prevTickX;
            double dz = caster.posZ - prevTickZ;
            if (dx * dx + dz * dz < 0.0001) {
                stopMomentum(caster);
                signalCompletion();
                return;
            }
        }
        prevTickX = caster.posX;
        prevTickZ = caster.posZ;

        // Calculate distance traveled
        double distanceTraveled = Math.sqrt(
            Math.pow(caster.posX - startX, 2) +
                Math.pow(caster.posZ - startZ, 2)
        );

        // Check if reached max distance
        if (distanceTraveled >= maxRange) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }

        // Block detection (skip in preview - no real world collision)
        if (!isPreview() && isChargeBlocked(caster)) {
            stopMomentum(caster);
            signalCompletion();
            return;
        }

        // Move caster
        caster.motionX = chargeDirection.xCoord * chargeSpeed;
        caster.motionY = 0;
        caster.motionZ = chargeDirection.zCoord * chargeSpeed;
        if (!isPreview()) {
            caster.velocityChanged = true;
        }

        // Server-side collision damage (skip in preview)
        if (!world.isRemote && !isPreview()) {
            AxisAlignedBB hitBox = caster.boundingBox.expand(hitWidth, hitWidth * 0.5, hitWidth);

            @SuppressWarnings("unchecked")
            List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

            for (Entity entity : entities) {
                if (!(entity instanceof EntityLivingBase)) continue;
                if (entity == caster) continue;
                if (hitEntities.contains(entity.getEntityId())) continue;

                EntityLivingBase livingEntity = (EntityLivingBase) entity;

                // Hit this entity
                hitEntities.add(entity.getEntityId());

                // Apply damage with scripted event support
                boolean wasHit = applyAbilityDamageWithDirection(caster, livingEntity, damage, knockback,
                    chargeDirection.xCoord, chargeDirection.zCoord);

                // Play impact sound if hit wasn't cancelled
                if (wasHit) {
                    world.playSoundAtEntity(livingEntity, "random.explode", 0.5f, 1.2f);
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

    private void stopMomentum(EntityLivingBase caster) {
        caster.motionX = 0;
        caster.motionZ = 0;
        if (!isPreview()) {
            caster.velocityChanged = true;
        }
    }

    private boolean isChargeBlocked(EntityLivingBase caster) {
        double nextX = chargeDirection.xCoord * chargeSpeed;
        double nextZ = chargeDirection.zCoord * chargeSpeed;
        AxisAlignedBB nextBox = caster.boundingBox.copy().offset(nextX, 0, nextZ);
        return !caster.worldObj.getCollidingBoundingBoxes(caster, nextBox).isEmpty();
    }

    @Override
    public void reset() {
        super.reset();
        chargeDirection = null;
        hitEntities.clear();
        maxActiveTicks = 0;
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
        this.chargeSpeed = nbt.hasKey("chargeSpeed") ? nbt.getFloat("chargeSpeed") : 0.8f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 8.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 3.0f;
        if (nbt.hasKey("hitWidth")) {
            this.hitWidth = nbt.getFloat("hitWidth");
        } else if (nbt.hasKey("hitRadius")) {
            this.hitWidth = nbt.getFloat("hitRadius");
        } else {
            this.hitWidth = 1.5f;
        }
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
