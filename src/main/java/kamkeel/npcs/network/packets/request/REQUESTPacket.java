package kamkeel.npcs.network.packets.request;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcsPermissions;

import java.io.IOException;

public final class REQUESTPacket extends AbstractPacket {
    public static final String packetName = "Request|Name";

    public REQUESTPacket() {
    }


    @Override
    public Enum getType() {
        return EnumRequestPacket.NPCDelete;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @Override
    public CustomNpcsPermissions.Permission getPermission() {
        return null;
    }

    @Override
    public boolean needsNPC() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {


    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {

    }


}
