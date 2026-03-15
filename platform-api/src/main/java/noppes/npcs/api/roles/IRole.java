package noppes.npcs.api.roles;

/**
 * Base interface for all NPC roles (Trader, Follower, Bank, Mailman, Transporter).
 */
public interface IRole {

    /** @return the NPC this role is assigned to. */
    Object getNpc();

    /**
     * @return the role type ordinal.
     *         0: None, 1: Trader, 2: Follower, 3: Bank, 4: Transporter, 5: Mailman.
     */
    int getType();
}
