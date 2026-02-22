package kamkeel.npcs.controllers.data.ability.data.energy;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.data.IEnergyLifespanData;

/**
 * Groups lifespan/range properties for energy projectile abilities.
 * Used as a parameter object for entity constructors and ability configuration.
 */
public class EnergyLifespanData implements IEnergyLifespanData {
    public float maxDistance = 150.0f;
    public int maxLifetime = 200;

    public EnergyLifespanData() {
    }

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
        this.maxDistance = Float.isNaN(maxDistance) || maxDistance <= 0 ? 150.0f : maxDistance;
    }

    @Override
    public int getMaxLifetime() {
        return maxLifetime;
    }

    @Override
    public void setMaxLifetime(int maxLifetime) {
        this.maxLifetime = maxLifetime <= 0 ? 200 : maxLifetime;
    }

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setFloat("maxDistance", maxDistance);
        nbt.setInteger("maxLifetime", maxLifetime);
    }

    public void readNBT(NBTTagCompound nbt) {
        maxDistance = nbt.getFloat("maxDistance");
        maxLifetime = nbt.getInteger("maxLifetime");

        // Sanitize: ensure minimum values so entities don't die instantly or live forever
        if (Float.isNaN(maxDistance) || Float.isInfinite(maxDistance) || maxDistance <= 0) maxDistance = 150.0f;
        if (maxLifetime <= 0) maxLifetime = 200;
    }

    public EnergyLifespanData copy() {
        return new EnergyLifespanData(maxDistance, maxLifetime);
    }
}
