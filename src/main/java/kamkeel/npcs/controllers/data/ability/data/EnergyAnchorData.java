package kamkeel.npcs.controllers.data.ability.data;

import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import net.minecraft.nbt.NBTTagCompound;

public class EnergyAnchorData {
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
        this.anchorPoint = AnchorPoint.fromId(anchorPoint);
    }

    public EnergyAnchorData(int anchorPoint, float anchorOffsetX, float anchorOffsetY, float anchorOffsetZ) {
        this.anchorPoint = AnchorPoint.fromId(anchorPoint);
        this.anchorOffsetX = anchorOffsetX;
        this.anchorOffsetY = anchorOffsetY;
        this.anchorOffsetZ = anchorOffsetZ;
    }

    public void writeNBT(NBTTagCompound nbt) {
        nbt.setInteger("anchorPoint", anchorPoint.getId());
        nbt.setFloat("anchorOffsetX", anchorOffsetX);
        nbt.setFloat("anchorOffsetY", anchorOffsetY);
        nbt.setFloat("anchorOffsetZ", anchorOffsetZ);
    }

    public void readNBT(NBTTagCompound nbt) {
        this.anchorPoint = nbt.hasKey("anchorPoint") ? AnchorPoint.fromId(nbt.getInteger("anchorPoint")) : AnchorPoint.FRONT;
        this.anchorOffsetX = nbt.hasKey("anchorOffsetX") ? nbt.getFloat("anchorOffsetX") : 0;
        this.anchorOffsetY = nbt.hasKey("anchorOffsetY") ? nbt.getFloat("anchorOffsetY") : 0;
        this.anchorOffsetZ = nbt.hasKey("anchorOffsetZ") ? nbt.getFloat("anchorOffsetZ") : 0;
    }

    public EnergyAnchorData copy() {
        return new EnergyAnchorData(anchorPoint, anchorOffsetX, anchorOffsetY, anchorOffsetZ);
    }
}
