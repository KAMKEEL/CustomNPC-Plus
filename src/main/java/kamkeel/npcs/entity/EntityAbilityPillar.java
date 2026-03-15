package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.energy.*;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

/**
 * Pillar zone entity — a column of energy that rises from the ground or falls from the sky.
 *
 * Behavior modes:
 * - ANCHORED: spawns at a fixed position and stays there.
 * - MOVING:   spawns in front of the caster and travels forward,
 *             optionally homing toward a target. Without homing, travels in a straight
 *             line along the initial direction set by setInitialMotion().
 *
 * Lifecycle:
 * 1. Charging   — charge visual grows in radius at ground level during windup.
 * 2. spawnDelay — entity is alive but does not grow and does not deal damage.
 * 3. Active     — pillarHeight grows toward targetHeight; damage checks begin.
 *
 * FROM_ABOVE: posY is always the top anchor. The pillar grows downward.
 *             Hitbox spans from posY - height to posY.
 * FROM_GROUND: posY is the ground anchor. The pillar grows upward.
 *              Hitbox spans from posY to posY + height.
 */
public class EntityAbilityPillar extends EntityEnergyZone {

    // ==================== CONSTANTS ====================

    private static final float MIN_HEIGHT = 0.05f;

    private static final int DW_PILLAR_RADIUS = 21;
    private static final int DW_PILLAR_HEIGHT = 22;

    // ==================== ENUMS ====================

    public enum PillarMode {
        ANCHORED,
        MOVING
    }

    public enum PillarOrigin {
        FROM_GROUND,
        FROM_ABOVE
    }

    public enum PillarShape {
        CIRCLE,
        SQUARE;

        public TelegraphType getTelegraphType() {
            if (this == CIRCLE) return TelegraphType.CIRCLE;
            else return TelegraphType.SQUARE;
        }
    }

    public enum OffsetAxis {
        X,
        Z
    }

    // ==================== PILLAR CONFIG ====================

    private EnergyPillarData pillarData = new EnergyPillarData();

    // ==================== PILLAR DIMENSIONS ====================

    private float pillarRadius = 0.01f;
    private float pillarHeight = MIN_HEIGHT;

    private float renderPillarRadius = 0.01f;
    private float renderPillarHeight = MIN_HEIGHT;

    private float prevRenderPillarRadius = 0.01f;
    private float prevRenderPillarHeight = MIN_HEIGHT;

    // ==================== HOMING ====================

    protected EnergyHomingData homingData = new EnergyHomingData();
    private int targetEntityId = -1;

    // ==================== RUNTIME STATE ====================

    private boolean growing = false;
    private int spawnDelayTick = 0;

    // ==================== CONSTRUCTORS ====================

    public EntityAbilityPillar(World world) {
        super(world);
    }

    public EntityAbilityPillar(World world, EntityLivingBase owner,
                               double x, double y, double z,
                               EnergyPillarData pillarData, EnergyDisplayData display,
                               EnergyCombatData combat, EnergyHomingData homing,
                               EnergyLightningData lightning, EnergyLifespanData lifespan) {
        super(world);

        this.ownerEntityId = owner != null ? owner.getEntityId() : -1;
        this.pillarData = pillarData != null ? pillarData.copy() : new EnergyPillarData();
        this.displayData = display != null ? display.copy() : new EnergyDisplayData();
        this.combatData = combat != null ? combat.copy() : new EnergyCombatData();
        this.homingData = homing != null ? homing.copy() : new EnergyHomingData();
        this.lightningData = lightning != null ? lightning.copy() : new EnergyLightningData();
        this.lifespanData = lifespan != null ? lifespan.copy() : new EnergyLifespanData();

        // FROM_ABOVE: posY is the top anchor — position at target height, not ground
        if (pillarData != null && pillarData.origin == PillarOrigin.FROM_ABOVE) {
            this.setPosition(x, y + pillarData.targetHeight, z);
        } else {
            snapToGround(x, y, z);
        }

        setPillarRadiusInternal(0.01f);
        setPillarHeightInternal(MIN_HEIGHT);
        syncHitbox();
    }

    // ==================== ENTITY INIT ====================

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(DW_PILLAR_RADIUS, 0.01f);
        this.dataWatcher.addObject(DW_PILLAR_HEIGHT, MIN_HEIGHT);
    }

    // ==================== INTERNAL DIMENSION SETTERS ====================

    private void setPillarRadiusInternal(float value) {
        this.pillarRadius = value;
        if (!previewMode && worldObj != null && !worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_PILLAR_RADIUS, value);
        }
    }

    private void setPillarHeightInternal(float value) {
        this.pillarHeight = value;
        if (!previewMode && worldObj != null && !worldObj.isRemote) {
            this.dataWatcher.updateObject(DW_PILLAR_HEIGHT, value);
        }
    }

    // ==================== SETUP ====================

    public void setTarget(EntityLivingBase target) {
        this.targetEntityId = target != null ? target.getEntityId() : -1;
    }

    /**
     * Set straight-line motion for MOVING mode without homing.
     */
    public void setInitialMotion(double mx, double mz) {
        this.motionX = mx;
        this.motionZ = mz;
    }

    public void setupPreview(EntityLivingBase owner, EnergyPillarData pillarData,
                             EnergyDisplayData display, EnergyLightningData lightning,
                             int chargeDuration) {
        setupPreview(owner);
        this.pillarData = pillarData != null ? pillarData.copy() : new EnergyPillarData();
        this.displayData = display != null ? display.copy() : new EnergyDisplayData();
        this.lightningData = lightning != null ? lightning.copy() : new EnergyLightningData();
        setupCharging(chargeDuration);
        setPillarRadiusInternal(0.01f);
        setPillarHeightInternal(MIN_HEIGHT);
        this.renderPillarRadius = 0.01f;
        this.prevRenderPillarRadius = 0.01f;
        this.renderPillarHeight = MIN_HEIGHT;
        this.prevRenderPillarHeight = MIN_HEIGHT;
    }

    /**
     * Called by the ability when charging ends.
     * Starts the spawnDelay countdown. No damage is dealt during spawnDelay.
     */
    public void startGrowing() {
        setCharging(false);
        this.spawnDelayTick = 0;
        this.growing = pillarData.spawnDelay <= 0;
        setPillarRadiusInternal(pillarData.targetRadius);
        setPillarHeightInternal(MIN_HEIGHT);
        syncHitbox();
    }

    // ==================== CHARGING ====================

    @Override
    protected void updateCharging() {
        chargeTick++;
        float progress = getChargeProgress();
        setPillarRadiusInternal(pillarData.targetRadius * progress);
        setPillarHeightInternal(MIN_HEIGHT);
        syncHitbox();
    }

    // ==================== ZONE UPDATE ====================

    @Override
    protected void updateZone() {
        prevRenderPillarRadius = renderPillarRadius;
        prevRenderPillarHeight = renderPillarHeight;

        if (worldObj.isRemote) {
            float dwRadius = this.dataWatcher.getWatchableObjectFloat(DW_PILLAR_RADIUS);
            float dwHeight = this.dataWatcher.getWatchableObjectFloat(DW_PILLAR_HEIGHT);
            this.pillarRadius = dwRadius;
            this.pillarHeight = dwHeight;
            renderPillarRadius += (pillarRadius - renderPillarRadius) * 0.15f;
            renderPillarHeight += (pillarHeight - renderPillarHeight) * 0.15f;
            return;
        }

        // spawnDelay: entity is alive but not growing and not dealing damage
        if (!growing) {
            spawnDelayTick++;
            if (spawnDelayTick >= pillarData.spawnDelay) {
                growing = true;
            }
            return;
        }

        if (pillarRadius < pillarData.targetRadius) {
            setPillarRadiusInternal(Math.min(pillarData.targetRadius, pillarRadius + pillarData.radiusGrowSpeed));
        }
        if (pillarHeight < pillarData.targetHeight) {
            setPillarHeightInternal(Math.min(pillarData.targetHeight, pillarHeight + pillarData.heightGrowSpeed));
        }

        syncHitbox();

        if (pillarData.mode == PillarMode.MOVING) {
            updateMovement();
        }

        // Only deal damage once growing has started and height is meaningful
        if (pillarHeight > MIN_HEIGHT) {
            checkEntityCollision();
        }
    }

    // ==================== MOVEMENT (MOVING mode) ====================

    private void updateMovement() {
        if (!homingData.isHoming()) {
            this.posX += motionX;
            this.posZ += motionZ;
            repositionY();
            return;
        }

        Entity target = getTargetEntity();
        if (target == null || !target.isEntityAlive()) {
            this.posX += motionX;
            this.posZ += motionZ;
            repositionY();
            return;
        }

        double dx = target.posX - posX;
        double dz = target.posZ - posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 0 && dist <= homingData.getHomingRange()) {
            float strength = homingData.getHomingStrength();
            double desiredVX = (dx / dist) * homingData.getSpeed();
            double desiredVZ = (dz / dist) * homingData.getSpeed();
            motionX += (desiredVX - motionX) * strength;
            motionZ += (desiredVZ - motionZ) * strength;

            double vLen = Math.sqrt(motionX * motionX + motionZ * motionZ);
            if (vLen > 0) {
                motionX = (motionX / vLen) * homingData.getSpeed();
                motionZ = (motionZ / vLen) * homingData.getSpeed();
            }
        }

        this.posX += motionX;
        this.posZ += motionZ;
        repositionY();
    }

    /**
     * Re-anchor Y position after horizontal movement.
     * FROM_GROUND: snap to ground.
     * FROM_ABOVE: snap to ground + targetHeight (top anchor stays at ceiling height).
     */
    private void repositionY() {
        if (pillarData.origin == PillarOrigin.FROM_ABOVE) {
            double groundY = kamkeel.npcs.controllers.data.ability.Ability.findGroundLevel(worldObj, posX, posY, posZ);
            this.setPosition(posX, groundY + pillarData.targetHeight, posZ);
        } else {
            snapToGround(posX, posY, posZ);
        }
    }

    // ==================== COLLISION ====================

    private void checkEntityCollision() {
        AxisAlignedBB broadBox = buildHitBox();
        if (broadBox == null) return;

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, broadBox);

        for (EntityLivingBase entity : entities) {
            if (shouldIgnoreEntity(entity)) continue;
            if (!canHitEntityNow(entity)) continue;
            if (!isInPillar(entity)) continue;

            boolean hit = applyDamage(entity, getDamage());
            if (hit) {
                applyEffects(entity);
                recordEntityHit(entity);
                if (shouldTerminateAfterHit()) {
                    this.setDead();
                    return;
                }
            }
        }
    }

    /**
     * Build the hit box based on origin.
     * FROM_GROUND: posY (base) to posY + height (tip).
     * FROM_ABOVE:  posY - height (tip) to posY (base/ceiling).
     */
    private AxisAlignedBB buildHitBox() {
        float minY, maxY;
        if (pillarData.origin == PillarOrigin.FROM_ABOVE) {
            maxY = (float) posY;
            minY = (float) posY - pillarHeight;
        } else {
            minY = (float) posY;
            maxY = (float) posY + pillarHeight;
        }
        return AxisAlignedBB.getBoundingBox(
            posX - pillarRadius, minY, posZ - pillarRadius,
            posX + pillarRadius, maxY, posZ + pillarRadius
        );
    }

    private boolean isInPillar(EntityLivingBase entity) {
        double dx = entity.posX - posX;
        double dz = entity.posZ - posZ;
        switch (pillarData.shape) {
            case SQUARE:
                return Math.abs(dx) <= pillarRadius && Math.abs(dz) <= pillarRadius;
            case CIRCLE:
            default:
                return Math.sqrt(dx * dx + dz * dz) <= pillarRadius;
        }
    }

    private void syncHitbox() {
        float w = Math.max(0.1f, pillarRadius * 2.0f);
        float h = Math.max(0.1f, pillarHeight);
        this.setSize(w, h);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double extent = Math.max(pillarRadius, pillarHeight) * 2.0D;
        double range = Math.max(128.0D, extent * 4.0D + 64.0D);
        return distance < range * range;
    }

    // ==================== ENTITY HELPERS ====================

    public Entity getTargetEntity() {
        if (targetEntityId == -1) return null;
        return worldObj.getEntityByID(targetEntityId);
    }

    // ==================== RENDERER GETTERS ====================

    public float getPillarRadius() { return pillarRadius; }
    public float getPillarHeight() { return pillarHeight; }
    public PillarMode getPillarMode() { return pillarData.mode; }
    public PillarOrigin getPillarOrigin() { return pillarData.origin; }
    public PillarShape getPillarShape() { return pillarData.shape; }
    public boolean isGrowing() { return growing; }
    public EnergyPillarData getPillarData() { return pillarData; }

    public float getInterpolatedPillarRadius(float partialTicks) {
        return prevRenderPillarRadius + (renderPillarRadius - prevRenderPillarRadius) * partialTicks;
    }

    public float getInterpolatedPillarHeight(float partialTicks) {
        return prevRenderPillarHeight + (renderPillarHeight - prevRenderPillarHeight) * partialTicks;
    }

    // ==================== SETTERS ====================

    public void setPillarData(EnergyPillarData data) { this.pillarData = data != null ? data.copy() : new EnergyPillarData(); }
    public void setHomingData(EnergyHomingData homing) { this.homingData = homing != null ? homing.copy() : new EnergyHomingData(); }

    // ==================== NBT ====================

    @Override
    protected void writeZoneNBT(NBTTagCompound nbt) {
        pillarData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        nbt.setFloat("PillarRadius", pillarRadius);
        nbt.setFloat("PillarHeight", pillarHeight);
        nbt.setInteger("SpawnDelayTick", spawnDelayTick);
        nbt.setBoolean("Growing", growing);
        nbt.setInteger("TargetEntityId", targetEntityId);
        nbt.setDouble("MotionX", motionX);
        nbt.setDouble("MotionZ", motionZ);
    }

    @Override
    protected void readZoneNBT(NBTTagCompound nbt) {
        pillarData.readNBT(nbt);
        homingData.readNBT(nbt);

        float radius = Math.max(0.01f, nbt.getFloat("PillarRadius"));
        float height = Math.max(MIN_HEIGHT, nbt.getFloat("PillarHeight"));

        this.pillarRadius = radius;
        this.pillarHeight = height;

        if (this.dataWatcher != null) {
            this.dataWatcher.updateObject(DW_PILLAR_RADIUS, radius);
            this.dataWatcher.updateObject(DW_PILLAR_HEIGHT, height);
        }

        this.renderPillarRadius = radius;
        this.prevRenderPillarRadius = radius;
        this.renderPillarHeight = height;
        this.prevRenderPillarHeight = height;

        this.spawnDelayTick = nbt.getInteger("SpawnDelayTick");
        this.growing = nbt.getBoolean("Growing");
        this.targetEntityId = nbt.hasKey("TargetEntityId") ? nbt.getInteger("TargetEntityId") : -1;
        this.motionX = nbt.getDouble("MotionX");
        this.motionZ = nbt.getDouble("MotionZ");

        syncHitbox();
    }
}
