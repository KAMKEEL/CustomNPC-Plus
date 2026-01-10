package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Orb ability: Homing projectile sphere that tracks target.
 * Can be explosive on impact and/or apply debuffs.
 */
public class AbilityOrb extends Ability {

    private float orbSpeed = 0.5f;
    private float orbSize = 1.0f;
    private float damage = 15.0f;
    private float knockback = 1.0f;
    private float maxDistance = 30.0f;
    private int maxLifetime = 200;

    private boolean homing = true;
    private float homingStrength = 0.15f;
    private float homingRange = 20.0f;

    private boolean explosive = false;
    private float explosionRadius = 3.0f;
    private float explosionDamageFalloff = 0.5f;

    private int stunDuration = 0;
    private int slowDuration = 0;
    private int slowLevel = 0;

    // Runtime state
    private transient double orbX, orbY, orbZ;
    private transient double velocityX, velocityY, velocityZ;
    private transient double startX, startY, startZ;
    private transient boolean hasHit = false;
    private transient int ticksAlive = 0;

    public AbilityOrb() {
        this.typeId = "cnpc:orb";
        this.name = "Orb";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 25.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 100;
        this.windUpTicks = 30;
        this.activeTicks = 200;
        this.recoveryTicks = 10;
        this.telegraphType = TelegraphType.POINT;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public noppes.npcs.client.gui.advanced.SubGuiAbilityConfig createConfigGui(
            noppes.npcs.client.gui.advanced.IAbilityConfigCallback callback) {
        return new noppes.npcs.client.gui.advanced.ability.SubGuiAbilityOrb(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AGGRO_TARGET };
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        startX = orbX = npc.posX;
        startY = orbY = npc.posY + npc.getEyeHeight() * 0.8;
        startZ = orbZ = npc.posZ;
        hasHit = false;
        ticksAlive = 0;

        if (target != null) {
            double dx = target.posX - orbX;
            double dy = (target.posY + target.height * 0.5) - orbY;
            double dz = target.posZ - orbZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                velocityX = (dx / len) * orbSpeed;
                velocityY = (dy / len) * orbSpeed;
                velocityZ = (dz / len) * orbSpeed;
            }
        } else {
            float yaw = (float) Math.toRadians(npc.rotationYaw);
            float pitch = (float) Math.toRadians(npc.rotationPitch);
            velocityX = -Math.sin(yaw) * Math.cos(pitch) * orbSpeed;
            velocityY = -Math.sin(pitch) * orbSpeed;
            velocityZ = Math.cos(yaw) * Math.cos(pitch) * orbSpeed;
        }
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (hasHit) return;

        ticksAlive++;

        if (ticksAlive >= maxLifetime) {
            hasHit = true;
            return;
        }

        double distTraveled = Math.sqrt(
            Math.pow(orbX - startX, 2) +
            Math.pow(orbY - startY, 2) +
            Math.pow(orbZ - startZ, 2)
        );
        if (distTraveled >= maxDistance) {
            hasHit = true;
            return;
        }

        if (homing && target != null && target.isEntityAlive()) {
            double targetX = target.posX;
            double targetY = target.posY + target.height * 0.5;
            double targetZ = target.posZ;

            double distToTarget = Math.sqrt(
                Math.pow(targetX - orbX, 2) +
                Math.pow(targetY - orbY, 2) +
                Math.pow(targetZ - orbZ, 2)
            );

            if (distToTarget <= homingRange) {
                double dx = targetX - orbX;
                double dy = targetY - orbY;
                double dz = targetZ - orbZ;
                double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (len > 0) {
                    double desiredVX = (dx / len) * orbSpeed;
                    double desiredVY = (dy / len) * orbSpeed;
                    double desiredVZ = (dz / len) * orbSpeed;

                    velocityX += (desiredVX - velocityX) * homingStrength;
                    velocityY += (desiredVY - velocityY) * homingStrength;
                    velocityZ += (desiredVZ - velocityZ) * homingStrength;

                    double vLen = Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
                    if (vLen > 0) {
                        velocityX = (velocityX / vLen) * orbSpeed;
                        velocityY = (velocityY / vLen) * orbSpeed;
                        velocityZ = (velocityZ / vLen) * orbSpeed;
                    }
                }
            }
        }

        orbX += velocityX;
        orbY += velocityY;
        orbZ += velocityZ;

        if (!world.isRemote) {
            checkCollision(npc, world);
        }
    }

    private void checkCollision(EntityNPCInterface npc, World world) {
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            orbX - orbSize, orbY - orbSize, orbZ - orbSize,
            orbX + orbSize, orbY + orbSize, orbZ + orbSize
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

        for (EntityLivingBase entity : entities) {
            if (entity == npc) continue;

            hasHit = true;

            if (explosive) {
                doExplosion(npc, world);
            } else {
                // Apply damage with scripted event support
                boolean wasHit = applyAbilityDamage(npc, entity, damage, knockback, 0.2f);
                if (wasHit) {
                    applyEffects(entity);
                }
            }
            break;
        }

        if (!hasHit && world.getBlock((int)orbX, (int)orbY, (int)orbZ).getMaterial().isSolid()) {
            hasHit = true;
            if (explosive) {
                doExplosion(npc, world);
            }
        }
    }

    private void doExplosion(EntityNPCInterface npc, World world) {
        AxisAlignedBB explosionBox = AxisAlignedBB.getBoundingBox(
            orbX - explosionRadius, orbY - explosionRadius, orbZ - explosionRadius,
            orbX + explosionRadius, orbY + explosionRadius, orbZ + explosionRadius
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> blastTargets = world.getEntitiesWithinAABB(EntityLivingBase.class, explosionBox);

        for (EntityLivingBase blastTarget : blastTargets) {
            if (blastTarget == npc) continue;

            double dist = Math.sqrt(
                Math.pow(blastTarget.posX - orbX, 2) +
                Math.pow(blastTarget.posY - orbY, 2) +
                Math.pow(blastTarget.posZ - orbZ, 2)
            );

            if (dist <= explosionRadius) {
                float falloff = 1.0f - (float)(dist / explosionRadius) * explosionDamageFalloff;
                // Apply damage with scripted event support
                boolean wasHit = applyAbilityDamage(npc, blastTarget, damage * falloff, knockback * falloff, 0.3f);
                if (wasHit) {
                    applyEffects(blastTarget);
                }
            }
        }

        world.playSoundEffect(orbX, orbY, orbZ, "random.explode", 1.0f, 1.0f);
    }

    private void applyKnockback(EntityLivingBase target, float strength, float upward) {
        double dx = target.posX - orbX;
        double dz = target.posZ - orbZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0) {
            target.motionX += (dx / dist) * strength;
            target.motionZ += (dz / dist) * strength;
        }
        target.motionY += upward;
        target.velocityChanged = true;
    }

    private void applyEffects(EntityLivingBase target) {
        if (stunDuration > 0) {
            target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, stunDuration, 10));
            target.addPotionEffect(new PotionEffect(Potion.weakness.id, stunDuration, 2));
        }
        if (slowDuration > 0 && slowLevel > 0) {
            target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, slowDuration, slowLevel));
        }
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        hasHit = false;
        ticksAlive = 0;
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
        hasHit = false;
        ticksAlive = 0;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("orbSpeed", orbSpeed);
        nbt.setFloat("orbSize", orbSize);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setFloat("maxDistance", maxDistance);
        nbt.setInteger("maxLifetime", maxLifetime);
        nbt.setBoolean("homing", homing);
        nbt.setFloat("homingStrength", homingStrength);
        nbt.setFloat("homingRange", homingRange);
        nbt.setBoolean("explosive", explosive);
        nbt.setFloat("explosionRadius", explosionRadius);
        nbt.setFloat("explosionDamageFalloff", explosionDamageFalloff);
        nbt.setInteger("stunDuration", stunDuration);
        nbt.setInteger("slowDuration", slowDuration);
        nbt.setInteger("slowLevel", slowLevel);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.orbSpeed = nbt.hasKey("orbSpeed") ? nbt.getFloat("orbSpeed") : 0.5f;
        this.orbSize = nbt.hasKey("orbSize") ? nbt.getFloat("orbSize") : 1.0f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 15.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.0f;
        this.maxDistance = nbt.hasKey("maxDistance") ? nbt.getFloat("maxDistance") : 30.0f;
        this.maxLifetime = nbt.hasKey("maxLifetime") ? nbt.getInteger("maxLifetime") : 200;
        this.homing = !nbt.hasKey("homing") || nbt.getBoolean("homing");
        this.homingStrength = nbt.hasKey("homingStrength") ? nbt.getFloat("homingStrength") : 0.15f;
        this.homingRange = nbt.hasKey("homingRange") ? nbt.getFloat("homingRange") : 20.0f;
        this.explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        this.explosionRadius = nbt.hasKey("explosionRadius") ? nbt.getFloat("explosionRadius") : 3.0f;
        this.explosionDamageFalloff = nbt.hasKey("explosionDamageFalloff") ? nbt.getFloat("explosionDamageFalloff") : 0.5f;
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.slowDuration = nbt.hasKey("slowDuration") ? nbt.getInteger("slowDuration") : 0;
        this.slowLevel = nbt.hasKey("slowLevel") ? nbt.getInteger("slowLevel") : 0;
    }

    // Getters & Setters
    public float getOrbSpeed() { return orbSpeed; }
    public void setOrbSpeed(float orbSpeed) { this.orbSpeed = orbSpeed; }

    public float getOrbSize() { return orbSize; }
    public void setOrbSize(float orbSize) { this.orbSize = orbSize; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getKnockback() { return knockback; }
    public void setKnockback(float knockback) { this.knockback = knockback; }

    public float getMaxDistance() { return maxDistance; }
    public void setMaxDistance(float maxDistance) { this.maxDistance = maxDistance; }

    public int getMaxLifetime() { return maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { this.maxLifetime = maxLifetime; }

    public boolean isHoming() { return homing; }
    public void setHoming(boolean homing) { this.homing = homing; }

    public float getHomingStrength() { return homingStrength; }
    public void setHomingStrength(float homingStrength) { this.homingStrength = homingStrength; }

    public float getHomingRange() { return homingRange; }
    public void setHomingRange(float homingRange) { this.homingRange = homingRange; }

    public boolean isExplosive() { return explosive; }
    public void setExplosive(boolean explosive) { this.explosive = explosive; }

    public float getExplosionRadius() { return explosionRadius; }
    public void setExplosionRadius(float explosionRadius) { this.explosionRadius = explosionRadius; }

    public float getExplosionDamageFalloff() { return explosionDamageFalloff; }
    public void setExplosionDamageFalloff(float explosionDamageFalloff) { this.explosionDamageFalloff = explosionDamageFalloff; }

    public int getStunDuration() { return stunDuration; }
    public void setStunDuration(int stunDuration) { this.stunDuration = stunDuration; }

    public int getSlowDuration() { return slowDuration; }
    public void setSlowDuration(int slowDuration) { this.slowDuration = slowDuration; }

    public int getSlowLevel() { return slowLevel; }
    public void setSlowLevel(int slowLevel) { this.slowLevel = slowLevel; }

    // Runtime getters for rendering
    public double getOrbX() { return orbX; }
    public double getOrbY() { return orbY; }
    public double getOrbZ() { return orbZ; }
    public boolean hasHit() { return hasHit; }
}
