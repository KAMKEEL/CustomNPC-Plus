package kamkeel.npcs.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.enums.EnumChannelType;
import kamkeel.npcs.network.enums.EnumRequestPacket;
import kamkeel.npcs.network.packets.data.*;
import kamkeel.npcs.network.packets.data.gui.*;
import kamkeel.npcs.network.packets.data.large.*;
import kamkeel.npcs.network.packets.data.npc.*;
import kamkeel.npcs.network.packets.data.script.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.*;

public final class PacketHandler {
    public static PacketHandler Instance;

    // Channels
    public Map<EnumChannelType, FMLEventChannel> channels = new Hashtable<>();

    // Client to Server
    public static final PacketChannel REQUEST_PACKET = new PacketChannel("CNPC+|Req",   EnumChannelType.REQUEST);

    // Client to Server - Typically information
    public static final PacketChannel PLAYER_PACKET = new PacketChannel("CNPC+|Player",   EnumChannelType.PLAYER);

    // Server to Client
    public static final PacketChannel DATA_PACKET = new PacketChannel("CNPC+|Data", EnumChannelType.DATA);

    private static final List<PacketChannel> packetChannels = new ArrayList<>();

    public PacketHandler() {
        // Register Channels
        packetChannels.add(REQUEST_PACKET);
        packetChannels.add(PLAYER_PACKET);
        packetChannels.add(DATA_PACKET);

        this.registerChannels();
        this.registerDataPackets();
    }

    public void registerDataPackets(){
        // Client Packets
        DATA_PACKET.registerPacket(new AchievementPacket());
        DATA_PACKET.registerPacket(new ChatAlertPacket());
        DATA_PACKET.registerPacket(new ChatBubblePacket());
        DATA_PACKET.registerPacket(new ConfigCommandPacket());
        DATA_PACKET.registerPacket(new DisableMouseInputPacket());
        DATA_PACKET.registerPacket(new MarkDataPacket());
        DATA_PACKET.registerPacket(new OverlayQuestTrackingPacket());
        DATA_PACKET.registerPacket(new ParticlePacket());
        DATA_PACKET.registerPacket(new PlayerUpdateSkinOverlaysPacket());
        DATA_PACKET.registerPacket(new QuestCompletionPacket());
        DATA_PACKET.registerPacket(new ScrollSelectedPacket());
        DATA_PACKET.registerPacket(new SoundManagementPacket());
        DATA_PACKET.registerPacket(new SwingPlayerArmPacket());
        DATA_PACKET.registerPacket(new UpdateAnimationsPacket());
        DATA_PACKET.registerPacket(new VillagerListPacket());

        // Client | GUI Packets
        DATA_PACKET.registerPacket(new GuiClosePacket());
        DATA_PACKET.registerPacket(new GuiOpenPacket());
        DATA_PACKET.registerPacket(new GuiErrorPacket());
        DATA_PACKET.registerPacket(new GuiRedstonePacket());
        DATA_PACKET.registerPacket(new GuiTeleporterPacket());
        DATA_PACKET.registerPacket(new GuiWaypointPacket());
        DATA_PACKET.registerPacket(new IsGuiOpenPacket());
        DATA_PACKET.registerPacket(new GuiOpenBookPacket());

        // Client | NPC Packets
        DATA_PACKET.registerPacket(new DeleteNpcPacket());
        DATA_PACKET.registerPacket(new EditNpcPacket());
        DATA_PACKET.registerPacket(new UpdateNpcPacket());
        DATA_PACKET.registerPacket(new DialogPacket());
        DATA_PACKET.registerPacket(new RolePacket());
        DATA_PACKET.registerPacket(new WeaponNpcPacket());

        // Client | Script Packets
        DATA_PACKET.registerPacket(new ScriptedParticlePacket());
        DATA_PACKET.registerPacket(new ScriptOverlayClosePacket());
        DATA_PACKET.registerPacket(new ScriptOverlayDataPacket());

        // Client | Large Packets
        DATA_PACKET.registerPacket(new ClonerPacket());
        DATA_PACKET.registerPacket(new ScrollGroupPacket());
        DATA_PACKET.registerPacket(new ScrollDataPacket());
        DATA_PACKET.registerPacket(new ScrollListPacket());
        DATA_PACKET.registerPacket(new SyncPacket());
        DATA_PACKET.registerPacket(new GuiDataPacket());
        DATA_PACKET.registerPacket(new PartyDataPacket());
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

    public PacketChannel getPacketChannel(EnumChannelType type) {
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
            EnumChannelType packetType = EnumChannelType.values()[packetTypeOrdinal];

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

            // Check if permission is allowed
            if(abstractPacket.getPermission() != null && !CustomNpcsPermissions.hasPermission(player, abstractPacket.getPermission())){
                return;
            }

            // Check for required NPC
            if(abstractPacket.needsNPC()){
                EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
                if(npc == null)
                    return;

                abstractPacket.setNPC(npc);
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
