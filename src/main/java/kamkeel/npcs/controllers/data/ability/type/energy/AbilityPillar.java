package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLightningData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyPillarData;
import kamkeel.npcs.controllers.data.ability.enums.HitType;
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

/**
 * Pillar zone ability — spawns one or more energy pillars that rise from the ground
 * or fall from the sky.
 *
 * ANCHORED: pillar spawns at the target's position at execute time and stays there.
 *           When count > 1, each subsequent pillar gets a random nearby position.
 *
 * MOVING:   pillar spawns in front of the caster at execute time and travels forward.
 *           With homing: steers toward target.
 *           Without homing: straight line along caster look vector or toward target.
 *           When count > 1, pillarOffset spaces pillars along the chosen OffsetAxis.
 *
 * Charge visual appears at the target/spawn position during windup (tick 1),
 * growing in radius at ground level — exactly above the telegraph.
 */

// TODO 1. add another mode, static, 2. fix charging radius, rotation, etc
public class AbilityPillar extends AbilityEnergyZone<EntityAbilityPillar> {

    // ==================== PILLAR CONFIG ====================

    private EnergyPillarData pillarData = new EnergyPillarData();
    private EnergyHomingData homingData = new EnergyHomingData();

    /** Spacing between pillars when count > 1. 0 = same position. */
    private float pillarOffset = 0f;

    /** Axis along which pillarOffset is applied in MOVING mode. */
    private OffsetAxis offsetAxis = OffsetAxis.Z;

    // ==================== CONSTRUCTOR ====================

    public AbilityPillar() {
        super(
            new EnergyDisplayData(),
            new EnergyCombatData(),
            new EnergyLifespanData()
        );
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
        // Charge visual: entity is already at the correct ground position.
        // Just set it up for charging — it will grow in radius at that position.
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

    /**
     * Spawn position at execute time.
     * MOVING: in front of caster with offset per index along offsetAxis.
     * ANCHORED index 0: pre-calculated telegraph position.
     * ANCHORED index > 0: random position near target.
     */
    @Override
    protected double[] getSpawnPosition(EntityLivingBase caster, EntityLivingBase target, int index) {
        if (pillarData.mode == PillarMode.MOVING) {
            Vec3 look = caster.getLookVec();
            if (look == null) return new double[]{caster.posX, caster.posY, caster.posZ};
            float baseDist = Math.max(1.0f, pillarData.targetRadius);
            double ox = offsetAxis == OffsetAxis.X ? pillarOffset * index : 0;
            double oz = offsetAxis == OffsetAxis.Z ? pillarOffset * index : 0;
            return new double[]{
                caster.posX + look.xCoord * baseDist + ox,
                caster.posY,
                caster.posZ + look.zCoord * baseDist + oz
            };
        }

        if (index == 0) {
            return super.getSpawnPosition(caster, target, index);
        }

        double baseX = target != null ? target.posX : caster.posX;
        double baseZ = target != null ? target.posZ : caster.posZ;
        double baseY = target != null ? target.posY : caster.posY;
        float spread = Math.max(pillarData.targetRadius * 2.0f, 3.0f);
        double angle = Math.random() * Math.PI * 2;
        double dist = Math.sqrt(Math.random()) * spread;
        return new double[]{
            baseX + Math.cos(angle) * dist,
            baseY,
            baseZ + Math.sin(angle) * dist
        };
    }

    /**
     * Windup spawn position for charge visual.
     * Always uses the target/telegraph position so the charge visual
     * appears at ground level exactly above the telegraph.
     */
    @Override
    protected double[] getWindupSpawnPosition(EntityLivingBase caster, EntityLivingBase target, int index) {
        if (pillarData.mode == PillarMode.MOVING) {
            // Charge visual appears at the same spot the pillar will spawn
            Vec3 look = caster.getLookVec();
            if (look == null) return new double[]{caster.posX, caster.posY, caster.posZ};
            float baseDist = Math.max(1.0f, pillarData.targetRadius);
            double ox = offsetAxis == OffsetAxis.X ? pillarOffset * index : 0;
            double oz = offsetAxis == OffsetAxis.Z ? pillarOffset * index : 0;
            return new double[]{
                caster.posX + look.xCoord * baseDist + ox,
                caster.posY,
                caster.posZ + look.zCoord * baseDist + oz
            };
        }
        // ANCHORED: use pre-calculated telegraph position for index 0,
        // target position for the rest
        if (index == 0 && !preCalculatedPositions.isEmpty()) {
            return preCalculatedPositions.get(0);
        }
        if (target != null) {
            return new double[]{target.posX, target.posY, target.posZ};
        }
        return new double[]{caster.posX, caster.posY, caster.posZ};
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

        if (pillarData.mode == PillarMode.MOVING && !homingData.isHoming()) {
            double mx = 0, mz = 0;
            if (target != null) {
                double dx = target.posX - entity.posX;
                double dz = target.posZ - entity.posZ;
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0) {
                    mx = (dx / len) * homingData.getSpeed();
                    mz = (dz / len) * homingData.getSpeed();
                }
            } else {
                Vec3 look = caster.getLookVec();
                if (look != null) {
                    mx = look.xCoord * homingData.getSpeed();
                    mz = look.zCoord * homingData.getSpeed();
                }
            }
            entity.setInitialMotion(mx, mz);
        }

        entity.startGrowing();
    }

    // ==================== TELEGRAPH ====================

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || isPlayerCaster(caster)) {
            return null;
        }

        Telegraph telegraph;
        if (pillarData.shape == PillarShape.SQUARE) {
            telegraph = Telegraph.square(pillarData.targetRadius);
        } else {
            telegraph = Telegraph.circle(pillarData.targetRadius);
        }

        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        double telegraphX, telegraphZ, telegraphY;

        if (pillarData.mode == PillarMode.MOVING) {
            Vec3 look = caster.getLookVec();
            if (look != null) {
                float dist = Math.max(1.0f, pillarData.targetRadius);
                telegraphX = caster.posX + look.xCoord * dist;
                telegraphZ = caster.posZ + look.zCoord * dist;
            } else {
                telegraphX = caster.posX;
                telegraphZ = caster.posZ;
            }
            telegraphY = findGroundLevel(caster.worldObj, telegraphX, caster.posY, telegraphZ);
        } else {
            if (target == null) return null;
            telegraphX = target.posX;
            telegraphZ = target.posZ;
            telegraphY = findGroundLevel(caster.worldObj, telegraphX, target.posY, telegraphZ);
        }

        preCalculatedPositions.clear();
        preCalculatedPositions.add(new double[]{telegraphX, telegraphY, telegraphZ});

        TelegraphInstance instance = new TelegraphInstance(
            telegraph, telegraphX, telegraphY, telegraphZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());
        instance.setEntityIdToFollow(-1);

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
    }

    @Override
    protected void readTypeSpecificNBT(NBTTagCompound nbt) {
        pillarData.readNBT(nbt);
        homingData.readNBT(nbt);
        this.pillarOffset = nbt.hasKey("pillarOffset") ? nbt.getFloat("pillarOffset") : 0f;
        int axisOrd = nbt.hasKey("offsetAxis") ? nbt.getInteger("offsetAxis") : 1;
        this.offsetAxis = (axisOrd >= 0 && axisOrd < OffsetAxis.values().length)
            ? OffsetAxis.values()[axisOrd] : OffsetAxis.Z;
    }

    // ==================== GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    protected void addTypeDefinitions(List<FieldDef> defs) {
        FieldDef.insertAfter(defs, "ability.fireDelay", FieldDef.row(
            FieldDef.floatField("ability.pillar.offset", () -> pillarOffset, v -> pillarOffset = Math.max(0f, v)).range(0f, 20f),
            FieldDef.enumField("ability.pillar.offsetAxis", OffsetAxis.class, () -> offsetAxis, v -> offsetAxis = v)
        ).visibleWhen(() -> zoneCount > 1));

        defs.add(FieldDef.section("ability.section.pillar"));

        defs.add(FieldDef.enumField("ability.pillarMode", PillarMode.class,
            () -> pillarData.mode, v -> pillarData.mode = v));

        defs.add(FieldDef.enumField("ability.pillarOrigin", EntityAbilityPillar.PillarOrigin.class,
            () -> pillarData.origin, v -> pillarData.origin = v));

        defs.add(FieldDef.enumField("ability.pillarShape", PillarShape.class,
            () -> pillarData.shape, v -> pillarData.shape = v));

        defs.add(FieldDef.intField("ability.pillar.spawnDelay", () -> pillarData.spawnDelay, v -> pillarData.spawnDelay = Math.max(0, v))
            .range(0, 200));

        defs.add(FieldDef.section("ability.section.size"));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.pillar.targetRadius", () -> pillarData.targetRadius, v -> pillarData.targetRadius = v).range(0.1f, 64f),
            FieldDef.floatField("ability.pillar.targetHeight", () -> pillarData.targetHeight, v -> pillarData.targetHeight = v).range(0.1f, 64f)
        ));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.pillar.radiusGrowSpeed", () -> pillarData.radiusGrowSpeed, v -> pillarData.radiusGrowSpeed = Math.max(0.01f, v)),
            FieldDef.floatField("ability.pillar.heightGrowSpeed", () -> pillarData.heightGrowSpeed, v -> pillarData.heightGrowSpeed = Math.max(0.01f, v))
        ));

        defs.add(FieldDef.section("ability.section.movement"));

        defs.add(FieldDef.floatField("ability.speed", () -> homingData.getSpeed(), v -> homingData.setSpeed(v))
            .range(0.01f, 10f));

        defs.add(FieldDef.boolField("ability.homing", () -> homingData.isHoming(), v -> homingData.setHoming(v)));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.homingStrength", () -> homingData.getHomingStrength(), v -> homingData.setHomingStrength(v)).range(0f, 1f),
            FieldDef.floatField("ability.homingRange", () -> homingData.getHomingRange(), v -> homingData.setHomingRange(v))
        ).visibleWhen(() -> homingData.isHoming()));

        defs.add(FieldDef.section("ability.section.combat"));

        defs.add(FieldDef.floatField("ability.damage", this::getDamage, this::setDamage).range(0f, 1000f));
        defs.add(FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback).range(0f, 10f));
        defs.add(FieldDef.intField("ability.maxLifetime", this::getMaxLifetime, this::setMaxLifetime).range(1, 1200));

        defs.add(AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects));
    }

    // ==================== GETTERS & SETTERS ====================

    public EnergyPillarData getPillarData() { return pillarData; }
    public void setPillarData(EnergyPillarData data) { this.pillarData = data != null ? data : new EnergyPillarData(); }

    public EnergyHomingData getHomingData() { return homingData; }
    public void setHomingData(EnergyHomingData data) { this.homingData = data != null ? data : new EnergyHomingData(); }

    public float getPillarOffset() { return pillarOffset; }
    public void setPillarOffset(float offset) { this.pillarOffset = Math.max(0f, offset); }

    public OffsetAxis getOffsetAxis() { return offsetAxis; }
    public void setOffsetAxis(OffsetAxis axis) { this.offsetAxis = axis != null ? axis : OffsetAxis.Z; }
}
