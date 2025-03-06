package kamkeel.npcs.network.packets.data.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

import java.io.IOException;

public final class GuiTeleporterPacket extends AbstractPacket {
    public static final String packetName = "Data|Teleporter";

    public GuiTeleporterPacket() {
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.GUI_TELEPORTER;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        CustomNpcs.proxy.openGui((EntityNPCInterface) null, EnumGuiType.NpcDimensions);
    }
}
