package kamkeel.npcs.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Base class for all ability projectiles (Orb, Disc, Beam, Laser).
 * Provides common functionality for visuals, combat, effects, and interpolation.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public abstract class EntityAbilityProjectile extends Entity implements IEntityAdditionalSpawnData {

    // ==================== VISUAL PROPERTIES ====================
    protected float size = 1.0f;
    protected int innerColor = 0xFFFFFF;
    protected int outerColor = 0x8888FF;
    protected boolean outerColorEnabled = true;
    protected float outerColorWidth = 1.8f;
    protected float rotationSpeed = 4.0f;

    // ==================== COMBAT PROPERTIES ====================
    protected float damage = 7.0f;
    protected float knockback = 1.0f;
    protected float knockbackUp = 0.1f;

    // ==================== EFFECT PROPERTIES ====================
    protected int stunDuration = 0;
    protected int slowDuration = 0;
    protected int slowLevel = 0;

    // ==================== EXPLOSION PROPERTIES ====================
    protected boolean explosive = false;
    protected float explosionRadius = 3.0f;
    protected float explosionDamageFalloff = 0.5f;

    // ==================== LIFESPAN ====================
    protected float maxDistance = 30.0f;
    protected int maxLifetime = 200;
    protected long deathWorldTime = -1;  // World time when entity should die (-1 = not set)

    // ==================== TRACKING ====================
    protected double startX, startY, startZ;
    protected int ownerEntityId = -1;
    protected int targetEntityId = -1;

    // ==================== STATE ====================
    protected boolean hasHit = false;

    // ==================== ROTATION INTERPOLATION ====================
    public float prevRotationValX, prevRotationValY, prevRotationValZ;
    public float rotationValX, rotationValY, rotationValZ;

    // ==================== SIZE INTERPOLATION ====================
    protected float renderCurrentSize;
    protected float prevRenderSize;

    // ==================== POSITION INTERPOLATION ====================
    protected double interpTargetX, interpTargetY, interpTargetZ;
    protected double interpTargetMotionX, interpTargetMotionY, interpTargetMotionZ;
    protected int interpSteps;

    public EntityAbilityProjectile(World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.noClip = false;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    /**
     * Initialize common properties. Call from subclass constructors.
     */
    protected void initProjectile(EntityNPCInterface owner, EntityLivingBase target,
                                   double x, double y, double z,
                                   float size, int innerColor, int outerColor,
                                   boolean outerColorEnabled, float outerColorWidth, float rotationSpeed,
                                   float damage, float knockback, float knockbackUp,
                                   boolean explosive, float explosionRadius, float explosionDamageFalloff,
                                   int stunDuration, int slowDuration, int slowLevel,
                                   float maxDistance, int maxLifetime) {
        this.setPosition(x, y, z);
        this.startX = x;
        this.startY = y;
        this.startZ = z;

        this.ownerEntityId = owner.getEntityId();
        this.targetEntityId = target != null ? target.getEntityId() : -1;

        // Visual
        this.size = size;
        this.innerColor = innerColor;
        this.outerColor = outerColor;
        this.outerColorEnabled = outerColorEnabled;
        this.outerColorWidth = outerColorWidth;
        this.rotationSpeed = rotationSpeed;

        // Combat
        this.damage = damage;
        this.knockback = knockback;
        this.knockbackUp = knockbackUp;

        // Explosion
        this.explosive = explosive;
        this.explosionRadius = explosionRadius;
        this.explosionDamageFalloff = explosionDamageFalloff;

        // Effects
        this.stunDuration = stunDuration;
        this.slowDuration = slowDuration;
        this.slowLevel = slowLevel;

        // Lifespan
        this.maxDistance = maxDistance;
        this.maxLifetime = maxLifetime;
        // deathWorldTime will be set on first tick when world is available

        // Initialize render size
        this.renderCurrentSize = size;
        this.prevRenderSize = size;
    }

    @Override
    protected void entityInit() {
        // No DataWatcher needed - using IEntityAdditionalSpawnData with NBT
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d1 = this.boundingBox.getAverageEdgeLength() * 4.0D;
        d1 *= 128.0D;
        return distance < d1 * d1;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1; // Translucent pass
    }

    @Override
    public void onUpdate() {
        // Store previous values for interpolation
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationValX = this.rotationValX;
        this.prevRotationValY = this.rotationValY;
        this.prevRotationValZ = this.rotationValZ;
        this.prevRenderSize = this.renderCurrentSize;

        super.onUpdate();

        // Update rotation
        updateRotation();

        // Lerp render size toward actual size
        this.renderCurrentSize = this.renderCurrentSize + (this.size - this.renderCurrentSize) * 0.15f;

        // Set death time on first tick if not already set (handles chunk load/unload)
        if (deathWorldTime < 0 && worldObj != null) {
            deathWorldTime = worldObj.getTotalWorldTime() + maxLifetime;
        }

        // Check lifespan using world time (survives chunk unload/reload)
        if (deathWorldTime > 0 && worldObj.getTotalWorldTime() >= deathWorldTime) {
            if (!worldObj.isRemote) {
                noppes.npcs.LogWriter.info("[Projectile:" + this.getClass().getSimpleName() + "] DEAD: Lifetime exceeded. worldTime=" + worldObj.getTotalWorldTime() + " deathTime=" + deathWorldTime);
            }
            this.setDead();
            return;
        }

        // Check max distance (subclass can override if needed)
        if (checkMaxDistance()) {
            this.setDead();
            return;
        }

        if (hasHit) {
            if (!worldObj.isRemote) {
                noppes.npcs.LogWriter.info("[Projectile:" + this.getClass().getSimpleName() + "] DEAD: hasHit=true at tick " + ticksExisted);
            }
            this.setDead();
            return;
        }

        // Subclass-specific update
        updateProjectile();
    }

    /**
     * Update rotation values. Override for custom rotation behavior.
     */
    protected void updateRotation() {
        this.rotationValX += rotationSpeed * 0.7f;
        this.rotationValY += rotationSpeed;
        this.rotationValZ += rotationSpeed * 0.5f;

        if (this.rotationValX > 360.0f) this.rotationValX -= 360.0f;
        if (this.rotationValY > 360.0f) this.rotationValY -= 360.0f;
        if (this.rotationValZ > 360.0f) this.rotationValZ -= 360.0f;
    }

    /**
     * Check if projectile has exceeded max distance.
     * @return true if should die
     */
    protected boolean checkMaxDistance() {
        double distTraveled = Math.sqrt(
            (posX - startX) * (posX - startX) +
            (posY - startY) * (posY - startY) +
            (posZ - startZ) * (posZ - startZ)
        );
        return distTraveled >= maxDistance;
    }

    /**
     * Subclass-specific update logic. Called every tick.
     */
    protected abstract void updateProjectile();

    // ==================== POSITION INTERPOLATION ====================

    @Override
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements) {
        this.interpTargetX = x;
        this.interpTargetY = y;
        this.interpTargetZ = z;
        this.interpSteps = posRotationIncrements;
    }

    @Override
    public void setVelocity(double motionX, double motionY, double motionZ) {
        this.interpTargetMotionX = motionX;
        this.interpTargetMotionY = motionY;
        this.interpTargetMotionZ = motionZ;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    /**
     * Handle client-side position interpolation.
     */
    protected void handleClientInterpolation() {
        if (this.interpSteps > 0) {
            double newX = this.posX + (this.interpTargetX - this.posX) / this.interpSteps;
            double newY = this.posY + (this.interpTargetY - this.posY) / this.interpSteps;
            double newZ = this.posZ + (this.interpTargetZ - this.posZ) / this.interpSteps;

            this.motionX = this.motionX + (this.interpTargetMotionX - this.motionX) / this.interpSteps;
            this.motionY = this.motionY + (this.interpTargetMotionY - this.motionY) / this.interpSteps;
            this.motionZ = this.motionZ + (this.interpTargetMotionZ - this.motionZ) / this.interpSteps;

            this.setPosition(newX, newY, newZ);
            this.interpSteps--;
        } else {
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.setPosition(this.posX, this.posY, this.posZ);
        }
    }

    // ==================== DAMAGE & EFFECTS ====================

    protected void applyDamage(EntityLivingBase target) {
        applyDamage(target, this.damage, this.knockback);
    }

    protected void applyDamage(EntityLivingBase target, float dmg, float kb) {
        Entity owner = getOwner();
        EntityNPCInterface npc = (owner instanceof EntityNPCInterface) ? (EntityNPCInterface) owner : null;

        if (npc != null) {
            target.attackEntityFrom(new NpcDamageSource("npc_ability", npc), dmg);
        } else {
            target.attackEntityFrom(new NpcDamageSource("npc_ability", null), dmg);
        }

        if (kb > 0) {
            double dx = target.posX - posX;
            double dz = target.posZ - posZ;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0) {
                double kbX = (dx / len) * kb * 0.5;
                double kbZ = (dz / len) * kb * 0.5;
                double kbY = knockbackUp > 0 ? knockbackUp : 0.1;
                target.addVelocity(kbX, kbY, kbZ);
                target.velocityChanged = true;
            }
        }

        applyEffects(target);
    }

    protected void applyEffects(EntityLivingBase target) {
        if (stunDuration > 0) {
            target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, stunDuration, 10));
            target.addPotionEffect(new PotionEffect(Potion.weakness.id, stunDuration, 2));
        }
        if (slowDuration > 0 && slowLevel > 0) {
            target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, slowDuration, slowLevel));
        }
    }

    protected void doExplosion() {
        worldObj.playSoundEffect(posX, posY, posZ, "random.explode", 1.0f, 1.0f);

        Entity owner = getOwner();

        AxisAlignedBB explosionBox = AxisAlignedBB.getBoundingBox(
            posX - explosionRadius, posY - explosionRadius, posZ - explosionRadius,
            posX + explosionRadius, posY + explosionRadius, posZ + explosionRadius
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> targets = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, explosionBox);

        for (EntityLivingBase target : targets) {
            if (target == owner) continue;

            double dist = Math.sqrt(
                Math.pow(target.posX - posX, 2) +
                Math.pow(target.posY - posY, 2) +
                Math.pow(target.posZ - posZ, 2)
            );

            if (dist <= explosionRadius) {
                float falloff = 1.0f - (float) (dist / explosionRadius) * explosionDamageFalloff;
                applyDamage(target, damage * falloff, knockback * falloff);
            }
        }
    }

    // ==================== ENTITY HELPERS ====================

    protected Entity getOwner() {
        if (ownerEntityId == -1) return null;
        return worldObj.getEntityByID(ownerEntityId);
    }

    protected Entity getTarget() {
        if (targetEntityId == -1) return null;
        return worldObj.getEntityByID(targetEntityId);
    }

    /**
     * Check if an entity should be ignored for collision.
     */
    protected boolean shouldIgnoreEntity(Entity entity) {
        Entity owner = getOwner();
        if (entity == owner) return true;

        if (owner instanceof EntityNPCInterface && entity instanceof EntityNPCInterface) {
            EntityNPCInterface ownerNpc = (EntityNPCInterface) owner;
            EntityNPCInterface targetNpc = (EntityNPCInterface) entity;
            if (ownerNpc.faction.id == targetNpc.faction.id) return true;
        }
        return false;
    }

    // ==================== VISUAL GETTERS ====================

    public float getSize() {
        return size;
    }

    public int getInnerColor() {
        return innerColor;
    }

    public int getOuterColor() {
        return outerColor;
    }

    public boolean isOuterColorEnabled() {
        return outerColorEnabled;
    }

    public float getOuterColorWidth() {
        return outerColorWidth;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public float getInterpolatedRotationX(float partialTicks) {
        return this.prevRotationValX + (this.rotationValX - this.prevRotationValX) * partialTicks;
    }

    public float getInterpolatedRotationY(float partialTicks) {
        return this.prevRotationValY + (this.rotationValY - this.prevRotationValY) * partialTicks;
    }

    public float getInterpolatedRotationZ(float partialTicks) {
        return this.prevRotationValZ + (this.rotationValZ - this.prevRotationValZ) * partialTicks;
    }

    public float getInterpolatedSize(float partialTicks) {
        return this.prevRenderSize + (this.renderCurrentSize - this.prevRenderSize) * partialTicks;
    }

    // ==================== BRIGHTNESS ====================

    @Override
    public float getBrightness(float partialTicks) {
        return 1.0f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float partialTicks) {
        return 0xF000F0;
    }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        readBaseNBT(nbt);
        readProjectileNBT(nbt);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        writeBaseNBT(nbt);
        writeProjectileNBT(nbt);
    }

    /**
     * Read base projectile properties from NBT.
     */
    protected void readBaseNBT(NBTTagCompound nbt) {
        this.size = nbt.getFloat("Size");
        this.innerColor = nbt.getInteger("InnerColor");
        this.outerColor = nbt.getInteger("OuterColor");
        this.outerColorEnabled = !nbt.hasKey("OuterColorEnabled") || nbt.getBoolean("OuterColorEnabled");
        this.outerColorWidth = nbt.hasKey("OuterColorWidth") ? nbt.getFloat("OuterColorWidth") : 1.8f;
        this.rotationSpeed = nbt.hasKey("RotationSpeed") ? nbt.getFloat("RotationSpeed") : 4.0f;

        this.damage = nbt.getFloat("Damage");
        this.knockback = nbt.getFloat("Knockback");
        this.knockbackUp = nbt.getFloat("KnockbackUp");

        this.explosive = nbt.getBoolean("Explosive");
        this.explosionRadius = nbt.getFloat("ExplosionRadius");
        this.explosionDamageFalloff = nbt.getFloat("ExplosionDamageFalloff");

        this.stunDuration = nbt.getInteger("StunDuration");
        this.slowDuration = nbt.getInteger("SlowDuration");
        this.slowLevel = nbt.getInteger("SlowLevel");

        this.maxDistance = nbt.getFloat("MaxDistance");
        this.maxLifetime = nbt.getInteger("MaxLifetime");
        this.deathWorldTime = nbt.hasKey("DeathWorldTime") ? nbt.getLong("DeathWorldTime") : -1;

        this.startX = nbt.getDouble("StartX");
        this.startY = nbt.getDouble("StartY");
        this.startZ = nbt.getDouble("StartZ");

        this.ownerEntityId = nbt.getInteger("OwnerId");
        this.targetEntityId = nbt.getInteger("TargetId");

        this.motionX = nbt.getDouble("MotionX");
        this.motionY = nbt.getDouble("MotionY");
        this.motionZ = nbt.getDouble("MotionZ");

        if (this.worldObj != null && this.worldObj.isRemote) {
            this.renderCurrentSize = this.size;
            this.prevRenderSize = this.size;
        }
    }

    /**
     * Write base projectile properties to NBT.
     */
    protected void writeBaseNBT(NBTTagCompound nbt) {
        nbt.setFloat("Size", size);
        nbt.setInteger("InnerColor", innerColor);
        nbt.setInteger("OuterColor", outerColor);
        nbt.setBoolean("OuterColorEnabled", outerColorEnabled);
        nbt.setFloat("OuterColorWidth", outerColorWidth);
        nbt.setFloat("RotationSpeed", rotationSpeed);

        nbt.setFloat("Damage", damage);
        nbt.setFloat("Knockback", knockback);
        nbt.setFloat("KnockbackUp", knockbackUp);

        nbt.setBoolean("Explosive", explosive);
        nbt.setFloat("ExplosionRadius", explosionRadius);
        nbt.setFloat("ExplosionDamageFalloff", explosionDamageFalloff);

        nbt.setInteger("StunDuration", stunDuration);
        nbt.setInteger("SlowDuration", slowDuration);
        nbt.setInteger("SlowLevel", slowLevel);

        nbt.setFloat("MaxDistance", maxDistance);
        nbt.setLong("DeathWorldTime", deathWorldTime);
        nbt.setInteger("MaxLifetime", maxLifetime);

        nbt.setDouble("StartX", startX);
        nbt.setDouble("StartY", startY);
        nbt.setDouble("StartZ", startZ);

        nbt.setInteger("OwnerId", ownerEntityId);
        nbt.setInteger("TargetId", targetEntityId);

        nbt.setDouble("MotionX", motionX);
        nbt.setDouble("MotionY", motionY);
        nbt.setDouble("MotionZ", motionZ);
    }

    /**
     * Subclass-specific NBT reading.
     */
    protected abstract void readProjectileNBT(NBTTagCompound nbt);

    /**
     * Subclass-specific NBT writing.
     */
    protected abstract void writeProjectileNBT(NBTTagCompound nbt);

    // ==================== SPAWN DATA ====================

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = new NBTTagCompound();
            this.writeEntityToNBT(compound);
            cpw.mods.fml.common.network.ByteBufUtils.writeTag(buffer, compound);
        } catch (Exception ignored) {}
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = cpw.mods.fml.common.network.ByteBufUtils.readTag(buffer);
            if (compound != null) {
                this.readEntityFromNBT(compound);
            }
        } catch (Exception ignored) {}
    }

    // ==================== COLLISION SETTINGS ====================

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean isBurning() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public float getShadowSize() {
        return 0.0f;
    }
}
