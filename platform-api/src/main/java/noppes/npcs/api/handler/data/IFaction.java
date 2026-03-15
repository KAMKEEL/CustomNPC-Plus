package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;

public interface IFaction {
    /**
     * @return The faction's unique ID
     */
    int getId();

    /**
     * @return The faction's display name
     */
    String getName();

    /**
     * Sets the faction's display name.
     * @param name The new name
     */
    void setName(String name);

    /**
     * Sets the default faction points assigned to new players.
     * @param var1 The default point value
     */
    void setDefaultPoints(int var1);

    /**
     * @return The default faction points assigned to new players
     */
    int getDefaultPoints();

    /**
     * Sets the point threshold at or above which a player is considered friendly.
     * @param p The friendly point threshold
     */
    void setFriendlyPoints(int p);

    /**
     * @return The point threshold at or above which a player is considered friendly
     */
    int getFriendlyPoints();

    /**
     * Sets the point threshold below which a player is considered an enemy.
     * @param p The neutral point threshold
     */
    void setNeutralPoints(int p);

    /**
     * @return The point threshold below which a player is considered an enemy
     */
    int getNeutralPoints();

    /**
     * Sets the faction's display color.
     * @param c The color as an RGB integer
     */
    void setColor(int c);

    /**
     * @return The faction's display color as an RGB integer
     */
    int getColor();

    /**
     * Returns the player's standing with this faction.
     * @param player The player to check
     * @return 1 for friendly, 0 for neutral, -1 for enemy
     */
    int playerStatus(IPlayer player);

    /**
     * Checks whether this faction is hostile toward the given NPC's faction.
     * @param npc The NPC to check against
     * @return True if this faction is aggressive toward the NPC
     */
    boolean isAggressiveToNpc(ICustomNpc npc);

    /**
     * @return Whether this faction is hidden from the player's faction list
     */
    boolean getIsHidden();

    /**
     * Sets whether this faction is hidden from the player's faction list.
     * @param hidden True to hide the faction
     */
    void setIsHidden(boolean hidden);

    /**
     * @return Whether NPCs in this faction are passive and will not attack
     */
    boolean isPassive();

    /**
     * Sets whether NPCs in this faction are passive.
     * @param passive True to make faction NPCs passive
     */
    void setIsPassive(boolean passive);

    /**
     * @return Whether NPCs in this faction can be attacked by hostile mobs
     */
    boolean attackedByMobs();

    /**
     * Sets whether NPCs in this faction can be attacked by hostile mobs.
     * @param attacked True to allow mobs to attack faction NPCs
     */
    void setAttackedByMobs(boolean attacked);

    /**
     * Checks whether the given faction is an enemy of this faction.
     * @param faction The faction to check
     * @return True if the given faction is an enemy
     */
    boolean isEnemyFaction(IFaction faction);

    /**
     * @return An array of all factions that are enemies of this faction
     */
    IFaction[] getEnemyFactions();

    /**
     * Adds a faction as an enemy of this faction.
     * @param faction The faction to add as an enemy
     */
    void addEnemyFaction(IFaction faction);

    /**
     * Removes a faction from this faction's enemy list.
     * @param faction The faction to remove
     */
    void removeEnemyFaction(IFaction faction);

    /**
     * Saves the faction data to the server.
     */
    void save();
}
