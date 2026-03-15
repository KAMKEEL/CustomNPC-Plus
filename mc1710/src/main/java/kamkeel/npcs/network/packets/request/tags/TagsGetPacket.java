package kamkeel.npcs.network.packets.request.tags;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.PacketUtil;
import kamkeel.npcs.network.enums.EnumItemPacketType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;

import java.io.IOException;

public final class TagsGetPacket extends AbstractPacket {
    public static final String packetName = "Request|TagsGet";

    public TagsGetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.TagsGet;
    }

    @Override
    public PacketChannel getChannel() {
        return PacketHandler.REQUEST_PACKET;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void sendData(ByteBuf out) throws IOException {
    }

    @Override
    public void receiveData(ByteBuf in, EntityPlayer player) throws IOException {
        if (!PacketUtil.verifyItemPacket(packetName, player, EnumItemPacketType.WAND, EnumItemPacketType.CLONER))
            return;

        NoppesUtilServer.sendTagDataAll((EntityPlayerMP) player);
    }


}
