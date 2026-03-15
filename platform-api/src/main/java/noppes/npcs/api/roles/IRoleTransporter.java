package noppes.npcs.api.roles;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ITransportLocation;

/**
 * Represents the transporter role for an NPC, allowing players to teleport
 * between unlocked transport locations.
 */
public interface IRoleTransporter extends IRole {

    /** @return the display name of this transporter NPC. */
    String getName();

    /** @return the transport location ID assigned to this NPC. */
    int getTransportId();

    /**
     * Unlocks the given transport location for the player.
     *
     * @param player   the player.
     * @param location the transport location to unlock.
     */
    void unlock(IPlayer player, ITransportLocation location);

    /** @return the transport location assigned to this NPC, or null if none. */
    ITransportLocation getTransport();

    /** @return true if this NPC has a transport location assigned. */
    boolean hasTransport();

    /**
     * Assigns a transport location to this NPC.
     *
     * @param location the transport location.
     */
    void setTransport(ITransportLocation location);
}
