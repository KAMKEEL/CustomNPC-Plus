/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a mail message sent to a player, containing pages of text,
 * sender info, optional quest, and item attachments.
  * @javaFqn noppes.npcs.api.handler.data.IPlayerMail
*/
export interface IPlayerMail {
    /**
     * Sets the page text content.
     *
     * @param pages an array of strings, one per page.
     */
    setPageText(pages: String[]): import('./void').void;
    /** @return the page text as an array of strings. */
    getPageText(): String[];
    /** @return the number of pages. */
    getPageCount(): import('./int').int;
    /** @param sender the sender name. */
    setSender(sender: String): import('./void').void;
    /** @return the sender name. */
    getSender(): String;
    /** @param subject the mail subject line. */
    setSubject(subject: String): import('./void').void;
    /** @return the mail subject line. */
    getSubject(): String;
    /** @return milliseconds since the mail was sent. */
    getTimePast(): import('./long').long;
    /** @return the timestamp when the mail was sent. */
    getTimeSent(): import('./long').long;
    /** @return true if this mail has an associated quest. */
    hasQuest(): import('./boolean').boolean;
    /** @return the quest attached to this mail, or null if none. */
    getQuest(): import('./IQuest').IQuest;
    /** @return the item attachments. */
    getItems(): import('../../item/IItemStack').IItemStack[];
    /** @param items the item attachments. */
    setItems(items: import('../../item/IItemStack').IItemStack[]): import('./void').void;
    subject: String;
    sender: String;
    message: import('./NBTTagCompound').NBTTagCompound;
    time: import('./long').long;
    beenRead: import('./boolean').boolean;
    questId: import('./int').int;
    questTitle: String;
    items: import('./ItemStack').ItemStack[];
    timePast: import('./long').long;
}
