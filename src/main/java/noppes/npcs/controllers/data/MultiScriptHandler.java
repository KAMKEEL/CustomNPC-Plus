package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Base class for script handlers that manage multiple script containers (tabs).
 * Provides shared NBT serialization and common script execution patterns.
 *
 * Subclasses should:
 * - Override getContext() to return the appropriate ScriptContext
 * - Override getHookContext() if not using ScriptContext's hookContext
 * - Override requestData() and sendSavePacket() for packet communication
 * - Override isEnabled() if custom enable logic is needed
 */
public abstract class MultiScriptHandler extends ScriptHandler implements IScriptHandlerPacket {
    
    // ==================== NBT SERIALIZATION ====================

    /**
     * Read script data from NBT using the Tab0/Tab1/etc. format.
     */
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Scripts")) {
            this.scripts = new ArrayList<>(NBTTags.GetScriptOld(compound.getTagList("Scripts", 10), this));
        } else {
            this.scripts = new ArrayList<>(NBTTags.GetScript(compound, this));
        }

        this.scriptLanguage = compound.getString("ScriptLanguage");
        normalizeLanguage();

        this.enabled = compound.getBoolean("ScriptEnabled");
    }

    /**
     * Write script data to NBT using the Tab0/Tab1/etc. format.
     */
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("TotalScripts", this.scripts.size());
        for (int i = 0; i < this.scripts.size(); i++) {
            compound.setTag("Tab" + i, this.scripts.get(i).writeToNBT(new NBTTagCompound()));
        }
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        return compound;
    }

    /**
     * Normalize the language to a valid one if the current language is not available.
     */
    protected void normalizeLanguage() {
        if (!ScriptController.Instance.languages.containsKey(this.scriptLanguage)) {
            if (!ScriptController.Instance.languages.isEmpty()) {
                this.scriptLanguage = (String) ScriptController.Instance.languages.keySet().toArray()[0];
            } else {
                this.scriptLanguage = "ECMAScript";
            }
        }
    }

    // ==================== SCRIPT EXECUTION ====================

    /**
     * Check if scripts need to be re-initialized due to reload.
     * Subclasses can override to add additional update tracking (e.g., lastPlayerUpdate).
     *
     * @return true if scripts should be re-initialized
     */
    protected boolean needsReInit() {
        return ScriptController.Instance.lastLoaded > lastInited;
    }

    /**
     * Called when scripts need re-initialization.
     * Clears error state on all script containers.
     */
    protected void reInitScripts() {
        lastInited = ScriptController.Instance.lastLoaded;
        for (IScriptUnit script : this.scripts) {
            if (script instanceof ScriptContainer) {
                ((ScriptContainer) script).errored = false;
            }
        }
    }

    @Override
    public void callScript(String hookName, Event event) {
        if (!canRunScripts()) {
            return;
        }

        if (needsReInit()) {
            reInitScripts();
        }

        for (IScriptUnit script : this.scripts) {
            if (script == null || script.hasErrored() || !script.hasCode()) {
                continue;
            }
            script.run(hookName, event);
        }
    }
}
