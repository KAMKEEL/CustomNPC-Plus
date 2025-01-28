package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.gui.player.GuiNpcMobSpawnerAdd;
import noppes.npcs.NoppesUtil;

import java.io.IOException;

public final class ClonePacket extends AbstractPacket {
    public static final String packetName = "Client|Clone";

    public ClonePacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.CLONE;
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
        NoppesUtil.openGUI(player, new GuiNpcMobSpawnerAdd(nbt));
    }
}
