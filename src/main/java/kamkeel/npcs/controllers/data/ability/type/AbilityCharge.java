package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
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
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityCharge;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.ability.type.IAbilityCharge;

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
        this.cooldownTicks = 0;
        this.windUpTicks = 20;
        // LINE telegraph showing charge path
        this.telegraphType = TelegraphType.LINE;
        this.showTelegraph = true;
        this.windUpSound = "mob.zombie.wood";
        this.activeSound = "mob.zombie.attack";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityCharge(this, callback);
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
    public void onWindUpTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (tick == 1) {
            // Lock direction on first windup tick (same time telegraph is created)
            lockChargeDirection(npc, target);
        }
        // Keep NPC facing the locked direction during windup
        enforceLockedRotation(npc);
    }

    /**
     * Locks the charge direction based on current target position.
     * Called once at windup start - direction won't change even if target moves.
     */
    private void lockChargeDirection(EntityNPCInterface npc, EntityLivingBase target) {
        if (target != null) {
            double dx = target.posX - npc.posX;
            double dz = target.posZ - npc.posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0) {
                chargeDirection = Vec3.createVectorHelper(dx / len, 0, dz / len);
            } else {
                float yaw = (float) Math.toRadians(npc.rotationYaw);
                chargeDirection = Vec3.createVectorHelper(-Math.sin(yaw), 0, Math.cos(yaw));
            }
        } else {
            float yaw = (float) Math.toRadians(npc.rotationYaw);
            chargeDirection = Vec3.createVectorHelper(-Math.sin(yaw), 0, Math.cos(yaw));
        }
        lockedYaw = (float) Math.toDegrees(Math.atan2(-chargeDirection.xCoord, chargeDirection.zCoord));
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        // Initialize charge - direction was already locked during windup
        startX = npc.posX;
        startY = npc.posY;
        startZ = npc.posZ;
        hitEntities.clear();

        // If direction wasn't set during windup (shouldn't happen), set it now
        if (chargeDirection == null) {
            lockChargeDirection(npc, target);
        }

        enforceLockedRotation(npc);
    }

    private void enforceLockedRotation(EntityNPCInterface npc) {
        npc.rotationYaw = lockedYaw;
        npc.rotationYawHead = lockedYaw;
        npc.prevRotationYaw = lockedYaw;
        npc.prevRotationYawHead = lockedYaw;
        npc.renderYawOffset = lockedYaw;
        npc.prevRenderYawOffset = lockedYaw;
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (chargeDirection == null) return;

        // Enforce rotation every tick
        enforceLockedRotation(npc);

        // Calculate distance traveled
        double distanceTraveled = Math.sqrt(
            Math.pow(npc.posX - startX, 2) +
                Math.pow(npc.posZ - startZ, 2)
        );

        // Check if reached max distance
        if (distanceTraveled >= maxRange) {
            npc.motionX = 0;
            npc.motionZ = 0;
            npc.velocityChanged = true;
            signalCompletion();
            return;
        }

        if (isChargeBlocked(npc)) {
            stopMomentum(npc);
            signalCompletion();
            return;
        }

        // Move NPC
        npc.motionX = chargeDirection.xCoord * chargeSpeed;
        npc.motionY = 0;
        npc.motionZ = chargeDirection.zCoord * chargeSpeed;
        npc.velocityChanged = true;

        // Server-side collision damage
        if (!world.isRemote) {
            AxisAlignedBB hitBox = npc.boundingBox.expand(hitWidth, hitWidth * 0.5, hitWidth);

            @SuppressWarnings("unchecked")
            List<Entity> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

            for (Entity entity : entities) {
                if (!(entity instanceof EntityLivingBase)) continue;
                if (entity == npc) continue;
                if (hitEntities.contains(entity.getEntityId())) continue;

                EntityLivingBase livingEntity = (EntityLivingBase) entity;

                // Hit this entity
                hitEntities.add(entity.getEntityId());

                // Apply damage with scripted event support
                boolean wasHit = applyAbilityDamageWithDirection(npc, livingEntity, damage, knockback,
                    chargeDirection.xCoord, chargeDirection.zCoord);

                // Play impact sound if hit wasn't cancelled
                if (wasHit) {
                    world.playSoundAtEntity(livingEntity, "random.explode", 0.5f, 1.2f);
                }
            }
        }
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        stopMomentum(npc);
        super.onComplete(npc, target);
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, net.minecraft.util.DamageSource source, float damage) {
        stopMomentum(npc);
        super.onInterrupt(npc, source, damage);
    }

    private void stopMomentum(EntityNPCInterface npc) {
        npc.motionX = 0;
        npc.motionZ = 0;
        npc.velocityChanged = true;
    }

    private boolean isChargeBlocked(EntityNPCInterface npc) {
        double nextX = chargeDirection.xCoord * chargeSpeed;
        double nextZ = chargeDirection.zCoord * chargeSpeed;
        AxisAlignedBB nextBox = npc.boundingBox.copy().offset(nextX, 0, nextZ);
        return !npc.worldObj.getCollidingBoundingBoxes(npc, nextBox).isEmpty();
    }

    @Override
    public void reset() {
        super.reset();
        chargeDirection = null;
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
     * Creates a LINE telegraph from NPC position towards target.
     * Telegraph follows NPC during windup (so NPC can reposition before charging).
     * Direction is locked at creation based on target position.
     */
    @Override
    public TelegraphInstance createTelegraph(EntityNPCInterface npc, EntityLivingBase target) {
        if (!isShowTelegraph() || getTelegraphType() == TelegraphType.NONE) {
            return null;
        }

        // Calculate direction to target at this moment (locked)
        float yaw;
        if (target != null) {
            double dx = target.posX - npc.posX;
            double dz = target.posZ - npc.posZ;
            yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            yaw = npc.rotationYaw;
        }

        // Create LINE telegraph
        Telegraph telegraph = Telegraph.line(getTelegraphLength(), getTelegraphWidth());
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        // Position at NPC ground level, direction towards target
        double groundY = findGroundLevel(npc.worldObj, npc.posX, npc.posY, npc.posZ);
        TelegraphInstance instance = new TelegraphInstance(telegraph, npc.posX, groundY, npc.posZ, yaw);
        instance.setCasterEntityId(npc.getEntityId());
        // Telegraph follows NPC during windup - allows NPC to reposition
        instance.setEntityIdToFollow(npc.getEntityId());

        return instance;
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
}
