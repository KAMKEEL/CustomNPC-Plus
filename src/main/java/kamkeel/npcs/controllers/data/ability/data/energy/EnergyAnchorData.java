package kamkeel.npcs.controllers.data.ability.data.energy;

import kamkeel.npcs.controllers.data.ability.enums.AnchorPoint;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.data.IEnergyAnchorData;

public class EnergyAnchorData implements IEnergyAnchorData {
    public AnchorPoint anchorPoint = AnchorPoint.FRONT;
    public float anchorOffsetX = 0;
    public float anchorOffsetY = 0;
    public float anchorOffsetZ = 0;
    public boolean launchFromAnchor = false;

    public EnergyAnchorData() {
    }

    public EnergyAnchorData(AnchorPoint anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    public EnergyAnchorData(AnchorPoint anchorPoint, float anchorOffsetX, float anchorOffsetY, float anchorOffsetZ) {
        this.anchorPoint = anchorPoint;
        this.anchorOffsetX = anchorOffsetX;
        this.anchorOffsetY = anchorOffsetY;
        this.anchorOffsetZ = anchorOffsetZ;
    }

    public EnergyAnchorData(int anchorPoint) {
        this.anchorPoint = AnchorPoint.fromOrdinal(anchorPoint);
    }

    public EnergyAnchorData(int anchorPoint, float anchorOffsetX, float anchorOffsetY, float anchorOffsetZ) {
        this.anchorPoint = AnchorPoint.fromOrdinal(anchorPoint);
        this.anchorOffsetX = anchorOffsetX;
        this.anchorOffsetY = anchorOffsetY;
        this.anchorOffsetZ = anchorOffsetZ;
    }

    public AnchorPoint getAnchorPoint() {
        return anchorPoint;
    }

    public void setAnchorPoint(AnchorPoint anchorPoint) {
        this.anchorPoint = anchorPoint;
    }

    @Override
    public int getAnchor() {
        return anchorPoint.ordinal();
    }

    @Override
    public void setAnchor(int anchor) {
        this.anchorPoint = AnchorPoint.fromOrdinal(anchor);
    }

    @Override
    public float getAnchorOffsetX() {
        return anchorOffsetX;
    }

    @Override
    public void setAnchorOffsetX(float anchorOffsetX) {
        this.anchorOffsetX = anchorOffsetX;
    }

    @Override
    public float getAnchorOffsetY() {
        return anchorOffsetY;
    }

    @Override
    public void setAnchorOffsetY(float anchorOffsetY) {
        this.anchorOffsetY = anchorOffsetY;
    }

    @Override
    public float getAnchorOffsetZ() {
        return anchorOffsetZ;
    }

    @Override
    public void setAnchorOffsetZ(float anchorOffsetZ) {
        this.anchorOffsetZ = anchorOffsetZ;
    }

    @Override
    public boolean getLaunchFromAnchor() {
        return launchFromAnchor;
    }

    @Override
    public void setLaunchFromAnchor(boolean launchFromAnchor) {
        this.launchFromAnchor = launchFromAnchor;
    }

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setInteger("anchorPoint", anchorPoint.ordinal());
        nbt.setFloat("anchorOffsetX", anchorOffsetX);
        nbt.setFloat("anchorOffsetY", anchorOffsetY);
        nbt.setFloat("anchorOffsetZ", anchorOffsetZ);
        nbt.setBoolean("launchFromAnchor", launchFromAnchor);
    }

    public void readNBT(NBTTagCompound nbt) {
        this.anchorPoint = AnchorPoint.fromOrdinal(nbt.getInteger("anchorPoint"));
        this.anchorOffsetX = nbt.getFloat("anchorOffsetX");
        this.anchorOffsetY = nbt.getFloat("anchorOffsetY");
        this.anchorOffsetZ = nbt.getFloat("anchorOffsetZ");
        this.launchFromAnchor = nbt.getBoolean("launchFromAnchor");
    }

    public EnergyAnchorData copy() {
        EnergyAnchorData copy = new EnergyAnchorData(anchorPoint, anchorOffsetX, anchorOffsetY, anchorOffsetZ);
        copy.launchFromAnchor = this.launchFromAnchor;
        return copy;
    }
}
