package kamkeel.npcs.platform.entity;

/**
 * Platform-independent player abstraction.
 * Extends {@link IPlatformLiving} with player-specific operations.
 */
public interface IPlatformPlayer extends IPlatformLiving {

    /**
     * @return the player's username
     */
    String getName();

    /**
     * @return the player's UUID as a string
     */
    @Override
    String getUniqueID();

    /**
     * @return true if the player is a server operator
     */
    boolean isOp();

    /**
     * Send a chat message to this player.
     *
     * @param text plain text message
     */
    void sendMessage(String text);

    /**
     * @return the item currently held in the player's main hand, or null if empty
     */
    IPlatformStack getHeldItem();
}
