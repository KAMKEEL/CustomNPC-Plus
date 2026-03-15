package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IParty;
import noppes.npcs.api.handler.data.IQuest;

/**
 * Events fired during party operations such as quest progress, invitations, kicks, and disbanding.
 */
public interface IPartyEvent {

    /** @return the party involved in this event. */
    IParty getParty();

    /** @return the quest associated with this party event. */
    IQuest getQuest();

    /**
     * Fired when a party quest is completed.
     * @hookName partyQuestCompleted
     */
    interface PartyQuestCompletedEvent extends IPartyEvent {
    }

    /**
     * Fired when a quest is assigned to the party. Cancelable.
     * @hookName partyQuestSet
     */
    @Cancelable
    interface PartyQuestSetEvent extends IPartyEvent {
    }

    /**
     * Fired when a party quest is turned in. Cancelable.
     * @hookName partyQuestTurnedIn
     */
    @Cancelable
    interface PartyQuestTurnedInEvent extends IPartyEvent {
    }

    /**
     * Fired when a player is invited to the party. Cancelable.
     * @hookName partyInvite
     */
    @Cancelable
    interface PartyInviteEvent extends IPartyEvent {
        /** @return the invited player. */
        IPlayer getPlayer();

        /** @return the invited player's name. */
        String getPlayerName();
    }

    /**
     * Fired when a player is kicked from the party. Cancelable.
     * @hookName partyKick
     */
    @Cancelable
    interface PartyKickEvent extends IPartyEvent {
        /** @return the kicked player. */
        IPlayer getPlayer();

        /** @return the kicked player's name. */
        String getPlayerName();
    }

    /**
     * Fired when a player voluntarily leaves the party.
     * @hookName partyLeave
     */
    interface PartyLeaveEvent extends IPartyEvent {
        /** @return the leaving player. */
        IPlayer getPlayer();

        /** @return the leaving player's name. */
        String getPlayerName();
    }

    /**
     * Fired when the party is disbanded.
     * @hookName partyDisband
     */
    interface PartyDisbandEvent extends IPartyEvent {
    }
}
