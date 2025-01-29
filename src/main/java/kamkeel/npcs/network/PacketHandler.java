package kamkeel.npcs.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.enums.EnumPacketType;
import kamkeel.npcs.network.packets.large.LargeScrollDataPacket;
import kamkeel.npcs.network.packets.large.LargeScrollGroupPacket;
import kamkeel.npcs.network.packets.large.LargeScrollListPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import noppes.npcs.CustomNpcs;

import java.io.IOException;
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
        packetChannels.add(LARGE_PACKET);
        this.registerChannels();

        // Large Packets
        LARGE_PACKET.registerPacket(new LargeScrollGroupPacket());
        LARGE_PACKET.registerPacket(new LargeScrollDataPacket());
        LARGE_PACKET.registerPacket(new LargeScrollListPacket());
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

    public FMLEventChannel getEventChannel(AbstractPacket abstractPacket){
        return channels.get(getPacketChannel(abstractPacket.getChannel().getChannelType()));
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        try {
            handlePacket(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        try {
            handlePacket(event.packet, CustomNpcs.proxy.getPlayer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePacket(FMLProxyPacket packet, EntityPlayer player) {
        ByteBuf buf = packet.payload();
        try {
            int channelTypeOrdinal = buf.readInt();
            int packetTypeOrdinal = buf.readInt();

            EnumPacketType channelType = EnumPacketType.values()[channelTypeOrdinal];
            PacketChannel packetChannel = getPacketChannel(channelType);

            AbstractPacket abstractPacket = packetChannel.packets.get(packetTypeOrdinal);
            if (abstractPacket != null) {
                try {
                    abstractPacket.receiveData(buf, player);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToPlayer(AbstractPacket packet, EntityPlayerMP player) {
        try {
            FMLProxyPacket proxyPacket = packet.generatePacket();
            getEventChannel(packet).sendTo(proxyPacket, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(AbstractPacket packet) {
        try {
            FMLProxyPacket proxyPacket = packet.generatePacket();
            getEventChannel(packet).sendToServer(proxyPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToAll(AbstractPacket packet) {
        try {
            FMLProxyPacket proxyPacket = packet.generatePacket();
            getEventChannel(packet).sendToAll(proxyPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToAllAround(AbstractPacket packet, NetworkRegistry.TargetPoint point) {
        try {
            FMLProxyPacket proxyPacket = packet.generatePacket();
            getEventChannel(packet).sendToAllAround(proxyPacket, point);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToDimension(AbstractPacket packet, int dimensionId) {
        try {
            FMLProxyPacket proxyPacket = packet.generatePacket();
            getEventChannel(packet).sendToDimension(proxyPacket, dimensionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
