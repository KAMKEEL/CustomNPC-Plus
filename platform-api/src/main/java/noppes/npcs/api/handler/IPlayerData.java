package noppes.npcs.api.handler;

import noppes.npcs.api.ability.IPlayerAbilityData;
import noppes.npcs.api.entity.ICustomNpc;

/**
 * Provides access to all persistent data categories for a player,
 * including companion, dialog, quest, faction, transport, mail, trade, and ability data.
 */
public interface IPlayerData {

    /**
     * Sets the player's companion NPC.
     *
     * @param npc the companion NPC, or null to clear.
     */
    void setCompanion(ICustomNpc npc);

    /**
     * Returns the player's companion NPC.
     *
     * @return the companion NPC, or null if none.
     */
    ICustomNpc getCompanion();

    /**
     * @return true if the player currently has a companion NPC.
     */
    boolean hasCompanion();

    /**
     * @return the entity ID of the companion, or -1 if none.
     */
    int getCompanionID();

    /** @return the player's dialog data. */
    IPlayerDialogData getDialogData();

    /** @return the player's bank data. */
    IPlayerBankData getBankData();

    /** @return the player's quest data. */
    IPlayerQuestData getQuestData();

    /** @return the player's transport data. */
    IPlayerTransportData getTransportData();

    /** @return the player's faction data. */
    IPlayerFactionData getFactionData();

    /** @return the player's item giver data. */
    IPlayerItemGiverData getItemGiverData();

    /** @return the player's mail data. */
    IPlayerMailData getMailData();

    /** @return the player's trade data. */
    IPlayerTradeData getTradeData();

    /** @return the player's ability data. */
    IPlayerAbilityData getAbilityData();

    /**
     * Saves all player data to disk.
     */
    void save();
}
