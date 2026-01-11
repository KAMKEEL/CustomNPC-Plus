package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.api.ability.IAbilityHolder;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityCharge;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Charge ability: Rush attack where NPC charges in a line, damaging all targets hit.
 */
public class AbilityCharge extends Ability {

    // Type-specific parameters
    private float chargeSpeed = 0.8f;
    private float damage = 15.0f;
    private float knockback = 3.0f;
    private float knockbackUp = 0.3f;
    private float maxDistance = 20.0f;
    private float hitRadius = 1.5f;

    // Runtime state (transient)
    private transient double startX, startY, startZ;
    private transient Vec3 chargeDirection;
    private transient Set<Integer> hitEntities = new HashSet<>();
    private transient float lockedYaw;

    public AbilityCharge() {
        this.typeId = "ability.cnpc.charge";
        this.name = "Charge";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.minRange = 4.0f;
        this.lockMovement = false; // Movement IS the ability
        this.cooldownTicks = 80;
        this.windUpTicks = 20;
        this.activeTicks = 40;
        this.recoveryTicks = 20;
        // LINE telegraph showing charge path
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityCharge(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AGGRO_TARGET };
    }

    @Override
    public boolean hasAbilityMovement() {
        return true; // This ability moves the NPC
    }

    /**
     * Called on the first tick of windup - lock direction here so it matches telegraph.
     */
    @Override
    public void onWindUpTick(IAbilityHolder holder, EntityLivingBase target, World world, int tick) {
        if (tick == 1) {
            // Lock direction on first windup tick (same time telegraph is created)
            lockChargeDirection(holder, target);
        }
        // Keep NPC facing the locked direction during windup
        enforceLockedRotation(holder);
    }

    /**
     * Locks the charge direction based on current target position.
     * Called once at windup start - direction won't change even if target moves.
     */
    private void lockChargeDirection(IAbilityHolder holder, EntityLivingBase target) {
        EntityLivingBase entity = (EntityLivingBase) holder;

        if (target != null) {
            double dx = target.posX - entity.posX;
            double dz = target.posZ - entity.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0) {
                chargeDirection = Vec3.createVectorHelper(dx / len, 0, dz / len);
            } else {
                float yaw = (float) Math.toRadians(entity.rotationYaw);
                chargeDirection = Vec3.createVectorHelper(-Math.sin(yaw), 0, Math.cos(yaw));
            }
        } else {
            float yaw = (float) Math.toRadians(entity.rotationYaw);
            chargeDirection = Vec3.createVectorHelper(-Math.sin(yaw), 0, Math.cos(yaw));
        }
        lockedYaw = (float) Math.toDegrees(Math.atan2(-chargeDirection.xCoord, chargeDirection.zCoord));
    }

    @Override
    public void onExecute(IAbilityHolder holder, EntityLivingBase target, World world) {
        EntityLivingBase entity = (EntityLivingBase) holder;

        // Initialize charge - direction was already locked during windup
        startX = entity.posX;
        startY = entity.posY;
        startZ = entity.posZ;
        hitEntities.clear();

        // If direction wasn't set during windup (shouldn't happen), set it now
        if (chargeDirection == null) {
            lockChargeDirection(holder, target);
        }

        enforceLockedRotation(holder);
    }

    private void enforceLockedRotation(IAbilityHolder holder) {
        EntityLivingBase entity = (EntityLivingBase) holder;
        entity.rotationYaw = lockedYaw;
        entity.rotationYawHead = lockedYaw;
        entity.prevRotationYaw = lockedYaw;
        entity.prevRotationYawHead = lockedYaw;
        entity.renderYawOffset = lockedYaw;
        entity.prevRenderYawOffset = lockedYaw;
    }

    @Override
    public void onActiveTick(IAbilityHolder holder, EntityLivingBase target, World world, int tick) {
        if (chargeDirection == null) return;

        EntityLivingBase entity = (EntityLivingBase) holder;

        // Enforce rotation every tick
        enforceLockedRotation(holder);

        // Calculate distance traveled
        double distanceTraveled = Math.sqrt(
            Math.pow(entity.posX - startX, 2) +
            Math.pow(entity.posZ - startZ, 2)
        );

        // Check if reached max distance
        if (distanceTraveled >= maxDistance) {
            entity.motionX = 0;
            entity.motionZ = 0;
            entity.velocityChanged = true;
            return;
        }

        // Move NPC
        entity.motionX = chargeDirection.xCoord * chargeSpeed;
        entity.motionY = 0;
        entity.motionZ = chargeDirection.zCoord * chargeSpeed;
        entity.velocityChanged = true;

        // Server-side collision damage
        if (!world.isRemote) {
            AxisAlignedBB hitBox = entity.boundingBox.expand(hitRadius, hitRadius * 0.5, hitRadius);

            @SuppressWarnings("unchecked")
            List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

            for (Entity e : entities) {
                if (!(e instanceof EntityLivingBase)) continue;
                if (e == entity) continue;
                if (hitEntities.contains(e.getEntityId())) continue;

                EntityLivingBase livingEntity = (EntityLivingBase) e;

                // Hit this e
                hitEntities.add(entity.getEntityId());

                // Apply damage with scripted event support
                boolean wasHit = applyAbilityDamageWithDirection(holder, livingEntity, damage, knockback, knockbackUp,
                    chargeDirection.xCoord, chargeDirection.zCoord);

                // Play impact sound if hit wasn't cancelled
                if (wasHit) {
                    world.playSoundAtEntity(livingEntity, "random.explode", 0.5f, 1.2f);
                }
            }
        }
    }

    @Override
    public void onComplete(IAbilityHolder holder, EntityLivingBase target) {
        stopMomentum(holder);
        super.onComplete(holder, target);
    }

    @Override
    public void onInterrupt(IAbilityHolder holder, net.minecraft.util.DamageSource source, float damage) {
        stopMomentum(holder);
        super.onInterrupt(holder, source, damage);
    }

    private void stopMomentum(IAbilityHolder holder) {
        EntityLivingBase entity = (EntityLivingBase) holder;
        entity.motionX = 0;
        entity.motionZ = 0;
        entity.velocityChanged = true;
    }

    @Override
    public void reset() {
        super.reset();
        chargeDirection = null;
        hitEntities.clear();
    }

    @Override
    public float getTelegraphLength() {
        return maxDistance;
    }

    @Override
    public float getTelegraphWidth() {
        return hitRadius * 2;
    }

    /**
     * Creates a LINE telegraph from NPC position towards target.
     * Telegraph follows NPC during windup (so NPC can reposition before charging).
     * Direction is locked at creation based on target position.
     */
    @Override
    public TelegraphInstance createTelegraph(IAbilityHolder holder, EntityLivingBase target) {
        if (!isShowTelegraph() || getTelegraphType() == TelegraphType.NONE) {
            return null;
        }
        EntityLivingBase entity = (EntityLivingBase) holder;

        // Calculate direction to target at this moment (locked)
        float yaw;
        if (target != null) {
            double dx = target.posX - entity.posX;
            double dz = target.posZ - entity.posZ;
            yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            yaw = entity.rotationYaw;
        }

        // Create LINE telegraph
        Telegraph telegraph = Telegraph.line(getTelegraphLength(), getTelegraphWidth());
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        // Position at NPC ground level, direction towards target
        double groundY = findGroundLevel(entity.worldObj, entity.posX, entity.posY, entity.posZ);
        TelegraphInstance instance = new TelegraphInstance(telegraph, entity.posX, groundY, entity.posZ, yaw);
        instance.setCasterEntityId(entity.getEntityId());
        // Telegraph follows NPC during windup - allows NPC to reposition
        instance.setEntityIdToFollow(entity.getEntityId());

        return instance;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("chargeSpeed", chargeSpeed);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("knockbackUp", knockbackUp);
        nbt.setFloat("maxDistance", maxDistance);
        nbt.setFloat("hitRadius", hitRadius);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.chargeSpeed = nbt.hasKey("chargeSpeed") ? nbt.getFloat("chargeSpeed") : 0.8f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 15.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 3.0f;
        this.knockbackUp = nbt.hasKey("knockbackUp") ? nbt.getFloat("knockbackUp") : 0.3f;
        this.maxDistance = nbt.hasKey("maxDistance") ? nbt.getFloat("maxDistance") : 20.0f;
        this.hitRadius = nbt.hasKey("hitRadius") ? nbt.getFloat("hitRadius") : 1.5f;
    }

    // Getters & Setters
    public float getChargeSpeed() { return chargeSpeed; }
    public void setChargeSpeed(float chargeSpeed) { this.chargeSpeed = chargeSpeed; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getKnockback() { return knockback; }
    public void setKnockback(float knockback) { this.knockback = knockback; }

    public float getKnockbackUp() { return knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { this.knockbackUp = knockbackUp; }

    public float getMaxDistance() { return maxDistance; }
    public void setMaxDistance(float maxDistance) { this.maxDistance = maxDistance; }

    public float getHitRadius() { return hitRadius; }
    public void setHitRadius(float hitRadius) { this.hitRadius = hitRadius; }
}
