package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyCombatData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyHomingData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyLifespanData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyPillarData;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityPillar;
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
 * ANCHORED: pillar spawns at the target's position and stays there.
 * MOVING:   pillar spawns in front of the caster and travels forward,
 *           optionally homing toward a target.
 *
 * Telegraph shape matches PillarShape (CIRCLE or SQUARE).
 */
public class AbilityPillar extends AbilityEnergyZone<EntityAbilityPillar> {

    // ==================== PILLAR CONFIG ====================

    private EnergyPillarData pillarData = new EnergyPillarData();
    private EnergyHomingData homingData = new EnergyHomingData();

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
        return new EntityAbilityPillar(
            caster.worldObj, caster,
            x, y, z,
            pillarData,
            resolved,
            lightningData,
            homingData
        );
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

    // ==================== SPAWN POSITION ====================

    @Override
    protected double[] getSpawnPosition(EntityLivingBase caster, EntityLivingBase target, int index) {
        if (pillarData.mode == PillarMode.MOVING) {
            // Spawn in front of caster along look vector
            Vec3 look = caster.getLookVec();
            if (look != null) {
                float dist = Math.max(1.0f, pillarData.targetRadius);
                return new double[]{
                    caster.posX + look.xCoord * dist,
                    caster.posY,
                    caster.posZ + look.zCoord * dist
                };
            }
            return new double[]{caster.posX, caster.posY, caster.posZ};
        }

        // ANCHORED: use pre-calculated position or target position
        return super.getSpawnPosition(caster, target, index);
    }

    // ==================== EXECUTE — startGrowing ====================

    @Override
    protected void spawnZoneAt(EntityLivingBase caster, EntityLivingBase target, int index) {
        super.spawnZoneAt(caster, target, index);
        EntityAbilityPillar entity = entities[index];
        if (entity != null && !entity.isDead) {
            if (target != null) {
                entity.setTarget(target);
            }
            entity.startGrowing();
        }
    }

    // ==================== TELEGRAPH ====================

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || isPlayerCaster(caster) || target == null) {
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

        double groundY = findGroundLevel(caster.worldObj, target.posX, target.posY, target.posZ);

        // Store position for use in getSpawnPosition (ANCHORED mode)
        preCalculatedPositions.clear();
        for (int i = 0; i < zoneCount; i++) {
            preCalculatedPositions.add(new double[]{target.posX, groundY, target.posZ});
        }

        TelegraphInstance instance = new TelegraphInstance(telegraph, target.posX, groundY, target.posZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());
        instance.setEntityIdToFollow(homingData.isHoming() ? target.getEntityId() : -1);

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
    }

    @Override
    protected void readTypeSpecificNBT(NBTTagCompound nbt) {
        pillarData.readNBT(nbt);
        homingData.readNBT(nbt);
    }

    // ==================== GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    protected void addTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.section("ability.section.pillar"));

        defs.add(FieldDef.enumField("ability.pillarMode", PillarMode.class,
            () -> pillarData.mode,
            v -> pillarData.mode = v));

        defs.add(FieldDef.enumField("ability.pillarOrigin", EntityAbilityPillar.PillarOrigin.class,
            () -> pillarData.origin,
            v -> pillarData.origin = v));

        defs.add(FieldDef.enumField("ability.pillarShape", PillarShape.class,
            () -> pillarData.shape,
            v -> pillarData.shape = v));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.pillar.targetRadius", () -> pillarData.targetRadius, v -> pillarData.targetRadius = v).range(0.1f, 64f),
            FieldDef.floatField("ability.pillar.targetHeight", () -> pillarData.targetHeight, v -> pillarData.targetHeight = v).range(0.1f, 64f)
        ));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.pillar.radiusGrowSpeed", () -> pillarData.radiusGrowSpeed, v -> pillarData.radiusGrowSpeed = Math.max(0.01f, v)),
            FieldDef.floatField("ability.pillar.heightGrowSpeed", () -> pillarData.heightGrowSpeed, v -> pillarData.heightGrowSpeed = Math.max(0.01f, v))
        ));

        defs.add(FieldDef.intField("ability.pillar.spawnDelay", () -> pillarData.spawnDelay, v -> pillarData.spawnDelay = Math.max(0, v))
            .range(0, 200));

        defs.add(FieldDef.section("ability.section.homing")
            .visibleWhen(() -> pillarData.mode == PillarMode.MOVING));

        defs.add(FieldDef.boolField("ability.homing", () -> homingData.isHoming(), v -> homingData.setHoming(v))
            .visibleWhen(() -> pillarData.mode == PillarMode.MOVING));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.homingStrength", () -> homingData.getHomingStrength(), v -> homingData.setHomingStrength(v)).range(0f, 1f),
            FieldDef.floatField("ability.homingRange", () -> homingData.getHomingRange(), v -> homingData.setHomingRange(v))
        ).visibleWhen(() -> pillarData.mode == PillarMode.MOVING && homingData.isHoming()));

        defs.add(FieldDef.floatField("ability.speed", () -> homingData.getSpeed(), v -> homingData.setSpeed(v))
            .visibleWhen(() -> pillarData.mode == PillarMode.MOVING)
            .range(0.01f, 10f));

        defs.add(FieldDef.section("ability.section.combat"));

        defs.add(FieldDef.floatField("ability.damage", this::getDamage, this::setDamage).range(0f, 1000f));
        defs.add(FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback).range(0f, 10f));
        defs.add(FieldDef.intField("ability.maxLifetime", this::getMaxLifetime, this::setMaxLifetime).range(1, 1200));
    }

    // ==================== GETTERS & SETTERS ====================

    public EnergyPillarData getPillarData() { return pillarData; }
    public void setPillarData(EnergyPillarData data) { this.pillarData = data != null ? data : new EnergyPillarData(); }

    public EnergyHomingData getHomingData() { return homingData; }
    public void setHomingData(EnergyHomingData data) { this.homingData = data != null ? data : new EnergyHomingData(); }
}
