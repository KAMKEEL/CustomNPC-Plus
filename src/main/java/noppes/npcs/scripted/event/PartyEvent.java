package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.IPartyEvent;
import noppes.npcs.api.handler.data.IParty;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.constants.EnumScriptType;

public class PartyEvent extends CustomNPCsEvent implements IPartyEvent {
    public final IQuest quest;
    public final IParty party;

    public PartyEvent(IParty party, IQuest quest) {
        this.quest = quest;
        this.party = party;
    }

    public IParty getParty() {
        return party;
    }

    public IQuest getQuest() {
        return quest;
    }

    public String getHookName() {
        return EnumScriptType.PARTY_EVENT.function;
    }

    public static class PartyQuestCompletedEvent extends PartyEvent implements IPartyEvent.PartyQuestCompletedEvent {
        public PartyQuestCompletedEvent(IParty party, IQuest quest) {
            super(party, quest);
        }

        public String getHookName() {
            return EnumScriptType.PARTY_QUEST_COMPLETED.function;
        }
    }

    @Cancelable
    public static class PartyQuestSetEvent extends PartyEvent implements IPartyEvent.PartyQuestSetEvent {
        public PartyQuestSetEvent(IParty party, IQuest quest) {
            super(party, quest);
        }

        public String getHookName() {
            return EnumScriptType.PARTY_QUEST_SET.function;
        }
    }

    @Cancelable
    public static class PartyQuestTurnedInEvent extends PartyEvent implements IPartyEvent.PartyQuestTurnedInEvent {
        public PartyQuestTurnedInEvent(IParty party, IQuest quest) {
            super(party, quest);
        }

        public String getHookName() {
            return EnumScriptType.PARTY_QUEST_TURNED_IN.function;
        }
    }

    @Cancelable
    public static class PartyInviteEvent extends PartyEvent implements IPartyEvent.PartyInviteEvent {
        public IPlayer player;

        public PartyInviteEvent(IParty party, IQuest quest, IPlayer player) {
            super(party, quest);
            this.player = player;
        }

        public String getHookName() {
            return EnumScriptType.PARTY_INVITE.function;
        }

        @Override
        public IPlayer getPlayer() {
            return player;
        }

        @Override
        public String getPlayerName() {
            return player.getName();
        }
    }

    @Cancelable
    public static class PartyKickEvent extends PartyEvent implements IPartyEvent.PartyKickEvent {
        public IPlayer player;

        public PartyKickEvent(IParty party, IQuest quest, IPlayer player) {
            super(party, quest);
            this.player = player;
        }

        public String getHookName() {
            return EnumScriptType.PARTY_KICK.function;
        }

        @Override
        public IPlayer getPlayer() {
            return player;
        }

        @Override
        public String getPlayerName() {
            return player.getName();
        }
    }

    public static class PartyLeaveEvent extends PartyEvent implements IPartyEvent.PartyLeaveEvent {
        public IPlayer player;

        public PartyLeaveEvent(IParty party, IQuest quest, IPlayer player) {
            super(party, quest);
            this.player = player;
        }

        public String getHookName() {
            return EnumScriptType.PARTY_LEAVE.function;
        }

        @Override
        public IPlayer getPlayer() {
            return player;
        }

        @Override
        public String getPlayerName() {
            return player.getName();
        }
    }

    public static class PartyDisbandEvent extends PartyEvent implements IPartyEvent.PartyDisbandEvent {
        public PartyDisbandEvent(IParty party, IQuest quest) {
            super(party, quest);
        }

        public String getHookName() {
            return EnumScriptType.PARTY_DISBAND.function;
        }
    }

}
