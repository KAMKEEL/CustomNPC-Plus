package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.controllers.ScriptController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ScriptHandler implements IScriptHandler {
    public IScriptUnit container;
    protected String scriptLanguage = "ECMAScript";
    protected boolean enabled = false;

    protected boolean canRunScripts() {
        return enabled && ScriptController.HasStart && ConfigScript.ScriptingEnabled && container != null;
    }

    @Override
    public void callScript(String hookName, Event event) {
        if (!canRunScripts()) {
            return;
        }
        container.run(hookName, event);
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

    @Override
    public void setScripts(List<IScriptUnit> list) {
        if (list == null || list.isEmpty()) {
            container = null;
            return;
        }
        container = list.get(0);
    }

    @Override
    public List<IScriptUnit> getScripts() {
        if (container == null) {
            return new ArrayList<>();
        }
        return Collections.singletonList(container);
    }
}
