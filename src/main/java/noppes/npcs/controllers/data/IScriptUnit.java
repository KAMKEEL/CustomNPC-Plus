package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Common interface for script units that can be edited in a script GUI.
 * Implemented by both ScriptContainer (ECMAScript) and JaninoScript (Java/Janino).
 * This allows GuiScriptInterface to work with both script types uniformly.
 */
public interface IScriptUnit {
    
    // ==================== SCRIPT CONTENT ====================
    
    /**
     * Get the main script text content.
     */
    String getScript();
    
    /**
     * Set the main script text content.
     */
    void setScript(String script);
    
    /**
     * Get the list of external script file names loaded for this unit.
     */
    List<String> getExternalScripts();
    
    /**
     * Set the list of external script file names to load.
     */
    void setExternalScripts(List<String> scripts);
    
    // ==================== CONSOLE ====================
    
    /**
     * Get the console output map (timestamp -> message).
     */
    TreeMap<Long, String> getConsole();
    
    /**
     * Clear all console output.
     */
    void clearConsole();
    
    /**
     * Append a message to the console.
     */
    void appendConsole(String message);
    
    // ==================== LANGUAGE ====================
    
    /**
     * Get the scripting language for this unit.
     * Returns "ECMAScript", "Java", etc.
     */
    String getLanguage();
    
    /**
     * Set the scripting language for this unit.
     * @param language The language name (e.g., "ECMAScript", "Java")
     */
    void setLanguage(String language);
    
    /**
     * Check if this script unit uses Janino (Java) compilation.
     */
    default boolean isJanino() {
        return "Java".equals(getLanguage());
    }
    
    // ==================== HOOK GENERATION ====================
    
    /**
     * Generate a stub/template for the given hook name.
     * ECMAScript: function hookName(event) { }
     * Janino: full Java method signature with return type
     * 
     * @param hookName The hook name to generate a stub for
     * @param hookData Optional data (e.g., Method object for Janino)
     * @return The generated stub string
     */
    String generateHookStub(String hookName, Object hookData);
    
    // ==================== NBT ====================
    
    /**
     * Write this script unit to NBT.
     */
    NBTTagCompound writeToNBT(NBTTagCompound compound);
    
    /**
     * Read this script unit from NBT.
     */
    void readFromNBT(NBTTagCompound compound);
    
    // ==================== STATE ====================
    
    /**
     * Check if this script unit has any code (script or external scripts).
     */
    boolean hasCode();
    
    /**
     * Check if this script unit has encountered an error during execution.
     */
    boolean hasErrored();
    
    /**
     * Set the error state of this script unit.
     */
    void setErrored(boolean errored);
}
