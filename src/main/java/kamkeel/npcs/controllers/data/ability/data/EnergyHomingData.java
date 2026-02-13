package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.data.IEnergyHomingData;


/**
 * Groups homing/movement properties for energy projectile abilities.
 * Used by Orb, Disc, and Beam (not Laser or Sweeper).
 */
public class EnergyHomingData implements IEnergyHomingData {
    public float speed = 0.5f;
    public boolean homing = true;
    public float homingStrength = 0.15f;
    public float homingRange = 20.0f;

    public EnergyHomingData() {}

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
        this.speed = speed;
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

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setFloat("speed", speed);
        nbt.setBoolean("homing", homing);
        nbt.setFloat("homingStrength", homingStrength);
        nbt.setFloat("homingRange", homingRange);
    }

    public void readNBT(NBTTagCompound nbt) {
        speed = nbt.getFloat("speed");
        homing = nbt.getBoolean("homing");
        homingStrength = nbt.getFloat("homingStrength");
        homingRange = nbt.getFloat("homingRange");
    }

    public EnergyHomingData copy() {
        return new EnergyHomingData(speed, homing, homingStrength, homingRange);
    }
}
