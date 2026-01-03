/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface ICloneHandler {

    // Methods
    spawn(x: number, y: number, z: number, tab: number, name: string, world: import('../IWorld').IWorld, ignoreProtection: boolean): import('../entity/IEntity').IEntity;
    spawn(pos: import('../IPos').IPos, tab: number, name: string, world: import('../IWorld').IWorld, ignoreProtection: boolean): import('../entity/IEntity').IEntity;
    spawn(x: number, y: number, z: number, tab: number, name: string, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity;
    spawn(pos: import('../IPos').IPos, tab: number, name: string, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity;
    getTab(tab: number, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity[];
    get(tab: number, name: string, world: import('../IWorld').IWorld): import('../entity/IEntity').IEntity;
    has(tab: number, name: string): boolean;
    set(tab: number, name: string, entity: import('../entity/IEntity').IEntity): void;
    remove(tab: number, name: string): void;

}
