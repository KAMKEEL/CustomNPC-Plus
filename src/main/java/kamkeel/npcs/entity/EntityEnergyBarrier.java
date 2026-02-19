package kamkeel.npcs.entity;

import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.EventHooks;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Base class for energy barrier entities (Dome, Panel).
 * Provides shared health, damage, hit flash, knockback target,
 * and barrier lifecycle management.
 * Extends EntityEnergyAbility for shared visual/owner/charging state.
 */
public abstract class EntityEnergyBarrier extends EntityEnergyAbility {

    // ==================== BARRIER PROPERTIES ====================
    protected EnergyBarrierData barrierData = new EnergyBarrierData();
    protected float currentHealth;
    protected int ticksAlive = 0;

    // ==================== DATA WATCHER INDICES ====================
    protected static final int DW_HEALTH_PERCENT = 21;
    protected static final int DW_HIT_FLASH = 22;

    public EntityEnergyBarrier(World world) {
        super(world);
        this.noClip = true;
    }

    @Override
    protected void entityInit() {
        super.entityInit(); // DW_CHARGING = 20
        this.dataWatcher.addObject(DW_HEALTH_PERCENT, 1.0f);
        this.dataWatcher.addObject(DW_HIT_FLASH, (byte) 0);
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Check if a projectile is incoming toward this barrier.
     * Dome uses spherical check, Panel uses planar check.
     */
    public abstract boolean isIncomingProjectile(EntityEnergyProjectile projectile);

    /**
     * Setup charging state. Called during windup phase.
     */
    public abstract void setupCharging(int duration);

    /**
     * Finish charging — snap to full size and become active.
     */
    public abstract void finishCharging();

    /**
     * Push entities away from the barrier surface.
     */
    protected abstract void knockbackEntities();

    // ==================== PROJECTILE HIT ====================

    /**
     * Apply damage to this barrier from a projectile.
     * Returns true if the barrier absorbed the hit (projectile should be destroyed).
     */
    public boolean onProjectileHit(EntityEnergyProjectile projectile, float baseDamage) {
        // Get damage multiplier for this projectile type
        String typeId = "";
        if (projectile.getSourceAbility() != null) {
            typeId = projectile.getSourceAbility().getTypeId();
        }
        float multiplier = barrierData.getMultiplier(typeId);
        float damage = baseDamage * multiplier;

        // Fire hit event (may cancel or modify damage)
        if (!worldObj.isRemote) {
            float eventDamage = EventHooks.onEnergyBarrierHit(this, ownerEntityId, projectile, damage);
            if (eventDamage < 0) return false; // Event cancelled — don't block
            damage = eventDamage;
        }

        triggerHitFlash();

        if (!barrierData.useHealth) {
            return true; // Duration-only mode: block but don't take damage
        }

        currentHealth -= damage;
        syncHealthPercent();

        if (currentHealth <= 0) {
            onBarrierDestroyed();
            this.setDead();
        }

        return true;
    }

    // ==================== MELEE DAMAGE ====================

    @Override
    public boolean canBeCollidedWith() {
        return barrierData.meleeEnabled;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (worldObj.isRemote || !barrierData.meleeEnabled) return false;
        if (isCharging()) return false;

        float damage = amount * barrierData.meleeDamageMultiplier;

        // Fire hit event with null projectile (melee hit)
        float eventDamage = EventHooks.onEnergyBarrierHit(this, ownerEntityId, null, damage);
        if (eventDamage < 0) return false;
        damage = eventDamage;

        triggerHitFlash();

        if (!barrierData.useHealth) return true;

        currentHealth -= damage;
        syncHealthPercent();

        if (currentHealth <= 0) {
            onBarrierDestroyed();
            this.setDead();
        }
        return true;
    }

    // ==================== SHARED UPDATE LOGIC ====================

    /**
     * Shared barrier tick logic for onUpdate(). Handles spawned event, owner death,
     * duration check, hit flash decay, and tick event.
     * Call this after incrementing ticksAlive in subclass onUpdate().
     *
     * @return true if barrier died this tick (caller should return early)
     */
    protected boolean updateBarrierTick() {
        if (!worldObj.isRemote) {
            // Fire spawned event on first tick
            if (ticksAlive == 1) {
                EventHooks.onEnergyBarrierSpawned(this, ownerEntityId);
            }

            // Check owner death
            if (ownerEntityId >= 0 && ticksAlive > 5) {
                Entity owner = worldObj.getEntityByID(ownerEntityId);
                if (owner != null) {
                    if (owner.isDead) {
                        onBarrierDestroyed();
                        this.setDead();
                        return true;
                    }
                    if (owner instanceof EntityNPCInterface && ((EntityNPCInterface) owner).isKilled()) {
                        onBarrierDestroyed();
                        this.setDead();
                        return true;
                    }
                }
            }

            // Duration check
            if (barrierData.useDuration && ticksAlive >= barrierData.durationTicks) {
                onBarrierDestroyed();
                this.setDead();
                return true;
            }

            // Reset hit flash
            if (getHitFlash() > 0) {
                setHitFlash((byte) (getHitFlash() - 1));
            }

            // Fire tick event
            EventHooks.onEnergyBarrierTick(this, ownerEntityId);
        }
        return false;
    }

    // ==================== HELPERS ====================

    protected void onBarrierDestroyed() {
        EventHooks.onEnergyBarrierDestroyed(this, ownerEntityId);
    }

    protected void syncHealthPercent() {
        float percent = barrierData.useHealth && barrierData.maxHealth > 0
            ? Math.max(0, currentHealth / barrierData.maxHealth)
            : 1.0f;
        if (!worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_HEALTH_PERCENT, percent);
        }
    }

    protected void triggerHitFlash() {
        if (!worldObj.isRemote) {
            setHitFlash((byte) 4);
        }
    }

    protected void setHitFlash(byte value) {
        this.dataWatcher.updateObject(DW_HIT_FLASH, value);
    }

    public byte getHitFlash() {
        return this.dataWatcher.getWatchableObjectByte(DW_HIT_FLASH);
    }

    public float getHealthPercent() {
        return this.dataWatcher.getWatchableObjectFloat(DW_HEALTH_PERCENT);
    }

    protected boolean isKnockbackTarget(EntityLivingBase entity) {
        switch (barrierData.knockbackTarget) {
            case 1:
                return entity instanceof EntityPlayer;
            case 2:
                return entity instanceof EntityNPCInterface;
            default:
                return entity instanceof EntityPlayer || entity instanceof EntityNPCInterface;
        }
    }

    // ==================== GETTERS ====================

    public EnergyBarrierData getBarrierData() {
        return barrierData;
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(float health) {
        this.currentHealth = health;
        syncHealthPercent();
    }

    public int getTicksAlive() {
        return ticksAlive;
    }

    // ==================== NBT HELPERS ====================

    /**
     * Write shared barrier base fields to NBT.
     * Call from subclass writeEntityToNBT() before writing type-specific fields.
     */
    protected void writeBarrierBaseNBT(NBTTagCompound nbt) {
        writeEnergyBaseNBT(nbt);
        nbt.setInteger("TicksAlive", ticksAlive);
        nbt.setFloat("CurrentHealth", currentHealth);
        nbt.setBoolean("Charging", charging);
        nbt.setInteger("ChargeTick", chargeTick);
        nbt.setInteger("ChargeDuration", chargeDuration);
        barrierData.writeNBT(nbt);
    }

    /**
     * Read shared barrier base fields from NBT.
     * Call from subclass readEntityFromNBT() before reading type-specific fields.
     */
    protected void readBarrierBaseNBT(NBTTagCompound nbt) {
        readEnergyBaseNBT(nbt);
        this.ticksAlive = nbt.getInteger("TicksAlive");
        this.currentHealth = nbt.getFloat("CurrentHealth");
        this.charging = nbt.hasKey("Charging") && nbt.getBoolean("Charging");
        this.chargeTick = nbt.getInteger("ChargeTick");
        this.chargeDuration = nbt.getInteger("ChargeDuration");
        this.dataWatcher.updateObject(DW_CHARGING, (byte) (charging ? 1 : 0));
        barrierData.readNBT(nbt);
    }
}
