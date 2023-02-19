//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

import java.util.*;
import java.util.Map.Entry;

public class ForgeDataScript implements IScriptHandler {
    private List<ScriptContainer> scripts = new ArrayList();
    private String scriptLanguage = "ECMAScript";
    public long lastInited = -1L;
    private boolean enabled = false;
    private Map<Long, String> console = new TreeMap();

    public ForgeDataScript() {
    }

    public void clear() {
        this.scripts = new ArrayList();
        this.console = new TreeMap();
    }

    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Scripts")) {
            this.scripts = NBTTags.GetScriptOld(compound.getTagList("Scripts", 10), this);
        } else {
            this.scripts = NBTTags.GetScript(compound,this);
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
        this.console = NBTTags.GetLongStringMap(compound.getTagList("ScriptConsole", 10));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("TotalScripts",this.scripts.size());
        for (int i = 0; i < this.scripts.size(); i++) {
            compound.setTag("Tab"+i,this.scripts.get(i).writeToNBT(new NBTTagCompound()));
        }
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(this.console));
        return compound;
    }

    @Override
    public void callScript(EnumScriptType var1, Event var2) {
        callScript(var1.function, var2);
    }

    public void callScript(String type, Event event) {
        if(this.isEnabled()) {
            if (ScriptController.Instance.lastLoaded > this.lastInited) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                if (!type.equals("init")) {
                    EventHooks.onForgeInit(this);
                }
            }

            int i = 0;
            for (ScriptContainer script : this.scripts) {
                script.run(type, event);

                for (Entry<Long, String> longStringEntry : script.console.entrySet()) {
                    if (!ScriptController.Instance.forgeScripts.console.containsKey(longStringEntry.getKey())) {
                        ScriptController.Instance.forgeScripts.console.put(longStringEntry.getKey(), " tab " + (i + 1) + ":\n" + longStringEntry.getValue());
                    }
                }
                i++;
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

    public void setScripts(List<ScriptContainer> list) {
        this.scripts = list;
    }

    public List<ScriptContainer> getScripts() {
        return this.scripts;
    }

    public String noticeString() {
        return "ForgeScript";
    }

    public void setConsoleText(Map<Long, String> map) {
        this.console = map;
    }

    public Map<Long, String> getConsoleText() {
        return this.console;
    }
    public void clearConsole() {
        this.console.clear();
    }
}
