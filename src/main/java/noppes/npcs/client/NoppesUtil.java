package noppes.npcs.client;

import io.netty.buffer.ByteBuf;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.GuiRequestPacket;
import kamkeel.npcs.network.packets.request.IsGuiOpenInform;
import kamkeel.npcs.util.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.player.GuiDialogInteract;
import noppes.npcs.client.gui.player.GuiQuestCompletion;
import noppes.npcs.client.gui.player.modern.GuiModernDialogInteract;
import noppes.npcs.client.gui.player.modern.GuiModernQuestDialog;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IPlayerDataInfo;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.config.ConfigExperimental;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumScrollData;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.MagicData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.SkinOverlay;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.Sys;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class NoppesUtil {

    public static void requestOpenGUI(EnumGuiType gui) {
        requestOpenGUI(gui, 0, 0, 0);
    }

    public static void requestOpenGUI(EnumGuiType gui, int i, int j, int k) {
        PacketClient.sendClient(new GuiRequestPacket(gui.ordinal(), i, j, k));
    }

    public static void updateSkinOverlayData(EntityPlayer player, NBTTagCompound compound) {
        HashMap<Integer, SkinOverlay> skinOverlays = new HashMap<>();
        HashMap<Integer, SkinOverlay> oldOverlays = new HashMap<>();
        NBTTagList skinOverlayList = compound.getTagList("SkinOverlayData", 10);
        if (ClientCacheHandler.skinOverlays.containsKey(player.getUniqueID())) {
            oldOverlays = ClientCacheHandler.skinOverlays.get(player.getUniqueID());
        }

        for (int i = 0; i < skinOverlayList.tagCount(); i++) {
            int tagID = skinOverlayList.getCompoundTagAt(i).getInteger("SkinOverlayID");
            SkinOverlay overlay = (SkinOverlay) SkinOverlay.overlayFromNBT(skinOverlayList.getCompoundTagAt(i));
            if (oldOverlays.containsKey(tagID)) {
                overlay.ticks = oldOverlays.get(tagID).ticks;
            }
            skinOverlays.put(tagID, overlay);
        }
        ClientCacheHandler.skinOverlays.put(player.getUniqueID(), skinOverlays);
    }

    public static void clickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
    }

    private static EntityNPCInterface lastNpc;

    public static EntityNPCInterface getLastNpc() {
        return lastNpc;
    }

    public static void setLastNpc(EntityNPCInterface npc) {
        lastNpc = npc;
    }

    public static void openGUI(EntityPlayer player, Object guiscreen) {
        CustomNpcs.proxy.openGui(player, guiscreen);
    }

    public static void openFolder(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            // Log an error or simply return if there's nothing to open
            return;
        }

        String s = dir.getAbsolutePath();

        if (Util.getOSType() == Util.EnumOS.OSX) {
            try {
                //logger.info(s);
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", s});
                return;
            } catch (IOException ioexception1) {
                //logger.error("Couldn\'t open file", ioexception1);
            }
        } else if (Util.getOSType() == Util.EnumOS.WINDOWS) {
            String s1 = String.format("cmd.exe /C start \"Open file\" \"%s\"", new Object[]{s});

            try {
                Runtime.getRuntime().exec(s1);
                return;
            } catch (IOException ioexception) {
                //logger.error("Couldn\'t open file", ioexception);
            }
        }

        boolean flag = false;

        try {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
            oclass.getMethod("browse", new Class[]{URI.class}).invoke(object, new Object[]{dir.toURI()});
        } catch (Throwable throwable) {
            //logger.error("Couldn\'t open link", throwable);
            flag = true;
        }

        if (flag) {
            //logger.info("Opening via system class!");
            Sys.openURL("file://" + s);
        }
    }

    public static void setScrollList(ByteBuf buffer) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui())
            gui = ((GuiNPCInterface) gui).getSubGui();
        if (gui == null || !(gui instanceof IScrollData))
            return;
        Vector<String> data = new Vector<String>();
        String line;

        EnumScrollData dataType = EnumScrollData.values()[buffer.readInt()];

        try {
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                data.add(ByteBufUtils.readString(buffer));
            }
        } catch (Exception e) {

        }

        ((IScrollData) gui).setData(data, null, dataType);
    }

    private static HashMap<String, Integer> data = new HashMap<String, Integer>();

    public static void setScrollData(ByteBuf buffer) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui == null)
            return;

        EnumScrollData dataType = EnumScrollData.values()[buffer.readInt()];
        try {
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                int id = buffer.readInt();
                String name = ByteBufUtils.readString(buffer);
                data.put(name, id);
            }
        } catch (Exception e) {
        }
        if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui()) {
            gui = (GuiScreen) ((GuiNPCInterface) gui).getSubGui();
        }
        if (gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface) gui).hasSubGui()) {
            gui = (GuiScreen) ((GuiContainerNPCInterface) gui).getSubGui();
        }
        if (gui instanceof IScrollData)
            ((IScrollData) gui).setData(new Vector<String>(data.keySet()), data, dataType);
        data = new HashMap<String, Integer>();
    }

    public static void guiQuestCompletion(EntityPlayer player, NBTTagCompound read) {
        Quest quest = new Quest();
        quest.readNBT(read);
        if (!quest.completeText.isEmpty()) {
            NoppesUtil.openGUI(player, new GuiQuestCompletion(quest));
        }
    }

    public static void openDialog(NBTTagCompound compound, EntityNPCInterface npc, EntityPlayer player) {
        Dialog dialog = new Dialog();
        dialog.readNBT(compound);
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (!(gui instanceof GuiDialogInteract)) {
            if (ConfigExperimental.ModernGuiSystem) {
                if (dialog.hasQuest()) {
                    CustomNpcs.proxy.openGui(player, new GuiModernQuestDialog(npc, dialog.getQuest(), dialog, -2));
                } else {
                    CustomNpcs.proxy.openGui(player, new GuiModernDialogInteract(npc, dialog));
                }
            } else {
                CustomNpcs.proxy.openGui(player, new GuiDialogInteract(npc, dialog));
            }
        } else {
            GuiDialogInteract dia = (GuiDialogInteract) gui;
            dia.appendDialog(dialog);
        }
    }

    public static void saveRedstoneBlock(EntityPlayer player, NBTTagCompound compound) {
        int x = compound.getInteger("x");
        int y = compound.getInteger("y");
        int z = compound.getInteger("z");

        TileEntity tile = player.worldObj.getTileEntity(x, y, z);
        tile.readFromNBT(compound);

        CustomNpcs.proxy.openGui(x, y, z, EnumGuiType.RedstoneBlock, player);
    }

    public static void saveWayPointBlock(EntityPlayer player, NBTTagCompound compound) {
        int x = compound.getInteger("x");
        int y = compound.getInteger("y");
        int z = compound.getInteger("z");

        TileEntity tile = player.worldObj.getTileEntity(x, y, z);
        tile.readFromNBT(compound);

        CustomNpcs.proxy.openGui(x, y, z, EnumGuiType.Waypoint, player);
    }

    public static void isGUIOpen(boolean isGUIOpen) {
        PacketClient.sendClient(new IsGuiOpenInform(isGUIOpen));
    }

    /**
     * Handles the incoming large packet with full player data.
     * The packet writes:
     * - player name (String)
     * - questCategories, questActive, questFinished (maps)
     * - dialogCategories, dialogRead (maps)
     * - transportCategories, transportLocations (maps)
     * - bankData, factionData (maps)
     * <p>
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
        MagicData magicData = new MagicData();
        magicData.readToNBT(ByteBufUtils.readNBT(data));

        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui == null)
            return;
        if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui()) {
            gui = (GuiScreen) ((GuiNPCInterface) gui).getSubGui();
        }

        if (gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface) gui).hasSubGui()) {
            gui = (GuiScreen) ((GuiContainerNPCInterface) gui).getSubGui();
        }
        if (gui instanceof IPlayerDataInfo) {
            IPlayerDataInfo info = (IPlayerDataInfo) gui;
            info.setQuestData(questCategories, questActive, questFinished);
            info.setDialogData(dialogCategories, dialogRead);
            info.setTransportData(transportCategories, transportLocations);
            info.setBankData(bankData);
            info.setFactionData(factionData);
            info.setMagicData(magicData);
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
