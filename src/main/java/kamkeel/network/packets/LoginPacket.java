package kamkeel.network.packets;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumInfoPacket;
import kamkeel.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

public final class LoginPacket extends AbstractPacket {
    public static final String packetName = "CNPC+|LoginPacket";

    public LoginPacket() {}


    @Override
    public Enum getType() {
        return EnumInfoPacket.LOGIN;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.INFO_PACKET;
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
