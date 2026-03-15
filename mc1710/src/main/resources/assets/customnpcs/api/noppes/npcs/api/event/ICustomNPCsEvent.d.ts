/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Base interface for all CustomNPC+ script events.
  * @javaFqn noppes.npcs.api.event.ICustomNPCsEvent
*/
export interface ICustomNPCsEvent {
    /** @return the script hook name that triggered this event. */
    getHookName(): String;
    readonly API: import('./AbstractNpcAPI').AbstractNpcAPI;
}

export namespace ICustomNPCsEvent {
    
    /**
     * @hookName onCNPCNaturalSpawn
          * @javaFqn noppes.npcs.api.event.ICustomNPCsEvent.CNPCNaturalSpawnEvent
*/
    export interface CNPCNaturalSpawnEvent extends ICustomNPCsEvent {
        getNaturalSpawn(): import('../handler/data/INaturalSpawn').INaturalSpawn;
        setAttemptPosition(attemptPosition: import('../IPos').IPos): import('./void').void;
        getAttemptPosition(): import('../IPos').IPos;
        animalSpawnPassed(): import('./boolean').boolean;
        monsterSpawnPassed(): import('./boolean').boolean;
        liquidSpawnPassed(): import('./boolean').boolean;
        airSpawnPassed(): import('./boolean').boolean;
        readonly entity: import('./IEntityLivingBase').IEntityLivingBase;
        readonly naturalSpawn: import('../handler/data/INaturalSpawn').INaturalSpawn;
        attemptPosition: import('../IPos').IPos;
        readonly animalSpawnPassed: import('./boolean').boolean;
        readonly monsterSpawnPassed: import('./boolean').boolean;
        readonly liquidSpawnPassed: import('./boolean').boolean;
        readonly airSpawnPassed: import('./boolean').boolean;
    }
    /**
     * @javaFqn noppes.npcs.api.event.ICustomNPCsEvent.ScriptedCommandEvent
     */
    export interface ScriptedCommandEvent extends ICustomNPCsEvent {
        getSenderWorld(): import('../IWorld').IWorld;
        getSenderPosition(): import('../IPos').IPos;
        getSenderName(): String;
        setReplyMessage(message: String): import('./void').void;
        getId(): String;
        getArgs(): String[];
        world: import('../IWorld').IWorld;
        pos: import('../IPos').IPos;
        senderName: String;
        id: String;
        args: String[];
        replyMessage: String;
    }
}
