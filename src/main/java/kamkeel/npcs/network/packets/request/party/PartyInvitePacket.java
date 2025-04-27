package kamkeel.npcs.network.packets.request.party;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.PartyDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.PartyEvent;

import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

public final class PartyInvitePacket extends AbstractPacket {
    public static final String packetName = "Request|PartyInvite";

    private String name;

    public PartyInvitePacket() {
    }

    public PartyInvitePacket(String playername) {
        this.name = playername;
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartyInvite;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, this.name);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        EntityPlayer invitedPlayer = NoppesUtilServer.getPlayerByName(ByteBufUtils.readString(in));
        if (invitedPlayer != null) {
            PlayerData senderData = PlayerData.get(player);
            PlayerData invitedData = PlayerData.get(invitedPlayer);
            if (senderData.partyUUID != null && invitedData.partyUUID == null) {
                Party party = PartyController.Instance().getParty(senderData.partyUUID);
                if (!party.getIsLocked()) {
                    PartyEvent.PartyInviteEvent partyEvent = new PartyEvent.PartyInviteEvent(party, party.getQuest(), (IPlayer) NpcAPI.Instance().getIEntity(invitedPlayer));
                    EventHooks.onPartyInvite(party, partyEvent);
                    if (!partyEvent.isCancelled()) {
                        invitedData.inviteToParty(party);
                        sendInviteData((EntityPlayerMP) invitedPlayer);
                    }
                }
            }
        }
    }

    public static void sendInviteData(EntityPlayerMP player) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData.partyUUID == null) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            HashSet<UUID> partyInvites = playerData.getPartyInvites();
            for (UUID uuid : partyInvites) {
                Party party = PartyController.Instance().getParty(uuid);
                NBTTagCompound partyCompound = new NBTTagCompound();
                partyCompound.setString("PartyLeader", party.getPartyLeaderName());
                partyCompound.setString("PartyUUID", party.getPartyUUID().toString());
                list.appendTag(partyCompound);
            }
            compound.setTag("PartyInvites", list);
            PartyDataPacket.sendPartyData(player, compound);
        }
    }
}
