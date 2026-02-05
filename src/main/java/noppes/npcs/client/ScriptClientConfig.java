package noppes.npcs.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Client-side cache for script configuration synced from server.
 * Prevents client from using local ConfigScript which may differ from server settings.
 *
 * IMPORTANT: This class is CLIENT-ONLY. Server-side NBT writing is done in
 * ScriptConfigSync to avoid @SideOnly crashes on dedicated servers.
 */
@SideOnly(Side.CLIENT)
public class ScriptClientConfig {
    // Cached values from server
    private static boolean scriptingEnabled = true;
    private static boolean runLoadedScriptsFirst = true;
    private static boolean globalPlayerScripts = true;
    private static boolean globalForgeScripts = true;
    private static boolean globalNPCScripts = false;

    /**
     * Update cached config from server NBT data.
     * Called when receiving login packet or script sync packets.
     */
    public static void readFromNBT(NBTTagCompound compound) {
        if (compound == null) return;

        scriptingEnabled = compound.getBoolean("ScriptingEnabled");
        runLoadedScriptsFirst = compound.getBoolean("RunLoadedScriptsFirst");
        globalPlayerScripts = compound.getBoolean("GlobalPlayerScripts");
        globalForgeScripts = compound.getBoolean("GlobalForgeScripts");
        globalNPCScripts = compound.getBoolean("GlobalNPCScripts");
    }

    /**
     * Reset to defaults (called on disconnect).
     */
    public static void reset() {
        scriptingEnabled = true;
        runLoadedScriptsFirst = true;
        globalPlayerScripts = true;
        globalForgeScripts = true;
        globalNPCScripts = false;
    }

    // Setters for individual packet updates
    public static void setScriptingEnabled(boolean value) { scriptingEnabled = value; }
    public static void setRunLoadedScriptsFirst(boolean value) { runLoadedScriptsFirst = value; }
    public static void setGlobalPlayerScripts(boolean value) { globalPlayerScripts = value; }
    public static void setGlobalForgeScripts(boolean value) { globalForgeScripts = value; }
    public static void setGlobalNPCScripts(boolean value) { globalNPCScripts = value; }

    // Getters
    public static boolean isScriptingEnabled() { return scriptingEnabled; }
    public static boolean isRunLoadedScriptsFirst() { return runLoadedScriptsFirst; }
    public static boolean isGlobalPlayerScripts() { return globalPlayerScripts; }
    public static boolean isGlobalForgeScripts() { return globalForgeScripts; }
    public static boolean isGlobalNPCScripts() { return globalNPCScripts; }
}
