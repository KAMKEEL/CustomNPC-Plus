package kamkeel.npcs.network.packets.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumClientPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumGuiType;

import java.io.IOException;

public final class GuiOpenPacket extends AbstractPacket {
    public static final String packetName = "Client|OpenGui";

    private EnumGuiType type;
    private int x;
    private int y;
    private int z;

    public GuiOpenPacket() {}

    public GuiOpenPacket(EnumGuiType type, int x, int y, int z) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Enum getType() {
        return EnumClientPacket.GUI;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.CLIENT_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        out.writeInt(type.ordinal());
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        EnumGuiType gui = EnumGuiType.values()[in.readInt()];
        int x = in.readInt();
        int y = in.readInt();
        int z = in.readInt();
        CustomNpcs.proxy.openGui(null, gui, x, y, z);
    }
}
