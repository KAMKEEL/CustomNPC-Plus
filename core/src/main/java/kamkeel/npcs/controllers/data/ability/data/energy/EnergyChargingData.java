package kamkeel.npcs.controllers.data.ability.data.energy;

import noppes.npcs.platform.nbt.INBTCompound;

public class EnergyChargingData {

    public int chargeDuration = 60;
    public int chargeDelay = 0;
    public int maxCharge = 100;
    public boolean hasCharge = true;
    public boolean chargeWindUpSync = true;

    public EnergyChargingData() {
    }

    public EnergyChargingData(int chargeDuration, int chargeDelay, int maxCharge, boolean hasCharge, boolean chargeWindUpSync) {
        this.chargeDuration = chargeDuration;
        this.chargeDelay = chargeDelay;
        this.maxCharge = maxCharge;
        this.hasCharge = hasCharge;
        this.chargeWindUpSync = chargeWindUpSync;
    }

    public boolean hasCharge() {
        return hasCharge;
    }

    public void setHasCharge(boolean hasCharge) {
        this.hasCharge = hasCharge;
    }

    public boolean isChargeWindUpSync() {
        return chargeWindUpSync;
    }

    public void setChargeWindUpSync(boolean chargeWindUpSync) {
        this.chargeWindUpSync = chargeWindUpSync;
    }

    public int getChargeDuration() {
        return chargeDuration;
    }

    public void setChargeDuration(int chargeDuration) {
        this.chargeDuration = chargeDuration;
    }

    public int getChargeDelay() {
        return chargeDelay;
    }

    public void setChargeDelay(int chargeDelay) {
        this.chargeDelay = chargeDelay;
    }

    public int getMaxCharge() {
        return maxCharge;
    }

    public void setMaxCharge(int maxCharge) {
        this.maxCharge = Math.max(0, maxCharge);
    }

    public void writeNBT(INBTCompound nbt) {
        nbt.setInteger("chargeDuration", chargeDuration);
        nbt.setInteger("chargeDelay", chargeDelay);
        nbt.setInteger("maxCharge", maxCharge);
        nbt.setBoolean("hasCharge", hasCharge);
        nbt.setBoolean("chargeWindUpSync", chargeWindUpSync);
    }

    public void readNBT(INBTCompound nbt) {
        chargeDuration = nbt.hasKey("chargeDuration") ? nbt.getInteger("chargeDuration") : 60;
        chargeDelay = nbt.hasKey("chargeDelay") ? nbt.getInteger("chargeDelay") : 0;
        maxCharge = nbt.hasKey("maxCharge") ? nbt.getInteger("maxCharge") : 100;
        hasCharge = !nbt.hasKey("hasCharge") || nbt.getBoolean("hasCharge");
        chargeWindUpSync = !nbt.hasKey("chargeWindUpSync") || nbt.getBoolean("chargeWindUpSync");
    }

    public EnergyChargingData copy() {
        return new EnergyChargingData(chargeDuration, chargeDelay, maxCharge, hasCharge, chargeWindUpSync);
    }
}
