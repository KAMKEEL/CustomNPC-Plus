package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.controllers.ScriptContainer;

import java.util.List;
import java.util.Map;

public interface IScriptHandler {
    void callScript(String hookName, Event event);

    boolean isClient();

    boolean getEnabled();

    void setEnabled(boolean enabled);

    String getLanguage();

    void setLanguage(String language);

    void setScripts(List<ScriptContainer> list);

    List<ScriptContainer> getScripts();

    String noticeString();

    Map<Long, String> getConsoleText();

    void clearConsole();
}
