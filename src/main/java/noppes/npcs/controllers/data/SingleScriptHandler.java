package noppes.npcs.controllers.data;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.constants.EnumScriptType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple IScriptHandler implementation for single scripts that don't have their own handler.
 * Useful for standalone scripts like OverlayScript that need to be edited in GuiScriptInterface.
 */
public class SingleScriptHandler implements IScriptHandler {
    private final IScriptUnit script;
    private final List<IScriptUnit> scripts = new ArrayList<>();

    public SingleScriptHandler(IScriptUnit script) {
        this.script = script;
        this.scripts.add(script);
    }

    @Override
    public void callScript(EnumScriptType type, Event event) {
        // No-op: single scripts don't respond to standard hook calls
    }

    @Override
    public void callScript(String hookName, Event event) {
        // No-op: single scripts don't respond to standard hook calls
    }

    public boolean isClient() {
        return FMLCommonHandler.instance()
                               .getEffectiveSide()
                               .isClient();
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        // No-op: single scripts are always enabled
    }

    @Override
    public String getLanguage() {
        return script.getLanguage();
    }

    @Override
    public void setLanguage(String language) {
        // No-op: language is determined by script type
    }

    @Override
    public void setScripts(List<IScriptUnit> list) {
        // No-op: single script handler only manages one script
    }

    @Override
    public List<IScriptUnit> getScripts() {
        return scripts;
    }

    @Override
    public String noticeString() {
        return "";
    }

    @Override
    public Map<Long, String> getConsoleText() {
        return script.getConsole();
    }

    @Override
    public void clearConsole() {
        script.clearConsole();
    }

    @Override
    public IScriptUnit createJaninoScriptUnit() {
        return null; // Single script handlers don't create new scripts
    }

    @Override
    public boolean supportsJanino() {
        return script.isJanino();
    }
}
