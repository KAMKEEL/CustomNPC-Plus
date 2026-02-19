package noppes.npcs.controllers;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.config.ConfigScript;

/**
 * Server-side utility for syncing script configuration to clients.
 * <p>
 * This class is SERVER-SIDE ONLY. Client reads via ScriptClientConfig.
 */
public class ScriptConfigSync {

    /**
     * Write script config to NBT for sending to client.
     * Called on server side when building login packet.
     */
    public static NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("ScriptingEnabled", ConfigScript.ScriptingEnabled);
        compound.setBoolean("RunLoadedScriptsFirst", ConfigScript.RunLoadedScriptsFirst);
        compound.setBoolean("GlobalPlayerScripts", ConfigScript.GlobalPlayerScripts);
        compound.setBoolean("GlobalForgeScripts", ConfigScript.GlobalForgeScripts);
        compound.setBoolean("GlobalNPCScripts", ConfigScript.GlobalNPCScripts);
        return compound;
    }
}
