package kamkeel.npcs.controllers.data.ability.data.energy;


import noppes.npcs.api.ability.data.IEnergyLifespanData;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

/**
 * Groups lifespan/range properties for energy projectile abilities.
 * Used as a parameter object for IEntity constructors and ability IConfiguration.
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

    public void writeNBT(INbt nbt) {
        nbt.setFloat("maxDistance", maxDistance);
        nbt.setInteger("maxLifetime", maxLifetime);
    }

    public void readNBT(INbt nbt) {
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
