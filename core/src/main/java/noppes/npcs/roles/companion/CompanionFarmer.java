package noppes.npcs.roles.companion;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
public class CompanionFarmer extends CompanionJobInterface {
    public boolean isStanding = false;

    @Override
    public INbt getNBT() {
        INbt compound = new INbt();
        compound.setBoolean("CompanionFarmerStanding", isStanding);
        return compound;
    }

    @Override
    public void setNBT(INbt compound) {
        isStanding = compound.getBoolean("CompanionFarmerStanding");
    }

    @Override
    public boolean isSelfSufficient() {
        return isStanding;
    }

    @Override
    public void onUpdate() {

    }
}
