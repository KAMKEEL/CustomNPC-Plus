package noppes.npcs.controllers;


import java.io.IOException;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.CustomNpcs;

public class CustomGuiController {
    public CustomGuiController() {
    }

    public static void openGui(IPlayer player, ScriptGui gui) {
        IPlayer IPlayer = (IPlayer) player.getMCEntity();
        IPlayer.openGui(CustomNpcs.instance, EnumGuiType.CustomGui.ordinal(), player.getWorld().getMCWorld(), gui.getSlots().size(), 0, 0);
        ((ContainerCustomGui) ((IPlayer) player.getMCEntity()).openContainer).setGui(gui, (IPlayer) player.getMCEntity());
        GuiDataPacket.sendGuiData((IPlayer) player.getMCEntity(), gui.toNBT());
    }

    public static boolean updateGui(IPlayer player, ScriptGui gui) {
        if (((IPlayer) player.getMCEntity()).openContainer instanceof ContainerCustomGui) {
            ((ContainerCustomGui) ((IPlayer) player.getMCEntity()).openContainer).setGui(gui, (IPlayer) player.getMCEntity());
            GuiDataPacket.sendGuiData((IPlayer) player.getMCEntity(), gui.toNBT());
            return true;
        } else {
            return false;
        }
    }

    public static void openOverlay(IPlayer player, ScriptOverlay gui) {
        PacketHandler.Instance.sendToPlayer(new ScriptOverlayDataPacket(gui.toNBT()), (IPlayer) player.getMCEntity());
    }

    public static boolean updateOverlay(IPlayer player, ScriptOverlay gui) {
        PacketHandler.Instance.sendToPlayer(new ScriptOverlayDataPacket(gui.toNBT()), (IPlayer) player.getMCEntity());
        return true;
    }

    static boolean checkGui(CustomGuiEvent event) {
        IPlayer player = (IPlayer) event.player.getMCEntity();
        if (!(player.openContainer instanceof ContainerCustomGui)) {
            return false;
        } else {
            return ((ContainerCustomGui) player.openContainer).customGui.getID() == event.gui.getID();
        }
    }

    public static IItemStack[] getSlotContents(IPlayer player) {
        IItemStack[] slotContents = new IItemStack[0];
        if (player.openContainer instanceof ContainerCustomGui) {
            ContainerCustomGui container = (ContainerCustomGui) player.openContainer;
            slotContents = new IItemStack[container.guiInventory.getSizeInventory()];

            for (int i = 0; i < container.guiInventory.getSizeInventory(); ++i) {
                slotContents[i] = NpcAPI.Instance().getIItemStack(container.guiInventory.getStackInSlot(i));
            }
        }

        return slotContents;
    }

    public static void onButton(CustomGuiEvent.ButtonEvent event) {
        IPlayer player = (IPlayer) event.player.getMCEntity();
        if (player != null) {
            ScriptGui gui = getOpenGui(player);
            if (checkGui(event) && gui != null) {
                gui.getScriptHandler(player).callScript(EnumScriptType.CUSTOM_GUI_BUTTON, event);
            }
        }
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onSlotChange(CustomGuiEvent.SlotEvent event) {
        IPlayer player = (IPlayer) event.player.getMCEntity();
        if (player != null) {
            ScriptGui gui = getOpenGui(player);
            if (checkGui(event) && gui != null) {
                gui.getScriptHandler(player).callScript(EnumScriptType.CUSTOM_GUI_SLOT, event);
            }
        }
        NpcAPI.EVENT_BUS.post(event);
    }

    public static boolean onSlotClick(CustomGuiEvent.SlotClickEvent event) {
        IPlayer player = (IPlayer) event.player.getMCEntity();
        if (player != null) {
            ScriptGui gui = getOpenGui(player);
            if (checkGui(event) && gui != null) {
                gui.getScriptHandler(player).callScript(EnumScriptType.CUSTOM_GUI_SLOT_CLICKED, event);
            }
        }
        return NpcAPI.EVENT_BUS.post(event);
    }

    public static void onCustomGuiUnfocused(CustomGuiEvent.UnfocusedEvent event) {
        IPlayer player = (IPlayer) event.player.getMCEntity();
        if (player != null) {
            ScriptGui gui = getOpenGui(player);
            if (checkGui(event) && gui != null) {
                gui.getScriptHandler(player).callScript(EnumScriptType.CUSTOM_GUI_TEXTFIELD, event);
            }
        }
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onScrollClick(CustomGuiEvent.ScrollEvent event) {
        IPlayer player = (IPlayer) event.player.getMCEntity();
        if (player != null) {
            ScriptGui gui = getOpenGui(player);
            if (checkGui(event) && gui != null) {
                gui.getScriptHandler(player).callScript(EnumScriptType.CUSTOM_GUI_SCROLL, event);
            }
        }
        NpcAPI.EVENT_BUS.post(event);
    }

    public static void onClose(CustomGuiEvent.CloseEvent event) {
        IPlayer player = (IPlayer) event.player.getMCEntity();
        if (player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
            handler.callScript(EnumScriptType.CUSTOM_GUI_CLOSED, event);
        }
        NpcAPI.EVENT_BUS.post(event);
    }

    public static ScriptGui getOpenGui(IPlayer player) {
        return player.openContainer instanceof ContainerCustomGui ? ((ContainerCustomGui) player.openContainer).customGui : null;
    }

    public static String[] readScrollSelection(ByteBuf buffer) {
        try {
            INbtList list = ByteBufUtils.readNBT(buffer).getTagList("selection", 8);
            String[] selection = new String[list.tagCount()];

            for (int i = 0; i < list.tagCount(); ++i) {
                selection[i] = list.getStringTagAt(i);
            }

            return selection;
        } catch (IOException var4) {
            var4.printStackTrace();
            return null;
        }
    }
}
