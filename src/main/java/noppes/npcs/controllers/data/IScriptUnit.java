package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Common interface for script units that can be edited in a script GUI.
 * Implemented by both ScriptContainer (ECMAScript) and JaninoScript (Java/Janino).
 * This allows GuiScriptInterface to work with both script types uniformly.
 */
public interface IScriptUnit {
    
    // NBT type identifiers
    String NBT_TYPE_KEY = "ScriptUnitType";
    String TYPE_ECMASCRIPT = "ECMAScript";
    String TYPE_JANINO = "Janino";
    
    /**
     * Create an IScriptUnit from NBT data.
     * Uses the "ScriptUnitType" tag to determine which implementation to create.
     * 
     * @param compound The NBT data
     * @param handler The script handler (used for ScriptContainer)
     * @return A ScriptContainer or JaninoScript based on the NBT type
     */
    static IScriptUnit createFromNBT(NBTTagCompound compound, IScriptHandler handler) {
        String type = compound.getString(NBT_TYPE_KEY);
        IScriptUnit unit;
        
        if (TYPE_JANINO.equals(type)) {
            // Create appropriate JaninoScript based on handler
            unit = handler.createJaninoScriptUnit();
            if (unit == null) {
                // Fallback to ScriptContainer if handler doesn't support Janino
                unit = new ScriptContainer(handler);
            }
        } else {
            // Default to ScriptContainer for ECMAScript or missing type
            unit = new ScriptContainer(handler);
        }
        
        unit.readFromNBT(compound);
        return unit;
    }
    
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
     * Ensure this script unit is compiled (for Janino).
     * No-op for ScriptContainer.
     */
    void ensureCompiled();
    
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
    
    // ==================== EXECUTION ====================
    
    /**
     * Execute this script unit for the given hook type and event.
     * 
     * @param type The script hook type
     * @param event The event object
     */
    void run(EnumScriptType type, Event event);
    
    /**
     * Execute this script unit for the given hook name and event.
     *
     * @param hookName The hook name (e.g., "init", "interact")
     * @param event The event object
     */
    void run(String hookName, Object event);

    /**
     * Call a script function by name and return its result.
     *
     * Default implementation returns null (not supported).
     */
    default Object callFunction(String hookName, Object... args) {
        return null;
    }

    /**
     * Typed convenience wrapper around {@link #callFunction(String, Object...)}.
     */
    default <S> S callFunction(String hookName, Class<S> returnType, Object... args) {
        Object result = callFunction(hookName, args);
        if (result == null || returnType == null)
            return null;

        if (returnType.isInstance(result))
            return returnType.cast(result);

        return null;
    }
}
