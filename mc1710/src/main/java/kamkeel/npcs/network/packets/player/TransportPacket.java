package kamkeel.npcs.network.packets.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.constants.EnumRoleType;

import java.io.IOException;

public class TransportPacket extends AbstractPacket {
    public static final String packetName = "Player|Transport";

    private String destination;

    public TransportPacket() {

    }

    public TransportPacket(String destination) {
        this.destination = destination;
    }

    @Override
    public Enum getType() {
        return EnumPlayerPacket.Transport;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @Override
    public boolean needsNPC() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeString(out, destination);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (npc.advanced.role != EnumRoleType.Transporter)
            return;
        NoppesUtilPlayer.transport((EntityPlayerMP) player, npc, ByteBufUtils.readString(in));
    }
}
