package noppes.npcs.api.handler.data;

/**
 * Represents an active custom effect instance on a player.
 * <p>
 * Note: performEffect(IPlayer) is omitted from the platform API
 * because IPlayer depends on MC entity types.
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

    /** @return the effect source index (0: CNPC+, 1: DBC Addon). */
    int getIndex();

    /** @param index the effect source index (0: CNPC+, 1: DBC Addon). */
    void setIndex(int index);
}
