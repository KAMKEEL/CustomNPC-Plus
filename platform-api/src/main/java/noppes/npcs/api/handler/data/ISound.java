package noppes.npcs.api.handler.data;

import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.IEntity;

/**
 * Represents a sound that can be played at a position or attached to an entity,
 * with configurable volume, pitch, repeat behavior, and position.
 */
public interface ISound {

    /**
     * Attaches this sound to an entity. The sound follows the entity.
     *
     * @param entity the entity to attach to, or null to detach.
     */
    void setEntity(IEntity entity);

    /** @return the entity this sound is attached to, or null. */
    IEntity getEntity();

    /** @param repeat true to loop this sound. */
    void setRepeat(boolean repeat);

    /** @return true if this sound loops. */
    boolean repeats();

    /** @param delay the delay in ticks between repeats. */
    void setRepeatDelay(int delay);

    /** @return the delay in ticks between repeats. */
    int getRepeatDelay();

    /** @param volume the playback volume (1.0 = normal). */
    void setVolume(float volume);

    /** @return the playback volume. */
    float getVolume();

    /** @param pitch the playback pitch (1.0 = normal). */
    void setPitch(float pitch);

    /** @return the playback pitch. */
    float getPitch();

    /**
     * Sets the position where this sound plays.
     *
     * @param pos the position.
     */
    void setPosition(IPos pos);

    /**
     * Sets the position where this sound plays.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     */
    void setPosition(float x, float y, float z);

    /** @return the x coordinate of the sound position. */
    float getX();

    /** @return the y coordinate of the sound position. */
    float getY();

    /** @return the z coordinate of the sound position. */
    float getZ();
}
