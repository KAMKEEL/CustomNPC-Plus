package kamkeel.npcs.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.enums.EnumPacketType;
import kamkeel.npcs.network.packets.client.*;
import kamkeel.npcs.network.packets.client.gui.*;
import kamkeel.npcs.network.packets.client.large.LargeScrollDataPacket;
import kamkeel.npcs.network.packets.client.large.LargeScrollGroupPacket;
import kamkeel.npcs.network.packets.client.large.LargeScrollListPacket;
import kamkeel.npcs.network.packets.client.large.LargeSyncPacket;
import kamkeel.npcs.network.packets.client.npc.DeleteNpcPacket;
import kamkeel.npcs.network.packets.client.npc.EditNpcPacket;
import kamkeel.npcs.network.packets.client.script.ScriptedParticlePacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

import java.util.*;

public final class PacketHandler {
    public static PacketHandler Instance;

    // Channels
    public Map<EnumPacketType, FMLEventChannel> channels = new Hashtable<>();

    public static final PacketChannel INFO_PACKET   = new PacketChannel("CNPC+|Info",   EnumPacketType.INFO);
    public static final PacketChannel DATA_PACKET   = new PacketChannel("CNPC+|Data",   EnumPacketType.DATA);
    public static final PacketChannel CLIENT_PACKET = new PacketChannel("CNPC+|Client", EnumPacketType.CLIENT);
    public static final PacketChannel LARGE_PACKET  = new PacketChannel("CNPC+|Large",  EnumPacketType.LARGE);

    private static final List<PacketChannel> packetChannels = new ArrayList<>();

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
        LARGE_PACKET.registerPacket(new LargeSyncPacket());

        // Client Packets
        CLIENT_PACKET.registerPacket(new ChatBubblePacket());
        CLIENT_PACKET.registerPacket(new ChatAlertPacket());
        CLIENT_PACKET.registerPacket(new AchievementPacket());
        CLIENT_PACKET.registerPacket(new EditNpcPacket());
        CLIENT_PACKET.registerPacket(new RolePacket());
        CLIENT_PACKET.registerPacket(new GuiTeleporterPacket());
        CLIENT_PACKET.registerPacket(new SoundManagementPacket());
        CLIENT_PACKET.registerPacket(new ParticlePacket());
        CLIENT_PACKET.registerPacket(new ScriptedParticlePacket());
        CLIENT_PACKET.registerPacket(new ConfigCommandPacket());

        // Client | GUI Packets
        CLIENT_PACKET.registerPacket(new GuiClosePacket());
        CLIENT_PACKET.registerPacket(new GuiOpenPacket());
        CLIENT_PACKET.registerPacket(new GuiErrorPacket());
        CLIENT_PACKET.registerPacket(new GuiDataPacket());
        CLIENT_PACKET.registerPacket(new GuiClonerPacket());

        // Client | NPC Packets
        CLIENT_PACKET.registerPacket(new DeleteNpcPacket());
    }

    private void registerChannels() {
        for (PacketChannel channel : packetChannels) {
            FMLEventChannel eventChannel =
                NetworkRegistry.INSTANCE.newEventDrivenChannel(channel.getChannelName());
            eventChannel.register(this);
            channels.put(channel.getChannelType(), eventChannel);
            System.out.println("Registered channel: " + channel.getChannelName());
        }
    }

    public PacketChannel getPacketChannel(EnumPacketType type) {
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
            // First two ints are channelTypeOrdinal and packetTypeOrdinal
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

            // Let the packet parse the rest
            abstractPacket.receiveData(buf, player);

        } catch (IndexOutOfBoundsException e) {
            LogWriter.error("Error: IndexOutOfBoundsException in handlePacket: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LogWriter.error("Error: Exception in handlePacket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends every FMLProxyPacket produced by packet.generatePackets()
     */
    private void sendAllPackets(AbstractPacket packet, SendAction action) {
        // get all generated FMLProxyPacket objects (could be 1 for normal, or many for large)
        List<FMLProxyPacket> proxyPackets = packet.generatePackets();
        if (proxyPackets.isEmpty()) {
            LogWriter.error("Warning: No packets generated for " + packet.getClass().getName());
        }

        for (FMLProxyPacket proxy : proxyPackets) {
            action.send(proxy);
        }
    }

    // ------------------------------------------------------------------------
    // Public API methods for sending

    public void sendToPlayer(AbstractPacket packet, EntityPlayerMP player) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendAllPackets(packet, p -> eventChannel.sendTo(p, player));
    }

    public void sendToServer(AbstractPacket packet) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendAllPackets(packet, eventChannel::sendToServer);
    }

    public void sendToAll(AbstractPacket packet) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendAllPackets(packet, eventChannel::sendToAll);
    }

    public void sendToDimension(AbstractPacket packet, final int dimensionId) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        sendAllPackets(packet, p -> eventChannel.sendToDimension(p, dimensionId));
    }

    public void sendTracking(AbstractPacket packet, final Entity entity) {
        FMLEventChannel eventChannel = getEventChannel(packet);
        if (eventChannel == null) {
            LogWriter.error("Error: Event channel is null for packet: " + packet.getClass().getName());
            return;
        }
        final NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 60);
        sendAllPackets(packet, p -> eventChannel.sendToAllAround(p, point));
    }

    // Simple functional interface to unify the "send" action
    private interface SendAction {
        void send(FMLProxyPacket proxy);
    }
}
