package kamkeel.npcs.network.packets.request.party;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.data.Party;

final class PartyPacketUtil {
    private PartyPacketUtil() {
    }

    static boolean canManageParty(EntityPlayer player, Party party) {
        if (party == null) {
            return false;
        }
        if (NoppesUtilServer.isOp(player)) {
            return true;
        }
        return party.getLeaderUUID() != null && party.getLeaderUUID().equals(player.getUniqueID());
    }
}
