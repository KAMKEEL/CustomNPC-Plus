//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.EventHooks;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.NBTTags;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.wrapper.WrapperNpcAPI;

import javax.script.ScriptEngine;

public class ForgeDataScript implements IScriptHandler {
    private List<ScriptContainer> scripts = new ArrayList();
    private String scriptLanguage = "ECMAScript";
    public long lastInited = -1L;
    private boolean enabled = false;

    public ForgeDataScript() {
    }

    public void clear() {
        this.scripts = new ArrayList();
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this);
        this.scriptLanguage = compound.getString("ScriptLanguage");
        this.enabled = compound.getBoolean("ScriptEnabled");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        return compound;
    }

    @Override
    public void callScript(EnumScriptType var1, Event var2, Object... obs) {
        callScript(var1.function, var2);
    }

    public void callScript(String type, Event event) {
        if(this.isEnabled()) {
            //Minecraft.getMinecraft().func_152344_a(() -> {
                if (ScriptController.Instance.lastLoaded > this.lastInited) {
                    this.lastInited = ScriptController.Instance.lastLoaded;
                    if (!type.equals("init")) {
                        EventHooks.onForgeInit(this);
                    }
                }

                Iterator var3 = this.scripts.iterator();

                while (var3.hasNext()) {
                    ScriptContainer script = (ScriptContainer) var3.next();

                    script.setEngine(scriptLanguage);
                    if(script.engine == null)
                        return;

                    Event result = (Event) script.engine.get("event");
                    if(result == null)
                        script.engine.put("event", event);
                    script.engine.put("API", new WrapperNpcAPI());

                    script.run(type, event);
                }
            //});
        }
    }

    public boolean isEnabled() {
        return this.enabled && ScriptController.HasStart && this.scripts.size() > 0;
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

    public List<ScriptContainer> getScripts() {
        return this.scripts;
    }

    public String noticeString() {
        return "ForgeScript";
    }

    public Map<Long, String> getConsoleText() {
        TreeMap map = new TreeMap();
        int tab = 0;
        Iterator var3 = this.getScripts().iterator();

        while(var3.hasNext()) {
            ScriptContainer script = (ScriptContainer)var3.next();
            ++tab;
            Iterator var5 = script.console.entrySet().iterator();

            while(var5.hasNext()) {
                Entry entry = (Entry)var5.next();
                map.put(entry.getKey(), " tab " + tab + ":\n" + (String)entry.getValue());
            }
        }

        return map;
    }

    public void clearConsole() {
        Iterator var1 = this.getScripts().iterator();

        while(var1.hasNext()) {
            ScriptContainer script = (ScriptContainer)var1.next();
            script.console.clear();
        }

    }
}
