package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.energy.*;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityPillar;
import kamkeel.npcs.entity.EntityAbilityPillar.OffsetAxis;
import kamkeel.npcs.entity.EntityAbilityPillar.PillarMode;
import kamkeel.npcs.entity.EntityAbilityPillar.PillarShape;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

import static kamkeel.npcs.entity.EntityAbilityPillar.MIN_HEIGHT;
import static kamkeel.npcs.entity.EntityAbilityPillar.MIN_RADIUS;
import static kamkeel.npcs.entity.EntityAbilityPillar.MAX_ZONE_HEIGHT;
import static kamkeel.npcs.entity.EntityAbilityPillar.MAX_ZONE_RADIUS;

/**
 * Pillar zone ability.
 *
 * ANCHORED: spawns at target position at execute time.
 *   - trackTarget: predicts where target will be based on current velocity, spawns there.
 *   - followTarget (requires homing): pillar chases target after spawn.
 *   When count > 1: each pillar after index 0 gets a random nearby position.
 *
 * MOVING: spawns in front of caster and travels forward.
 *   - Without homing: straight line.
 *   - With homing: steers toward target.
 *   When count > 1: pillarOffset spaces pillars along offsetAxis relative to look vector.
 *     OffsetAxis.Z = along look direction (one behind the other).
 *     OffsetAxis.X = perpendicular to look direction (side by side).
 *
 *     TODO 1. fix charging visual for ABOVE origin; 2. fix hitbox for ABOVE origin; 3. fix trackTarget to actually track the player
 */
public class AbilityPillar extends AbilityEnergyZone<EntityAbilityPillar> {

    // ==================== CONFIG ====================

    private EnergyPillarData pillarData = new EnergyPillarData();
    private EnergyHomingData homingData = new EnergyHomingData();

    private float pillarOffset = 0f;
    private OffsetAxis offsetAxis = OffsetAxis.Z;

    /** ANCHORED only: follow target after spawn (requires homing). */
    private boolean followTarget = false;

    /** ANCHORED only: predict target position at spawn time from target velocity. */
    private boolean trackTarget = false;

    // ==================== CONSTRUCTOR ====================

    public AbilityPillar() {
        super(new EnergyDisplayData(), new EnergyCombatData(), new EnergyLifespanData());
        this.typeId = "ability.cnpc.pillar";
        this.name = "Pillar";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 25.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 30;
        this.lockMovement = LockMode.WINDUP;
        this.telegraphType = pillarData.shape.getTelegraphType();
        this.showTelegraph = true;
    }

    // ==================== ABSTRACT IMPLEMENTATIONS ====================

    @Override
    protected EntityAbilityPillar createEntity(EntityLivingBase caster, EntityLivingBase target,
                                               double x, double y, double z,
                                               EnergyDisplayData resolved, int index) {
        return new EntityAbilityPillar(caster.worldObj, caster, x, y, z,
            pillarData, resolved, combatData, homingData, lightningData, lifespanData);
    }

    @Override
    protected void setupEntityCharging(EntityAbilityPillar entity, int index) {
        entity.setupCharging(windUpTicks);
    }

    @Override
    protected void setupEntityPreview(EntityAbilityPillar entity, EntityLivingBase caster,
                                      EnergyDisplayData resolved, int index) {
        entity.setupPreview(caster, pillarData, resolved, lightningData, windUpTicks);
    }

    @Override
    protected EntityAbilityPillar[] createEntityArray(int size) {
        return new EntityAbilityPillar[size];
    }

    @Override
    protected float getZoneTelegraphRadius() {
        return pillarData.targetRadius;
    }

    // ==================== SPAWN POSITIONS ====================

    @Override
    protected double[] getSpawnPosition(EntityLivingBase caster, EntityLivingBase target, int index) {
        if (pillarData.mode == PillarMode.MOVING) {
            return getMovingSpawnPosition(caster, index);
        }

        // ANCHORED: spawn at target's current position at execute time
        if (index == 0) {
            if (target == null) return new double[]{caster.posX, caster.posY, caster.posZ};

            double spawnX = target.posX;
            double spawnZ = target.posZ;

            if (trackTarget) {
                double motionX = target.posX - target.prevPosX;
                double motionZ = target.posZ - target.prevPosZ;

                // Predict where target will be based on current motion and spawnDelay
                double prediction = pillarData.spawnDelay * homingData.getHomingStrength();

                spawnX += motionX * prediction;
                spawnZ += motionZ * prediction;
            }

            return new double[]{spawnX, target.posY, spawnZ};
        }

        // index > 0: random nearby position
        double baseX = target != null ? target.posX : caster.posX;
        double baseZ = target != null ? target.posZ : caster.posZ;
        double baseY = target != null ? target.posY : caster.posY;
        float spread = Math.max(pillarData.targetRadius * 2.0f, 3.0f);
        double angle = Math.random() * Math.PI * 2;
        double dist = Math.sqrt(Math.random()) * spread;
        return new double[]{baseX + Math.cos(angle) * dist, baseY, baseZ + Math.sin(angle) * dist};
    }

    @Override
    protected double[] getWindupSpawnPosition(EntityLivingBase caster, EntityLivingBase target, int index) {
        if (pillarData.mode == PillarMode.MOVING) {
            return getMovingSpawnPosition(caster, index);
        }
        if (index == 0 && !preCalculatedPositions.isEmpty()) {
            return preCalculatedPositions.get(0);
        }
        if (target != null) return new double[]{target.posX, target.posY, target.posZ};
        return new double[]{caster.posX, caster.posY, caster.posZ};
    }

    /**
     * Spawn position for MOVING mode.
     * OffsetAxis.Z = offset along look direction (one behind the other).
     * OffsetAxis.X = offset perpendicular to look direction (side by side).
     */
    private double[] getMovingSpawnPosition(EntityLivingBase caster, int index) {
        Vec3 look = caster.getLookVec();
        if (look == null) return new double[]{caster.posX, caster.posY, caster.posZ};

        float baseDist = Math.max(1.0f, pillarData.targetRadius);

        // Forward direction (normalized XZ)
        double fwdX = look.xCoord;
        double fwdZ = look.zCoord;
        double fwdLen = Math.sqrt(fwdX * fwdX + fwdZ * fwdZ);
        if (fwdLen > 0) { fwdX /= fwdLen; fwdZ /= fwdLen; }

        // Right direction = perpendicular to forward in XZ plane
        double rightX = fwdZ;
        double rightZ = -fwdX;

        double ox, oz;
        if (offsetAxis == OffsetAxis.Z) {
            // Along look direction
            ox = fwdX * pillarOffset * index;
            oz = fwdZ * pillarOffset * index;
        } else {
            // Perpendicular to look direction
            // Center the group: offset by -(count-1)/2 * spacing + index * spacing
            double centerOffset = -(zoneCount - 1) * 0.5 * pillarOffset + index * pillarOffset;
            ox = rightX * centerOffset;
            oz = rightZ * centerOffset;
        }

        return new double[]{
            caster.posX + fwdX * baseDist + ox,
            caster.posY,
            caster.posZ + fwdZ * baseDist + oz
        };
    }

    // ==================== EXECUTE ====================

    @Override
    protected void spawnZoneAt(EntityLivingBase caster, EntityLivingBase target, int index) {
        super.spawnZoneAt(caster, target, index);
        EntityAbilityPillar entity = entities[index];
        if (entity == null || entity.isDead) return;

        if (target != null) {
            entity.setTarget(target);
        }

        if (pillarData.mode == PillarMode.ANCHORED) {
            entity.setFollowTarget(followTarget && homingData.isHoming());
        }

        if (pillarData.mode == PillarMode.MOVING && !homingData.isHoming()) {
            Vec3 look = caster.getLookVec();
            double mx = 0, mz = 0;
            if (target != null) {
                double dx = target.posX - entity.posX;
                double dz = target.posZ - entity.posZ;
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0) { mx = (dx / len) * homingData.getSpeed(); mz = (dz / len) * homingData.getSpeed(); }
            } else if (look != null) {
                mx = look.xCoord * homingData.getSpeed();
                mz = look.zCoord * homingData.getSpeed();
            }
            entity.setInitialMotion(mx, mz);
        }

        entity.startGrowing();
    }

    // ==================== TELEGRAPH ====================

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || isPlayerCaster(caster)) return null;

        Telegraph telegraph = pillarData.shape == PillarShape.SQUARE
            ? Telegraph.square(pillarData.targetRadius)
            : Telegraph.circle(pillarData.targetRadius);

        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        double telegraphX, telegraphZ, telegraphY;

        if (pillarData.mode == PillarMode.MOVING) {
            double[] pos = getMovingSpawnPosition(caster, 0);
            telegraphX = pos[0];
            telegraphZ = pos[2];
            telegraphY = findGroundLevel(caster.worldObj, telegraphX, caster.posY, telegraphZ);
        } else {
            if (target == null) return null;
            // Regular: start at target's current position, telegraph follows target
            telegraphX = target.posX;
            telegraphZ = target.posZ;
            telegraphY = findGroundLevel(caster.worldObj, telegraphX, target.posY, telegraphZ);
        }

        preCalculatedPositions.clear();
        preCalculatedPositions.add(new double[]{telegraphX, telegraphY, telegraphZ});

        TelegraphInstance instance = new TelegraphInstance(
            telegraph, telegraphX, telegraphY, telegraphZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());

        // ANCHORED: telegraph follows target in real time (regular and trackTarget)
        // MOVING: telegraph is static at spawn point
        if (pillarData.mode == PillarMode.ANCHORED && target != null) {
            instance.setEntityIdToFollow(target.getEntityId());
        } else {
            instance.setEntityIdToFollow(-1);
        }
        return instance;
    }

    @Override
    public float getTelegraphRadius() {
        return pillarData.targetRadius;
    }

    // ==================== NBT ====================

    @Override
    protected void writeTypeSpecificNBT(NBTTagCompound nbt) {
        pillarData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        nbt.setFloat("pillarOffset", pillarOffset);
        nbt.setInteger("offsetAxis", offsetAxis.ordinal());
        nbt.setBoolean("followTarget", followTarget);
        nbt.setBoolean("trackTarget", trackTarget);
    }

    @Override
    protected void readTypeSpecificNBT(NBTTagCompound nbt) {
        pillarData.readNBT(nbt);
        homingData.readNBT(nbt);
        this.pillarOffset = nbt.hasKey("pillarOffset") ? nbt.getFloat("pillarOffset") : 0f;
        int axisOrd = nbt.hasKey("offsetAxis") ? nbt.getInteger("offsetAxis") : 1;
        this.offsetAxis = (axisOrd >= 0 && axisOrd < OffsetAxis.values().length)
            ? OffsetAxis.values()[axisOrd] : OffsetAxis.Z;
        this.followTarget = nbt.hasKey("followTarget") && nbt.getBoolean("followTarget");
        this.trackTarget = nbt.hasKey("trackTarget") && nbt.getBoolean("trackTarget");
    }

    // ==================== GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    protected void addTypeDefinitions(List<FieldDef> defs) {
        FieldDef.insertAfter(defs, "ability.fireDelay", FieldDef.row(
            FieldDef.floatField("ability.pillar.offset", this::getPillarOffset, this::setPillarOffset).range(0f, 20f),
            FieldDef.enumField("ability.pillar.offsetAxis", OffsetAxis.class, this::getOffsetAxis, this::setOffsetAxis)
        ).visibleWhen(() -> zoneCount > 1 && pillarData.mode == PillarMode.MOVING));

        defs.add(FieldDef.section("ability.section.pillar"));

        defs.add(FieldDef.enumField("ability.pillarMode", PillarMode.class,
            this::getMode, this::setMode));

        defs.add(FieldDef.enumField("ability.pillarOrigin", EntityAbilityPillar.PillarOrigin.class,
            this::getOrigin, this::setOrigin));

        defs.add(FieldDef.enumField("ability.pillarShape", PillarShape.class,
            this::getShape, this::setShape));

        defs.add(FieldDef.intField("ability.pillar.spawnDelay",
            this::getSpawnDelay, this::setSpawnDelay).range(0, getMaxLifetime()));

        defs.add(FieldDef.section("ability.section.size"));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.pillar.targetRadius", this::getTargetRadius, this::setTargetRadius).range(0.1f, 64f),
            FieldDef.floatField("ability.pillar.targetHeight", this::getTargetHeight, this::setTargetHeight).range(0.1f, 64f)
        ));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.pillar.radiusGrowSpeed", this::getRadiusGrowSpeed, this::setRadiusGrowSpeed),
            FieldDef.floatField("ability.pillar.heightGrowSpeed", this::getHeightGrowSpeed, this::setRadiusGrowSpeed)
        ));

        defs.add(FieldDef.section("ability.section.movement"));

        defs.add(FieldDef.floatField("ability.speed", this::getPillarSpeed, this::setPillarSpeed)
            .range(0.01f, 10f));

        defs.add(FieldDef.boolField("ability.homing", this::isHoming, this::setHoming));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.homingStrength", this::getHomingStrength, this::setHomingStrength),
            FieldDef.floatField("ability.homingRange", this::getHomingRange, this::setHomingRange)
        ).visibleWhen(this::isHoming));

        // ANCHORED-only options
        defs.add(FieldDef.boolField("ability.pillar.trackTarget", this::isTrackTarget, this::setTrackTarget)
            .visibleWhen(() -> pillarData.mode == PillarMode.ANCHORED));

        defs.add(FieldDef.boolField("ability.pillar.followTarget", this::isFollowTarget, this::setFollowTarget)
            .visibleWhen(() -> pillarData.mode == PillarMode.ANCHORED && homingData.isHoming()));

        defs.add(FieldDef.section("ability.section.combat"));

        defs.add(FieldDef.floatField("ability.damage", this::getDamage, this::setDamage).range(0f, 1000f));
        defs.add(FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback).range(0f, 10f));
        defs.add(FieldDef.intField("ability.maxLifetime", this::getMaxLifetime, this::setMaxLifetime).range(1, 1200));

        defs.add(AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects));
    }

    // ==================== GETTERS & SETTERS ====================

    public EnergyPillarData getPillarData() { return pillarData; }
    public void setPillarData(EnergyPillarData data) { this.pillarData = data != null ? data : new EnergyPillarData(); }

    public float getPillarOffset() { return pillarOffset; }
    public void setPillarOffset(float offset) { this.pillarOffset = Math.max(0f, Math.min(20f, offset)); }

    public OffsetAxis getOffsetAxis() { return offsetAxis; }
    public void setOffsetAxis(OffsetAxis axis) { this.offsetAxis = axis != null ? axis : OffsetAxis.Z; }

    public boolean isFollowTarget() { return followTarget; }
    public void setFollowTarget(boolean v) { this.followTarget = v; }

    public boolean isTrackTarget() { return trackTarget; }
    public void setTrackTarget(boolean v) { this.trackTarget = v; }

    public float getTargetRadius() {
        return pillarData.targetRadius;
    }

    public void setTargetRadius(float targetRadius) {
        pillarData.targetRadius = Math.max(MIN_RADIUS, Math.min(MAX_ZONE_RADIUS, targetRadius));
    }

    public float getTargetHeight() {
        return pillarData.targetHeight;
    }

    public void setTargetHeight(float targetHeight) {
        pillarData.targetHeight = Math.max(MIN_HEIGHT, Math.min(MAX_ZONE_HEIGHT, targetHeight));
    }

    public float getRadiusGrowSpeed() {
        return pillarData.radiusGrowSpeed;
    }

    public void setRadiusGrowSpeed(float radiusGrowSpeed) {
        pillarData.radiusGrowSpeed = Math.max(0.01f, radiusGrowSpeed);
    }

    public float getHeightGrowSpeed() {
        return pillarData.heightGrowSpeed;
    }

    public void setHeightGrowSpeed(float heightGrowSpeed) {
        pillarData.heightGrowSpeed = Math.max(0.01f, heightGrowSpeed);
    }

    public PillarMode getMode() {
        return pillarData.mode;
    }

    public void setMode(PillarMode mode) {
        pillarData.mode = mode;
    }

    public EntityAbilityPillar.PillarOrigin getOrigin() {
        return pillarData.origin;
    }

    public void setOrigin(EntityAbilityPillar.PillarOrigin origin) {
        pillarData.origin = origin;
    }

    public PillarShape getShape() {
        return pillarData.shape;
    }

    public void setShape(PillarShape shape) {
        pillarData.shape = shape;
    }

    public int getSpawnDelay() {
        return pillarData.spawnDelay;
    }

    public void setSpawnDelay(int spawnDelay) {
        pillarData.spawnDelay = Math.max(0, Math.min(getMaxLifetime(), spawnDelay));
    }

    public float getPillarSpeed() {
        return homingData.speed;
    }

    public void setPillarSpeed(float speed) {
        homingData.speed = speed;
    }

    public boolean isHoming() {
        return homingData.homing;
    }

    public void setHoming(boolean homing) {
        homingData.homing = homing;
    }

    public float getHomingStrength() {
        return homingData.homingStrength;
    }

    public void setHomingStrength(float strength) {
        homingData.homingStrength = strength;
    }

    public float getHomingRange() {
        return homingData.homingRange;
    }

    public void setHomingRange(float range) {
        homingData.homingRange = range;
    }
}
