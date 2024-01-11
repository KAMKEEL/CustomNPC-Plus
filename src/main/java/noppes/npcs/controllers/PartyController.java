package noppes.npcs.controllers;

import noppes.npcs.controllers.data.Party;

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

    public void deleteParty(UUID partyUUID) {
        this.parties.remove(partyUUID);
    }
}
