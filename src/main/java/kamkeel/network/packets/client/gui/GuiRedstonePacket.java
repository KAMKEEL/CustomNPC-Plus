package kamkeel.network.packets.client.gui;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class GuiRedstonePacket extends AbstractPacket {
    public static final String packetName = "Client|GuiRedstone";

    public GuiRedstonePacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.GUI_REDSTONE;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        // TODO: Send Packet
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound nbt = Server.readNBT(in);
        NoppesUtil.saveRedstoneBlock(player, nbt);
    }
}
