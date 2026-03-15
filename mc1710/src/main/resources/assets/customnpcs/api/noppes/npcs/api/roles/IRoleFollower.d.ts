/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.roles
 */

/**
 * @javaFqn noppes.npcs.api.roles.IRoleFollower
 */
export interface IRoleFollower extends import('./IRole').IRole {
    /**
     * @param player Player who is set as the owner. If null given everything resets
     * @since 1.7.10c
     */
    setOwner(player: import('../entity/IPlayer').IPlayer): import('./void').void;
    /**
     * @return Returns the followers owner. Returns null if he has no owner or the owner is offline
     * @since 1.7.10c
     */
    getOwner(): import('../entity/IPlayer').IPlayer;
    /**
     * @return Returns whether or not the follower has an owner
     * @since 1.7.10c
     */
    hasOwner(): import('./boolean').boolean;
    /** @return true if the follower is currently following the owner. */
    isFollowing(): import('./boolean').boolean;
    /** @param following true to make the follower follow the owner. */
    setIsFollowing(following: import('./boolean').boolean): import('./void').void;
    /**
     * @return Returns days left
     * @since 1.7.10c
     */
    getDaysLeft(): import('./int').int;
    /**
     * @param days The days you want to add to the days remaining
     * @since 1.7.10c
     */
    addDaysLeft(days: import('./int').int): import('./void').void;
    /**
     * @return Returns whether or not the follower is set to infinite days
     * @since 1.7.10c
     */
    getInfiniteDays(): import('./boolean').boolean;
    /**
     * @param infinite Sets whether the days hired are infinite
     * @since 1.7.10c
     */
    setInfiniteDays(infinite: import('./boolean').boolean): import('./void').void;
    /**
     * @return Return whether the gui is disabled
     * @since 1.7.10c
     */
    getGuiDisabled(): import('./boolean').boolean;
    /**
     * @param disabled Set the gui to be disabled or not
     * @since 1.7.10c
     */
    setGuiDisabled(disabled: import('./boolean').boolean): import('./void').void;
    /**
     * @param index  Index of Rate [0 - 2]
     * @param amount Amount hired for
     */
    setRate(index: import('./int').int, amount: import('./int').int): import('./void').void;
    /**
     * @param index Index of Rate [0 - 2]
     * @return amount fired for
     */
    getRate(index: import('./int').int): import('./int').int;
    /**
     * @param dialogHire New dialog hire string
     */
    setDialogHire(dialogHire: String): import('./void').void;
    /**
     * @return dialog hire string
     */
    getDialogHire(): String;
    /**
     * @param dialogFarewell New dialog farewell string
     */
    setDialogFarewell(dialogFarewell: String): import('./void').void;
    /**
     * @return dialog farewell string
     */
    getDialogFarewell(): String;
}
