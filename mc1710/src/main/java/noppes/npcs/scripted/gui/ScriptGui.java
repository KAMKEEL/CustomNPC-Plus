package noppes.npcs.scripted.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.gui.ILabel;
import noppes.npcs.api.gui.ILine;
import noppes.npcs.api.gui.IScroll;
import noppes.npcs.api.gui.ITextField;
import noppes.npcs.api.gui.ITexturedRect;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerDataScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ScriptGui implements ICustomGui {
    int id;
    int width;
    int height;
    int playerInvX;
    int playerInvY;
    boolean pauseGame;
    boolean showPlayerInv;
    boolean closeOnEsc = true;
    String backgroundTexture = "";
    HashMap<Integer, ICustomGuiComponent> components = new HashMap<>();
    List<IItemSlot> slots = new ArrayList();

    public ScriptGui() {
    }

    public ScriptGui(int id, int width, int height, boolean pauseGame) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.pauseGame = pauseGame;
        //this.scriptHandler = ScriptContainer.Current;
    }

    public int getID() {
        return this.id;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public List<ICustomGuiComponent> getComponents() {
        return new ArrayList<>(this.components.values());
    }

    public void clear() {
        this.components.clear();
    }

    public List<IItemSlot> getSlots() {
        return this.slots;
    }

    public PlayerDataScript getScriptHandler(EntityPlayer player) {
        return ScriptController.Instance.getPlayerScripts(player);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setCloseOnEscape(boolean close) {
        this.closeOnEsc = close;
    }

    public boolean doesCloseOnEscape() {
        return this.closeOnEsc;
    }

    public void setDoesPauseGame(boolean pauseGame) {
        this.pauseGame = pauseGame;
    }

    public boolean doesPauseGame() {
        return this.pauseGame;
    }

    public void setBackgroundTexture(String resourceLocation) {
        this.backgroundTexture = resourceLocation;
    }

    public String getBackgroundTexture() {
        return this.backgroundTexture;
    }

    public IButton addButton(int id, String label, int x, int y) {
        ScriptGuiButton component = new ScriptGuiButton(id, label, x, y);
        this.updateComponent(component);
        return (IButton) this.getComponent(id);
    }

    public IButton addButton(int id, String label, int x, int y, int width, int height) {
        ScriptGuiButton component = new ScriptGuiButton(id, label, x, y, width, height);
        this.updateComponent(component);
        return (IButton) this.getComponent(id);
    }

    public IButton addTexturedButton(int id, String label, int x, int y, int width, int height, String texture) {
        ScriptGuiButton component = new ScriptGuiButton(id, label, x, y, width, height, texture);
        this.updateComponent(component);
        return (IButton) this.getComponent(id);
    }

    public IButton addTexturedButton(int id, String label, int x, int y, int width, int height, String texture, int textureX, int textureY) {
        ScriptGuiButton component = new ScriptGuiButton(id, label, x, y, width, height, texture, textureX, textureY);
        this.updateComponent(component);
        return (IButton) this.getComponent(id);
    }

    public ILabel addLabel(int id, String label, int x, int y, int width, int height) {
        ScriptGuiLabel component = new ScriptGuiLabel(id, label, x, y, width, height);
        this.updateComponent(component);
        return (ILabel) this.getComponent(id);
    }

    public ILabel addLabel(int id, String label, int x, int y, int width, int height, int color) {
        ScriptGuiLabel component = new ScriptGuiLabel(id, label, x, y, width, height, color);
        this.updateComponent(component);
        return (ILabel) this.getComponent(id);
    }

    public ITextField addTextField(int id, int x, int y, int width, int height) {
        ScriptGuiTextField component = new ScriptGuiTextField(id, x, y, width, height);
        this.updateComponent(component);
        return (ITextField) this.getComponent(id);
    }

    public ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height) {
        ScriptGuiTexturedRect component = new ScriptGuiTexturedRect(id, texture, x, y, width, height);
        this.updateComponent(component);
        return (ITexturedRect) this.getComponent(id);
    }

    public ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
        ScriptGuiTexturedRect component = new ScriptGuiTexturedRect(id, texture, x, y, width, height, textureX, textureY);
        this.updateComponent(component);
        return (ITexturedRect) this.getComponent(id);
    }

    public IItemSlot addItemSlot(int id, int x, int y) {
        ScriptGuiItemSlot slot = new ScriptGuiItemSlot(id, x, y);
        this.updateComponent(slot);
        return (IItemSlot) this.getComponent(id);
    }

    public IItemSlot addItemSlot(int id, int x, int y, IItemStack stack) {
        ScriptGuiItemSlot slot = new ScriptGuiItemSlot(id, x, y, stack);
        this.updateComponent(slot);
        return (IItemSlot) this.getComponent(id);
    }

    public IItemSlot addItemSlot(int x, int y) {
        return this.addItemSlot(-1, x, y);
    }

    public IItemSlot addItemSlot(int x, int y, IItemStack stack) {
        return this.addItemSlot(-1, x, y, stack);
    }

    public IScroll addScroll(int id, int x, int y, int width, int height, String[] list) {
        ScriptGuiScroll component = new ScriptGuiScroll(id, x, y, width, height, list);
        this.updateComponent(component);
        return (IScroll) this.getComponent(id);
    }

    public ILine addLine(int id, int x1, int y1, int x2, int y2, int color, int thickness) {
        ScriptGuiLine component = new ScriptGuiLine(id, x1, y1, x2, y2, color, thickness);
        this.updateComponent(component);
        return (ILine) this.getComponent(id);
    }

    public ILine addLine(int id, int x1, int y1, int x2, int y2) {
        ScriptGuiLine component = new ScriptGuiLine(id, x1, y1, x2, y2);
        this.updateComponent(component);
        return (ILine) this.getComponent(id);
    }

    public void showPlayerInventory(int x, int y) {
        this.showPlayerInv = true;
        this.playerInvX = x;
        this.playerInvY = y;
    }

    public ICustomGuiComponent getComponent(int componentID) {
        return this.components.get(componentID);
    }

    public void removeComponent(int componentID) {
        if (this.components.containsKey(componentID)) {
            ICustomGuiComponent component = this.getComponent(componentID);
            if (component instanceof IItemSlot) {
                for (IItemSlot slot : this.slots) {
                    if (slot.getID() == componentID) {
                        this.slots.remove(slot);
                        break;
                    }
                }
            }
            this.components.remove(componentID);
        }
    }

    public void updateComponent(ICustomGuiComponent component) {
        if (component != null) {
            this.removeComponent(component.getID());
            this.components.put(component.getID(), component);
            if (component instanceof IItemSlot) {
                this.slots.add((IItemSlot) component);
            }
        }
    }

    public void update(IPlayer player) {
        CustomGuiController.updateGui(player, this);
    }

    public boolean getShowPlayerInv() {
        return this.showPlayerInv;
    }

    public int getPlayerInvX() {
        return this.playerInvX;
    }

    public int getPlayerInvY() {
        return this.playerInvY;
    }

    private int getMaxId() {
        int max = 0;
        for (ICustomGuiComponent component : this.getComponents()) {
            if (component.getID() >= max) {
                max = component.getID();
            }
        }
        return max;
    }

    public ICustomGui fromNBT(NBTTagCompound tag) {
        this.id = tag.getInteger("id");
        this.width = tag.getIntArray("size")[0];
        this.height = tag.getIntArray("size")[1];
        this.pauseGame = tag.getBoolean("pause");
        this.backgroundTexture = tag.getString("bgTexture");
        this.closeOnEsc = tag.getBoolean("closeOnEsc");
        NBTTagList list = tag.getTagList("components", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTBase b = list.getCompoundTagAt(i);
            ScriptGuiComponent component = ScriptGuiComponent.createFromNBT((NBTTagCompound) b);
            if (component != null && !(component instanceof ScriptGuiItemSlot)) {
                components.put(component.getID(), component);
            }
        }
        List<IItemSlot> slots = new ArrayList<>();
        list = tag.getTagList("slots", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound slotNbt = list.getCompoundTagAt(i);
            int id = slotNbt.getInteger("id");
            if (!components.containsKey(id)) {
                components.put(id, ScriptGuiComponent.createFromNBT(slotNbt));
            } else {
                components.get(id).fromNBT(slotNbt);
            }
            ScriptGuiItemSlot slot = (ScriptGuiItemSlot) components.get(id);
            slots.add(slot);
        }
        this.slots = slots;
        this.showPlayerInv = tag.getBoolean("showPlayerInv");
        if (this.showPlayerInv) {
            this.playerInvX = tag.getIntArray("pInvPos")[0];
            this.playerInvY = tag.getIntArray("pInvPos")[1];
        }
        return this;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("id", this.id);
        tag.setIntArray("size", new int[]{this.width, this.height});
        tag.setBoolean("pause", this.pauseGame);
        tag.setString("bgTexture", this.backgroundTexture);
        tag.setBoolean("closeOnEsc", this.closeOnEsc);
        NBTTagList list = new NBTTagList();
        Iterator var3 = this.components.values().iterator();
        ICustomGuiComponent c;
        while (var3.hasNext()) {
            c = (ICustomGuiComponent) var3.next();
            list.appendTag(c.toNBT(new NBTTagCompound()));
        }
        tag.setTag("components", list);
        list = new NBTTagList();
        var3 = this.slots.iterator();
        while (var3.hasNext()) {
            c = (ICustomGuiComponent) var3.next();
            if (c.getID() == -1) {
                c.setID(this.getMaxId() + 1);
            }
            list.appendTag(c.toNBT(new NBTTagCompound()));
        }
        tag.setTag("slots", list);
        tag.setBoolean("showPlayerInv", this.showPlayerInv);
        if (this.showPlayerInv) {
            tag.setIntArray("pInvPos", new int[]{this.playerInvX, this.playerInvY});
        }
        return tag;
    }
}
