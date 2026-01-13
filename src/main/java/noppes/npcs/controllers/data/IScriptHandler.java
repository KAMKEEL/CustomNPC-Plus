package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.constants.EnumScriptType;

import java.util.List;
import java.util.Map;

public interface IScriptHandler {
    void callScript(EnumScriptType type, Event event);
    
    void callScript(String hookName, Event event);

    boolean isClient();

    boolean getEnabled();

    void setEnabled(boolean enabled);

    String getLanguage();

    void setLanguage(String language);

    void setScripts(List<IScriptUnit> list);

    List<IScriptUnit> getScripts();

    String noticeString();

    Map<Long, String> getConsoleText();

    void clearConsole();
    
    /**
     * Create a new Janino (Java) script unit for this handler.
     * Handlers that don't support Janino scripts should return null.
     * 
     * @return A new JaninoScript instance, or null if not supported
     */
    default IScriptUnit createJaninoScriptUnit() {
        return null; // Default: Janino not supported
    }
    
    /**
     * Check if this handler supports Janino (Java) scripts.
     * 
     * @return true if createJaninoScriptUnit() can create valid Janino scripts
     */
    default boolean supportsJanino() {
        return createJaninoScriptUnit() != null;
    }
}
