package noppes.npcs.api.handler.data;

import noppes.npcs.api.item.IItemStack;

/**
 * Represents a mail message sent to a player, containing pages of text,
 * sender info, optional quest, and item attachments.
 */
public interface IPlayerMail {

    /**
     * Sets the page text content.
     *
     * @param pages an array of strings, one per page.
     */
    void setPageText(String[] pages);

    /** @return the page text as an array of strings. */
    String[] getPageText();

    /** @return the number of pages. */
    int getPageCount();

    /** @param sender the sender name. */
    void setSender(String sender);

    /** @return the sender name. */
    String getSender();

    /** @param subject the mail subject line. */
    void setSubject(String subject);

    /** @return the mail subject line. */
    String getSubject();

    /** @return milliseconds since the mail was sent. */
    long getTimePast();

    /** @return the timestamp when the mail was sent. */
    long getTimeSent();

    /** @return true if this mail has an associated quest. */
    boolean hasQuest();

    /** @return the quest attached to this mail, or null if none. */
    IQuest getQuest();

    /** @return the item attachments. */
    IItemStack[] getItems();

    /** @param items the item attachments. */
    void setItems(IItemStack[] items);
}
