package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.EventScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.DataScript;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.List;

public class GuiNPCEventScripts extends GuiScriptInterface {
    private final DataScript script;

    public GuiNPCEventScripts(EntityNPCInterface npc) {
        this.hookList = new ArrayList<>();
        hookList.add("init");
        hookList.add("tick");
        hookList.add("interact");
        hookList.add("dialog");
        hookList.add("damaged");
        hookList.add("killed");
        hookList.add("meleeAttack");
        hookList.add("meleeSwing");
        hookList.add("rangedLaunched");
        hookList.add("target");
        hookList.add("collide");
        hookList.add("kills");
        hookList.add("dialogClose");
        hookList.add("timer");
        hookList.add("targetLost");
        hookList.add("projectileTick");
        hookList.add("projectileImpact");

        this.script = new DataScript(npc);
        this.handler = this.script;
        EventScriptPacket.Get();
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
                EventScriptPacket.Save(i, containers.size(), container.writeToNBT(new NBTTagCompound()));
            }
            NBTTagCompound scriptData = new NBTTagCompound();
            scriptData.setString("ScriptLanguage", this.script.getLanguage());
            scriptData.setBoolean("ScriptEnabled", this.script.getEnabled());
            scriptData.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.script.getConsoleText()));
            EventScriptPacket.Save(-1, containers.size(), scriptData);
        }
    }
}
