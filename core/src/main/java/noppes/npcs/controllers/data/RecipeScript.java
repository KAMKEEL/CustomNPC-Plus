package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.constants.ScriptContext;
/**
 * Script handler for Recipe scripts (both carpentry and anvil).
 * Manages a single script container for recipe hooks (pre, post).
 */
public class RecipeScript extends SingleScriptHandler implements IScriptHandlerPacket {

    /**
     * The recipe ID for packet communication. -1 if not bound.
     */
    private int recipeId = -1;

    /**
     * Whether this is an anvil recipe (vs carpentry).
     */
    private boolean anvil = false;

    /**
     * Create an unbound RecipeScript (for server-side use).
     */
    public RecipeScript() {
    }

    /**
     * Create a RecipeScript bound to a specific recipe (for GUI use).
     *
     * @param recipeId The ID of the recipe
     * @param anvil    true for anvil recipe, false for carpentry recipe
     */
    public RecipeScript(int recipeId, boolean anvil) {
        this.recipeId = recipeId;
        this.anvil = anvil;
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.RECIPE;
    }

    @Override
    public IScriptUnit createJaninoScriptUnit() {
        return new EventJaninoScript(ScriptContext.RECIPE);
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
    public void sendSavePacket(int index, int totalCount, INbt nbt) {
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
