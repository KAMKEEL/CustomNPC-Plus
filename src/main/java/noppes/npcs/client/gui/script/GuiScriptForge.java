//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.ForgeDataScript;

public class GuiScriptForge extends GuiScriptInterface {
    private ForgeDataScript script = new ForgeDataScript();

    public GuiScriptForge() {
        this.handler = this.script;
        Client.sendData(EnumPacketServer.ScriptForgeGet, new Object[0]);
    }

    public void setGuiData(NBTTagCompound compound) {
        this.script.readFromNBT(compound);
        super.setGuiData(compound);
    }

    public void save() {
        super.save();
        Client.sendData(EnumPacketServer.ScriptForgeSave, new Object[]{this.script.writeToNBT(new NBTTagCompound())});
    }
}
