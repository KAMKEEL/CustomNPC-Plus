package kamkeel.npcs.network.packets.request.party;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.EventHooks;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.scripted.event.PartyEvent;

import java.io.IOException;

public final class PartySetQuestPacket extends AbstractPacket {
    public static final String packetName = "Request|PartySetQuest";

    private int questID;

    public PartySetQuestPacket() {
    }

    public PartySetQuestPacket(int questID) {
        this.questID = questID;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartySetQuest;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(this.questID);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        PlayerData playerData = PlayerData.get(player);
        if (playerData.partyUUID != null) {
            Party party = PartyController.Instance().getParty(playerData.partyUUID);
            if (party != null) {
                if (party.getLeaderUUID().equals(player.getUniqueID())) {
                    int questID = in.readInt();
                    party.setQuest(null);
                    if (questID != -1) {
                        Quest foundQuest = QuestController.Instance.quests.get(questID);
                        if (foundQuest != null) {
                            if (foundQuest.partyOptions.allowParty) {
                                if (party.validateQuest(questID, true)) {
                                    PartyEvent.PartyQuestSetEvent partyEvent = new PartyEvent.PartyQuestSetEvent(party, foundQuest);
                                    EventHooks.onPartyQuestSet(party, partyEvent);
                                    if (!partyEvent.isCancelled()) {
                                        if (playerData.questData.hasActiveQuest(questID)) {
                                            QuestData questdata = new QuestData(foundQuest);
                                            playerData.questData.activeQuests.put(questID, questdata);
                                        }
                                        party.setQuest(foundQuest);
                                        PartyController.Instance().sendQuestChat(party, "party.setChat", " ", foundQuest.title);
                                    }
                                }
                            }
                        }
                    }
                    PartyController.Instance().pingPartyUpdate(party);
                }
            }
        }
    }
}
