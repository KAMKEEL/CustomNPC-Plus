package noppes.npcs.controllers;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.config.ConfigScript;

/**
 * Server-side utility for syncing script IConfiguration to clients.
 * <p>
 * This class is SERVER-SIDE ONLY. Client reads via ScriptClientConfig.
 */
public class ScriptConfigSync {

    /**
     * Write script config to NBT for sending to client.
     * Called on server side when building login packet.
     */
    public static INbt writeToNBT(INbt compound) {
        compound.setBoolean("ScriptingEnabled", ConfigScript.ScriptingEnabled);
        compound.setBoolean("RunLoadedScriptsFirst", ConfigScript.RunLoadedScriptsFirst);
        compound.setBoolean("GlobalPlayerScripts", ConfigScript.GlobalPlayerScripts);
        compound.setBoolean("GlobalForgeScripts", ConfigScript.GlobalForgeScripts);
        compound.setBoolean("GlobalNPCScripts", ConfigScript.GlobalNPCScripts);
        return compound;
    }
}
