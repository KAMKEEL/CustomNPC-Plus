package kamkeel.npcs.controllers.data.ability.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.data.IEnergyLifespanData;

/**
 * Groups lifespan/range properties for energy projectile abilities.
 * Used as a parameter object for entity constructors and ability configuration.
 */
public class EnergyLifespanData implements IEnergyLifespanData {
    public float maxDistance = 30.0f;
    public int maxLifetime = 200;

    public EnergyLifespanData() {}

    public EnergyLifespanData(float maxDistance, int maxLifetime) {
        this.maxDistance = maxDistance;
        this.maxLifetime = maxLifetime;
    }

    @Override
    public float getMaxDistance() {
        return maxDistance;
    }

    @Override
    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    @Override
    public int getMaxLifetime() {
        return maxLifetime;
    }

    @Override
    public void setMaxLifetime(int maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setFloat("maxDistance", maxDistance);
        nbt.setInteger("maxLifetime", maxLifetime);
    }

    public void readNBT(NBTTagCompound nbt) {
        maxDistance = nbt.hasKey("maxDistance") ? nbt.getFloat("maxDistance") : 30.0f;
        maxLifetime = nbt.hasKey("maxLifetime") ? nbt.getInteger("maxLifetime") : 200;
    }

    public EnergyLifespanData copy() {
        return new EnergyLifespanData(maxDistance, maxLifetime);
    }
}
