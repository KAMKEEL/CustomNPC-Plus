package kamkeel.npcs.controllers.data.ability.data.energy;

import noppes.npcs.api.ability.data.IEnergyHomingData;
import noppes.npcs.api.INbt;

/**
 * Groups homing/movement properties for energy projectile abilities.
 * Used by Orb, Disc, and Beam (not Laser or Sweeper).
 */
public class EnergyHomingData implements IEnergyHomingData {
    public float speed = 0.5f;
    public boolean homing = true;
    public float homingStrength = 0.15f;
    public float homingRange = 20.0f;

    public EnergyHomingData() {
    }

    public EnergyHomingData(float speed, boolean homing, float homingStrength, float homingRange) {
        this.speed = speed;
        this.homing = homing;
        this.homingStrength = homingStrength;
        this.homingRange = homingRange;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = Math.max(0.01f, speed);
    }

    @Override
    public boolean isHoming() {
        return homing;
    }

    @Override
    public void setHoming(boolean homing) {
        this.homing = homing;
    }

    @Override
    public float getHomingStrength() {
        return homingStrength;
    }

    @Override
    public void setHomingStrength(float homingStrength) {
        this.homingStrength = homingStrength;
    }

    @Override
    public float getHomingRange() {
        return homingRange;
    }

    @Override
    public void setHomingRange(float homingRange) {
        this.homingRange = homingRange;
    }

    public void writeNBT(INbt nbt) {
        nbt.setFloat("speed", speed);
        nbt.setBoolean("homing", homing);
        nbt.setFloat("homingStrength", homingStrength);
        nbt.setFloat("homingRange", homingRange);
    }

    public void readNBT(INbt nbt) {
        speed = nbt.hasKey("speed") ? nbt.getFloat("speed") : 0.5f;
        homing = !nbt.hasKey("homing") || nbt.getBoolean("homing");
        homingStrength = nbt.hasKey("homingStrength") ? nbt.getFloat("homingStrength") : 0.15f;
        homingRange = nbt.hasKey("homingRange") ? nbt.getFloat("homingRange") : 20.0f;

        // Sanitize speed to prevent stuck projectiles
        if (Float.isNaN(speed) || Float.isInfinite(speed) || speed <= 0) speed = 0.5f;
    }

    public EnergyHomingData copy() {
        return new EnergyHomingData(speed, homing, homingStrength, homingRange);
    }
}
