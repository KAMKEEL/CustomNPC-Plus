/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface ISound {

    // Methods
    setEntity(entity: import('../../entity/IEntity').IEntity): void;
    getEntity(): import('../../entity/IEntity').IEntity;
    setRepeat(repeat: boolean): void;
    repeats(): boolean;
    setRepeatDelay(delay: number): void;
    getRepeatDelay(): number;
    setVolume(volume: number): void;
    getVolume(): number;
    setPitch(pitch: number): void;
    getPitch(): number;
    setPosition(pos: import('../../IPos').IPos): void;
    setPosition(x: number, y: number, z: number): void;
    getX(): number;
    getY(): number;
    getZ(): number;

}
