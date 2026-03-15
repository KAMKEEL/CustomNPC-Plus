package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.IRecipeEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.data.RecipeScript;
import noppes.npcs.scripted.event.player.PlayerEvent;

public class RecipeScriptEvent extends PlayerEvent implements IRecipeEvent {
    public final Object recipe;
    public final IItemStack[] items;
    public final boolean isAnvil;

    public RecipeScriptEvent(IPlayer player, Object recipe, boolean anvil, IItemStack[] items) {
        super(player);
        this.recipe = recipe;
        this.items = items;
        this.isAnvil = anvil;
    }

    @Override
    public Object getRecipe() {
        return recipe;
    }

    @Override
    public IItemStack[] getItems() {
        return items;
    }

    @Override
    public boolean isAnvil() {
        return isAnvil;
    }

    @Cancelable
    public static class Pre extends RecipeScriptEvent implements IRecipeEvent.Pre {
        private String message = "";
        private int xpCost = 0;
        private int materialUsage = 0;

        public Pre(IPlayer player, Object recipe, boolean isAnvil, IItemStack[] items) {
            super(player, recipe, isAnvil, items);
        }

        @Override
        public void setMessage(String message) {
            this.message = message == null ? "" : message;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public int getXpCost() {
            return xpCost;
        }

        @Override
        public void setXpCost(int xpCost) {
            if (this.isAnvil)
                this.xpCost = xpCost;
        }

        @Override
        public int getMaterialUsage() {
            return materialUsage;
        }

        @Override
        public void setMaterialUsage(int materialUsage) {
            if (this.isAnvil)
                this.materialUsage = materialUsage;
        }

        public String getHookName() {
            return RecipeScript.ScriptType.PRE.function;
        }
    }

    public static class Post extends RecipeScriptEvent implements IRecipeEvent.Post {
        private IItemStack result;

        public Post(IPlayer player, Object recipe, boolean isAnvil, IItemStack[] items, IItemStack result) {
            super(player, recipe, isAnvil, items);
            this.result = result;
        }

        @Override
        public IItemStack getCraft() {
            return result;
        }

        @Override
        public void setResult(IItemStack stack) {
            this.result = stack;
        }

        public String getHookName() {
            return RecipeScript.ScriptType.POST.function;
        }
    }
}
