package noppes.npcs.controllers.data;

import kamkeel.npcs.network.packets.request.script.RecipeScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.IScriptHookHandler;

/**
 * Script handler for Recipe scripts (both carpentry and anvil).
 * Manages a single script container for recipe hooks (pre, post).
 */
public class RecipeScript extends SingleScriptHandler implements IScriptHandlerPacket {
    
    /** The recipe ID for packet communication. -1 if not bound. */
    private int recipeId = -1;
    
    /** Whether this is an anvil recipe (vs carpentry). */
    private boolean anvil = false;
    
    /**
     * Create an unbound RecipeScript (for server-side use).
     */
    public RecipeScript() {
    }
    
    /**
     * Create a RecipeScript bound to a specific recipe (for GUI use).
     * @param recipeId The ID of the recipe
     * @param anvil true for anvil recipe, false for carpentry recipe
     */
    public RecipeScript(int recipeId, boolean anvil) {
        this.recipeId = recipeId;
        this.anvil = anvil;
    }
    
    @Override
    public String getHookContext() {
        return IScriptHookHandler.CONTEXT_RECIPE;
    }
    
    @Override
    public String noticeString() {
        return "RecipeScript";
    }
    
    @Override
    public void requestData() {
        if (recipeId >= 0) 
            RecipeScriptPacket.Get(anvil, recipeId);
    }
    
    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        if (recipeId >= 0) 
            RecipeScriptPacket.Save(anvil, recipeId, index, totalCount, nbt);
    }

    public enum ScriptType {
        PRE("pre"),
        POST("post");
        public final String function;

        ScriptType(String functionName) {
            this.function = functionName;
        }
    }
}