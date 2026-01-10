package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Projectile ability: Ranged attack that deals damage to target.
 * Currently instant damage - can be enhanced with custom projectile entities.
 */
public class AbilityProjectile extends Ability {

    // Type-specific parameters
    private float damage = 8.0f;
    private float speed = 1.5f;
    private float knockback = 0.5f;
    private String projectileType = "fireball";
    private boolean explosive = false;
    private float explosionRadius = 0.0f;
    private boolean homing = false;
    private float homingStrength = 0.1f;

    public AbilityProjectile() {
        this.typeId = "cnpc:projectile";
        this.name = "Projectile";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.minRange = 5.0f;
        this.maxRange = 20.0f;
        this.cooldownTicks = 60;
        this.windUpTicks = 15;
        this.activeTicks = 5;
        this.recoveryTicks = 10;
        // No telegraph for projectile - it's a ranged attack
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public noppes.npcs.client.gui.advanced.SubGuiAbilityConfig createConfigGui(
            noppes.npcs.client.gui.advanced.IAbilityConfigCallback callback) {
        return new noppes.npcs.client.gui.advanced.ability.SubGuiAbilityProjectile(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AGGRO_TARGET };
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        if (world.isRemote || target == null) return;

        // Calculate direction to target
        double dx = target.posX - npc.posX;
        double dy = (target.posY + target.height / 2) - (npc.posY + npc.getEyeHeight());
        double dz = target.posZ - npc.posZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (len > 0) {
            dx /= len;
            dy /= len;
            dz /= len;
        }

        // Deal instant damage with scripted event support
        // TODO: Use custom EntityAbilityProjectile for actual tracking
        applyAbilityDamageWithDirection(npc, target, damage, knockback, 0.1f, dx, dz);

        // Play sound
        world.playSoundAtEntity(npc, "random.bow", 1.0f, 0.8f);

        // Handle splash damage for explosive projectiles
        if (explosive && explosionRadius > 0) {
            @SuppressWarnings("unchecked")
            List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(target,
                target.boundingBox.expand(explosionRadius, explosionRadius, explosionRadius));

            for (Entity entity : entities) {
                if (entity instanceof EntityLivingBase && entity != npc) {
                    EntityLivingBase living = (EntityLivingBase) entity;
                    float dist = target.getDistanceToEntity(living);
                    if (dist < explosionRadius) {
                        float falloff = 1.0f - (dist / explosionRadius);
                        // Apply splash damage with scripted event support (no knockback)
                        applyAbilityDamage(npc, living, damage * falloff * 0.5f, 0, 0);
                    }
                }
            }

            // Explosion particles
            world.playSoundAtEntity(target, "random.explode", 0.5f, 1.0f);
            for (int i = 0; i < 10; i++) {
                world.spawnParticle("explode",
                    target.posX + (world.rand.nextDouble() - 0.5) * explosionRadius,
                    target.posY + world.rand.nextDouble() * target.height,
                    target.posZ + (world.rand.nextDouble() - 0.5) * explosionRadius,
                    0, 0.1, 0);
            }
        }

        // Spawn projectile particles (visual trail)
        spawnProjectileParticles(world, npc, target);
    }

    private void spawnProjectileParticles(World world, EntityNPCInterface npc, EntityLivingBase target) {
        double startX = npc.posX;
        double startY = npc.posY + npc.getEyeHeight();
        double startZ = npc.posZ;
        double endX = target.posX;
        double endY = target.posY + target.height / 2;
        double endZ = target.posZ;

        double dx = endX - startX;
        double dy = endY - startY;
        double dz = endZ - startZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        String particle = "flame";
        if (projectileType.equals("arrow")) {
            particle = "crit";
        } else if (projectileType.equals("magic")) {
            particle = "witchMagic";
        }

        // Spawn particles along path
        int steps = (int) Math.max(5, dist * 2);
        for (int i = 0; i < steps; i++) {
            double progress = (double) i / steps;
            double px = startX + dx * progress;
            double py = startY + dy * progress;
            double pz = startZ + dz * progress;
            world.spawnParticle(particle, px, py, pz, 0, 0, 0);
        }
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Projectile is instant, nothing to do per-tick
    }

    @Override
    public float getTelegraphRadius() {
        return 0; // No telegraph for projectile
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("damage", damage);
        nbt.setFloat("speed", speed);
        nbt.setFloat("knockback", knockback);
        nbt.setString("projectileType", projectileType);
        nbt.setBoolean("explosive", explosive);
        nbt.setFloat("explosionRadius", explosionRadius);
        nbt.setBoolean("homing", homing);
        nbt.setFloat("homingStrength", homingStrength);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 8.0f;
        this.speed = nbt.hasKey("speed") ? nbt.getFloat("speed") : 1.5f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 0.5f;
        this.projectileType = nbt.hasKey("projectileType") ? nbt.getString("projectileType") : "fireball";
        this.explosive = nbt.hasKey("explosive") && nbt.getBoolean("explosive");
        this.explosionRadius = nbt.hasKey("explosionRadius") ? nbt.getFloat("explosionRadius") : 0.0f;
        this.homing = nbt.hasKey("homing") && nbt.getBoolean("homing");
        this.homingStrength = nbt.hasKey("homingStrength") ? nbt.getFloat("homingStrength") : 0.1f;
    }

    // Getters & Setters
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public float getKnockback() { return knockback; }
    public void setKnockback(float knockback) { this.knockback = knockback; }

    public String getProjectileType() { return projectileType; }
    public void setProjectileType(String projectileType) { this.projectileType = projectileType; }

    public boolean isExplosive() { return explosive; }
    public void setExplosive(boolean explosive) { this.explosive = explosive; }

    public float getExplosionRadius() { return explosionRadius; }
    public void setExplosionRadius(float explosionRadius) { this.explosionRadius = explosionRadius; }

    public boolean isHoming() { return homing; }
    public void setHoming(boolean homing) { this.homing = homing; }

    public float getHomingStrength() { return homingStrength; }
    public void setHomingStrength(float homingStrength) { this.homingStrength = homingStrength; }
}
