/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a sound that can be played at a position or attached to an entity,
 * with configurable volume, pitch, repeat behavior, and position.
  * @javaFqn noppes.npcs.api.handler.data.ISound
*/
export interface ISound {
    /**
     * Attaches this sound to an entity. The sound follows the entity.
     *
     * @param entity the entity to attach to, or null to detach.
     */
    setEntity(entity: import('../../entity/IEntity').IEntity): import('./void').void;
    /** @return the entity this sound is attached to, or null. */
    getEntity(): import('../../entity/IEntity').IEntity;
    /** @param repeat true to loop this sound. */
    setRepeat(repeat: import('./boolean').boolean): import('./void').void;
    /** @return true if this sound loops. */
    repeats(): import('./boolean').boolean;
    /** @param delay the delay in ticks between repeats. */
    setRepeatDelay(delay: import('./int').int): import('./void').void;
    /** @return the delay in ticks between repeats. */
    getRepeatDelay(): import('./int').int;
    /** @param volume the playback volume (1.0 = normal). */
    setVolume(volume: import('./float').float): import('./void').void;
    /** @return the playback volume. */
    getVolume(): import('./float').float;
    /** @param pitch the playback pitch (1.0 = normal). */
    setPitch(pitch: import('./float').float): import('./void').void;
    /** @return the playback pitch. */
    getPitch(): import('./float').float;
    /**
     * Sets the position where this sound plays.
     *
     * @param pos the position.
     */
    setPosition(pos: import('../../IPos').IPos): import('./void').void;
    /**
     * Sets the position where this sound plays.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     */
    setPosition(x: import('./float').float, y: import('./float').float, z: import('./float').float): import('./void').void;
    /** @return the x coordinate of the sound position. */
    getX(): import('./float').float;
    /** @return the y coordinate of the sound position. */
    getY(): import('./float').float;
    /** @return the z coordinate of the sound position. */
    getZ(): import('./float').float;
    sourceEntity: import('../../entity/IEntity').IEntity;
    directory: String;
    volume: import('./float').float;
    pitch: import('./float').float;
    xPosF: import('./float').float;
    yPosF: import('./float').float;
    zPosF: import('./float').float;
    repeat: import('./boolean').boolean;
    repeatDelay: import('./int').int;
}
