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
        nbt.setFloat("targetRadius", targetRadius);
        nbt.setFloat("targetHeight", targetHeight);
        nbt.setFloat("radiusGrowSpeed", radiusGrowSpeed);
        nbt.setFloat("heightGrowSpeed", heightGrowSpeed);
        nbt.setInteger("pillarMode", mode.ordinal());
        nbt.setInteger("pillarOrigin", origin.ordinal());
        nbt.setInteger("pillarShape", shape.ordinal());
        nbt.setInteger("spawnDelay", spawnDelay);
    }

    public void readNBT(NBTTagCompound nbt) {
        this.targetRadius = nbt.hasKey("targetRadius") ? nbt.getFloat("targetRadius") : 1.0f;
        this.targetHeight = nbt.hasKey("targetHeight") ? nbt.getFloat("targetHeight") : 2.0f;
        this.radiusGrowSpeed = nbt.hasKey("radiusGrowSpeed") ? nbt.getFloat("radiusGrowSpeed") : 0.1f;
        this.heightGrowSpeed = nbt.hasKey("heightGrowSpeed") ? nbt.getFloat("heightGrowSpeed") : 0.2f;

        int modeOrd = nbt.hasKey("pillarMode") ? nbt.getInteger("pillarMode") : 0;
        this.mode = (modeOrd >= 0 && modeOrd < PillarMode.values().length) ? PillarMode.values()[modeOrd] : PillarMode.ANCHORED;

        int originOrd = nbt.hasKey("pillarOrigin") ? nbt.getInteger("pillarOrigin") : 0;
        this.origin = (originOrd >= 0 && originOrd < PillarOrigin.values().length) ? PillarOrigin.values()[originOrd] : PillarOrigin.FROM_GROUND;

        int shapeOrd = nbt.hasKey("pillarShape") ? nbt.getInteger("pillarShape") : 0;
        this.shape = (shapeOrd >= 0 && shapeOrd < PillarShape.values().length) ? PillarShape.values()[shapeOrd] : PillarShape.CIRCLE;

        this.spawnDelay = nbt.hasKey("spawnDelay") ? Math.max(0, nbt.getInteger("spawnDelay")) : 0;
    }

    public EnergyPillarData copy() {
        return new EnergyPillarData(
            targetRadius, targetHeight,
            radiusGrowSpeed, heightGrowSpeed,
            mode, origin, shape, spawnDelay
        );
    }
}
