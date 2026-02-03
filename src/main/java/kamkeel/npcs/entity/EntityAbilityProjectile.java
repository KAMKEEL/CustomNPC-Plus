package kamkeel.npcs.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.data.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.api.entity.IEnergyProjectile;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all ability projectiles (Orb, Disc, Beam, Laser).
 * Provides common functionality for visuals, combat, effects, and interpolation.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public abstract class EntityAbilityProjectile extends Entity implements IEnergyProjectile, IEntityAdditionalSpawnData {

    // ==================== VISUAL PROPERTIES ====================
    protected float size = 1.0f;

    // ==================== COMBAT PROPERTIES ====================
    protected EnergyCombatData combatData = new EnergyCombatData();

    // ==================== EFFECT PROPERTIES ====================
    protected List<AbilityEffect> effects = new ArrayList<>();

    // ==================== LIGHTNING EFFECT PROPERTIES ====================
    protected EnergyLightningData lightningData = new EnergyLightningData();

    // Client-side lightning state (not saved to NBT)
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public transient Object lightningState; // Actually AttachedLightningRenderer.LightningState

    // ==================== DISPLAY PROPERTIES ====================
    protected EnergyDisplayData displayData = new EnergyDisplayData();

    // ==================== ANCHOR PROPERTIES ====================
    protected EnergyAnchorData anchorData = new EnergyAnchorData(AnchorPoint.FRONT);

    // ==================== LIFESPAN PROPERTIES ====================
    protected EnergyLifespanData lifespanData = new EnergyLifespanData();
    protected long deathWorldTime = -1;  // World time when entity should die (-1 = not set)

    // ==================== HOMING PROPERTIES ====================
    protected EnergyHomingData homingData = new EnergyHomingData();

    // ==================== TRACKING ====================
    protected double startX, startY, startZ;
    protected int ownerEntityId = -1;
    protected int targetEntityId = -1;
    protected int siblingEntityId = -1;

    // ==================== STATE ====================
    protected boolean hasHit = false;
    protected boolean previewMode = false; // Client-side preview mode (no damage/effects)
    protected EntityLivingBase previewOwner = null; // Direct reference for GUI preview (no world lookup)

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
    protected void initProjectile(EntityLivingBase owner, EntityLivingBase target, EntityAbilityProjectile sibling,
                                  double x, double y, double z, float size,
                                  EnergyDisplayData display, EnergyCombatData combat,
                                  EnergyLightningData lightning, EnergyLifespanData lifespan) {
        this.setPosition(x, y, z);
        this.startX = x;
        this.startY = y;
        this.startZ = z;

        this.ownerEntityId = owner.getEntityId();
        this.targetEntityId = target != null ? target.getEntityId() : -1;
        this.siblingEntityId = sibling != null ? sibling.getEntityId() : -1;

        // Visual
        this.size = size;

        this.displayData = display;
        this.combatData = combat;
        this.lifespanData = lifespan; // deathWorldTime will be set on first tick when world is available
        this.lightningData = lightning;

        // Initialize render size
        this.renderCurrentSize = size;
        this.prevRenderSize = size;
    }

    // Initialize projectile without a sibling
    protected void initProjectile(EntityLivingBase owner, EntityLivingBase target,
                                  double x, double y, double z, float size,
                                  EnergyDisplayData display, EnergyCombatData combat,
                                  EnergyLightningData lightning, EnergyLifespanData lifespan) {
        initProjectile(owner, target, null, x, y, z, size, display, combat, lightning, lifespan);
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
            // Failsafe: if owner entity is gone, dead, or NPC was killed/reset, self-destruct
            if (ownerEntityId >= 0 && ticksExisted > 5) {
                Entity owner = worldObj.getEntityByID(ownerEntityId);
                if (owner == null || owner.isDead) {
                    this.setDead();
                    return;
                }
                if (owner instanceof EntityNPCInterface && ((EntityNPCInterface) owner).isKilled()) {
                    this.setDead();
                    return;
                }
            }

            // Set death time on first tick if not already set (handles chunk load/unload)
            if (deathWorldTime < 0 && worldObj != null) {
                deathWorldTime = worldObj.getTotalWorldTime() + getMaxLifetime();
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
        this.rotationValX += getRotationSpeed() * 0.7f;
        this.rotationValY += getRotationSpeed();
        this.rotationValZ += getRotationSpeed() * 0.5f;

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
        return distTraveled >= getMaxDistance();
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
        applyDamage(target, this.getDamage(), this.getKnockback());
    }

    protected void applyDamage(EntityLivingBase target, float dmg, float kb) {
        if (previewMode) return; // Skip damage in preview mode
        Entity owner = getOwnerEntity();

        if (owner instanceof EntityNPCInterface) {
            target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), dmg);
        } else if (owner instanceof EntityPlayer) {
            target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), dmg);
        } else if (owner instanceof EntityLivingBase) {
            target.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) owner), dmg);
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
                double kbY = getKnockbackUp() > 0 ? getKnockbackUp() : 0.1;
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

        Entity owner = getOwnerEntity();

        AxisAlignedBB explosionBox = AxisAlignedBB.getBoundingBox(
            posX - getExplosionRadius(), posY - getExplosionRadius(), posZ - getExplosionRadius(),
            posX + getExplosionRadius(), posY + getExplosionRadius(), posZ + getExplosionRadius()
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

            if (dist <= getExplosionRadius()) {
                float falloff = 1.0f - (float) (dist / getExplosionRadius()) * getExplosionDamageFalloff();
                applyDamage(target, getDamage() * falloff, getKnockback() * falloff);
            }
        }
    }

    // ==================== ENTITY HELPERS ====================

    public Entity getOwnerEntity() {
        // In preview mode, use direct reference (no world lookup)
        if (previewMode && previewOwner != null) {
            return previewOwner;
        }
        if (ownerEntityId == -1) return null;
        return worldObj.getEntityByID(ownerEntityId);
    }

    @Override
    public int getOwnerEntityId() {
        return ownerEntityId;
    }

    @Override
    public IEntity getOwner() {
        if (previewMode) return null;
        if (getOwnerEntity() == null) return null;
        return NpcAPI.Instance().getIEntity(getOwnerEntity());
    }

    /**
     * Set the preview owner for GUI preview mode.
     * This allows anchor point calculations without world entity lookup.
     */
    public void setPreviewOwner(EntityLivingBase owner) {
        this.previewOwner = owner;
    }

    @Override
    public int getTargetEntityId() {
        return targetEntityId;
    }

    public Entity getTargetEntity() {
        if (targetEntityId == -1) return null;
        return worldObj.getEntityByID(targetEntityId);
    }

    @Override
    public IEntity getTarget() {
        if (getTargetEntity() == null) return null;
        return NpcAPI.Instance().getIEntity(getTargetEntity());
    }

    @Override
    public int getSiblingEntityId() {
        return siblingEntityId;
    }

    @Override
    public void setSiblingEntityId(int siblingEntityId) {
        if (!(worldObj.getEntityByID(siblingEntityId) instanceof EntityAbilityProjectile)) return;

        this.siblingEntityId = siblingEntityId;
    }

    @Override
    public IEnergyProjectile getSibling() {
        if (siblingEntityId == -1) return null;
        return (EntityAbilityProjectile) worldObj.getEntityByID(siblingEntityId);
    }

    /**
     * Check if an entity should be ignored for collision.
     */
    protected boolean shouldIgnoreEntity(Entity entity) {
        Entity owner = getOwnerEntity();
        if (entity == owner) return true;

        if (owner instanceof EntityNPCInterface && entity instanceof EntityNPCInterface) {
            EntityNPCInterface ownerNpc = (EntityNPCInterface) owner;
            EntityNPCInterface targetNpc = (EntityNPCInterface) entity;
            if (ownerNpc.faction.id == targetNpc.faction.id) return true;
        }

        if (entity.getEntityId() == getSiblingEntityId())
            return true;

        return false;
    }

    // ==================== VISUAL GETTERS ====================

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public int getInnerColor() {
        return displayData.getInnerColor();
    }

    @Override
    public int getOuterColor() {
        return displayData.getOuterColor();
    }

    @Override
    public boolean isOuterColorEnabled() {
        return displayData.isOuterColorEnabled();
    }

    @Override
    public float getOuterColorWidth() {
        return displayData.getOuterColorWidth();
    }

    @Override
    public float getOuterColorAlpha() {
        return displayData.getOuterColorAlpha();
    }

    @Override
    public float getRotationSpeed() {
        return displayData.getRotationSpeed();
    }

    // ==================== ROTATION GETTERS ====================

    @Override
    public float getInterpolatedRotationX(float partialTicks) {
        return this.prevRotationValX + (this.rotationValX - this.prevRotationValX) * partialTicks;
    }

    @Override
    public float getInterpolatedRotationY(float partialTicks) {
        return this.prevRotationValY + (this.rotationValY - this.prevRotationValY) * partialTicks;
    }

    @Override
    public float getInterpolatedRotationZ(float partialTicks) {
        return this.prevRotationValZ + (this.rotationValZ - this.prevRotationValZ) * partialTicks;
    }

    @Override
    public float getInterpolatedSize(float partialTicks) {
        return this.prevRenderSize + (this.renderCurrentSize - this.prevRenderSize) * partialTicks;
    }

    // ==================== LIGHTNING GETTERS ====================

    @Override
    public boolean hasLightningEffect() {
        return lightningData.isLightningEffect();
    }

    @Override
    public float getLightningDensity() {
        return lightningData.getLightningDensity();
    }

    @Override
    public float getLightningRadius() {
        return lightningData.getLightningRadius();
    }

    @Override
    public int getLightningFadeTime() {
        return lightningData.getLightningFadeTime();
    }

    // ==================== LIFESPAN GETTERS ====================

    @Override
    public float getMaxDistance() { return lifespanData.getMaxDistance(); }

    @Override
    public int getMaxLifetime() { return lifespanData.getMaxLifetime(); }

    // ==================== COMBAT GETTERS ====================

    @Override
    public float getDamage() { return combatData.getDamage(); }

    @Override
    public float getKnockback() { return combatData.knockback; }

    @Override
    public float getKnockbackUp() { return combatData.knockbackUp; }

    @Override
    public boolean isExplosive() { return combatData.isExplosive(); }

    @Override
    public float getExplosionRadius() { return combatData.explosionRadius; }

    @Override
    public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }

    // ==================== VISUAL GETTERS ====================

    @Override
    public float getSpeed() { return homingData.getSpeed(); }

    @Override
    public boolean isHoming() { return homingData.isHoming(); }

    @Override
    public float getHomingStrength() { return homingData.getHomingStrength(); }

    @Override
    public float getHomingRange() { return homingData.getHomingRange(); }

    // ==================== ANCHOR GETTERS ====================

    public AnchorPoint getAnchorPoint() { return anchorData.getAnchorPoint(); }

    @Override
    public int getAnchor() { return anchorData.getAnchor(); }

    @Override
    public float getAnchorOffsetX() { return anchorData.getAnchorOffsetX(); }

    @Override
    public float getAnchorOffsetY() { return anchorData.getAnchorOffsetY(); }

    @Override
    public float getAnchorOffsetZ() { return anchorData.getAnchorOffsetZ(); }

    // ==================== MOVEMENT GETTERS ====================

    @Override public double getStartX() { return startX; }

    @Override public double getStartY() { return startY; }

    @Override public double getStartZ() { return startZ; }

    @Override public double getMotionX() { return motionX; }

    @Override public double getMotionY() { return motionY; }

    @Override public double getMotionZ() { return motionZ; }

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

        // Read effects list
        this.effects.clear();
        if (nbt.hasKey("Effects")) {
            NBTTagList effectsList = nbt.getTagList("Effects", 10); // 10 = compound tag
            for (int i = 0; i < effectsList.tagCount(); i++) {
                effects.add(AbilityEffect.fromNBT(effectsList.getCompoundTagAt(i)));
            }
        }

        this.deathWorldTime = nbt.hasKey("DeathWorldTime") ? nbt.getLong("DeathWorldTime") : -1;

        this.startX = nbt.getDouble("StartX");
        this.startY = nbt.getDouble("StartY");
        this.startZ = nbt.getDouble("StartZ");

        this.ownerEntityId = nbt.getInteger("OwnerId");
        this.targetEntityId = nbt.getInteger("TargetId");
        this.siblingEntityId = nbt.getInteger("SiblingId");

        this.motionX = nbt.getDouble("MotionX");
        this.motionY = nbt.getDouble("MotionY");
        this.motionZ = nbt.getDouble("MotionZ");

        if (this.worldObj != null && this.worldObj.isRemote) {
            this.renderCurrentSize = this.size;
            this.prevRenderSize = this.size;
        }

        anchorData.readNBT(nbt);
        combatData.readNBT(nbt);
        displayData.readNBT(nbt);
        homingData.readNBT(nbt);
        lifespanData.readNBT(nbt);
        lightningData.readNBT(nbt);
    }

    /**
     * Write base projectile properties to NBT.
     */
    protected void writeBaseNBT(NBTTagCompound nbt) {
        nbt.setFloat("Size", size);

        // Write effects list
        if (!effects.isEmpty()) {
            NBTTagList effectsList = new NBTTagList();
            for (AbilityEffect effect : effects) {
                effectsList.appendTag(effect.writeNBT());
            }
            nbt.setTag("Effects", effectsList);
        }

        nbt.setLong("DeathWorldTime", deathWorldTime);

        nbt.setDouble("StartX", startX);
        nbt.setDouble("StartY", startY);
        nbt.setDouble("StartZ", startZ);

        nbt.setInteger("OwnerId", ownerEntityId);
        nbt.setInteger("TargetId", targetEntityId);
        nbt.setInteger("SiblingId", targetEntityId);

        nbt.setDouble("MotionX", motionX);
        nbt.setDouble("MotionY", motionY);
        nbt.setDouble("MotionZ", motionZ);

        anchorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        displayData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
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
