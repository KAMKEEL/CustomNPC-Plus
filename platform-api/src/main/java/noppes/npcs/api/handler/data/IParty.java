package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IPlayer;

import java.util.List;

public interface IParty {

    /**
     * @return UUID of Party
     */
    String getPartyUUIDString();

    /**
     * @return if party is locked (quest is set)
     */
    boolean getIsLocked();

    /**
     * Set a quest to the party (this does not validate use validateQuest())
     *
     * @param quest Quest Object
     */
    void setQuest(IQuest quest);

    /**
     * Get the Quest for the Party, can be null
     *
     * @return quest
     */
    IQuest getQuest();

    /**
     * Get the Quest ID for Party | -1 if no quest
     *
     * @return quest id
     */
    int getCurrentQuestID();

    /**
     * Get the Quest Name for Party | BLANK if no quest
     *
     * @return quest name
     */
    String getCurrentQuestName();


    /**
     * @param playerName Username of Player (Case Sensitive)
     * @return successful or not
     */
    boolean addPlayer(String playerName);

    /**
     * @param playerName Username of Player (Case Sensitive)
     * @return successful or not
     */
    boolean removePlayer(String playerName);

    /**
     * @param player IPlayer
     * @return successful or not
     */
    boolean addPlayer(IPlayer player);

    /**
     * @param player IPlayer
     * @return successful or not
     */
    boolean removePlayer(IPlayer player);


    /**
     * Check if Player is in Party with IPlayer
     *
     * @param player the player to check
     * @return true/false if player is in party
     */
    boolean hasPlayer(IPlayer player);

    /**
     * Check if Player is in Party with Player Name
     *
     * @param playerName the player name to check
     * @return true/false if player is in party
     */
    boolean hasPlayer(String playerName);

    /**
     * @return Party Leader's Name
     */
    String getPartyLeaderName();

    /**
     * @return Collection of all Player Names in Party
     */
    List<String> getPlayerNamesList();


    /**
     * @param questID            ID for the Quest to Verify
     * @param sendLeaderMessages IF true will send ERROR message to Party Leader on why the quest could not be set
     * @return true/false if the party CAN have the quest set
     */
    boolean validateQuest(int questID, boolean sendLeaderMessages);

    /**
     * Toggles Friendly Fire
     */
    void toggleFriendlyFire();

    /**
     * @return Current Friendly Fire settings in Party
     */
    boolean friendlyFire();

    /**
     * Sends information for Quest Objectives to all Party Participants [Lower Data]
     */
    void updateQuestObjectiveData();

    /**
     * Sends all PARTY INFORMATION to all Party Participants [Higher Data]
     */
    void updatePartyData();
}
