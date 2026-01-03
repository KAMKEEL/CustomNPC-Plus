/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IPartyEvent {

    // Methods
    getParty(): import('../handler/data/IParty').IParty;
    getQuest(): import('../handler/data/IQuest').IQuest;

    // Nested interfaces
    interface PartyQuestCompletedEvent extends IPartyEvent {
    }
    interface PartyQuestSetEvent extends IPartyEvent {
    }
    interface PartyQuestTurnedInEvent extends IPartyEvent {
    }
    interface PartyInviteEvent extends IPartyEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getPlayerName(): string;
    }
    interface PartyKickEvent extends IPartyEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getPlayerName(): string;
    }
    interface PartyLeaveEvent extends IPartyEvent {
        getPlayer(): import('../entity/IPlayer').IPlayer;
        getPlayerName(): string;
    }
    interface PartyDisbandEvent extends IPartyEvent {
    }

}
