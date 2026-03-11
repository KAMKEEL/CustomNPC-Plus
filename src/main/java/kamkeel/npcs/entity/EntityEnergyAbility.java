package kamkeel.npcs.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.controllers.data.MagicData;

/**
 * Universal base class for all energy ability entities (Projectiles, Barriers, Sweeper).
 * Provides shared visual data, owner tracking, charging state, and common entity properties.
 */
public abstract class EntityEnergyAbility extends Entity implements IEntityAdditionalSpawnData {

    // ==================== SAFETY CONSTANTS ====================
    protected static final int CHARGE_TIMEOUT_GRACE = 60;          // Ticks of grace after charge should have ended
    protected static final int PREVIEW_CHARGE_GRACE = 3;            // Ticks of grace for client-only charge previews
    protected static final int HARD_LIFETIME_CAP = 1200;           // 60 seconds absolute max for projectiles/sweepers
    protected static final int BARRIER_HARD_LIFETIME_CAP = 12000;  // 10 minutes absolute max for barriers
    protected static final float MAX_ENTITY_SIZE = 100.0f;         // Max size/width/length for any energy entity
    protected static final float MAX_ENTITY_RADIUS = 50.0f;        // Max radius for dome barriers

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

    /**
     * Custom damage data for script-created entities that don't have a sourceAbility.
     * Used by EnergyController handlers (e.g. DBC Addon) to carry damage configuration
     * directly on the entity. Persistent — saved to spawn data for client sync.
     */
    protected NBTTagCompound customDamageData = null;

    /** Magic data for this entity — defines magic types for outgoing damage or barrier defense. */
    protected MagicData magicData = new MagicData();

    protected boolean ignoreIFrames = false;
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

    public void setChargeDuration(int duration) {
        this.chargeDuration = duration;
    }

    /**
     * Reset chargeTick back to chargeDuration so the charging timeout
     * grace period restarts.  Used by items that let the player hold
     * a fully-charged orb indefinitely.
     */
    public void resetChargeTick() {
        this.chargeTick = this.chargeDuration;
    }

    public float getChargeProgress() {
        if (chargeDuration <= 0) return 1.0f;
        return Math.min(1.0f, (float) chargeTick / chargeDuration);
    }

    public float getInterpolatedChargeProgress(float partialTicks) {
        if (chargeDuration <= 0) return 1.0f;
        float prevProgress = Math.min(1.0f, Math.max(0, (float) (chargeTick - 1) / chargeDuration));
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

    // ==================== CUSTOM DAMAGE DATA ====================

    public NBTTagCompound getCustomDamageData() {
        return customDamageData;
    }

    public void setCustomDamageData(NBTTagCompound data) {
        this.customDamageData = data;
    }

    public MagicData getMagicData() {
        return magicData;
    }

    public void setMagicData(MagicData data) {
        this.magicData = data != null ? data : new MagicData();
    }

    public boolean isIgnoreIFrames() {
        return ignoreIFrames;
    }

    public void setIgnoreIFrames(boolean ignoreIFrames) {
        this.ignoreIFrames = ignoreIFrames;
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

    // ==================== SAFETY HELPERS ====================

    /**
     * Sanitize a float value loaded from NBT or external sources.
     * Guards against NaN, Infinity, negative values, and values exceeding a max.
     */
    protected static float sanitize(float value, float fallback, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value) || value < 0) return fallback;
        return Math.min(value, max);
    }

    // ==================== NBT HELPERS ====================

    /**
     * Write shared energy base fields to NBT (owner, display, lightning).
     * Call from subclass NBT writers before writing type-specific fields.
     */
    protected void writeEnergyBaseNBT(NBTTagCompound nbt) {
        nbt.setInteger("OwnerId", ownerEntityId);
        nbt.setBoolean("IgnoreIFrames", ignoreIFrames);
        displayData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        if (customDamageData != null) {
            nbt.setTag("CustomDamageData", customDamageData);
        }
        magicData.writeToNBT(nbt);
    }

    /**
     * Read shared energy base fields from NBT (owner, display, lightning).
     * Call from subclass NBT readers before reading type-specific fields.
     */
    protected void readEnergyBaseNBT(NBTTagCompound nbt) {
        this.ownerEntityId = nbt.getInteger("OwnerId");
        this.ignoreIFrames = nbt.getBoolean("IgnoreIFrames");
        displayData.readNBT(nbt);
        lightningData.readNBT(nbt);
        if (nbt.hasKey("CustomDamageData")) {
            this.customDamageData = nbt.getCompoundTag("CustomDamageData");
        }
        magicData.readToNBT(nbt);
    }

    // ==================== WORLD NBT (INTENTIONALLY EMPTY) ====================

    /**
     * Ability entities are transient — they must not survive world saves.
     * If Minecraft deserializes one from a chunk, kill it immediately.
     */
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        // Intentionally empty — ability entities are transient (not saved to world)
    }

    // ==================== SPAWN DATA ====================

    /**
     * Write entity data for client sync. Subclasses override this
     * to serialize their fields. Separate from writeEntityToNBT
     * which is intentionally empty (entities are transient, not saved to world).
     */
    protected void writeSpawnNBT(NBTTagCompound nbt) {
        writeEnergyBaseNBT(nbt);
    }

    /**
     * Read entity data from client sync. Subclasses override this
     * to deserialize their fields. Separate from readEntityFromNBT
     * which is intentionally empty (entities are transient, not saved to world).
     */
    protected void readSpawnNBT(NBTTagCompound nbt) {
        readEnergyBaseNBT(nbt);
    }

    /**
     * Export spawn NBT for non-world preview rendering sync.
     */
    public final NBTTagCompound exportSpawnNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeSpawnNBT(nbt);
        return nbt;
    }

    /**
     * Import spawn NBT for non-world preview rendering sync.
     */
    public final void importSpawnNBT(NBTTagCompound nbt) {
        if (nbt != null) {
            readSpawnNBT(nbt);
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = new NBTTagCompound();
            this.writeSpawnNBT(compound);
            ByteBufUtils.writeNBT(buffer, compound);
        } catch (Exception e) {
            noppes.npcs.LogWriter.error("Error writing energy ability spawn data", e);
        }
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = ByteBufUtils.readNBT(buffer);
            if (compound != null) {
                this.readSpawnNBT(compound);
            }
        } catch (Exception e) {
            noppes.npcs.LogWriter.error("Error reading energy ability spawn data", e);
        }
    }
}
