package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

import java.util.ArrayList;
import java.util.List;

public class ForgeDataScript implements IScriptHandler {
    private List<IScriptUnit> scripts = new ArrayList<>();
    private String scriptLanguage = "ECMAScript";
    public long lastInited = -1L;
    private long lastForgeUpdate = -1L;
    private boolean enabled = false;

    public ForgeDataScript() {
    }

    public void clear() {
        this.scripts = new ArrayList<>();
    }

    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Scripts")) {
            this.scripts = new ArrayList<>(NBTTags.GetScriptOld(compound.getTagList("Scripts", 10), this));
        } else {
            this.scripts = new ArrayList<>(NBTTags.GetScript(compound, this));
        }
        this.scriptLanguage = compound.getString("ScriptLanguage");
        if (!ScriptController.Instance.languages.containsKey(scriptLanguage)) {
            if (!ScriptController.Instance.languages.isEmpty()) {
                this.scriptLanguage = (String) ScriptController.Instance.languages.keySet().toArray()[0];
            } else {
                this.scriptLanguage = "ECMAScript";
            }
        }
        this.enabled = compound.getBoolean("ScriptEnabled");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("TotalScripts", this.scripts.size());
        for (int i = 0; i < this.scripts.size(); i++) {
            compound.setTag("Tab" + i, this.scripts.get(i).writeToNBT(new NBTTagCompound()));
        }
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        return compound;
    }


    public void callScript(String type, Event event) {
        if (this.isEnabled()) {
            if (ScriptController.Instance.lastLoaded > this.lastInited || ScriptController.Instance.lastForgeUpdate > this.lastForgeUpdate) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                this.lastForgeUpdate = ScriptController.Instance.lastForgeUpdate;

                for (IScriptUnit script : this.scripts) {
                    if (script instanceof ScriptContainer)
                        ((ScriptContainer) script).errored = false;
                }

                if (!type.equals("init")) {
                    EventHooks.onForgeInit(this);
                }
            }

            for (IScriptUnit script : this.scripts) {
                if (script == null || script.hasErrored() || !script.hasCode())
                    continue;
                script.run(type, event);
            }
        }
    }

    public boolean isEnabled() {
        return this.enabled && ConfigScript.GlobalForgeScripts && ScriptController.HasStart && this.scripts.size() > 0;
    }

    public boolean isClient() {
        return false;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean bo) {
        this.enabled = bo;
    }

    public String getLanguage() {
        return this.scriptLanguage;
    }

    public void setLanguage(String lang) {
        this.scriptLanguage = lang;
    }

    public void setScripts(List<IScriptUnit> list) {
        this.scripts = list;
    }

    public List<IScriptUnit> getScripts() {
        return this.scripts;
    }


}
