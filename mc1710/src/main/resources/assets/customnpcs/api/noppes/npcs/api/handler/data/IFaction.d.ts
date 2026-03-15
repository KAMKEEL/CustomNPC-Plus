/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IFaction
 */
export interface IFaction {
    /**
     * @return The faction's unique ID
     */
    getId(): import('./int').int;
    /**
     * @return The faction's display name
     */
    getName(): String;
    /**
     * Sets the faction's display name.
     * @param name The new name
     */
    setName(name: String): import('./void').void;
    /**
     * Sets the default faction points assigned to new players.
     * @param var1 The default point value
     */
    setDefaultPoints(var1: import('./int').int): import('./void').void;
    /**
     * @return The default faction points assigned to new players
     */
    getDefaultPoints(): import('./int').int;
    /**
     * Sets the point threshold at or above which a player is considered friendly.
     * @param p The friendly point threshold
     */
    setFriendlyPoints(p: import('./int').int): import('./void').void;
    /**
     * @return The point threshold at or above which a player is considered friendly
     */
    getFriendlyPoints(): import('./int').int;
    /**
     * Sets the point threshold below which a player is considered an enemy.
     * @param p The neutral point threshold
     */
    setNeutralPoints(p: import('./int').int): import('./void').void;
    /**
     * @return The point threshold below which a player is considered an enemy
     */
    getNeutralPoints(): import('./int').int;
    /**
     * Sets the faction's display color.
     * @param c The color as an RGB integer
     */
    setColor(c: import('./int').int): import('./void').void;
    /**
     * @return The faction's display color as an RGB integer
     */
    getColor(): import('./int').int;
    /**
     * Returns the player's standing with this faction.
     * @param player The player to check
     * @return 1 for friendly, 0 for neutral, -1 for enemy
     */
    playerStatus(player: import('../../entity/IPlayer').IPlayer): import('./int').int;
    /**
     * Checks whether this faction is hostile toward the given NPC's faction.
     * @param npc The NPC to check against
     * @return True if this faction is aggressive toward the NPC
     */
    isAggressiveToNpc(npc: import('../../entity/ICustomNpc').ICustomNpc): import('./boolean').boolean;
    /**
     * @return Whether this faction is hidden from the player's faction list
     */
    getIsHidden(): import('./boolean').boolean;
    /**
     * Sets whether this faction is hidden from the player's faction list.
     * @param hidden True to hide the faction
     */
    setIsHidden(hidden: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether NPCs in this faction are passive and will not attack
     */
    isPassive(): import('./boolean').boolean;
    /**
     * Sets whether NPCs in this faction are passive.
     * @param passive True to make faction NPCs passive
     */
    setIsPassive(passive: import('./boolean').boolean): import('./void').void;
    /**
     * @return Whether NPCs in this faction can be attacked by hostile mobs
     */
    attackedByMobs(): import('./boolean').boolean;
    /**
     * Sets whether NPCs in this faction can be attacked by hostile mobs.
     * @param attacked True to allow mobs to attack faction NPCs
     */
    setAttackedByMobs(attacked: import('./boolean').boolean): import('./void').void;
    /**
     * Checks whether the given faction is an enemy of this faction.
     * @param faction The faction to check
     * @return True if the given faction is an enemy
     */
    isEnemyFaction(faction: import('./IFaction').IFaction): import('./boolean').boolean;
    /**
     * @return An array of all factions that are enemies of this faction
     */
    getEnemyFactions(): import('./IFaction').IFaction[];
    /**
     * Adds a faction as an enemy of this faction.
     * @param faction The faction to add as an enemy
     */
    addEnemyFaction(faction: import('./IFaction').IFaction): import('./void').void;
    /**
     * Removes a faction from this faction's enemy list.
     * @param faction The faction to remove
     */
    removeEnemyFaction(faction: import('./IFaction').IFaction): import('./void').void;
    /**
     * Saves the faction data to the server.
     */
    save(): import('./void').void;
    name: String;
    color: import('./int').int;
    attackFactions: Integer[];
    id: import('./int').int;
    neutralPoints: import('./int').int;
    friendlyPoints: import('./int').int;
    defaultPoints: import('./int').int;
    hideFaction: import('./boolean').boolean;
    getsAttacked: import('./boolean').boolean;
    isPassive: import('./boolean').boolean;
}
