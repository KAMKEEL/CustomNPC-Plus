package kamkeel.npcs.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Energy Dome entity - a spherical barrier that blocks incoming energy projectiles.
 * Centered on the caster's position at time of casting.
 * Only blocks incoming attacks (not outgoing from allies inside).
 */
public class EntityEnergyDome extends Entity implements IEntityAdditionalSpawnData {

    // ==================== VISUAL PROPERTIES ====================
    protected EnergyDisplayData displayData = new EnergyDisplayData();
    protected EnergyLightningData lightningData = new EnergyLightningData();

    // ==================== BARRIER PROPERTIES ====================
    protected EnergyBarrierData barrierData = new EnergyBarrierData();
    protected float currentHealth;
    protected float domeRadius = 5.0f;

    // ==================== TRACKING ====================
    protected int ownerEntityId = -1;
    protected int ticksAlive = 0;

    // ==================== STATE ====================
    protected transient Ability sourceAbility = null;

    // ==================== DATA WATCHER INDICES ====================
    private static final int DW_HEALTH_PERCENT = 20;
    private static final int DW_HIT_FLASH = 21;

    // ==================== CLIENT STATE ====================
    @SideOnly(Side.CLIENT)
    public transient Object lightningState;

    public EntityEnergyDome(World world) {
        super(world);
        this.setSize(1.0f, 1.0f); // Hitbox doesn't really matter, we use radius checks
        this.noClip = true;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    public EntityEnergyDome(World world, EntityLivingBase owner, double x, double y, double z,
                            float domeRadius, EnergyDisplayData display, EnergyLightningData lightning,
                            EnergyBarrierData barrier) {
        this(world);
        this.setPosition(x, y, z);
        this.ownerEntityId = owner.getEntityId();
        this.domeRadius = domeRadius;
        this.displayData = display;
        this.lightningData = lightning;
        this.barrierData = barrier;
        this.currentHealth = barrier.maxHealth;
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(DW_HEALTH_PERCENT, 1.0f);
        this.dataWatcher.addObject(DW_HIT_FLASH, (byte) 0);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        super.onUpdate();
        ticksAlive++;

        if (!worldObj.isRemote) {
            // Check owner death
            if (ownerEntityId >= 0 && ticksAlive > 5) {
                Entity owner = worldObj.getEntityByID(ownerEntityId);
                if (owner != null) {
                    if (owner.isDead) { this.setDead(); return; }
                    if (owner instanceof EntityNPCInterface && ((EntityNPCInterface) owner).isKilled()) {
                        this.setDead(); return;
                    }
                }
            }

            // Duration check
            if (barrierData.useDuration && ticksAlive >= barrierData.durationTicks) {
                this.setDead();
                return;
            }

            // Reset hit flash
            if (getHitFlash() > 0) {
                setHitFlash((byte) (getHitFlash() - 1));
            }
        }
    }

    /**
     * Apply damage to this dome from a projectile.
     * Returns true if the dome absorbed the hit (projectile should be destroyed).
     */
    public boolean onProjectileHit(EntityAbilityProjectile projectile, float baseDamage) {
        if (!barrierData.useHealth) {
            // Duration-only mode: block but don't take damage
            triggerHitFlash();
            return true;
        }

        // Get damage multiplier for this projectile type
        String typeId = "";
        if (projectile.getSourceAbility() != null) {
            typeId = projectile.getSourceAbility().getTypeId();
        }
        float multiplier = barrierData.getMultiplier(typeId);
        float damage = baseDamage * multiplier;

        currentHealth -= damage;
        syncHealthPercent();
        triggerHitFlash();

        if (currentHealth <= 0) {
            this.setDead();
        }

        return true;
    }

    /**
     * Check if a projectile at the given position is entering this dome from outside.
     * Only blocks incoming projectiles (dot product check).
     */
    public boolean isIncomingProjectile(EntityAbilityProjectile projectile) {
        // Don't block projectiles from the dome's owner
        if (projectile.getOwnerEntityId() == this.ownerEntityId) return false;

        // Don't block projectiles from same-faction NPCs
        Entity owner = getOwnerEntity();
        Entity projOwner = projectile.getOwnerEntity();
        if (owner instanceof EntityNPCInterface && projOwner instanceof EntityNPCInterface) {
            if (((EntityNPCInterface) owner).faction.id == ((EntityNPCInterface) projOwner).faction.id) {
                return false;
            }
        }

        double dx = projectile.posX - this.posX;
        double dy = projectile.posY - this.posY;
        double dz = projectile.posZ - this.posZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        // Check if projectile is within dome radius
        if (distSq > domeRadius * domeRadius) return false;

        // Check if projectile is moving inward (dot product of velocity and position relative to center)
        double dot = dx * projectile.motionX + dy * projectile.motionY + dz * projectile.motionZ;
        // Negative dot = moving toward center = incoming
        return dot < 0;
    }

    // ==================== HELPERS ====================

    private void syncHealthPercent() {
        float percent = barrierData.useHealth && barrierData.maxHealth > 0
            ? Math.max(0, currentHealth / barrierData.maxHealth)
            : 1.0f;
        if (!worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_HEALTH_PERCENT, percent);
        }
    }

    private void triggerHitFlash() {
        if (!worldObj.isRemote) {
            setHitFlash((byte) 4);
        }
    }

    private void setHitFlash(byte value) {
        this.dataWatcher.updateObject(DW_HIT_FLASH, value);
    }

    public byte getHitFlash() {
        return this.dataWatcher.getWatchableObjectByte(DW_HIT_FLASH);
    }

    public float getHealthPercent() {
        return this.dataWatcher.getWatchableObjectFloat(DW_HEALTH_PERCENT);
    }

    public Entity getOwnerEntity() {
        if (ownerEntityId == -1) return null;
        return worldObj.getEntityByID(ownerEntityId);
    }

    public int getOwnerEntityId() { return ownerEntityId; }
    public float getDomeRadius() { return domeRadius; }
    public EnergyDisplayData getDisplayData() { return displayData; }
    public EnergyLightningData getLightningData() { return lightningData; }
    public EnergyBarrierData getBarrierData() { return barrierData; }
    public float getCurrentHealth() { return currentHealth; }

    public void setSourceAbility(Ability ability) { this.sourceAbility = ability; }
    public Ability getSourceAbility() { return sourceAbility; }

    // ==================== VISUAL GETTERS ====================

    public int getInnerColor() { return displayData.innerColor; }
    public int getOuterColor() { return displayData.outerColor; }
    public boolean isOuterColorEnabled() { return displayData.outerColorEnabled; }
    public float getOuterColorWidth() { return displayData.outerColorWidth; }
    public float getOuterColorAlpha() { return displayData.outerColorAlpha; }
    public boolean hasLightningEffect() { return lightningData.lightningEffect; }
    public float getLightningDensity() { return lightningData.lightningDensity; }
    public float getLightningRadius() { return lightningData.lightningRadius; }
    public int getLightningFadeTime() { return lightningData.lightningFadeTime; }

    // ==================== BRIGHTNESS ====================

    @Override
    public float getBrightness(float partialTicks) { return 1.0f; }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float partialTicks) { return 0xF000F0; }

    @Override
    public boolean shouldRenderInPass(int pass) { return pass == 1; }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d = domeRadius * 8.0D;
        d *= 64.0D;
        return distance < d * d;
    }

    // ==================== COLLISION SETTINGS ====================

    @Override
    public boolean canBeCollidedWith() { return false; }

    @Override
    public boolean canBePushed() { return false; }

    @Override
    protected boolean canTriggerWalking() { return false; }

    @Override
    public boolean isBurning() { return false; }

    @SideOnly(Side.CLIENT)
    public float getShadowSize() { return 0.0f; }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.domeRadius = nbt.getFloat("DomeRadius");
        this.ownerEntityId = nbt.getInteger("OwnerId");
        this.ticksAlive = nbt.getInteger("TicksAlive");
        this.currentHealth = nbt.getFloat("CurrentHealth");
        displayData.readNBT(nbt);
        lightningData.readNBT(nbt);
        barrierData.readNBT(nbt);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setFloat("DomeRadius", domeRadius);
        nbt.setInteger("OwnerId", ownerEntityId);
        nbt.setInteger("TicksAlive", ticksAlive);
        nbt.setFloat("CurrentHealth", currentHealth);
        displayData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        barrierData.writeNBT(nbt);
    }

    // ==================== SPAWN DATA ====================

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = new NBTTagCompound();
            this.writeEntityToNBT(compound);
            cpw.mods.fml.common.network.ByteBufUtils.writeTag(buffer, compound);
        } catch (Exception e) {
            noppes.npcs.LogWriter.error("Error writing energy dome spawn data", e);
        }
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = cpw.mods.fml.common.network.ByteBufUtils.readTag(buffer);
            if (compound != null) {
                this.readEntityFromNBT(compound);
            }
        } catch (Exception e) {
            noppes.npcs.LogWriter.error("Error reading energy dome spawn data", e);
        }
    }
}
