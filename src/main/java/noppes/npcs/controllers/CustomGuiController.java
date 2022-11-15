package noppes.npcs.controllers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.scripted.event.CustomGuiEvent;
import noppes.npcs.scripted.gui.ScriptGui;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.overlay.ScriptOverlay;
import noppes.npcs.scripted.NpcAPI;

import java.io.IOException;

public class CustomGuiController {
    public CustomGuiController() {
    }

    public static void openGui(IPlayer player, ScriptGui gui) {
        EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player.getMCEntity();
        entityPlayerMP.openGui(CustomNpcs.instance, EnumGuiType.CustomGui.ordinal(), player.getWorld().getMCWorld(), gui.getSlots().size(), 0, 0);
        ((ContainerCustomGui)((EntityPlayerMP)player.getMCEntity()).openContainer).setGui(gui, (EntityPlayer)player.getMCEntity());
        Server.sendData((EntityPlayerMP)player.getMCEntity(), EnumPacketClient.GUI_DATA, new Object[]{gui.toNBT()});
    }

    public static boolean updateGui(IPlayer player, ScriptGui gui) {
        if (((EntityPlayerMP)player.getMCEntity()).openContainer instanceof ContainerCustomGui) {
            Server.sendData((EntityPlayerMP)player.getMCEntity(), EnumPacketClient.GUI_DATA, new Object[]{gui.toNBT()});
            return true;
        } else {
            return false;
        }
    }

    public static void openOverlay(IPlayer player, ScriptOverlay gui) {
        Server.sendData((EntityPlayerMP)player.getMCEntity(), EnumPacketClient.SCRIPT_OVERLAY_DATA, new Object[]{gui.toNBT()});
    }

    public static boolean updateOverlay(IPlayer player, ScriptOverlay gui) {
        Server.sendData((EntityPlayerMP)player.getMCEntity(), EnumPacketClient.SCRIPT_OVERLAY_DATA, new Object[]{gui.toNBT()});
        return true;
    }

    static boolean checkGui(CustomGuiEvent event) {
        EntityPlayer player = (EntityPlayer) event.player.getMCEntity();
        if (!(player.openContainer instanceof ContainerCustomGui)) {
            return false;
        } else {
            return ((ContainerCustomGui)player.openContainer).customGui.getID() == event.gui.getID();
        }
    }

    public static IItemStack[] getSlotContents(EntityPlayer player) {
        IItemStack[] slotContents = new IItemStack[0];
        if (player.openContainer instanceof ContainerCustomGui) {
            ContainerCustomGui container = (ContainerCustomGui)player.openContainer;
            slotContents = new IItemStack[container.guiInventory.getSizeInventory()];

            for(int i = 0; i < container.guiInventory.getSizeInventory(); ++i) {
                slotContents[i] = NpcAPI.Instance().getIItemStack(container.guiInventory.getStackInSlot(i));
            }
        }

        return slotContents;
    }

    public static void onButton(CustomGuiEvent.ButtonEvent event) {
        EntityPlayer player = (EntityPlayer) event.player.getMCEntity();
        if (checkGui(event) && getOpenGui(player).getScriptHandler() != null) {
            getOpenGui(player).getScriptHandler().callScript(EnumScriptType.CUSTOM_GUI_BUTTON, event);
        }

        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onSlotChange(CustomGuiEvent.SlotEvent event) {
        EntityPlayer player = (EntityPlayer) event.player.getMCEntity();
        if (checkGui(event) && getOpenGui(player).getScriptHandler() != null) {
            getOpenGui(player).getScriptHandler().callScript(EnumScriptType.CUSTOM_GUI_SLOT, event);
        }

        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onSlotClick(CustomGuiEvent.SlotClickEvent event) {
        EntityPlayer player = (EntityPlayer) event.player.getMCEntity();
        if(checkGui(event) && getOpenGui(player).getScriptHandler()!=null) {
            (getOpenGui(player).getScriptHandler()).callScript(EnumScriptType.CUSTOM_GUI_SLOT_CLICKED, event);
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onCustomGuiUnfocused(CustomGuiEvent.UnfocusedEvent event) {
        EntityPlayer player = (EntityPlayer) event.player.getMCEntity();
        if (checkGui(event) && getOpenGui(player).getScriptHandler() != null) {
            getOpenGui(player).getScriptHandler().callScript(EnumScriptType.CUSTOM_GUI_TEXTFIELD, event);
        }

        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScrollClick(CustomGuiEvent.ScrollEvent event) {
        EntityPlayer player = (EntityPlayer) event.player.getMCEntity();
        if (checkGui(event) && getOpenGui(player).getScriptHandler() != null) {
            getOpenGui(player).getScriptHandler().callScript(EnumScriptType.CUSTOM_GUI_SCROLL, event);
        }

        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onClose(CustomGuiEvent.CloseEvent event) {
        if (ScriptController.Instance.playerScripts != null) {
            ScriptController.Instance.playerScripts.callScript(EnumScriptType.CUSTOM_GUI_CLOSED, event);
        }

        NpcAPI.EVENT_BUS.post(event);
    }

    public static ScriptGui getOpenGui(EntityPlayer player) {
        return player.openContainer instanceof ContainerCustomGui ? ((ContainerCustomGui)player.openContainer).customGui : null;
    }

    public static String[] readScrollSelection(ByteBuf buffer) {
        try {
            NBTTagList list = Server.readNBT(buffer).getTagList("selection", 8);
            String[] selection = new String[list.tagCount()];

            for(int i = 0; i < list.tagCount(); ++i) {
                selection[i] = list.getStringTagAt(i);
            }

            return selection;
        } catch (IOException var4) {
            var4.printStackTrace();
            return null;
        }
    }
}
