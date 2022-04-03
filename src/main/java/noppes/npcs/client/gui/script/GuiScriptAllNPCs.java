package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.NPCDataScript;

public class GuiScriptAllNPCs extends GuiScriptInterface {
    private NPCDataScript script = new NPCDataScript(null);

    public GuiScriptAllNPCs() {
        this.handler = this.script;
        Client.sendData(EnumPacketServer.ScriptNPCGet, new Object[0]);
    }

    public void setGuiData(NBTTagCompound compound) {
        this.script.readFromNBT(compound);
        super.setGuiData(compound);
    }

    public void save() {
        super.save();
        Client.sendData(EnumPacketServer.ScriptNPCSave, new Object[]{this.script.writeToNBT(new NBTTagCompound())});
    }

    public void close() {
        super.close();
        ScriptController.Instance.npcScripts.errored.clear();
    }
}
