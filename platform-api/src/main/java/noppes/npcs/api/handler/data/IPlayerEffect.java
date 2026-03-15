package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IPlayer;

/**
 * Represents an active custom effect instance on a player.
 */
public interface IPlayerEffect {

    /** Removes this effect from the player. */
    void kill();

    /** @return the custom effect ID. */
    int getId();

    /** @return the remaining duration in seconds (-100 for infinite). */
    int getDuration();

    /** @param duration the remaining duration in seconds (-100 for infinite). */
    void setDuration(int duration);

    /** @return the effect level/amplifier. */
    byte getLevel();

    /** @param level the effect level/amplifier. */
    void setLevel(byte level);

    /** @return the display name of the effect. */
    String getName();

    /**
     * Applies this effect's tick logic to the given player.
     *
     * @param player the player to apply the effect to.
     */
    default void performEffect(IPlayer player) {
    }

    /** @return the effect source index (0: CNPC+, 1: DBC Addon). */
    int getIndex();

    /** @param index the effect source index (0: CNPC+, 1: DBC Addon). */
    void setIndex(int index);
}
