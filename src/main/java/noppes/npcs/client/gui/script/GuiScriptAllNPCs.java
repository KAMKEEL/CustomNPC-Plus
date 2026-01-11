package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.GlobalNPCScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptHookController;
import noppes.npcs.controllers.data.GlobalNPCDataScript;

import java.util.ArrayList;
import java.util.List;

public class GuiScriptAllNPCs extends GuiScriptInterface {
    private final GlobalNPCDataScript script = new GlobalNPCDataScript(null);

    public GuiScriptAllNPCs() {
        this.hookList = new ArrayList<>(ScriptHookController.Instance.getAllHooks(IScriptHookHandler.CONTEXT_GLOBAL_NPC));

        this.handler = this.script;
        GlobalNPCScriptPacket.Get();
    }

    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("LoadComplete")) {
            loaded = true;
            return;
        }

        if (!compound.hasKey("Tab")) {
            script.setLanguage(compound.getString("ScriptLanguage"));
            script.setEnabled(compound.getBoolean("ScriptEnabled"));
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
            script.getScripts().set(tab, container);
            initGui();
        }
    }

    public void save() {
        if (loaded) {
            super.save();
            List<ScriptContainer> containers = this.script.getScripts();
            for (int i = 0; i < containers.size(); i++) {
                ScriptContainer container = containers.get(i);
                GlobalNPCScriptPacket.Save(i, containers.size(), container.writeToNBT(new NBTTagCompound()));
            }
            NBTTagCompound scriptData = new NBTTagCompound();
            scriptData.setString("ScriptLanguage", this.script.getLanguage());
            scriptData.setBoolean("ScriptEnabled", this.script.getEnabled());
            scriptData.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.script.getConsoleText()));
            GlobalNPCScriptPacket.Save(-1, containers.size(), scriptData);
        }
    }
}
