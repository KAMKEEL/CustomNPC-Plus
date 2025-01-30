package kamkeel.npcs.network.packets.data.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumDataPacket;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.NoppesUtil;

import java.io.IOException;

public final class GuiWaypointPacket extends AbstractPacket {
    public static final String packetName = "Client|GuiWaypoint";

    private NBTTagCompound compound;

    public GuiWaypointPacket() {}

    public GuiWaypointPacket(NBTTagCompound compound) {
        this.compound = compound;
    }

    @Override
    public Enum getType() {
        return EnumDataPacket.GUI_WAYPOINT;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.DATA_PACKET;
    }

    @Override
    public void sendData(ByteBuf out) throws IOException {
        ByteBufUtils.writeNBT(out, this.compound);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        NBTTagCompound nbt = ByteBufUtils.readNBT(in);
        NoppesUtil.saveWayPointBlock(player, nbt);
    }
}
