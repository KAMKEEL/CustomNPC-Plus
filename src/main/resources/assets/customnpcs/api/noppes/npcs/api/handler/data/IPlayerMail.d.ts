/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IPlayerMail {

    // Methods
    setPageText(pages: string[]): void;
    getPageText(): string[];
    getPageCount(): number;
    setSender(sender: string): void;
    getSender(): string;
    setSubject(subject: string): void;
    getSubject(): string;
    getTimePast(): number;
    getTimeSent(): number;
    hasQuest(): boolean;
    getQuest(): import('./IQuest').IQuest;
    getItems(): import('../../item/IItemStack').IItemStack[];
    setItems(items: import('../../item/IItemStack').IItemStack[]): void;

}
