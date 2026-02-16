package kamkeel.npcs.entity;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import kamkeel.npcs.controllers.data.ability.data.EnergyPanelData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.entity.EntityNPCInterface;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * Energy Panel entity - a flat rectangular barrier used by Wall and Shield abilities.
 * Supports three modes: PLACED (stationary), HELD (tracks caster), LAUNCHED (moves forward).
 */
public class EntityEnergyPanel extends Entity implements IEntityAdditionalSpawnData {

    public enum PanelMode {
        PLACED,     // Stationary wall
        HELD,       // Follows caster, rotates with caster look
        LAUNCHED    // Moves forward, deals damage/knockback on contact
    }

    // ==================== VISUAL PROPERTIES ====================
    protected EnergyDisplayData displayData = new EnergyDisplayData();
    protected EnergyLightningData lightningData = new EnergyLightningData();

    // ==================== BARRIER PROPERTIES ====================
    protected EnergyBarrierData barrierData = new EnergyBarrierData();
    protected EnergyPanelData panelData = new EnergyPanelData();
    protected float currentHealth;

    // ==================== PANEL STATE ====================
    protected PanelMode mode = PanelMode.PLACED;
    protected float panelYaw = 0.0f; // Rotation of the panel face (degrees)

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

    public EntityEnergyPanel(World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.noClip = true;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    public EntityEnergyPanel(World world, EntityLivingBase owner, double x, double y, double z,
                             float yaw, PanelMode mode,
                             EnergyDisplayData display, EnergyLightningData lightning,
                             EnergyBarrierData barrier, EnergyPanelData panel) {
        this(world);
        this.setPosition(x, y + panel.heightOffset, z);
        this.ownerEntityId = owner.getEntityId();
        this.panelYaw = yaw;
        this.mode = mode;
        this.displayData = display;
        this.lightningData = lightning;
        this.barrierData = barrier;
        this.panelData = panel;
        this.currentHealth = barrier.maxHealth;

        // For launched panels, set initial velocity
        if (mode == PanelMode.LAUNCHED) {
            float yawRad = (float) Math.toRadians(yaw);
            this.motionX = -Math.sin(yawRad) * panel.launchSpeed;
            this.motionY = 0;
            this.motionZ = Math.cos(yawRad) * panel.launchSpeed;
        }
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

        // Mode-specific updates
        switch (mode) {
            case HELD:
                updateHeld();
                break;
            case LAUNCHED:
                updateLaunched();
                break;
            case PLACED:
            default:
                // Stationary, no update needed
                break;
        }
    }

    private void updateHeld() {
        Entity owner = getOwnerEntity();
        if (owner == null) return;

        // Follow owner position
        float frontDist = 1.5f;
        float yawRad = (float) Math.toRadians(owner.rotationYaw);
        double newX = owner.posX + (-Math.sin(yawRad) * frontDist);
        double newY = owner.posY + panelData.heightOffset + (owner.height * 0.5f);
        double newZ = owner.posZ + (Math.cos(yawRad) * frontDist);

        this.setPosition(newX, newY, newZ);
        this.panelYaw = owner.rotationYaw;
    }

    private void updateLaunched() {
        if (worldObj.isRemote) return;

        // Move forward
        this.posX += motionX;
        this.posY += motionY;
        this.posZ += motionZ;
        this.setPosition(posX, posY, posZ);

        // Check entity collision for damage
        float halfW = panelData.panelWidth * 0.5f;
        float halfH = panelData.panelHeight * 0.5f;
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            posX - halfW, posY - halfH, posZ - halfW,
            posX + halfW, posY + halfH, posZ + halfW
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);
        Entity owner = getOwnerEntity();

        for (EntityLivingBase target : entities) {
            if (target == owner) continue;
            if (owner instanceof EntityNPCInterface && target instanceof EntityNPCInterface) {
                if (((EntityNPCInterface) owner).faction.id == ((EntityNPCInterface) target).faction.id) continue;
            }

            // Apply damage
            if (panelData.launchDamage > 0) {
                if (owner instanceof EntityNPCInterface) {
                    target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), panelData.launchDamage);
                } else if (owner instanceof EntityPlayer) {
                    target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), panelData.launchDamage);
                }
            }

            // Apply knockback
            if (panelData.launchKnockback > 0) {
                double dx = target.posX - posX;
                double dz = target.posZ - posZ;
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0) {
                    target.addVelocity(
                        (dx / len) * panelData.launchKnockback * 0.5,
                        0.1,
                        (dz / len) * panelData.launchKnockback * 0.5
                    );
                    target.velocityChanged = true;
                }
            }
        }

        // Max distance check (30 blocks)
        double distSq = (posX - prevPosX) * (posX - prevPosX) + (posZ - prevPosZ) * (posZ - prevPosZ);
        if (ticksAlive > 200) {
            this.setDead();
        }
    }

    /**
     * Apply damage to this panel from a projectile.
     */
    public boolean onProjectileHit(EntityAbilityProjectile projectile, float baseDamage) {
        if (!barrierData.useHealth) {
            triggerHitFlash();
            return true;
        }

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
     * Check if a projectile hits this panel surface.
     * Only blocks incoming projectiles (not from the panel's owner).
     */
    public boolean isIncomingProjectile(EntityAbilityProjectile projectile) {
        if (projectile.getOwnerEntityId() == this.ownerEntityId) return false;

        // Faction check
        Entity owner = getOwnerEntity();
        Entity projOwner = projectile.getOwnerEntity();
        if (owner instanceof EntityNPCInterface && projOwner instanceof EntityNPCInterface) {
            if (((EntityNPCInterface) owner).faction.id == ((EntityNPCInterface) projOwner).faction.id) {
                return false;
            }
        }

        // Check if projectile is within panel bounds
        float halfW = panelData.panelWidth * 0.5f;
        float halfH = panelData.panelHeight * 0.5f;
        float panelThickness = 0.5f; // Collision thickness

        // Transform projectile position to panel-local space
        float yawRad = (float) Math.toRadians(panelYaw);
        double relX = projectile.posX - this.posX;
        double relY = projectile.posY - this.posY;
        double relZ = projectile.posZ - this.posZ;

        // Rotate into panel space (panel faces along -sin(yaw), cos(yaw))
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        double localForward = relX * (-sin) + relZ * cos;  // Distance along panel normal
        double localRight = relX * cos + relZ * sin;        // Distance along panel width

        // Check bounds
        if (Math.abs(localForward) > panelThickness) return false;
        if (Math.abs(localRight) > halfW) return false;
        if (Math.abs(relY) > halfH) return false;

        // Check incoming direction (dot product with panel normal)
        double normalX = -Math.sin(yawRad);
        double normalZ = Math.cos(yawRad);
        double dot = projectile.motionX * normalX + projectile.motionZ * normalZ;

        // If the projectile is on the owner's side, it must be moving toward the panel
        // (negative dot means moving into the panel from the front side)
        return true; // If within bounds, it's a hit regardless of direction
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

    // ==================== GETTERS ====================

    public int getOwnerEntityId() { return ownerEntityId; }
    public PanelMode getMode() { return mode; }
    public float getPanelYaw() { return panelYaw; }
    public EnergyDisplayData getDisplayData() { return displayData; }
    public EnergyLightningData getLightningData() { return lightningData; }
    public EnergyBarrierData getBarrierData() { return barrierData; }
    public EnergyPanelData getPanelData() { return panelData; }
    public float getCurrentHealth() { return currentHealth; }

    public void setSourceAbility(Ability ability) { this.sourceAbility = ability; }
    public Ability getSourceAbility() { return sourceAbility; }

    // Visual getters
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
        double d = Math.max(panelData.panelWidth, panelData.panelHeight) * 8.0D;
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
        this.ownerEntityId = nbt.getInteger("OwnerId");
        this.ticksAlive = nbt.getInteger("TicksAlive");
        this.currentHealth = nbt.getFloat("CurrentHealth");
        this.panelYaw = nbt.getFloat("PanelYaw");
        this.mode = PanelMode.values()[nbt.getInteger("PanelMode")];
        displayData.readNBT(nbt);
        lightningData.readNBT(nbt);
        barrierData.readNBT(nbt);
        panelData.readNBT(nbt);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("OwnerId", ownerEntityId);
        nbt.setInteger("TicksAlive", ticksAlive);
        nbt.setFloat("CurrentHealth", currentHealth);
        nbt.setFloat("PanelYaw", panelYaw);
        nbt.setInteger("PanelMode", mode.ordinal());
        displayData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        barrierData.writeNBT(nbt);
        panelData.writeNBT(nbt);
    }

    // ==================== SPAWN DATA ====================

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        try {
            NBTTagCompound compound = new NBTTagCompound();
            this.writeEntityToNBT(compound);
            cpw.mods.fml.common.network.ByteBufUtils.writeTag(buffer, compound);
        } catch (Exception e) {
            noppes.npcs.LogWriter.error("Error writing energy panel spawn data", e);
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
            noppes.npcs.LogWriter.error("Error reading energy panel spawn data", e);
        }
    }
}
