package kamkeel.npcs.network.packets.request.party;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.data.Party;

import java.io.IOException;

import static noppes.npcs.PacketHandlerServer.sendPartyData;

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
}
