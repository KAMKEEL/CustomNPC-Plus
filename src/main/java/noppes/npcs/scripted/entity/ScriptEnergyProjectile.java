package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityEnergyProjectile;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.EventHooks;
import noppes.npcs.api.entity.IEnergyProjectile;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergyProjectile<T extends EntityEnergyProjectile> extends ScriptEnergyAbility<T> implements IEnergyProjectile {

    public ScriptEnergyProjectile(T entity) {
        super(entity);
    }

    // ==================== TYPE ====================

    @Override
    public int getType() {
        return EntityType.ENERGY_PROJECTILE;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_PROJECTILE || super.typeOf(type);
    }

    public int getEnergyType() {
        return -1;
    }

    // ==================== OWNER & TARGET ====================

    public void setOwner(IEntity owner) {
        if (owner != null) {
            entity.setOwnerEntityId(owner.getMCEntity().getEntityId());
        }
    }

    public int getTargetEntityId() {
        return entity.getTargetEntityId();
    }

    public IEntity getTarget() {
        return entity.getTarget();
    }

    public void setTarget(IEntity target) {
        if (target != null) {
            entity.setTargetEntityId(target.getMCEntity().getEntityId());
        } else {
            entity.setTargetEntityId(-1);
        }
    }

    // ==================== SIZE ====================

    public float getSize() {
        return entity.getSize();
    }

    public void setSize(float size) {
        entity.setProjectileSize(size);
    }

    // ==================== DISPLAY (PROJECTILE-SPECIFIC) ====================

    public float getRotationSpeed() {
        return entity.getRotationSpeed();
    }

    public void setRotationSpeed(float speed) {
        entity.setRotationSpeed(speed);
    }

    // ==================== INTERPOLATION ====================

    public float getInterpolatedRotationX(float partialTicks) {
        return entity.getInterpolatedRotationX(partialTicks);
    }

    public float getInterpolatedRotationY(float partialTicks) {
        return entity.getInterpolatedRotationY(partialTicks);
    }

    public float getInterpolatedRotationZ(float partialTicks) {
        return entity.getInterpolatedRotationZ(partialTicks);
    }

    public float getInterpolatedSize(float partialTicks) {
        return entity.getInterpolatedSize(partialTicks);
    }

    // ==================== LIFESPAN ====================

    public float getMaxDistance() {
        return entity.getMaxDistance();
    }

    public void setMaxDistance(float distance) {
        entity.setMaxDistance(distance);
    }

    public int getMaxLifetime() {
        return entity.getMaxLifetime();
    }

    public void setMaxLifetime(int ticks) {
        entity.setMaxLifetime(ticks);
    }

    // ==================== COMBAT ====================

    public float getDamage() {
        return entity.getDamage();
    }

    public void setDamage(float damage) {
        entity.setCombatDamage(damage);
    }

    public float getKnockback() {
        return entity.getKnockback();
    }

    public void setKnockback(float knockback) {
        entity.setCombatKnockback(knockback);
    }

    public float getKnockbackUp() {
        return entity.getKnockbackUp();
    }

    public void setKnockbackUp(float knockbackUp) {
        entity.setCombatKnockbackUp(knockbackUp);
    }

    public boolean isExplosive() {
        return entity.isExplosive();
    }

    public void setExplosive(boolean explosive) {
        entity.setExplosive(explosive);
    }

    public float getExplosionRadius() {
        return entity.getExplosionRadius();
    }

    public void setExplosionRadius(float radius) {
        entity.setExplosionRadius(radius);
    }

    public float getExplosionDamageFalloff() {
        return entity.getExplosionDamageFalloff();
    }

    public void setExplosionDamageFalloff(float falloff) {
        entity.setExplosionDamageFalloff(falloff);
    }

    public int getHitType() {
        return entity.getHitType();
    }

    public void setHitType(int hitType) {
        entity.setHitType(hitType);
    }

    public int getMultiHitDelayTicks() {
        return entity.getMultiHitDelayTicks();
    }

    public void setMultiHitDelayTicks(int delayTicks) {
        entity.setMultiHitDelayTicks(delayTicks);
    }

    public int getMaxHits() {
        return entity.getMaxHits();
    }

    public void setMaxHits(int maxHits) {
        entity.setMaxHits(maxHits);
    }

    // ==================== MOVEMENT ====================

    public float getSpeed() {
        return entity.getSpeed();
    }

    public void setSpeed(float speed) {
        entity.setSpeed(speed);
    }

    public boolean isHoming() {
        return entity.isHoming();
    }

    public void setHoming(boolean homing) {
        entity.setHomingEnabled(homing);
    }

    public float getHomingStrength() {
        return entity.getHomingStrength();
    }

    public void setHomingStrength(float strength) {
        entity.setHomingStrength(strength);
    }

    public float getHomingRange() {
        return entity.getHomingRange();
    }

    public void setHomingRange(float range) {
        entity.setHomingRange(range);
    }

    // ==================== ANCHOR ====================

    public int getAnchor() {
        return entity.getAnchor();
    }

    public float getAnchorOffsetX() {
        return entity.getAnchorOffsetX();
    }

    public float getAnchorOffsetY() {
        return entity.getAnchorOffsetY();
    }

    public float getAnchorOffsetZ() {
        return entity.getAnchorOffsetZ();
    }

    // ==================== POSITION ====================

    public double getStartX() {
        return entity.getStartX();
    }

    public double getStartY() {
        return entity.getStartY();
    }

    public double getStartZ() {
        return entity.getStartZ();
    }

    // ==================== STATE ====================

    public boolean hasHit() {
        return entity.getHasHit();
    }

    // ==================== FIRE ====================

    public void fireAt(IEntity target) {
        if (target != null) {
            EntityLivingBase living = resolveLivingTarget(target);
            if (living != null) {
                entity.setTargetEntityId(living.getEntityId());
                setMotionToward(living.posX, living.posY + living.getEyeHeight(), living.posZ);
            } else {
                net.minecraft.entity.Entity mcTarget = target.getMCEntity();
                setMotionToward(mcTarget.posX, mcTarget.posY + mcTarget.getEyeHeight(), mcTarget.posZ);
            }
        }
        ensureSpawned();
    }

    public void fireAt(double x, double y, double z) {
        setMotionToward(x, y, z);
        ensureSpawned();
    }

    public void fireDirection(float yaw, float pitch) {
        setMotionFromDirection(yaw, pitch);
        ensureSpawned();
    }

    public void fireFrom(IEntityLivingBase caster) {
        if (caster == null) return;
        initFromCaster(caster);
        launchFromOwner(null);
        ensureSpawned();
    }

    public void fireFrom(IEntityLivingBase caster, IEntity target) {
        if (caster == null) return;
        initFromCaster(caster);
        launchFromOwner(resolveLivingTarget(target));
        ensureSpawned();
    }

    /**
     * Sets owner from caster and positions projectile at caster's eye level.
     */
    private void initFromCaster(IEntityLivingBase caster) {
        EntityLivingBase mc = (EntityLivingBase) caster.getMCEntity();
        entity.setOwnerEntityId(mc.getEntityId());
        double eyeY = mc.posY + mc.getEyeHeight();
        entity.setPosition(mc.posX, eyeY, mc.posZ);
        entity.setStartPosition(mc.posX, eyeY, mc.posZ);
    }

    /**
     * Hook for subclasses to call their entity's full launch sequence (startMoving/startFiring).
     * Called by fireFrom methods — handles look-vector snap, charge exit, homing init, etc.
     * Default: sets motion toward target, or along owner's look direction if target is null.
     */
    protected void launchFromOwner(EntityLivingBase target) {
        if (target != null) {
            setMotionToward(target.posX, target.posY + target.getEyeHeight(), target.posZ);
        } else {
            net.minecraft.entity.Entity owner = entity.getOwnerEntity();
            if (owner != null) {
                setMotionFromDirection(owner.rotationYaw, owner.rotationPitch);
            }
        }
    }

    private void setMotionFromDirection(float yaw, float pitch) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        entity.motionX = -Math.sin(yawRad) * Math.cos(pitchRad) * entity.getSpeed();
        entity.motionY = -Math.sin(pitchRad) * entity.getSpeed();
        entity.motionZ = Math.cos(yawRad) * Math.cos(pitchRad) * entity.getSpeed();
    }

    protected static EntityLivingBase resolveLivingTarget(IEntity target) {
        if (target != null && target.getMCEntity() instanceof EntityLivingBase) {
            return (EntityLivingBase) target.getMCEntity();
        }
        return null;
    }

    /**
     * Calculates and sets motion from the projectile's current position toward a target point.
     */
    private void setMotionToward(double x, double y, double z) {
        double dx = x - entity.posX;
        double dy = y - entity.posY;
        double dz = z - entity.posZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) {
            entity.motionX = (dx / len) * entity.getSpeed();
            entity.motionY = (dy / len) * entity.getSpeed();
            entity.motionZ = (dz / len) * entity.getSpeed();
        }
    }

    public void syncClient() {
        if (entity.addedToChunk) {
            entity.sendClientSync();
        }
    }

    protected void ensureSpawned() {
        if (!entity.addedToChunk && entity.worldObj != null) {
            entity.worldObj.spawnEntityInWorld(entity);
            if (!entity.worldObj.isRemote) {
                EventHooks.onEnergyProjectileFired(entity);
            }
        } else if (entity.addedToChunk) {
            entity.sendClientSync();
        }
    }

}
