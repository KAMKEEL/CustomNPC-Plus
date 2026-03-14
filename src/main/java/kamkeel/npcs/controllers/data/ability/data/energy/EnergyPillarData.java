package kamkeel.npcs.controllers.data.ability.data.energy;

import kamkeel.npcs.entity.EntityAbilityPillar.PillarMode;
import kamkeel.npcs.entity.EntityAbilityPillar.PillarOrigin;
import kamkeel.npcs.entity.EntityAbilityPillar.PillarShape;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Groups pillar-specific configuration for EntityAbilityPillar.
 * Enum types are owned by EntityAbilityPillar; this class handles serialization only.
 */
public class EnergyPillarData {

    public float targetRadius = 2.0f;
    public float targetHeight = 10.0f;
    public float radiusGrowSpeed = 0.1f;
    public float heightGrowSpeed = 0.2f;
    public PillarMode mode = PillarMode.ANCHORED;
    public PillarOrigin origin = PillarOrigin.FROM_GROUND;
    public PillarShape shape = PillarShape.CIRCLE;
    public int spawnDelay = 10;

    public EnergyPillarData() {
    }

    public EnergyPillarData(float targetRadius, float targetHeight,
                            float radiusGrowSpeed, float heightGrowSpeed,
                            PillarMode mode, PillarOrigin origin, PillarShape shape,
                            int spawnDelay) {
        this.targetRadius = targetRadius;
        this.targetHeight = targetHeight;
        this.radiusGrowSpeed = radiusGrowSpeed;
        this.heightGrowSpeed = heightGrowSpeed;
        this.mode = mode != null ? mode : PillarMode.ANCHORED;
        this.origin = origin != null ? origin : PillarOrigin.FROM_GROUND;
        this.shape = shape != null ? shape : PillarShape.CIRCLE;
        this.spawnDelay = Math.max(0, spawnDelay);
    }

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setFloat("TargetRadius", targetRadius);
        nbt.setFloat("TargetHeight", targetHeight);
        nbt.setFloat("RadiusGrowSpeed", radiusGrowSpeed);
        nbt.setFloat("HeightGrowSpeed", heightGrowSpeed);
        nbt.setInteger("PillarMode", mode.ordinal());
        nbt.setInteger("PillarOrigin", origin.ordinal());
        nbt.setInteger("PillarShape", shape.ordinal());
        nbt.setInteger("SpawnDelay", spawnDelay);
    }

    public void readNBT(NBTTagCompound nbt) {
        this.targetRadius = nbt.hasKey("TargetRadius") ? nbt.getFloat("TargetRadius") : 1.0f;
        this.targetHeight = nbt.hasKey("TargetHeight") ? nbt.getFloat("TargetHeight") : 2.0f;
        this.radiusGrowSpeed = nbt.hasKey("RadiusGrowSpeed") ? nbt.getFloat("RadiusGrowSpeed") : 0.1f;
        this.heightGrowSpeed = nbt.hasKey("HeightGrowSpeed") ? nbt.getFloat("HeightGrowSpeed") : 0.2f;

        int modeOrd = nbt.hasKey("PillarMode") ? nbt.getInteger("PillarMode") : 0;
        this.mode = (modeOrd >= 0 && modeOrd < PillarMode.values().length) ? PillarMode.values()[modeOrd] : PillarMode.ANCHORED;

        int originOrd = nbt.hasKey("PillarOrigin") ? nbt.getInteger("PillarOrigin") : 0;
        this.origin = (originOrd >= 0 && originOrd < PillarOrigin.values().length) ? PillarOrigin.values()[originOrd] : PillarOrigin.FROM_GROUND;

        int shapeOrd = nbt.hasKey("PillarShape") ? nbt.getInteger("PillarShape") : 0;
        this.shape = (shapeOrd >= 0 && shapeOrd < PillarShape.values().length) ? PillarShape.values()[shapeOrd] : PillarShape.CIRCLE;

        this.spawnDelay = nbt.hasKey("SpawnDelay") ? Math.max(0, nbt.getInteger("SpawnDelay")) : 0;
    }

    public EnergyPillarData copy() {
        return new EnergyPillarData(
            targetRadius, targetHeight,
            radiusGrowSpeed, heightGrowSpeed,
            mode, origin, shape, spawnDelay
        );
    }
}
