package kamkeel.npcs.network;

import kamkeel.npcs.network.packets.large.LargeScrollGroupPacket;
import kamkeel.npcs.network.packets.large.LargeScrollDataPacket;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Map;

public class KamUtilServer {

    public static void sendScrollData(EntityPlayerMP player, Map<String, Integer> map) {
        LargeScrollDataPacket packet = new LargeScrollDataPacket(map);
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    public static void sendScrollGroup(EntityPlayerMP player, Map<String,Integer> map){
        LargeScrollGroupPacket packet = new LargeScrollGroupPacket(map);
        PacketHandler.Instance.sendToPlayer(packet, player);
    }
}
