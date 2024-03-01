package noppes.npcs.client.gui.script;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.scripted.item.ScriptCustomItem;

public class GuiScriptItem extends GuiScriptInterface {
    private ScriptCustomItem item;

    public GuiScriptItem() {
        hookList.add("init");
        hookList.add("tick");
        hookList.add("tossed");
        hookList.add("pickedUp");
        hookList.add("spawn");
        hookList.add("interact");
        hookList.add("rightClick");
        hookList.add("attack");
        hookList.add("startItem");
        hookList.add("usingItem");
        hookList.add("stopItem");
        hookList.add("finishItem");

        this.handler = this.item = new ScriptCustomItem(new ItemStack(CustomItems.scripted_item));
        Client.sendData(EnumPacketServer.ScriptItemDataGet, new Object[0]);
    }

    public void setGuiData(NBTTagCompound compound) {
        this.item.setMCNbt(compound);
        this.item.loadScriptData();
        super.setGuiData(compound);
    }

    public void save() {
        super.save();
        Client.sendData(EnumPacketServer.ScriptItemDataSave, new Object[]{this.item.getMCNbt()});
    }
}
