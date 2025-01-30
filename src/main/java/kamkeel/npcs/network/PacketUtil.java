package kamkeel.npcs.network;

import kamkeel.npcs.network.packets.client.ChatAlertPacket;
import kamkeel.npcs.network.packets.client.AchievementPacket;
import kamkeel.npcs.network.packets.client.QuestCompletionPacket;
import kamkeel.npcs.network.packets.client.ScrollSelectedPacket;
import kamkeel.npcs.network.packets.client.gui.GuiClosePacket;
import kamkeel.npcs.network.packets.client.large.*;
import kamkeel.npcs.network.packets.client.gui.GuiErrorPacket;
import kamkeel.npcs.network.packets.client.gui.GuiOpenPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumGuiType;

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

    public static void openGUI(EntityPlayerMP playerMP, EnumGuiType type, int x, int y, int z){
        GuiOpenPacket packet = new GuiOpenPacket(type, x, y, z);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    public static void closeGUI(EntityPlayerMP playerMP, int closeCode, NBTTagCompound compound){
        GuiClosePacket packet = new GuiClosePacket(closeCode, compound);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    public static void errorGUI(EntityPlayerMP playerMP, int closeCode, NBTTagCompound compound){
        GuiErrorPacket packet = new GuiErrorPacket(closeCode, compound);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    public static void sendGuiData(EntityPlayerMP playerMP, NBTTagCompound compound){
        LargeGuiDataPacket packet = new LargeGuiDataPacket(compound);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    public static void sendPartyData(EntityPlayerMP playerMP, NBTTagCompound compound){
        LargePartyDataPacket packet = new LargePartyDataPacket(compound);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }


    public static void sendQuestComplete(EntityPlayerMP playerMP, NBTTagCompound compound){
        QuestCompletionPacket packet = new QuestCompletionPacket(compound);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }

    public static void setSelectedList(EntityPlayerMP playerMP, String item){
        ScrollSelectedPacket packet = new ScrollSelectedPacket(item);
        PacketHandler.Instance.sendToPlayer(packet, playerMP);
    }
}
