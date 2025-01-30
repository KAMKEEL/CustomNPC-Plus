package kamkeel.npcs.network.packets.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
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

    @SideOnly(Side.CLIENT)
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
