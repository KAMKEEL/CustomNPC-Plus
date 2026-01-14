package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.RecipeScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.client.gui.global.GuiNpcManageRecipes;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptHookController;
import noppes.npcs.controllers.data.IScriptUnit;
import noppes.npcs.controllers.data.RecipeAnvil;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.controllers.data.RecipeScript;

import java.util.ArrayList;

public class GuiScriptRecipe extends GuiScriptInterface {

    public final int recipeId;
    public final boolean anvil;

    public GuiScriptRecipe(GuiNpcManageRecipes parent, RecipeCarpentry recipe) {
        super();
        this.parent = parent;
        this.handler = new RecipeScript();
        this.anvil = false;
        this.recipeId = recipe.id;
        this.singleContainer = true;

        this.hookList = new ArrayList<>(ScriptHookController.Instance.getAllHooks(IScriptHookHandler.CONTEXT_RECIPE));

        RecipeScriptPacket.Get(false, recipeId);
    }

    public GuiScriptRecipe(GuiNpcManageRecipes parent, RecipeAnvil recipe) {
        super();
        this.parent = parent;
        this.handler = new RecipeScript();
        this.anvil = true;
        this.recipeId = recipe.id;

        this.hookList = new ArrayList<>(ScriptHookController.Instance.getAllHooks(IScriptHookHandler.CONTEXT_RECIPE));

        RecipeScriptPacket.Get(true, recipeId);
    }

    @Override
    protected ScriptContext getScriptContext() {
        return ScriptContext.GLOBAL;
    }

    protected void setHandlerUnit(IScriptUnit container) {
        ((RecipeScript) handler).container = container;
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        setGuiDataWithOldContainer(compound);
    }

    protected void sendSavePacket(int index, int totalCount, NBTTagCompound scriptNBT) {
        RecipeScriptPacket.Save(anvil, recipeId, index, totalCount, scriptNBT);
    }
    
    @Override
    public void save() {
        saveWithPackets();
    }
}
