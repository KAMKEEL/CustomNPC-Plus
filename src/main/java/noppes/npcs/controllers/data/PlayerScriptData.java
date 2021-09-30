//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.controllers.data;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.entity.IPlayer;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

public class PlayerScriptData implements IScriptHandler {
    private List<ScriptContainer> scripts = new ArrayList();
    private String scriptLanguage = "ECMAScript";
    private EntityPlayer player;
    private IPlayer playerAPI;
    private long lastPlayerUpdate = 0L;
    public long lastInited = -1L;
    public boolean hadInteract = true;
    private boolean enabled = false;
    private static Map<Long, String> console = new TreeMap();
    private static List<Integer> errored = new ArrayList();

    public PlayerScriptData(EntityPlayer player) {
        this.player = player;
    }

    public void clear() {
        console = new TreeMap();
        errored = new ArrayList();
        this.scripts = new ArrayList();
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.scripts = NBTTags.GetScript(compound.func_150295_c("Scripts", 10), this);
        this.scriptLanguage = compound.func_74779_i("ScriptLanguage");
        this.enabled = compound.func_74767_n("ScriptEnabled");
        console = NBTTags.GetLongStringMap(compound.func_150295_c("ScriptConsole", 10));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.func_74782_a("Scripts", NBTTags.NBTScript(this.scripts));
        compound.func_74778_a("ScriptLanguage", this.scriptLanguage);
        compound.func_74757_a("ScriptEnabled", this.enabled);
        compound.func_74782_a("ScriptConsole", NBTTags.NBTLongStringMap(console));
        return compound;
    }

    public void runScript(EnumScriptType type, Event event) {
        if(this.isEnabled()) {
            ScriptContainer script;
            if(ScriptController.Instance.lastLoaded > this.lastInited || ScriptController.Instance.lastPlayerUpdate > this.lastPlayerUpdate) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                errored.clear();
                if(this.player != null) {
                    this.scripts.clear();
                    Iterator i = ScriptController.Instance.playerScripts.scripts.iterator();

                    while(i.hasNext()) {
                        script = (ScriptContainer)i.next();
                        ScriptContainer s = new ScriptContainer(this);
                        s.readFromNBT(script.writeToNBT(new NBTTagCompound()));
                        this.scripts.add(s);
                    }
                }

                this.lastPlayerUpdate = ScriptController.Instance.lastPlayerUpdate;
                if(type != EnumScriptType.INIT) {
                    EventHooks.onPlayerInit(this);
                }
            }

            for(int var7 = 0; var7 < this.scripts.size(); ++var7) {
                script = (ScriptContainer)this.scripts.get(var7);
                if(!errored.contains(Integer.valueOf(var7))) {
                    script.run(type, event);
                    if(script.errored) {
                        errored.add(Integer.valueOf(var7));
                    }

                    Iterator var8 = script.console.entrySet().iterator();

                    while(var8.hasNext()) {
                        Entry entry = (Entry)var8.next();
                        if(!console.containsKey(entry.getKey())) {
                            console.put(entry.getKey(), " tab " + (var7 + 1) + ":\n" + (String)entry.getValue());
                        }
                    }

                    script.console.clear();
                }
            }

        }
    }

    public boolean isEnabled() {
        return ScriptController.Instance.playerScripts.enabled && ScriptController.HasStart && (this.player == null || !this.player.field_70170_p.field_72995_K);
    }

    public boolean isClient() {
        return !this.player.func_70613_aW();
    }

    public boolean getEnabled() {
        return ScriptController.Instance.playerScripts.enabled;
    }

    public void setEnabled(boolean bo) {
        this.enabled = bo;
    }

    public String getLanguage() {
        return ScriptController.Instance.playerScripts.scriptLanguage;
    }

    public void setLanguage(String lang) {
        this.scriptLanguage = lang;
    }

    public List<ScriptContainer> getScripts() {
        return this.scripts;
    }

    public String noticeString() {
        if(this.player == null) {
            return "Global script";
        } else {
            BlockPos pos = this.player.func_180425_c();
            return MoreObjects.toStringHelper(this.player).add("x", pos.func_177958_n()).add("y", pos.func_177956_o()).add("z", pos.func_177952_p()).toString();
        }
    }

    public IPlayer getPlayer() {
        if(this.playerAPI == null) {
            this.playerAPI = (IPlayer)NpcAPI.Instance().getIEntity(this.player);
        }

        return this.playerAPI;
    }

    public Map<Long, String> getConsoleText() {
        return console;
    }

    public void clearConsole() {
        console.clear();
    }
}
