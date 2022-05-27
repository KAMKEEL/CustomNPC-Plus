package noppes.npcs.client.gui.script;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomItems;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.scripted.item.ScriptCustomItem;

public class GuiScriptItem extends GuiScriptInterface {
    private ScriptCustomItem item;

    public GuiScriptItem(EntityPlayer player) {
        this.handler = this.item = new ScriptCustomItem(new ItemStack(CustomItems.scripted_item));
        Client.sendData(EnumPacketServer.ScriptItemDataGet, new Object[0]);
    }

    public void setGuiData(NBTTagCompound compound) {
        this.item.setMCNbt(compound);
        super.setGuiData(compound);
    }

    public void save() {
        super.save();
        Client.sendData(EnumPacketServer.ScriptItemDataSave, new Object[]{this.item.getMCNbt()});
    }
}