package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.NPCDataScript;

import java.util.List;

public class GuiScriptAllNPCs extends GuiScriptInterface {
    private NPCDataScript script = new NPCDataScript(null);

    public GuiScriptAllNPCs() {
        this.handler = this.script;
        Client.sendData(EnumPacketServer.ScriptNPCGet);
    }

    public void setGuiData(NBTTagCompound compound) {
        if (!compound.hasKey("Tab")) {
            script.setLanguage(compound.getString("ScriptLanguage"));
            script.setEnabled(compound.getBoolean("ScriptEnabled"));
            script.setConsoleText(NBTTags.GetLongStringMap(compound.getTagList("ScriptConsole", 10)));
            super.setGuiData(compound);
        } else {
            int tab = compound.getInteger("Tab");
            ScriptContainer container = new ScriptContainer(script);
            container.readFromNBT(compound.getCompoundTag("Script"));
            if (script.getScripts().isEmpty()) {
                for (int i = 0; i < compound.getInteger("TotalScripts"); i++) {
                    script.getScripts().add(new ScriptContainer(script));
                }
            }
            script.getScripts().set(tab,container);
            initGui();
        }
    }

    public void save() {
        super.save();
        List<ScriptContainer> containers = this.script.getScripts();
        for (int i = 0; i < containers.size(); i++) {
            ScriptContainer container = containers.get(i);
            Client.sendData(EnumPacketServer.ScriptNPCSave, i, containers.size(), container.writeToNBT(new NBTTagCompound()));
        }
        NBTTagCompound scriptData = new NBTTagCompound();
        scriptData.setString("ScriptLanguage", this.script.getLanguage());
        scriptData.setBoolean("ScriptEnabled", this.script.getEnabled());
        scriptData.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.script.getConsoleText()));
        Client.sendData(EnumPacketServer.ScriptNPCSave, -1, scriptData);
    }
}
