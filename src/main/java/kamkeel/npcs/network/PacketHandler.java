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
import noppes.npcs.LogWriter;

import java.util.*;
import java.util.function.Consumer;

public final class PacketHandler {
    public static PacketHandler Instance;

    public Map<EnumPacketType, FMLEventChannel> channels = new Hashtable<>();

    public final static PacketChannel INFO_PACKET = new PacketChannel("CNPC+|Info", EnumPacketType.INFO);
    public final static PacketChannel DATA_PACKET = new PacketChannel("CNPC+|Data", EnumPacketType.DATA);
    public final static PacketChannel CLIENT_PACKET = new PacketChannel("CNPC+|Client", EnumPacketType.CLIENT);
    public final static PacketChannel LARGE_PACKET = new PacketChannel("CNPC+|Large", EnumPacketType.LARGE);

    private final static List<PacketChannel> packetChannels = new ArrayList<>();

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

        // Log channel registration
        System.out.println("PacketHandler initialized and channels registered.");
    }

    public void registerChannels() {
        FMLEventChannel eventChannel;
        for (PacketChannel channel : packetChannels) {
            eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channel.getChannelName());
            eventChannel.register(this);
            channels.put(channel.getChannelType(), eventChannel);
            System.out.println("Registered channel: " + channel.getChannelName());
        }
    }

    public PacketChannel getPacketChannel(EnumPacketType type){
        return packetChannels.stream()
            .filter(channel -> channel.getChannelType() == type)
            .findFirst()
            .orElse(null);
    }

    public FMLEventChannel getEventChannel(AbstractPacket abstractPacket){
        PacketChannel packetChannel = getPacketChannel(abstractPacket.getChannel().getChannelType());
        if (packetChannel == null) {
            return null;
        }
        return channels.get(packetChannel.getChannelType());
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        handlePacket(event.packet, ((NetHandlerPlayServer) event.handler).playerEntity);
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        handlePacket(event.packet, CustomNpcs.proxy.getPlayer());
    }

    private void handlePacket(FMLProxyPacket packet, EntityPlayer player) {
        ByteBuf buf = packet.payload();
        try {
            int packetTypeOrdinal = buf.readInt();
            EnumPacketType packetType = EnumPacketType.values()[packetTypeOrdinal];
            PacketChannel packetChannel = getPacketChannel(packetType);
            if (packetChannel == null) {
                LogWriter.error("Error: Packet channel is null for packet type: " + packetType);
                return;
            }
            int packetId = buf.readInt();
            AbstractPacket abstractPacket = packetChannel.packets.get(packetId);
            if (abstractPacket == null) {
                LogWriter.error("Error: Abstract packet is null for packet ID: " + packetId);
                return;
            }
            abstractPacket.receiveData(buf, player);
        } catch (IndexOutOfBoundsException e) {
            LogWriter.error("Error: IndexOutOfBoundsException in handlePacket: " + e.getMessage());
        } catch (Exception e) {
            LogWriter.error("Error: Exception in handlePacket: " + e.getMessage());
        }
    }

    public void sendPacket(AbstractPacket packet, Consumer<FMLProxyPacket> sendFunction) {
        FMLProxyPacket proxyPacket = packet.generatePacket();
        if (proxyPacket != null) {
            sendFunction.accept(proxyPacket);
        }
    }

    public void sendToPlayer(AbstractPacket packet, EntityPlayerMP player) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendPacket(packet, proxyPacket -> eventChannel.sendTo(proxyPacket, player));
    }

    public void sendToServer(AbstractPacket packet) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendPacket(packet, eventChannel::sendToServer);
    }

    public void sendToAll(AbstractPacket packet) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendPacket(packet, eventChannel::sendToAll);
    }

    public void sendToAllAround(AbstractPacket packet, NetworkRegistry.TargetPoint point) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendPacket(packet, proxyPacket -> eventChannel.sendToAllAround(proxyPacket, point));
    }

    public void sendToDimension(AbstractPacket packet, int dimensionId) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendPacket(packet, proxyPacket -> eventChannel.sendToDimension(proxyPacket, dimensionId));
    }
}
