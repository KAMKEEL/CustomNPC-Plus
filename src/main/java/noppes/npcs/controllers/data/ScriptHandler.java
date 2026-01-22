package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptController;

import java.util.ArrayList;
import java.util.List;

public abstract class ScriptHandler implements IScriptHandler {
    public List<IScriptUnit> scripts = new ArrayList<>();
    protected String scriptLanguage = "ECMAScript";
    protected boolean enabled = false;
    protected long lastInited = -1;

    protected boolean canRunScripts() {
        return enabled && ScriptController.HasStart && ConfigScript.ScriptingEnabled && scripts != null && !scripts.isEmpty();
    }

    @Override
    public void callScript(String hookName, Event event) {
        if (!canRunScripts())
            return;

        if (ScriptController.Instance.lastLoaded > lastInited) {
            lastInited = ScriptController.Instance.lastLoaded;
            for (IScriptUnit script : scripts) {
                if (script != null)
                    script.setErrored(false);
            }
        }

        for (IScriptUnit script : scripts) {
            if (script == null || script.hasErrored() || !script.hasCode())
                continue;
            script.run(hookName, event);
        }
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getLanguage() {
        return scriptLanguage;
    }

    @Override
    public void setLanguage(String language) {
        this.scriptLanguage = language;
    }

    public void setScripts(List<IScriptUnit> list) {
        this.scripts = list;
    }

    public List<IScriptUnit> getScripts() {
        return this.scripts;
    }

    /**
     * Reset the lastInited timestamp to force re-initialization on next script call.
     */
    public void resetLastInited() {
        this.lastInited = -1;
    }
}
