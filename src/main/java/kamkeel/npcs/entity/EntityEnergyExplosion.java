package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.EnergyController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import kamkeel.npcs.util.AttributeAttackUtil;
import kamkeel.npcs.util.CNPCDebug;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Short-lived explosion entity for energy projectile impacts and standalone API use.
 * Renders an expanding voxel shell. When damage is enabled, applies area damage on the first tick.
 */
public class EntityEnergyExplosion extends EntityEnergyAbility {

    private float maxRadius = 2.0f;
    private int durationTicks = 10;
    private float prevRenderRadius = 0.0f;
    private float renderRadius = 0.0f;
    private long renderSeed = 0L;

    // ==================== DAMAGE (for standalone API use) ====================
    private float damage = 0f;
    private float knockback = 0f;
    private float knockbackUp = 0.1f;
    private float damageFalloff = 0.5f;
    private boolean damageEnabled = false;
    private boolean hasDamaged = false;

    public EntityEnergyExplosion(World world) {
        super(world);
        this.noClip = true;
        this.setSize(0.1f, 0.1f);
    }

    /**
     * Constructor for projectile-spawned explosions (render-only, no damage).
     */
    public EntityEnergyExplosion(World world, EntityEnergyProjectile source, float radius) {
        this(world);

        if (source != null) {
            this.ownerEntityId = source.getOwnerEntityId();
            EnergyDisplayData sourceDisplay = source.getDisplayData();
            this.displayData = sourceDisplay != null ? sourceDisplay.copy() : new EnergyDisplayData();
            EnergyLightningData sourceLightning = source.getLightningData();
            this.lightningData = sourceLightning != null ? sourceLightning.copy() : new EnergyLightningData();
            this.setPosition(source.posX, source.posY, source.posZ);
        }

        setExplosionRadius(radius);
        this.renderSeed = world != null ? world.rand.nextLong() : 0L;
    }

    /**
     * Constructor for standalone API-spawned explosions.
     */
    public EntityEnergyExplosion(World world, Entity owner, double x, double y, double z, float radius) {
        this(world);
        if (owner != null) {
            this.ownerEntityId = owner.getEntityId();
        }
        this.setPosition(x, y, z);
        setExplosionRadius(radius);
        this.renderSeed = world != null ? world.rand.nextLong() : 0L;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    public void onUpdate() {
        this.prevRenderRadius = this.renderRadius;

        super.onUpdate();

        float progress = getLifeProgress(0.0f);
        float eased = 1.0f - (1.0f - progress) * (1.0f - progress);
        this.renderRadius = maxRadius * eased;

        // Apply damage on first server tick
        if (damageEnabled && !hasDamaged && !worldObj.isRemote && ticksExisted >= 1) {
            applyExplosionDamage();
            hasDamaged = true;
        }

        // Debug logging
        {
            boolean isClient = worldObj.isRemote;
            if (isClient ? CNPCDebug.isClientEnabled("energy") : CNPCDebug.isServerEnabled("energy")) {
                CNPCDebug.log("energy", isClient, String.format(
                    "[Explosion id=%d tick=%d] pos=(%.2f,%.2f,%.2f) renderRadius=%.2f maxRadius=%.2f progress=%.2f",
                    getEntityId(), ticksExisted, posX, posY, posZ,
                    renderRadius, maxRadius, getLifeProgress(0f)));
            }
        }

        if (ticksExisted >= durationTicks) {
            setDead();
        }
    }

    // ==================== DAMAGE LOGIC ====================

    private void applyExplosionDamage() {
        if (damage <= 0 || maxRadius <= 0) return;

        Entity owner = getOwnerEntity();
        double explosionRadSq = maxRadius * maxRadius;

        AxisAlignedBB explosionBox = AxisAlignedBB.getBoundingBox(
            posX - maxRadius, posY - maxRadius, posZ - maxRadius,
            posX + maxRadius, posY + maxRadius, posZ + maxRadius
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> targets = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, explosionBox);

        for (EntityLivingBase target : targets) {
            if (target == owner) continue;
            if (target.boundingBox == null) continue;

            double closestX = Math.max(target.boundingBox.minX, Math.min(posX, target.boundingBox.maxX));
            double closestY = Math.max(target.boundingBox.minY, Math.min(posY, target.boundingBox.maxY));
            double closestZ = Math.max(target.boundingBox.minZ, Math.min(posZ, target.boundingBox.maxZ));
            double dx = closestX - posX;
            double dy = closestY - posY;
            double dz = closestZ - posZ;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > explosionRadSq) continue;

            float falloff = 1.0f;
            if (damageFalloff != 0.0f && distSq > 0.0D) {
                float dist = (float) Math.sqrt(distSq);
                falloff = 1.0f - (dist / maxRadius) * damageFalloff;
            }

            float finalDmg = damage * falloff;
            float finalKb = knockback * falloff;

            // Route through EnergyController for addon integration (e.g. DBC)
            boolean handled = false;
            if (customDamageData != null && owner instanceof EntityLivingBase) {
                double kbDirX = target.posX - posX;
                double kbDirZ = target.posZ - posZ;
                handled = EnergyController.Instance.fireOnEnergyDamage(
                    this, (EntityLivingBase) owner, target,
                    finalDmg, finalKb, knockbackUp, kbDirX, kbDirZ, 1.0f, customDamageData);
            }

            int prevHurtResist = Ability.clearHurtResistanceIfNeeded(target, ignoreIFrames);
            try {
                if (!handled) {
                    float dmgToApply = finalDmg;

                    // Magic pipeline
                    if (this.magicData != null && !this.magicData.isEmpty() && owner instanceof EntityLivingBase) {
                        dmgToApply = AttributeAttackUtil.calculateAbilityDamage(
                            (EntityLivingBase) owner, target, finalDmg, this.magicData);
                    }

                    if (owner instanceof EntityNPCInterface) {
                        target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), dmgToApply);
                    } else if (owner instanceof EntityPlayer) {
                        target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), dmgToApply);
                    } else if (owner instanceof EntityLivingBase) {
                        target.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) owner), dmgToApply);
                    } else {
                        target.attackEntityFrom(new NpcDamageSource("npc_ability", null), dmgToApply);
                    }
                }
            } finally {
                Ability.restoreHurtResistanceIfNeeded(target, ignoreIFrames, prevHurtResist);
            }

            // Apply knockback
            if (finalKb > 0) {
                double kbDx = target.posX - posX;
                double kbDz = target.posZ - posZ;
                double len = Math.sqrt(kbDx * kbDx + kbDz * kbDz);
                if (len > 0) {
                    target.addVelocity((kbDx / len) * finalKb * 0.5, knockbackUp, (kbDz / len) * finalKb * 0.5);
                    target.velocityChanged = true;
                }
            }
        }

        worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 1.0f, 1.0f);
    }

    // ==================== RADIUS / DURATION ====================

    public void setExplosionRadius(float radius) {
        this.maxRadius = Math.max(0.5f, sanitize(radius, 2.0f, MAX_ENTITY_RADIUS));
        this.durationTicks = Math.max(6, Math.min(18, (int) (6 + this.maxRadius * 1.8f)));
        this.setSize(this.maxRadius * 2.0f, this.maxRadius * 2.0f);
    }

    public float getExplosionRadius() {
        return maxRadius;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public float getInterpolatedRadius(float partialTicks) {
        return this.prevRenderRadius + (this.renderRadius - this.prevRenderRadius) * partialTicks;
    }

    public float getLifeProgress(float partialTicks) {
        if (durationTicks <= 0) return 1.0f;
        float age = (ticksExisted + partialTicks) / durationTicks;
        return MathHelper.clamp_float(age, 0.0f, 1.0f);
    }

    public long getRenderSeed() {
        return renderSeed;
    }

    // ==================== DAMAGE GETTERS/SETTERS ====================

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = Math.max(0, damage);
        this.damageEnabled = this.damage > 0;
    }

    public float getKnockback() {
        return knockback;
    }

    public void setKnockback(float knockback) {
        this.knockback = Math.max(0, knockback);
    }

    public float getKnockbackUp() {
        return knockbackUp;
    }

    public void setKnockbackUp(float knockbackUp) {
        this.knockbackUp = knockbackUp;
    }

    public float getDamageFalloff() {
        return damageFalloff;
    }

    public void setDamageFalloff(float falloff) {
        this.damageFalloff = MathHelper.clamp_float(falloff, 0.0f, 1.0f);
    }

    public boolean isDamageEnabled() {
        return damageEnabled;
    }

    // ==================== RENDER / COLLISION ====================

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d = Math.max(16.0D, maxRadius * 64.0D);
        return distance < d * d;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        // Intentionally empty - transient visual entity.
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        // Intentionally empty - transient visual entity.
    }

    @Override
    protected void writeSpawnNBT(NBTTagCompound nbt) {
        writeEnergyBaseNBT(nbt);
        nbt.setDouble("PosX", this.posX);
        nbt.setDouble("PosY", this.posY);
        nbt.setDouble("PosZ", this.posZ);
        nbt.setFloat("ExplosionRadius", maxRadius);
        nbt.setInteger("ExplosionDuration", durationTicks);
        nbt.setLong("ExplosionSeed", renderSeed);
        if (damageEnabled) {
            nbt.setFloat("Damage", damage);
            nbt.setFloat("Knockback", knockback);
            nbt.setFloat("KnockbackUp", knockbackUp);
            nbt.setFloat("DamageFalloff", damageFalloff);
        }
    }

    @Override
    protected void readSpawnNBT(NBTTagCompound nbt) {
        readEnergyBaseNBT(nbt);
        double x = nbt.hasKey("PosX") ? nbt.getDouble("PosX") : this.posX;
        double y = nbt.hasKey("PosY") ? nbt.getDouble("PosY") : this.posY;
        double z = nbt.hasKey("PosZ") ? nbt.getDouble("PosZ") : this.posZ;
        this.setPosition(x, y, z);
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.lastTickPosX = x;
        this.lastTickPosY = y;
        this.lastTickPosZ = z;
        setExplosionRadius(nbt.hasKey("ExplosionRadius") ? nbt.getFloat("ExplosionRadius") : 2.0f);
        this.durationTicks = nbt.hasKey("ExplosionDuration") ? nbt.getInteger("ExplosionDuration") : durationTicks;
        if (durationTicks <= 0) durationTicks = 10;
        this.renderSeed = nbt.hasKey("ExplosionSeed") ? nbt.getLong("ExplosionSeed") : 0L;
        this.renderRadius = 0.0f;
        this.prevRenderRadius = 0.0f;
        if (nbt.hasKey("Damage")) {
            this.damage = nbt.getFloat("Damage");
            this.knockback = nbt.getFloat("Knockback");
            this.knockbackUp = nbt.getFloat("KnockbackUp");
            this.damageFalloff = nbt.getFloat("DamageFalloff");
            this.damageEnabled = this.damage > 0;
        }
    }
}
