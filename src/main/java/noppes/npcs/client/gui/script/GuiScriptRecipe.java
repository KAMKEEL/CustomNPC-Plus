package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.RecipeScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.client.gui.global.GuiNpcManageRecipes;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.RecipeAnvil;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.controllers.data.RecipeScript;

import java.util.List;

public class GuiScriptRecipe extends GuiScriptInterface {
    private final GuiNpcManageRecipes parent;
    private final RecipeScript scriptHandler;
    private final boolean anvil;
    private final int recipeId;

    public GuiScriptRecipe(GuiNpcManageRecipes parent, RecipeCarpentry recipe) {
        this.parent = parent;
        this.scriptHandler = recipe.getOrCreateScriptHandler();
        this.anvil = false;
        this.recipeId = recipe.id;
        hookList.add(RecipeScript.ScriptType.PRE.function);
        hookList.add(RecipeScript.ScriptType.POST.function);
        RecipeScriptPacket.Get(false, recipeId);
        this.handler = scriptHandler;
    }

    public GuiScriptRecipe(GuiNpcManageRecipes parent, RecipeAnvil recipe) {
        this.parent = parent;
        this.scriptHandler = recipe.getOrCreateScriptHandler();
        this.anvil = true;
        this.recipeId = recipe.id;
        hookList.add(RecipeScript.ScriptType.PRE.function);
        hookList.add(RecipeScript.ScriptType.POST.function);
        RecipeScriptPacket.Get(true, recipeId);
        this.handler = scriptHandler;
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (!compound.hasKey("Tab")) {
            scriptHandler.setLanguage(compound.getString("ScriptLanguage"));
            scriptHandler.setEnabled(compound.getBoolean("ScriptEnabled"));
            copiedSetGuiData(compound);
        } else {
            int tab = compound.getInteger("Tab");
            ScriptContainer container = new ScriptContainer(scriptHandler);
            container.readFromNBT(compound.getCompoundTag("Script"));
            scriptHandler.setScripts(java.util.Collections.singletonList(container));
        }
        loaded = true;
        initGui();
    }

    private void copiedSetGuiData(NBTTagCompound compound) {
        net.minecraft.nbt.NBTTagList data = compound.getTagList("Languages", 10);
        java.util.HashMap languages = new java.util.HashMap();
        for (int i = 0; i < data.tagCount(); ++i) {
            NBTTagCompound comp = data.getCompoundTagAt(i);
            java.util.ArrayList scripts = new java.util.ArrayList();
            net.minecraft.nbt.NBTTagList list = comp.getTagList("Scripts", 8);
            for (int j = 0; j < list.tagCount(); ++j) {
                scripts.add(list.getStringTagAt(j));
            }
            languages.put(comp.getString("Language"), scripts);
        }
        this.languages = languages;
    }

    @Override
    public void save() {
        if (loaded) {
            super.save();
            List<ScriptContainer> containers = scriptHandler.getScripts();
            for (int i = 0; i < containers.size(); i++) {
                ScriptContainer sc = containers.get(i);
                RecipeScriptPacket.Save(anvil, recipeId, i, containers.size(), sc.writeToNBT(new NBTTagCompound()));
            }
            NBTTagCompound data = new NBTTagCompound();
            data.setString("ScriptLanguage", scriptHandler.getLanguage());
            data.setBoolean("ScriptEnabled", scriptHandler.getEnabled());
            data.setTag("ScriptConsole", NBTTags.NBTLongStringMap(scriptHandler.getConsoleText()));
            RecipeScriptPacket.Save(anvil, recipeId, -1, containers.size(), data);
        }
    }
}
