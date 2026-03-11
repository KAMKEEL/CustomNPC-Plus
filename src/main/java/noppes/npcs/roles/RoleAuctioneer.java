package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleAuctioneer extends RoleInterface {

    public RoleAuctioneer(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        // Simple role - no custom data needed
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        // Simple role - no custom data needed
    }

    @Override
    public void interact(EntityPlayer player) {
        if (!ConfigMarket.AuctionEnabled) {
            return;
        }

        // Open the auction house GUI
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerAuction, npc);
    }

    @Override
    public void delete() {
        // No cleanup needed
    }
}
