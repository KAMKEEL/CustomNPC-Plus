package noppes.npcs.controllers.data;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.constants.EnumScriptType;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface IScriptHandler {
    default void callScript(EnumScriptType type, Event event) {
        if (type != null) 
            callScript(type.function, event);
        
    }
    
    void callScript(String hookName, Event event);

    default boolean isClient() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    boolean getEnabled();

    void setEnabled(boolean enabled);

    String getLanguage();

    void setLanguage(String language);

    void setScripts(List<IScriptUnit> list);

    List<IScriptUnit> getScripts();

    default String noticeString() {
        return "";
    }

    default Map<Long, String> getConsoleText() {
        Map<Long, String> map = new TreeMap<>();
        int tab = 0;
        
        for (IScriptUnit script : getScripts()) {
            ++tab;
            for (Map.Entry<Long, String> entry : script.getConsole().entrySet()) {
                map.put(entry.getKey(), " tab " + tab + ":\n" + entry.getValue());
            }
        }
        return map;
    }

    default void clearConsole() {
        for (IScriptUnit script : getScripts()) 
            script.clearConsole();
        
    }
    
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
