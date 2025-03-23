package kamkeel.npcs.network.packets.player.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumPlayerPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.constants.EnumGuiType;

import java.io.IOException;

public class GuiPaintbrushPacket extends AbstractPacket {
    public static String packetName = "Player|GuiPaintbrush";

    private int guiIndex, posX, posY, posZ;

    public GuiPaintbrushPacket(int guiIndex, int posX, int posY, int posZ) {
        this.guiIndex = guiIndex;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public GuiPaintbrushPacket() {}

    @Override
    public Enum getType() {
        return EnumPlayerPacket.GuiPaintbrush;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.PLAYER_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(guiIndex);
        out.writeInt(posX);
        out.writeInt(posY);
        out.writeInt(posZ);
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!(player instanceof EntityPlayerMP))
            return;
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.BRUSH))
            return;

        int index = in.readInt();
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        EnumGuiType gui = EnumGuiType.values()[index];
        NoppesUtilServer.sendOpenGui(player, gui, null, x, y, z);
    }
}
