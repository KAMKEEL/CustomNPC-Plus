/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Manages a player's mail inbox, including sending, removing, and querying mail.
  * @javaFqn noppes.npcs.api.handler.IPlayerMailData
*/
export interface IPlayerMailData {
    /** @return true if the player has any mail. */
    hasMail(): import('./boolean').boolean;
    /**
     * Adds mail to the player's inbox.
     *
     * @param mail the mail to add.
     */
    addMail(mail: import('./data/IPlayerMail').IPlayerMail): import('./void').void;
    /**
     * Removes specific mail from the player's inbox.
     *
     * @param mail the mail to remove.
     */
    removeMail(mail: import('./data/IPlayerMail').IPlayerMail): import('./void').void;
    /**
     * Checks if the player has the specified mail.
     *
     * @param mail the mail to check.
     * @return true if the mail exists in the inbox.
     */
    hasMail(mail: import('./data/IPlayerMail').IPlayerMail): import('./boolean').boolean;
    /** @return all mail in the player's inbox. */
    getAllMail(): import('./data/IPlayerMail').IPlayerMail[];
    /** @return all unread mail in the player's inbox. */
    getUnreadMail(): import('./data/IPlayerMail').IPlayerMail[];
    /** @return all read mail in the player's inbox. */
    getReadMail(): import('./data/IPlayerMail').IPlayerMail[];
    /**
     * Returns all mail from the given sender.
     *
     * @param sender the sender name.
     * @return an array of matching mail.
     */
    getMailFrom(sender: String): import('./data/IPlayerMail').IPlayerMail[];
    playermail: import('./PlayerMail').PlayerMail[];
}
