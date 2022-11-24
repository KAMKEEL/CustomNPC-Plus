package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.api.event.ICustomGuiEvent;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IPlayer;

public class CustomGuiEvent extends Event implements ICustomGuiEvent {
    public final IPlayer player;
    public final ICustomGui gui;

    public CustomGuiEvent(IPlayer player, ICustomGui gui) {
        this.player = player;
        this.gui = gui;
    }

    public IPlayer getPlayer() {
        return this.player;
    }

    public ICustomGui getGui() {
        return gui;
    }

    public int getId() {
        return -1;
    }

    public static class ButtonEvent extends CustomGuiEvent implements ICustomGuiEvent.ButtonEvent {
        public final int buttonId;

        public ButtonEvent(IPlayer player, ICustomGui gui, int buttonId) {
            super(player, gui);
            this.buttonId = buttonId;
        }

        public int getId() {
            return this.buttonId;
        }
    }

    public static class UnfocusedEvent extends CustomGuiEvent implements ICustomGuiEvent.UnfocusedEvent {
        public final int textfieldId;

        public UnfocusedEvent(IPlayer player, ICustomGui gui, int textfieldId) {
            super(player, gui);
            this.textfieldId = textfieldId;
        }

        public int getId() {
            return this.textfieldId;
        }
    }

    public static class CloseEvent extends CustomGuiEvent implements ICustomGuiEvent.CloseEvent {
        public CloseEvent(IPlayer player, ICustomGui gui) {
            super(player, gui);
        }
    }

    public static class ScrollEvent extends CustomGuiEvent implements ICustomGuiEvent.ScrollEvent {
        public final int scrollId;
        public final String[] selection;
        public final boolean doubleClick;
        public final int scrollIndex;

        public ScrollEvent(IPlayer player, ICustomGui gui, int scrollId, int scrollIndex, String[] selection, boolean doubleClick) {
            super(player, gui);
            this.scrollId = scrollId;
            this.selection = selection;
            this.doubleClick = doubleClick;
            this.scrollIndex = scrollIndex;
        }

        public int getId() {
            return this.scrollId;
        }

        public String[] getSelection() {
            return selection;
        }

        public boolean doubleClick() {
            return doubleClick;
        }

        public int getScrollIndex() {
            return scrollIndex;
        }
    }

    public static class SlotEvent extends CustomGuiEvent implements ICustomGuiEvent.SlotEvent {
        public final int slotId;
        public final IItemStack stack;

        public SlotEvent(IPlayer player, ICustomGui gui, int slotId, IItemStack stack) {
            super(player, gui);
            this.slotId = slotId;
            this.stack = stack;
        }

        public int getId() {
            return this.slotId;
        }

        public IItemStack getStack() {
            return stack;
        }
    }

    @Cancelable
    public static class SlotClickEvent extends CustomGuiEvent implements ICustomGuiEvent.SlotClickEvent {
        public final int slotId;
        public final IItemStack stack;
        public final int dragType;
        /**
         *
         */
        public final int clickType;

        public SlotClickEvent(IPlayer player, ICustomGui gui, int slotId, IItemStack stack, int dragType, int clickType) {
            super(player,gui);
            this.slotId = slotId;
            this.stack = stack;
            this.dragType = dragType;
            this.clickType = clickType;
        }

        public int getId() {
            return this.slotId;
        }

        public IItemStack getStack() {
            return stack;
        }

        public int getDragType() {
            return dragType;
        }
    }
}
