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
 *           When count > 1, each subsequent pillar recalculates a random position
 *           near the target at spawn time.
 *
 * MOVING:   pillar spawns in front of the caster and travels forward.
 *           With homing: steers toward target.
 *           Without homing: straight line along caster look vector or toward target.
 *           When count > 1, pillarOffset spaces pillars apart along the travel direction.
 *
 * Telegraph shape matches PillarShape (CIRCLE or SQUARE).
 * In MOVING mode, telegraph appears at the spawn point in front of the caster.
 */
public class AbilityPillar extends AbilityEnergyZone<EntityAbilityPillar> {

    // ==================== PILLAR CONFIG ====================

    private EnergyPillarData pillarData = new EnergyPillarData();
    private EnergyHomingData homingData = new EnergyHomingData();

    /**
     * Spacing between pillars in MOVING mode when count > 1.
     * 0 = all spawn at the same position.
     */
    private float pillarOffset = 0f;

    // ==================== CONSTRUCTOR ====================

    public AbilityPillar() {
        super(
            new EnergyDisplayData(),
            new EnergyCombatData(),
            new EnergyLifespanData()
        );
        combatData.hitType = HitType.MULTI;
        combatData.multiHitDelayTicks = 20;
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
        // The charge visual should appear at ground level at the spawn position,
        // not following the caster anchor. Position is already set by createEntity.
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
            Vec3 look = caster.getLookVec();
            if (look == null) {
                return new double[]{caster.posX, caster.posY, caster.posZ};
            }
            float baseDist = Math.max(1.0f, pillarData.targetRadius);
            float offsetDist = baseDist + pillarOffset * index;
            return new double[]{
                caster.posX + look.xCoord * offsetDist,
                caster.posY,
                caster.posZ + look.zCoord * offsetDist
            };
        }

        // ANCHORED: index 0 uses pre-calculated telegraph position.
        // Subsequent indices recalculate a random nearby position at spawn time.
        if (index == 0) {
            return super.getSpawnPosition(caster, target, index);
        }

        // For index > 0, find a new random position near the target
        double baseX = target != null ? target.posX : caster.posX;
        double baseZ = target != null ? target.posZ : caster.posZ;
        double baseY = target != null ? target.posY : caster.posY;

        float spawnRadius = Math.max(pillarData.targetRadius * 2.0f, 3.0f);
        double angle = Math.random() * Math.PI * 2;
        double dist = Math.sqrt(Math.random()) * spawnRadius;
        return new double[]{
            baseX + Math.cos(angle) * dist,
            baseY,
            baseZ + Math.sin(angle) * dist
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

        // Set straight-line motion for MOVING mode without homing
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
            // Telegraph appears at the spawn point in front of the caster
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

            preCalculatedPositions.clear();
            preCalculatedPositions.add(new double[]{telegraphX, telegraphY, telegraphZ});
        } else {
            // ANCHORED: telegraph at target position
            if (target == null) return null;
            telegraphX = target.posX;
            telegraphZ = target.posZ;
            telegraphY = findGroundLevel(caster.worldObj, telegraphX, target.posY, telegraphZ);

            preCalculatedPositions.clear();
            preCalculatedPositions.add(new double[]{telegraphX, telegraphY, telegraphZ});
        }

        TelegraphInstance instance = new TelegraphInstance(
            telegraph, telegraphX, telegraphY, telegraphZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());

        // In ANCHORED+homing, telegraph follows the target
        if (pillarData.mode == PillarMode.ANCHORED && homingData.isHoming() && target != null) {
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
        nbt.setFloat("PillarOffset", pillarOffset);
    }

    @Override
    protected void readTypeSpecificNBT(NBTTagCompound nbt) {
        pillarData.readNBT(nbt);
        homingData.readNBT(nbt);
        this.pillarOffset = nbt.hasKey("PillarOffset") ? nbt.getFloat("PillarOffset") : 0f;
    }

    // ==================== GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    protected void addTypeDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.section("ability.section.pillar"));

        defs.add(FieldDef.enumField("ability.pillarMode", PillarMode.class,
            () -> pillarData.mode, v -> pillarData.mode = v));

        defs.add(FieldDef.enumField("ability.pillarOrigin", EntityAbilityPillar.PillarOrigin.class,
            () -> pillarData.origin, v -> pillarData.origin = v));

        defs.add(FieldDef.enumField("ability.pillarShape", PillarShape.class,
            () -> pillarData.shape, v -> pillarData.shape = v));

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

        defs.add(FieldDef.floatField("ability.pillar.offset", () -> pillarOffset, v -> pillarOffset = Math.max(0f, v))
            .visibleWhen(() -> zoneCount > 1)
            .range(0f, 20f));

        defs.add(FieldDef.section("ability.section.movement")
            .visibleWhen(() -> pillarData.mode == PillarMode.MOVING));

        defs.add(FieldDef.floatField("ability.speed", () -> homingData.getSpeed(), v -> homingData.setSpeed(v))
            .visibleWhen(() -> pillarData.mode == PillarMode.MOVING)
            .range(0.01f, 10f));

        defs.add(FieldDef.boolField("ability.homing", () -> homingData.isHoming(), v -> homingData.setHoming(v))
            .visibleWhen(() -> pillarData.mode == PillarMode.MOVING));

        defs.add(FieldDef.row(
            FieldDef.floatField("ability.homingStrength", () -> homingData.getHomingStrength(), v -> homingData.setHomingStrength(v)).range(0f, 1f),
            FieldDef.floatField("ability.homingRange", () -> homingData.getHomingRange(), v -> homingData.setHomingRange(v))
        ).visibleWhen(() -> pillarData.mode == PillarMode.MOVING && homingData.isHoming()));

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

    public float getPillarOffset() { return pillarOffset; }
    public void setPillarOffset(float offset) { this.pillarOffset = Math.max(0f, offset); }
}
