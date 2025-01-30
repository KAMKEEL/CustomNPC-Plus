package kamkeel.npcs.network;

import kamkeel.npcs.network.packets.client.ChatAlertPacket;
import kamkeel.npcs.network.packets.client.AchievementPacket;
import kamkeel.npcs.network.packets.client.large.LargeScrollGroupPacket;
import kamkeel.npcs.network.packets.client.large.LargeScrollDataPacket;
import kamkeel.npcs.network.packets.client.large.LargeScrollListPacket;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;
import java.util.Map;

public class PacketUtil {

    public static void sendScrollData(EntityPlayerMP player, Map<String, Integer> map) {
        LargeScrollDataPacket packet = new LargeScrollDataPacket(map);
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    public static void sendScrollGroup(EntityPlayerMP player, Map<String,Integer> map){
        LargeScrollGroupPacket packet = new LargeScrollGroupPacket(map);
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    public static void sendList(EntityPlayerMP player, List<String> list){
        LargeScrollListPacket packet = new LargeScrollListPacket(list);
        PacketHandler.Instance.sendToPlayer(packet, player);
    }

    public static void sendChatAlert(EntityPlayerMP playerMP, final Object... obs){
        ChatAlertPacket packet = new ChatAlertPacket(obs);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    public static void sendAchievement(EntityPlayerMP playerMP, boolean isParty, String description, String message){
        AchievementPacket packet = new AchievementPacket(isParty, description, message);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }
}
