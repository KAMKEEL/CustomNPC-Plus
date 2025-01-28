package kamkeel.network.packets.client;

import io.netty.buffer.ByteBuf;
import kamkeel.network.AbstractPacket;
import kamkeel.network.PacketChannel;
import kamkeel.network.PacketHandler;
import kamkeel.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.client.gui.player.GuiBook;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class OpenBookPacket extends AbstractPacket {
    public static final String packetName = "Client|OpenBook";

    public OpenBookPacket() {}

    @Override
    public Enum getType() {
        return EnumClientPacket.OPEN_BOOK;
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
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        NBTTagCompound nbt = Server.readNBT(in);
        ItemStack book = ItemStack.loadItemStackFromNBT(nbt);
        NoppesUtil.openGUI(player, new GuiBook(player, book, x, y, z));
    }
}
