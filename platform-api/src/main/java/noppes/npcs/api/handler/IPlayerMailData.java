package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IPlayerMail;

/**
 * Manages a player's mail inbox, including sending, removing, and querying mail.
 */
public interface IPlayerMailData {

    /** @return true if the player has any mail. */
    boolean hasMail();

    /**
     * Adds mail to the player's inbox.
     *
     * @param mail the mail to add.
     */
    void addMail(IPlayerMail mail);

    /**
     * Removes specific mail from the player's inbox.
     *
     * @param mail the mail to remove.
     */
    void removeMail(IPlayerMail mail);

    /**
     * Checks if the player has the specified mail.
     *
     * @param mail the mail to check.
     * @return true if the mail exists in the inbox.
     */
    boolean hasMail(IPlayerMail mail);

    /** @return all mail in the player's inbox. */
    IPlayerMail[] getAllMail();

    /** @return all unread mail in the player's inbox. */
    IPlayerMail[] getUnreadMail();

    /** @return all read mail in the player's inbox. */
    IPlayerMail[] getReadMail();

    /**
     * Returns all mail from the given sender.
     *
     * @param sender the sender name.
     * @return an array of matching mail.
     */
    IPlayerMail[] getMailFrom(String sender);
}
