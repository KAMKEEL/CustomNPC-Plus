package kamkeel.npcs.network.packets.data;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumInfoPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public final class LoginPacket extends AbstractPacket {
    public static final String packetName = "CNPC+|Login";

    public LoginPacket() {}


    @Override
    public Enum getType() {
        return EnumInfoPacket.LOGIN;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
//        ByteBufUtils.writeUTF8String(out, this.data.player.getCommandSenderName());
//        ByteBufUtils.writeNBT(out,this.data.saveFromNBT(new NBTTagCompound()));
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        String playerName = ByteBufUtils.readUTF8String(in);

    }
}
