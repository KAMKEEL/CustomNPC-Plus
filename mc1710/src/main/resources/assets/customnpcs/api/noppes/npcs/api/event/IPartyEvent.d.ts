/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired during party operations such as quest progress, invitations, kicks, and disbanding.
  * @javaFqn noppes.npcs.api.event.IPartyEvent
*/
export interface IPartyEvent {
    /** @return the party involved in this event. */
    getParty(): import('../handler/data/IParty').IParty;
    /** @return the quest associated with this party event. */
    getQuest(): import('../handler/data/IQuest').IQuest;
    readonly quest: import('../handler/data/IQuest').IQuest;
    readonly party: import('../handler/data/IParty').IParty;
}

export namespace IPartyEvent {
    
    
    /**
     * Fired when a party quest is completed.
     * @hookName partyQuestCompleted
          * @javaFqn noppes.npcs.api.event.IPartyEvent.PartyQuestCompletedEvent
*/
    export interface PartyQuestCompletedEvent extends IPartyEvent {
    }
    /**
     * Fired when a quest is assigned to the party. Cancelable.
     * @hookName partyQuestSet
          * @javaFqn noppes.npcs.api.event.IPartyEvent.PartyQuestSetEvent
*/
    export interface PartyQuestSetEvent extends IPartyEvent {
    }
    /**
     * Fired when a party quest is turned in. Cancelable.
     * @hookName partyQuestTurnedIn
          * @javaFqn noppes.npcs.api.event.IPartyEvent.PartyQuestTurnedInEvent
*/
    export interface PartyQuestTurnedInEvent extends IPartyEvent {
    }
    /**
     * Fired when a player is invited to the party. Cancelable.
     * @hookName partyInvite
          * @javaFqn noppes.npcs.api.event.IPartyEvent.PartyInviteEvent
*/
    export interface PartyInviteEvent extends IPartyEvent {
        /** @return the invited player. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        /** @return the invited player's name. */
        getPlayerName(): String;
        player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * Fired when a player is kicked from the party. Cancelable.
     * @hookName partyKick
          * @javaFqn noppes.npcs.api.event.IPartyEvent.PartyKickEvent
*/
    export interface PartyKickEvent extends IPartyEvent {
        /** @return the kicked player. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        /** @return the kicked player's name. */
        getPlayerName(): String;
        player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * Fired when a player voluntarily leaves the party.
     * @hookName partyLeave
          * @javaFqn noppes.npcs.api.event.IPartyEvent.PartyLeaveEvent
*/
    export interface PartyLeaveEvent extends IPartyEvent {
        /** @return the leaving player. */
        getPlayer(): import('../entity/IPlayer').IPlayer;
        /** @return the leaving player's name. */
        getPlayerName(): String;
        player: import('../entity/IPlayer').IPlayer;
    }
    /**
     * Fired when the party is disbanded.
     * @hookName partyDisband
          * @javaFqn noppes.npcs.api.event.IPartyEvent.PartyDisbandEvent
*/
    export interface PartyDisbandEvent extends IPartyEvent {
    }
}
