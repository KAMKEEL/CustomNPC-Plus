/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IPlayerData {

    // Methods
    setCompanion(npc: import('../entity/ICustomNpc').ICustomNpc): void;
    getCompanion(): import('../entity/ICustomNpc').ICustomNpc;
    hasCompanion(): boolean;
    getCompanionID(): number;
    getDialogData(): import('./IPlayerDialogData').IPlayerDialogData;
    getBankData(): import('./IPlayerBankData').IPlayerBankData;
    getQuestData(): import('./IPlayerQuestData').IPlayerQuestData;
    getTransportData(): import('./IPlayerTransportData').IPlayerTransportData;
    getFactionData(): import('./IPlayerFactionData').IPlayerFactionData;
    getItemGiverData(): import('./IPlayerItemGiverData').IPlayerItemGiverData;
    getMailData(): import('./IPlayerMailData').IPlayerMailData;
    save(): void;

}
