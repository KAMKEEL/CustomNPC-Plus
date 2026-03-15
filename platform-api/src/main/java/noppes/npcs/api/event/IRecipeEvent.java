package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.item.IItemStack;

/**
 * Events fired before and after recipe crafting, for both crafting table and anvil recipes.
 */
public interface IRecipeEvent extends IPlayerEvent {

    /**
     * Returns the recipe object associated with this event.
     * This can be a custom recipe or an anvil recipe.
     *
     * @return the recipe object - IRecipe or IAnvilRecipe
     */
    Object getRecipe();

    /** @return the input item stacks used in the recipe. */
    IItemStack[] getItems();

    /** @return true if this is an anvil recipe, false for crafting table. */
    boolean isAnvil();

    /**
     * Fired before a recipe is crafted. Cancelable.
     * @hookName recipeCraftPre
     */
    @Cancelable
    interface Pre extends IRecipeEvent {
        /** @param message the denial message shown when canceled. */
        void setMessage(String message);

        /** @return the denial message. */
        String getMessage();

        /**
         * Only for IAnvilRecipe
         *
         * @return the XP Cost of Anvil Recipe
         */
        int getXpCost();

        /**
         * Sets the XP Cost of Anvil Recipe
         *
         * @param xpCost the XP Cost to set
         */
        void setXpCost(int xpCost);

        /**
         * Only for IAnvilRecipe
         *
         * @return the material usage of Anvil Recipe
         */
        int getMaterialUsage();

        /**
         * Sets the material usage of Anvil Recipe
         *
         * @param materialUsage the material usage to set
         */
        void setMaterialUsage(int materialUsage);
    }

    /**
     * Fired after a recipe is crafted.
     * @hookName recipeCraftPost
     */
    interface Post extends IRecipeEvent {
        /** @return the crafted item. */
        IItemStack getCraft();

        /** @param stack the item to set as the crafting result. */
        void setResult(IItemStack stack);
    }
}
