/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IPlayerMailData {

    // Methods
    hasMail(): boolean;
    addMail(mail: import('./data/IPlayerMail').IPlayerMail): void;
    removeMail(mail: import('./data/IPlayerMail').IPlayerMail): void;
    hasMail(mail: import('./data/IPlayerMail').IPlayerMail): boolean;
    getAllMail(): import('./data/IPlayerMail').IPlayerMail[];
    getUnreadMail(): import('./data/IPlayerMail').IPlayerMail[];
    getReadMail(): import('./data/IPlayerMail').IPlayerMail[];
    getMailFrom(sender: string): import('./data/IPlayerMail').IPlayerMail[];

}
