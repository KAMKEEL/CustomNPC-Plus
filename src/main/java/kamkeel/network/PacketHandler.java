package kamkeel.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import kamkeel.network.enums.EnumPacketType;
import kamkeel.util.ByteBufUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import noppes.npcs.CustomNpcs;

import java.util.*;

public final class PacketHandler {
    public static PacketHandler Instance;

    public Map<EnumPacketType, FMLEventChannel> channels = new Hashtable<>();

    public final static PacketChannel INFO_PACKET = new PacketChannel("CNPC+|Info", EnumPacketType.INFO);
    public final static PacketChannel DATA_PACKET = new PacketChannel("CNPC+|Data", EnumPacketType.DATA);
    public final static PacketChannel CLIENT_PACKET = new PacketChannel("CNPC+|Client", EnumPacketType.CLIENT);
    public final static PacketChannel LARGE_PACKET = new PacketChannel("CNPC+|Large", EnumPacketType.LARGE);

    public final static List<PacketChannel> packetChannels = new ArrayList<>();

    public PacketHandler() {
        // Register Channels
        packetChannels.add(INFO_PACKET);
        packetChannels.add(DATA_PACKET);
        packetChannels.add(CLIENT_PACKET);
        this.registerChannels();

        // Register Info Packets

        // Register Data Packets

        // Register Client Packets

        // Register Packets
        // map.put(PingPacket.packetName, new PingPacket());
    }

    public void registerChannels() {
        FMLEventChannel eventChannel;
        for (PacketChannel channel : packetChannels) {
            eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channel.getChannelName());
            eventChannel.register(this);
            channels.put(channel.getChannelType(), eventChannel);
        }
    }

    public PacketChannel getPacketChannel(EnumPacketType type){
        PacketChannel packetChannel;
        switch (type){
            case DATA:
                packetChannel = DATA_PACKET;
                break;
            case INFO:
                packetChannel = INFO_PACKET;
                break;
            case LARGE:
                packetChannel = LARGE_PACKET;
                break;
            default:
                packetChannel = CLIENT_PACKET;
                break;
        }
        return packetChannel;
    }
}
