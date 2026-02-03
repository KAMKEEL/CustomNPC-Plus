package kamkeel.npcs.controllers.data.ability.data;

import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.data.IEnergyAnchorData;

public class EnergyAnchorData implements IEnergyAnchorData {
    public AnchorPoint anchorPoint = AnchorPoint.FRONT;
    public float anchorOffsetX = 0;
    public float anchorOffsetY = 0;
    public float anchorOffsetZ = 0;

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

    @Override
    public AnchorPoint getAnchorPoint() {
        return anchorPoint;
    }

    @Override
    public void setAnchorPoint(AnchorPoint anchorPoint) {
        this.anchorPoint = anchorPoint;
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

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setInteger("anchorPoint", anchorPoint.ordinal());
        nbt.setFloat("anchorOffsetX", anchorOffsetX);
        nbt.setFloat("anchorOffsetY", anchorOffsetY);
        nbt.setFloat("anchorOffsetZ", anchorOffsetZ);
    }

    public void readNBT(NBTTagCompound nbt) {
        this.anchorPoint = nbt.hasKey("anchorPoint") ? AnchorPoint.fromOrdinal(nbt.getInteger("anchorPoint")) : AnchorPoint.FRONT;
        this.anchorOffsetX = nbt.hasKey("anchorOffsetX") ? nbt.getFloat("anchorOffsetX") : 0;
        this.anchorOffsetY = nbt.hasKey("anchorOffsetY") ? nbt.getFloat("anchorOffsetY") : 0;
        this.anchorOffsetZ = nbt.hasKey("anchorOffsetZ") ? nbt.getFloat("anchorOffsetZ") : 0;
    }

    public EnergyAnchorData copy() {
        return new EnergyAnchorData(anchorPoint, anchorOffsetX, anchorOffsetY, anchorOffsetZ);
    }
}
