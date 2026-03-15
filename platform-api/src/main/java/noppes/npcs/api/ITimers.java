package noppes.npcs.api;

public interface ITimers {

    int[] timerIds();

    /**
     * Adds a timer to the NPC or player with the given attributes. If this function is used and there is already a
     * timer with the given ID, an exception will be thrown.
     *
     * @param id     The id of the timer.
     * @param ticks  The amount of ticks before the timer ends.
     * @param repeat Whether this timer repeats when it reaches its maximum amount of ticks.
     */
    void start(int id, int ticks, boolean repeat);

    /**
     * Adds a timer to the NPC or player with the given attributes. No exception will be thrown if there is already a
     * timer with the given ID when using this function.
     *
     * @param id     The id of the timer.
     * @param ticks  The amount of ticks before the timer ends.
     * @param repeat Whether this timer repeats when it reaches its maximum amount of ticks.
     */
    void forceStart(int id, int ticks, boolean repeat);

    /**
     *
     * @param id the timer ID
     * @return True if the set of timers contains a timer with the given id, false otherwise.
     */
    boolean has(int id);

    /**
     * Stops the timer with the given id
     *
     * @param id the timer ID
     * @return True if there was a timer with the given id.
     */
    boolean stop(int id);

    /**
     * Resets the ticks elapsed in the timer with the given id to 0.
     * @param id the timer ID
     */
    void reset(int id);

    /**
     * Removes all timers.
     */
    void clear();

    /**
     * @param id the timer ID
     * @return The amount of ticks elapsed in the timer.
     */
    int ticks(int id);

    /**
     * Sets the amount of ticks elapsed in the timer with the given id to a different value.
     *
     * @param id the timer ID
     * @param ticks The new ticks elapsed by the timer.
     */
    void setTicks(int id, int ticks);

    /**
     * @param id the timer ID
     * @return The maximum amount of ticks the timer with the given id runs for before it stops.
     */
    int maxTicks(int id);

    /**
     * Sets the maximum amount of ticks the timer with the given id can run for before it stops.
     * @param id the timer ID
     * @param maxTicks the maximum tick count
     */
    void setMaxTicks(int id, int maxTicks);

    /**
     * @param id the timer ID
     * @return True if the timer with the given id repeats, false otherwise.
     */
    boolean repeats(int id);

    /**
     * Sets whether the timer with the given id repeats or not.
     * @param id the timer ID
     * @param repeat whether the timer repeats
     */
    void setRepeats(int id, boolean repeat);

    /**
     *
     * @return The amount of timers in the set of timers.
     */
    int size();
}
