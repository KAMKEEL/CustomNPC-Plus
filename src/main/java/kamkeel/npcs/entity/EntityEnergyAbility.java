package kamkeel.npcs.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Universal base class for all energy ability entities (Projectiles, Barriers, Sweeper).
 * Provides shared visual data, owner tracking, charging state, and common entity properties.
 */
public abstract class EntityEnergyAbility extends Entity implements IEntityAdditionalSpawnData {

    // ==================== VISUAL PROPERTIES ====================
    protected EnergyDisplayData displayData = new EnergyDisplayData();
    protected EnergyLightningData lightningData = new EnergyLightningData();

    // Client-side lightning state (not saved to NBT)
    @SideOnly(Side.CLIENT)
    public transient Object lightningState; // Actually AttachedLightningRenderer.LightningState

    // ==================== TRACKING ====================
    protected int ownerEntityId = -1;

    // ==================== STATE ====================
    /**
     * The ability that spawned this entity. Transient, not saved to NBT.
     */
    protected transient Ability sourceAbility = null;
    protected boolean previewMode = false;
    protected EntityLivingBase previewOwner = null;

    // ==================== CHARGING STATE ====================
    protected boolean charging = false;
    protected int chargeDuration = 0;
    protected int chargeTick = 0;
    protected static final int DW_CHARGING = 20;

    public EntityEnergyAbility(World world) {
        super(world);
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(DW_CHARGING, (byte) 0);
    }

    // ==================== CHARGING ====================

    /**
     * Check if entity is in charging state (synced via data watcher).
     * In preview mode, uses local field since data watcher isn't synced.
     */
    public boolean isCharging() {
        if (previewMode) return this.charging;
        return this.dataWatcher.getWatchableObjectByte(DW_CHARGING) == 1;
    }

    /**
     * Set charging state (server only, synced to clients via data watcher).
     */
    public void setCharging(boolean value) {
        this.charging = value;
        if (!worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_CHARGING, (byte) (value ? 1 : 0));
        }
    }

    public float getChargeProgress() {
        if (chargeDuration <= 0) return 1.0f;
        return Math.min(1.0f, (float) chargeTick / chargeDuration);
    }

    public float getInterpolatedChargeProgress(float partialTicks) {
        if (chargeDuration <= 0) return 1.0f;
        float prevProgress = Math.max(0, (float) (chargeTick - 1) / chargeDuration);
        float currProgress = Math.min(1.0f, (float) chargeTick / chargeDuration);
        return prevProgress + (currProgress - prevProgress) * partialTicks;
    }

    // ==================== OWNER ====================

    /**
     * Get the owner entity. In preview mode, returns the preview owner directly.
     */
    public Entity getOwnerEntity() {
        if (previewMode && previewOwner != null) {
            return previewOwner;
        }
        if (ownerEntityId == -1) return null;
        return worldObj.getEntityByID(ownerEntityId);
    }

    public int getOwnerEntityId() {
        return ownerEntityId;
    }

    public void setOwnerEntityId(int id) {
        this.ownerEntityId = id;
    }

    // ==================== SOURCE ABILITY ====================

    public Ability getSourceAbility() {
        return sourceAbility;
    }

    public void setSourceAbility(Ability ability) {
        this.sourceAbility = ability;
    }

    // ==================== PREVIEW MODE ====================

    public boolean isPreviewMode() {
        return previewMode;
    }

    public void setPreviewMode(boolean preview) {
        this.previewMode = preview;
    }

    public void setPreviewOwner(EntityLivingBase owner) {
        this.previewOwner = owner;
    }

    // ==================== DISPLAY GETTERS & SETTERS ====================

    public EnergyDisplayData getDisplayData() {
        return displayData;
    }

    public EnergyLightningData getLightningData() {
        return lightningData;
    }

    public int getInnerColor() {
        return displayData.getInnerColor();
    }

    public void setInnerColor(int color) {
        displayData.setInnerColor(color);
    }

    public float getInnerAlpha() {
        return displayData.getInnerAlpha();
    }

    public void setInnerAlpha(float alpha) {
        displayData.setInnerAlpha(alpha);
    }

    public int getOuterColor() {
        return displayData.getOuterColor();
    }

    public void setOuterColor(int color) {
        displayData.setOuterColor(color);
    }

    public boolean isOuterColorEnabled() {
        return displayData.isOuterColorEnabled();
    }

    public void setOuterColorEnabled(boolean enabled) {
        displayData.setOuterColorEnabled(enabled);
    }

    public float getOuterColorWidth() {
        return displayData.getOuterColorWidth();
    }

    public void setOuterColorWidth(float width) {
        displayData.setOuterColorWidth(width);
    }

    public float getOuterColorAlpha() {
        return displayData.getOuterColorAlpha();
    }

    public void setOuterColorAlpha(float alpha) {
        displayData.setOuterColorAlpha(alpha);
    }

    public float getRotationSpeed() {
        return displayData.getRotationSpeed();
    }

    public void setRotationSpeed(float speed) {
        displayData.setRotationSpeed(speed);
    }

    // ==================== LIGHTNING GETTERS & SETTERS ====================

    public boolean hasLightningEffect() {
        return lightningData.isLightningEffect();
    }

    public void setLightningEffect(boolean enabled) {
        lightningData.setLightningEffect(enabled);
    }

    public float getLightningDensity() {
        return lightningData.getLightningDensity();
    }

    public void setLightningDensity(float density) {
        lightningData.setLightningDensity(density);
    }

    public float getLightningRadius() {
        return lightningData.getLightningRadius();
    }

    public void setLightningRadius(float radius) {
        lightningData.setLightningRadius(radius);
    }

    public int getLightningFadeTime() {
        return lightningData.getLightningFadeTime();
    }

    public void setLightningFadeTime(int ticks) {
        lightningData.setLightningFadeTime(ticks);
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

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1; // Translucent pass
    }

    // ==================== COLLISION SETTINGS ====================

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

    // ==================== NBT HELPERS ====================

    /**
     * Write shared energy base fields to NBT (owner, display, lightning).
     * Call from subclass NBT writers before writing type-specific fields.
     */
    protected void writeEnergyBaseNBT(NBTTagCompound nbt) {
        nbt.setInteger("OwnerId", ownerEntityId);
        displayData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
    }

    /**
     * Read shared energy base fields from NBT (owner, display, lightning).
     * Call from subclass NBT readers before reading type-specific fields.
     */
    protected void readEnergyBaseNBT(NBTTagCompound nbt) {
        this.ownerEntityId = nbt.getInteger("OwnerId");
        displayData.readNBT(nbt);
        lightningData.readNBT(nbt);
    }

    // ==================== SPAWN DATA ====================

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = new NBTTagCompound();
            this.writeEntityToNBT(compound);
            cpw.mods.fml.common.network.ByteBufUtils.writeTag(buffer, compound);
        } catch (Exception e) {
            noppes.npcs.LogWriter.error("Error writing energy ability spawn data", e);
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
            noppes.npcs.LogWriter.error("Error reading energy ability spawn data", e);
        }
    }
}
