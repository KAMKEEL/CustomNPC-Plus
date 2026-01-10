package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import kamkeel.npcs.controllers.data.ability.Ability;
import noppes.npcs.client.gui.advanced.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityVortex;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Vortex ability: Pulls targets toward the caster.
 * Can pull single target or AOE, with optional damage and stun on arrival.
 */
public class AbilityVortex extends Ability {

    private float pullRadius = 8.0f;
    private float pullStrength = 0.8f;
    private float minPullDistance = 1.5f;
    private float pullToDistance = 0.0f;
    private float damage = 0.0f;
    private float knockback = 0.0f;
    private int stunDuration = 0;
    private int rootDuration = 0;
    private boolean aoe = false;
    private int maxTargets = 5;
    private boolean damageOnPull = false;
    private float pullDamage = 0.0f;

    // Runtime state
    private transient Set<UUID> pulledEntities = new HashSet<>();
    private transient boolean pullComplete = false;

    public AbilityVortex() {
        this.typeId = "cnpc:vortex";
        this.name = "Vortex";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 15.0f;
        this.lockMovement = true;
        this.cooldownTicks = 120;
        this.windUpTicks = 30;
        this.activeTicks = 40;
        this.recoveryTicks = 20;
        this.telegraphType = TelegraphType.CIRCLE;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(
            IAbilityConfigCallback callback) {
        return new SubGuiAbilityVortex(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AOE_SELF };
    }

    @Override
    public float getTelegraphRadius() { return pullRadius; }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        pulledEntities.clear();
        pullComplete = false;

        if (aoe) {
            AxisAlignedBB box = npc.boundingBox.expand(pullRadius, pullRadius / 2, pullRadius);
            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, box);

            int count = 0;
            for (EntityLivingBase entity : entities) {
                if (entity == npc) continue;
                if (entity.isDead) continue;

                double dist = npc.getDistanceToEntity(entity);
                if (dist <= pullRadius && dist > minPullDistance) {
                    pulledEntities.add(entity.getUniqueID());
                    count++;
                    if (count >= maxTargets) break;
                }
            }
        } else {
            // Single target mode - still check pullRadius
            if (target != null && !target.isDead) {
                double dist = npc.getDistanceToEntity(target);
                if (dist <= pullRadius && dist > minPullDistance) {
                    pulledEntities.add(target.getUniqueID());
                }
            }
        }
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (pullComplete || pulledEntities.isEmpty()) {
            return;
        }

        double destX = npc.posX;
        double destY = npc.posY;
        double destZ = npc.posZ;

        if (pullToDistance > 0) {
            double yaw = Math.toRadians(npc.rotationYaw);
            destX -= Math.sin(yaw) * pullToDistance;
            destZ += Math.cos(yaw) * pullToDistance;
        }

        boolean anyStillPulling = false;

        for (UUID uuid : new HashSet<>(pulledEntities)) {
            EntityLivingBase entity = findEntity(world, uuid);
            if (entity == null || entity.isDead) {
                pulledEntities.remove(uuid);
                continue;
            }

            double dx = destX - entity.posX;
            double dy = destY - entity.posY;
            double dz = destZ - entity.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist <= minPullDistance) {
                pulledEntities.remove(uuid);
                onTargetArrived(npc, entity, world);
                continue;
            }

            anyStillPulling = true;

            double factor = pullStrength / dist;
            entity.motionX = dx * factor;
            entity.motionY = dy * factor * 0.5;
            entity.motionZ = dz * factor;
            entity.velocityChanged = true;

            if (damageOnPull && pullDamage > 0) {
                // Apply damage with scripted event support (no knockback during pull)
                applyAbilityDamage(npc, entity, pullDamage, 0, 0);
            }
        }

        if (!anyStillPulling && pulledEntities.isEmpty()) {
            pullComplete = true;
        }
    }

    private void onTargetArrived(EntityNPCInterface npc, EntityLivingBase entity, World world) {
        // Apply damage with scripted event support
        boolean wasHit = applyAbilityDamage(npc, entity, damage, knockback * 0.5f, 0);

        // Only apply effects if hit wasn't cancelled
        if (wasHit) {
            if (stunDuration > 0) {
                entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, stunDuration, 10));
                entity.addPotionEffect(new PotionEffect(Potion.weakness.id, stunDuration, 2));
            }

            if (rootDuration > 0) {
                entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, rootDuration, 127));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private EntityLivingBase findEntity(World world, UUID uuid) {
        for (Object obj : world.loadedEntityList) {
            if (obj instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) obj;
                if (entity.getUniqueID().equals(uuid)) {
                    return entity;
                }
            }
        }
        return null;
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        pulledEntities.clear();
        pullComplete = false;
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
        pulledEntities.clear();
        pullComplete = false;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("pullRadius", pullRadius);
        nbt.setFloat("pullStrength", pullStrength);
        nbt.setFloat("minPullDistance", minPullDistance);
        nbt.setFloat("pullToDistance", pullToDistance);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setInteger("stunDuration", stunDuration);
        nbt.setInteger("rootDuration", rootDuration);
        nbt.setBoolean("aoe", aoe);
        nbt.setInteger("maxTargets", maxTargets);
        nbt.setBoolean("damageOnPull", damageOnPull);
        nbt.setFloat("pullDamage", pullDamage);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.pullRadius = nbt.hasKey("pullRadius") ? nbt.getFloat("pullRadius") : 8.0f;
        this.pullStrength = nbt.hasKey("pullStrength") ? nbt.getFloat("pullStrength") : 0.8f;
        this.minPullDistance = nbt.hasKey("minPullDistance") ? nbt.getFloat("minPullDistance") : 1.5f;
        this.pullToDistance = nbt.hasKey("pullToDistance") ? nbt.getFloat("pullToDistance") : 0.0f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 0.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 0.0f;
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.rootDuration = nbt.hasKey("rootDuration") ? nbt.getInteger("rootDuration") : 0;
        this.aoe = nbt.hasKey("aoe") && nbt.getBoolean("aoe");
        this.maxTargets = nbt.hasKey("maxTargets") ? nbt.getInteger("maxTargets") : 5;
        this.damageOnPull = nbt.hasKey("damageOnPull") && nbt.getBoolean("damageOnPull");
        this.pullDamage = nbt.hasKey("pullDamage") ? nbt.getFloat("pullDamage") : 0.0f;
    }

    // Getters & Setters
    public float getPullRadius() { return pullRadius; }
    public void setPullRadius(float pullRadius) { this.pullRadius = pullRadius; }

    public float getPullStrength() { return pullStrength; }
    public void setPullStrength(float pullStrength) { this.pullStrength = pullStrength; }

    public float getMinPullDistance() { return minPullDistance; }
    public void setMinPullDistance(float minPullDistance) { this.minPullDistance = minPullDistance; }

    public float getPullToDistance() { return pullToDistance; }
    public void setPullToDistance(float pullToDistance) { this.pullToDistance = pullToDistance; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getKnockback() { return knockback; }
    public void setKnockback(float knockback) { this.knockback = knockback; }

    public int getStunDuration() { return stunDuration; }
    public void setStunDuration(int stunDuration) { this.stunDuration = stunDuration; }

    public int getRootDuration() { return rootDuration; }
    public void setRootDuration(int rootDuration) { this.rootDuration = rootDuration; }

    public boolean isAoe() { return aoe; }
    public void setAoe(boolean aoe) { this.aoe = aoe; }

    public int getMaxTargets() { return maxTargets; }
    public void setMaxTargets(int maxTargets) { this.maxTargets = maxTargets; }

    public boolean isDamageOnPull() { return damageOnPull; }
    public void setDamageOnPull(boolean damageOnPull) { this.damageOnPull = damageOnPull; }

    public float getPullDamage() { return pullDamage; }
    public void setPullDamage(float pullDamage) { this.pullDamage = pullDamage; }
}
