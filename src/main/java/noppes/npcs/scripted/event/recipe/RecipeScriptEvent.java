package noppes.npcs.scripted.event.recipe;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.handler.data.IRecipe;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.player.PlayerEvent;

public class RecipeScriptEvent extends PlayerEvent {
    public final IRecipe recipe;
    public final IItemStack[] items;

    public RecipeScriptEvent(IPlayer player, IRecipe recipe, IItemStack[] items) {
        super(player);
        this.recipe = recipe;
        this.items = items;
    }

    public IRecipe getRecipe() {
        return recipe;
    }

    public IItemStack[] getItems() {
        return items;
    }

    @Cancelable
    public static class Pre extends RecipeScriptEvent {
        public Pre(IPlayer player, IRecipe recipe, IItemStack[] items) {
            super(player, recipe, items);
        }
        public String getHookName() {
            return RecipeScript.ScriptType.PRE.function;
        }
    }

    public static class Post extends RecipeScriptEvent {
        private IItemStack result;
        public Post(IPlayer player, IRecipe recipe, IItemStack[] items, IItemStack result) {
            super(player, recipe, items);
            this.result = result;
        }

        public IItemStack getResult() {
            return result;
        }

        public void setResult(IItemStack stack) {
            this.result = stack;
        }

        public String getHookName() {
            return RecipeScript.ScriptType.POST.function;
        }
    }
}
