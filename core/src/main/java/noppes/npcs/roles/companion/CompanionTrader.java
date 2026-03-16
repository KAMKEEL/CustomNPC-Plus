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
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;

public class CompanionTrader extends CompanionJobInterface {

    @Override
    public INbt getNBT() {
        INbt compound = new INbt();
        return compound;
    }

    @Override
    public void setNBT(INbt compound) {

    }

    public void interact(IPlayer player) {
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.CompanionTrader, npc);
    }
}
