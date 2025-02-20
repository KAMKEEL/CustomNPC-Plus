package noppes.npcs.client.gui.test;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IScrollData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class NoppesUtilClientNew {

    /**
     * Handles the incoming large packet with full player data.
     * The packet writes:
     *   - player name (String)
     *   - questCategories, questActive, questFinished (maps)
     *   - dialogCategories, dialogRead (maps)
     *   - transportCategories, transportLocations (maps)
     *   - bankData, factionData (maps)
     *
     * The existing PlayerDataController functions are used on the server side;
     * here we simply update the active SubGuiPlayerDataNew if present.
     */
    public static void handlePlayerData(ByteBuf data, EntityPlayer player) throws IOException {
        String playerName = ByteBufUtils.readString(data);
        Map<String, Integer> questCategories = readMap(data);
        Map<String, Integer> questActive = readMap(data);
        Map<String, Integer> questFinished = readMap(data);
        Map<String, Integer> dialogCategories = readMap(data);
        Map<String, Integer> dialogRead = readMap(data);
        Map<String, Integer> transportCategories = readMap(data);
        Map<String, Integer> transportLocations = readMap(data);
        Map<String, Integer> bankData = readMap(data);
        Map<String, Integer> factionData = readMap(data);


        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if(gui == null)
            return;
        if(gui instanceof GuiNPCInterface && ((GuiNPCInterface)gui).hasSubGui()){
            gui = (GuiScreen) ((GuiNPCInterface)gui).getSubGui();
        }

        if(gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface)gui).hasSubGui()){
            gui = (GuiScreen) ((GuiContainerNPCInterface)gui).getSubGui();
        }
        if (gui instanceof IPlayerDataInfo) {
            IPlayerDataInfo info = (IPlayerDataInfo) gui;
            info.setQuestData(questCategories, questActive, questFinished);
            info.setDialogData(dialogCategories, dialogRead);
            info.setTransportData(transportCategories, transportLocations);
            info.setBankData(bankData);
            info.setFactionData(factionData);
        }
    }

    private static Map<String, Integer> readMap(ByteBuf data) {
        int size = data.readInt();
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = ByteBufUtils.readString(data);
            int value = data.readInt();
            map.put(key, value);
        }
        return map;
    }
}
