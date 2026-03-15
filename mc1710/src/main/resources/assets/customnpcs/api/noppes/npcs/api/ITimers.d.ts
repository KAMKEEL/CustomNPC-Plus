/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * @javaFqn noppes.npcs.api.ITimers
 */
export interface ITimers {
    timerIds(): import('./int').int[];
    /**
     * Adds a timer to the NPC or player with the given attributes. If this function is used and there is already a
     * timer with the given ID, an exception will be thrown.
     *
     * @param id     The id of the timer.
     * @param ticks  The amount of ticks before the timer ends.
     * @param repeat Whether this timer repeats when it reaches its maximum amount of ticks.
     */
    start(id: import('./int').int, ticks: import('./int').int, repeat: import('./boolean').boolean): import('./void').void;
    /**
     * Adds a timer to the NPC or player with the given attributes. No exception will be thrown if there is already a
     * timer with the given ID when using this function.
     *
     * @param id     The id of the timer.
     * @param ticks  The amount of ticks before the timer ends.
     * @param repeat Whether this timer repeats when it reaches its maximum amount of ticks.
     */
    forceStart(id: import('./int').int, ticks: import('./int').int, repeat: import('./boolean').boolean): import('./void').void;
    /**
     *
     * @param id the timer ID
     * @return True if the set of timers contains a timer with the given id, false otherwise.
     */
    has(id: import('./int').int): import('./boolean').boolean;
    /**
     * Stops the timer with the given id
     *
     * @param id the timer ID
     * @return True if there was a timer with the given id.
     */
    stop(id: import('./int').int): import('./boolean').boolean;
    /**
     * Resets the ticks elapsed in the timer with the given id to 0.
     * @param id the timer ID
     */
    reset(id: import('./int').int): import('./void').void;
    /**
     * Removes all timers.
     */
    clear(): import('./void').void;
    /**
     * @param id the timer ID
     * @return The amount of ticks elapsed in the timer.
     */
    ticks(id: import('./int').int): import('./int').int;
    /**
     * Sets the amount of ticks elapsed in the timer with the given id to a different value.
     *
     * @param id the timer ID
     * @param ticks The new ticks elapsed by the timer.
     */
    setTicks(id: import('./int').int, ticks: import('./int').int): import('./void').void;
    /**
     * @param id the timer ID
     * @return The maximum amount of ticks the timer with the given id runs for before it stops.
     */
    maxTicks(id: import('./int').int): import('./int').int;
    /**
     * Sets the maximum amount of ticks the timer with the given id can run for before it stops.
     * @param id the timer ID
     * @param maxTicks the maximum tick count
     */
    setMaxTicks(id: import('./int').int, maxTicks: import('./int').int): import('./void').void;
    /**
     * @param id the timer ID
     * @return True if the timer with the given id repeats, false otherwise.
     */
    repeats(id: import('./int').int): import('./boolean').boolean;
    /**
     * Sets whether the timer with the given id repeats or not.
     * @param id the timer ID
     * @param repeat whether the timer repeats
     */
    setRepeats(id: import('./int').int, repeat: import('./boolean').boolean): import('./void').void;
    /**
     *
     * @return The amount of timers in the set of timers.
     */
    size(): import('./int').int;
}
