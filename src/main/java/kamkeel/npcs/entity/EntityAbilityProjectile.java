package kamkeel.npcs.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.data.EnergyColorData;
import kamkeel.npcs.controllers.data.ability.data.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
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
    protected float outerColorWidth = 0.4f; // Additive offset from inner size
    protected float outerColorAlpha = 0.5f;
    protected float rotationSpeed = 4.0f;

    // ==================== COMBAT PROPERTIES ====================
    protected float damage = 7.0f;
    protected float knockback = 1.0f;
    protected float knockbackUp = 0.1f;

    // ==================== EFFECT PROPERTIES ====================
    protected List<AbilityEffect> effects = new ArrayList<>();

    // ==================== EXPLOSION PROPERTIES ====================
    protected boolean explosive = false;
    protected float explosionRadius = 3.0f;
    protected float explosionDamageFalloff = 0.5f;

    // ==================== LIGHTNING EFFECT PROPERTIES ====================
    protected boolean lightningEffect = false;
    protected float lightningDensity = 0.15f;     // Bolts spawned per tick (0.15 = ~15% chance per frame)
    protected float lightningRadius = 0.5f;       // Max distance lightning can arc from center
    protected int lightningFadeTime = 6;          // Ticks before lightning fades out

    // Client-side lightning state (not saved to NBT)
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public transient Object lightningState; // Actually AttachedLightningRenderer.LightningState

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
    protected boolean previewMode = false; // Client-side preview mode (no damage/effects)
    protected EntityNPCInterface previewOwner = null; // Direct reference for GUI preview (no world lookup)

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
     * Initialize common properties using data classes. Call from subclass constructors.
     */
    protected void initProjectile(EntityNPCInterface owner, EntityLivingBase target,
                                   double x, double y, double z, float size,
                                   EnergyColorData color, EnergyCombatData combat,
                                   EnergyLightningData lightning, EnergyLifespanData lifespan) {
        this.setPosition(x, y, z);
        this.startX = x;
        this.startY = y;
        this.startZ = z;

        this.ownerEntityId = owner.getEntityId();
        this.targetEntityId = target != null ? target.getEntityId() : -1;

        // Visual
        this.size = size;
        this.innerColor = color.innerColor;
        this.outerColor = color.outerColor;
        this.outerColorEnabled = color.outerColorEnabled;
        this.outerColorWidth = color.outerColorWidth;
        this.outerColorAlpha = color.outerColorAlpha;
        this.rotationSpeed = color.rotationSpeed;

        // Combat
        this.damage = combat.damage;
        this.knockback = combat.knockback;
        this.knockbackUp = combat.knockbackUp;

        // Explosion
        this.explosive = combat.explosive;
        this.explosionRadius = combat.explosionRadius;
        this.explosionDamageFalloff = combat.explosionDamageFalloff;

        // Lifespan
        this.maxDistance = lifespan.maxDistance;
        this.maxLifetime = lifespan.maxLifetime;
        // deathWorldTime will be set on first tick when world is available

        // Lightning
        this.lightningEffect = lightning.lightningEffect;
        this.lightningDensity = lightning.lightningDensity;
        this.lightningRadius = lightning.lightningRadius;
        this.lightningFadeTime = lightning.lightningFadeTime;

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

        // Skip super.onUpdate() in preview mode to avoid world checks
        if (!previewMode) {
            super.onUpdate();
        }

        // Update rotation
        updateRotation();

        // Lerp render size toward actual size
        this.renderCurrentSize = this.renderCurrentSize + (this.size - this.renderCurrentSize) * 0.15f;

        // Skip lifetime/distance checks in preview mode
        if (!previewMode) {
            // Set death time on first tick if not already set (handles chunk load/unload)
            if (deathWorldTime < 0 && worldObj != null) {
                deathWorldTime = worldObj.getTotalWorldTime() + maxLifetime;
            }

            // Check lifespan using world time (survives chunk unload/reload)
            if (deathWorldTime > 0 && worldObj.getTotalWorldTime() >= deathWorldTime) {
                this.setDead();
                return;
            }

            // Check max distance (subclass can override if needed)
            if (checkMaxDistance()) {
                this.setDead();
                return;
            }

            if (hasHit) {
                this.setDead();
                return;
            }
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

    // ==================== PREVIEW MODE ====================

    /**
     * Set preview mode for GUI display.
     * In preview mode, no damage or world effects are applied.
     */
    public void setPreviewMode(boolean preview) {
        this.previewMode = preview;
    }

    /**
     * Check if entity is in preview mode.
     */
    public boolean isPreviewMode() {
        return previewMode;
    }

    // ==================== DAMAGE & EFFECTS ====================

    protected void applyDamage(EntityLivingBase target) {
        if (previewMode) return; // Skip damage in preview mode
        applyDamage(target, this.damage, this.knockback);
    }

    protected void applyDamage(EntityLivingBase target, float dmg, float kb) {
        if (previewMode) return; // Skip damage in preview mode
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
        for (AbilityEffect effect : effects) {
            effect.apply(target);
        }
    }

    /**
     * Set the effects list from the ability's configured effects.
     */
    public void setEffects(List<AbilityEffect> effects) {
        this.effects = effects != null ? effects : new ArrayList<>();
    }

    protected void doExplosion() {
        if (previewMode) return; // Skip explosion in preview mode
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
        // In preview mode, use direct reference (no world lookup)
        if (previewMode && previewOwner != null) {
            return previewOwner;
        }
        if (ownerEntityId == -1) return null;
        return worldObj.getEntityByID(ownerEntityId);
    }

    /**
     * Set the preview owner for GUI preview mode.
     * This allows anchor point calculations without world entity lookup.
     */
    public void setPreviewOwner(EntityNPCInterface owner) {
        this.previewOwner = owner;
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

    public float getOuterColorAlpha() {
        return outerColorAlpha;
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

    // ==================== LIGHTNING GETTERS ====================

    public boolean hasLightningEffect() {
        return lightningEffect;
    }

    public float getLightningDensity() {
        return lightningDensity;
    }

    public float getLightningRadius() {
        return lightningRadius;
    }

    public int getLightningFadeTime() {
        return lightningFadeTime;
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
        this.outerColorAlpha = nbt.hasKey("OuterColorAlpha") ? nbt.getFloat("OuterColorAlpha") : 0.5f;
        this.outerColorWidth = nbt.hasKey("OuterColorWidth") ? nbt.getFloat("OuterColorWidth") : 0.4f;
        this.rotationSpeed = nbt.hasKey("RotationSpeed") ? nbt.getFloat("RotationSpeed") : 4.0f;

        this.damage = nbt.getFloat("Damage");
        this.knockback = nbt.getFloat("Knockback");
        this.knockbackUp = nbt.getFloat("KnockbackUp");

        this.explosive = nbt.getBoolean("Explosive");
        this.explosionRadius = nbt.getFloat("ExplosionRadius");
        this.explosionDamageFalloff = nbt.getFloat("ExplosionDamageFalloff");

        // Read effects list
        this.effects.clear();
        if (nbt.hasKey("Effects")) {
            NBTTagList effectsList = nbt.getTagList("Effects", 10); // 10 = compound tag
            for (int i = 0; i < effectsList.tagCount(); i++) {
                effects.add(AbilityEffect.fromNBT(effectsList.getCompoundTagAt(i)));
            }
        }

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

        // Lightning
        this.lightningEffect = nbt.getBoolean("LightningEffect");
        this.lightningDensity = nbt.hasKey("LightningDensity") ? nbt.getFloat("LightningDensity") : 0.15f;
        this.lightningRadius = nbt.hasKey("LightningRadius") ? nbt.getFloat("LightningRadius") : 0.5f;
        this.lightningFadeTime = nbt.hasKey("LightningFadeTime") ? nbt.getInteger("LightningFadeTime") : 6;

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
        nbt.setFloat("OuterColorAlpha", outerColorAlpha);
        nbt.setFloat("RotationSpeed", rotationSpeed);

        nbt.setFloat("Damage", damage);
        nbt.setFloat("Knockback", knockback);
        nbt.setFloat("KnockbackUp", knockbackUp);

        nbt.setBoolean("Explosive", explosive);
        nbt.setFloat("ExplosionRadius", explosionRadius);
        nbt.setFloat("ExplosionDamageFalloff", explosionDamageFalloff);

        // Write effects list
        if (!effects.isEmpty()) {
            NBTTagList effectsList = new NBTTagList();
            for (AbilityEffect effect : effects) {
                effectsList.appendTag(effect.writeNBT());
            }
            nbt.setTag("Effects", effectsList);
        }

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

        // Lightning
        nbt.setBoolean("LightningEffect", lightningEffect);
        nbt.setFloat("LightningDensity", lightningDensity);
        nbt.setFloat("LightningRadius", lightningRadius);
        nbt.setInteger("LightningFadeTime", lightningFadeTime);
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
