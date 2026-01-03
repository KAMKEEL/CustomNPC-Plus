/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IFaction {

    // Methods
    getId(): number;
    getName(): string;
    setName(name: string): void;
    setDefaultPoints(var1: number): void;
    getDefaultPoints(): number;
    setFriendlyPoints(p: number): void;
    getFriendlyPoints(): number;
    setNeutralPoints(p: number): void;
    getNeutralPoints(): number;
    setColor(c: number): void;
    getColor(): number;
    playerStatus(player: import('../../entity/IPlayer').IPlayer): number;
    isAggressiveToNpc(npc: import('../../entity/ICustomNpc').ICustomNpc): boolean;
    getIsHidden(): boolean;
    setIsHidden(hidden: boolean): void;
    isPassive(): boolean;
    setIsPassive(passive: boolean): void;
    attackedByMobs(): boolean;
    setAttackedByMobs(attacked: boolean): void;
    isEnemyFaction(faction: import('./IFaction').IFaction): boolean;
    getEnemyFactions(): import('./IFaction').IFaction[];
    addEnemyFaction(faction: import('./IFaction').IFaction): void;
    removeEnemyFaction(faction: import('./IFaction').IFaction): void;
    save(): void;

}
