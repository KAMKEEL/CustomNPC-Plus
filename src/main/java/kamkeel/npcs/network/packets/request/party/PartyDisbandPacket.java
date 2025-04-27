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
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.PlayerData;

import java.io.IOException;

public final class PartyDisbandPacket extends AbstractPacket {
    public static final String packetName = "Request|PartyDisband";


    public PartyDisbandPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.PartyDisband;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        PlayerData playerData = PlayerData.get(player);
        if (playerData.partyUUID != null) {
            PartyController.Instance().disbandParty(playerData.partyUUID);
        }
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("Disband", true);
        PartyDataPacket.sendPartyData((EntityPlayerMP) player, compound);
    }
}
