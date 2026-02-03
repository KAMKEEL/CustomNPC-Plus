package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Groups homing/movement properties for energy projectile abilities.
 * Used by Orb, Disc, and Beam (not Laser or Sweeper).
 */
public class EnergyHomingData {
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

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setFloat("speed", speed);
        nbt.setBoolean("homing", homing);
        nbt.setFloat("homingStrength", homingStrength);
        nbt.setFloat("homingRange", homingRange);
    }

    public void readNBT(NBTTagCompound nbt) {
        speed = nbt.hasKey("speed") ? nbt.getFloat("speed") : 0.5f;
        homing = !nbt.hasKey("homing") || nbt.getBoolean("homing");
        homingStrength = nbt.hasKey("homingStrength") ? nbt.getFloat("homingStrength") : 0.15f;
        homingRange = nbt.hasKey("homingRange") ? nbt.getFloat("homingRange") : 20.0f;
    }

    public EnergyHomingData copy() {
        return new EnergyHomingData(speed, homing, homingStrength, homingRange);
    }
}
