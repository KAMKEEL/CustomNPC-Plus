/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IAvailability {

    // Methods
    isAvailable(player: import('../../entity/IPlayer').IPlayer): boolean;
    getDaytime(): number;
    setDaytime(daytime: number): void;
    getMinPlayerLevel(): number;
    setMinPlayerLevel(level: number): void;
    getDialog(index: number): number;
    setDialog(index: number, id: number, type: number): void;
    removeDialog(index: number): void;
    getQuest(index: number): number;
    setQuest(index: number, id: number, type: number): void;
    removeQuest(index: number): void;
    setFaction(slot: number, id: number, type: number, stance: number): void;
    removeFaction(slot: number): void;

}
