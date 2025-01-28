package kamkeel.network;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomNpcs;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public final class PacketHandler {
    public static PacketHandler Instance;

    public Map<String, AbstractPacket> packet_names = new Hashtable<>();
    public Map<String, FMLEventChannel> channels = new Hashtable<>();

    public final static String INFO_PACKET = "CNPC+|Info";
    public final static String DATA_PACKET = "CNPC+|Data";
    public final static String CLIENT_PACKET = "CNPC+|Client";

    public final static List<String> channelNames = new ArrayList<>();

    public PacketHandler() {
        // Register Channels
        channelNames.add(INFO_PACKET);
        channelNames.add(DATA_PACKET);
        channelNames.add(CLIENT_PACKET);
        this.registerChannels();


        // Register Packets
        // map.put(PingPacket.packetName, new PingPacket());
    }

    public void registerChannels() {
        FMLEventChannel eventChannel;
        for (String channel : channelNames) {
            eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channel);
            eventChannel.register(this);
            channels.put(channel, eventChannel);
        }
    }

    @SubscribeEvent
    public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
        try {
            packet_names.get(event.packet.channel()).receiveData(event.packet.payload(), ((NetHandlerPlayServer) event.handler).playerEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
        try {
            packet_names.get(event.packet.channel()).receiveData(event.packet.payload(), CustomNpcs.proxy.getPlayer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToPlayer(FMLProxyPacket packet, EntityPlayerMP player) {
        if (packet != null && CustomNpcs.side()== Side.SERVER) {
            channels.get(packet.channel()).sendTo(packet, player);
        }
    }

    public void sendToServer(FMLProxyPacket packet) {
        if (packet != null) {
            packet.setTarget(Side.SERVER);
            channels.get(packet.channel()).sendToServer(packet);
        }
    }

    public void sendToTrackingPlayers(Entity entity, FMLProxyPacket packet) {
        if (packet != null && CustomNpcs.side() == Side.SERVER) {
            EntityTracker tracker = ((WorldServer) entity.worldObj).getEntityTracker();
            tracker.func_151248_b(entity, packet); // Send packet to tracking players
        }
    }

    public void sendAround(Entity entity, double range, FMLProxyPacket packet) {
        if (packet != null && CustomNpcs.side() == Side.SERVER) {
            channels.get(packet.channel()).sendToAllAround(packet, new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, range));
        }
    }

    public void sendToAll(FMLProxyPacket packet) {
        if (packet != null && CustomNpcs.side() == Side.SERVER) {
            channels.get(packet.channel()).sendToAll(packet);
        }
    }
}
