/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface ICustomNPCsEvent {
    getHookName(): string;
}

export namespace ICustomNPCsEvent {
    export interface CNPCNaturalSpawnEvent extends ICustomNPCsEvent {
        getNaturalSpawn(): import('../handler/data/INaturalSpawn').INaturalSpawn;
        setAttemptPosition(attemptPosition: import('../IPos').IPos): void;
        getAttemptPosition(): import('../IPos').IPos;
        animalSpawnPassed(): boolean;
        monsterSpawnPassed(): boolean;
        liquidSpawnPassed(): boolean;
        airSpawnPassed(): boolean;
    }
    export interface ScriptedCommandEvent extends ICustomNPCsEvent {
        getSenderWorld(): import('../IWorld').IWorld;
        getSenderPosition(): import('../IPos').IPos;
        getSenderName(): string;
        setReplyMessage(message: string): void;
        getId(): string;
        getArgs(): string[];
    }

}
