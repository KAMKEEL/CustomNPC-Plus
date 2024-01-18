package noppes.npcs.controllers;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;

import java.util.HashMap;
import java.util.UUID;

public class PartyController {
    private static PartyController Instance;

    private HashMap<UUID, Party> parties = new HashMap<>();

    private PartyController() {
    }

    public static PartyController Instance() {
        if (Instance == null) {
            Instance = new PartyController();
        }
        return Instance;
    }

    public Party createParty() {
        Party party = new Party();
        this.parties.put(party.getPartyUUID(), party);
        return party;
    }

    public Party getParty(UUID partyUUID) {
        return this.parties.get(partyUUID);
    }

    public void disbandParty(UUID partyUUID) {
        Party party = this.parties.get(partyUUID);
        if (party != null) {
            for (UUID uuid: party.getPlayerUUIDs()) {
                EntityPlayer player = NoppesUtilServer.getPlayer(uuid);
                PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
                playerData.partyUUID = null;
            }
            this.parties.remove(partyUUID);
        }
    }
}
