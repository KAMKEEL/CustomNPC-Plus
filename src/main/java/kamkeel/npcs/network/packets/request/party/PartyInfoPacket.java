package kamkeel.npcs.network.packets.request.party;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.PartyDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;

import java.io.IOException;
import java.util.Vector;

import static kamkeel.npcs.network.packets.request.party.PartyInvitePacket.sendInviteData;

public final class PartyInfoPacket extends AbstractPacket {
    public static final String packetName = "Request|PartyInfo";


    boolean newParty;

    public PartyInfoPacket() {
    }

    public PartyInfoPacket(boolean party) {
        this.newParty = party;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartyInfo;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeBoolean(this.newParty);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        boolean isNew = in.readBoolean();
        if (!ConfigMain.PartiesEnabled)
            return;

        if (isNew) {
            Party party = PartyController.Instance().createParty();
            party.addPlayer(player);
            party.setLeader(player);
        }
        sendPartyData((EntityPlayerMP) player);
    }

    public static void sendPartyData(EntityPlayerMP player) {
        PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);
        if (playerData.partyUUID != null) {
            Party party = PartyController.Instance().getParty(playerData.partyUUID);
            NBTTagCompound compound = party.writeToNBT();
            if (party.getQuest() != null) {
                Quest quest = (Quest) party.getQuest();
                compound.setString("QuestName", quest.getCategory().getName() + ":" + quest.getName());
                Vector<String> vector = quest.questInterface.getPartyQuestLogStatus(party);
                NBTTagList list = new NBTTagList();
                for (String s : vector) {
                    list.appendTag(new NBTTagString(s));
                }
                compound.setTag("QuestProgress", list);
                if (quest.completion == EnumQuestCompletion.Npc && quest.questInterface.isPartyCompleted(party)) {
                    compound.setString("QuestCompleteWith", quest.completerNpc);
                }
            }
            PartyDataPacket.sendPartyData(player, compound);
        } else {
            sendInviteData(player);
        }
    }
}
