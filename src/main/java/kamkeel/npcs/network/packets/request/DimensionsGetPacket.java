package kamkeel.npcs.network.packets.request;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.AbstractPacket;
import kamkeel.npcs.network.PacketChannel;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.large.ScrollDataPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.constants.EnumScrollData;

import java.io.IOException;
import java.util.HashMap;

public final class DimensionsGetPacket extends AbstractPacket {
    public static final String packetName = "Request|DimensionsGet";

    public DimensionsGetPacket() {
    }

    @Override
    public Enum getType() {
        return EnumRequestPacket.DimensionsGet;
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
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (int id : DimensionManager.getStaticDimensionIDs()) {
            WorldProvider provider = DimensionManager.createProviderFor(id);
            map.put(provider.getDimensionName(), id);
        }
        ScrollDataPacket.sendScrollData((EntityPlayerMP) player, map, EnumScrollData.OPTIONAL);
    }


}
