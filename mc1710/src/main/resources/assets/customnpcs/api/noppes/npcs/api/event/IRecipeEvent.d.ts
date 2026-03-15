/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events fired before and after recipe crafting, for both crafting table and anvil recipes.
  * @javaFqn noppes.npcs.api.event.IRecipeEvent
*/
export interface IRecipeEvent extends import('./IPlayerEvent').IPlayerEvent {
    /**
     * Returns the recipe object associated with this event.
     * This can be a custom recipe or an anvil recipe.
     *
     * @return the recipe object - IRecipe or IAnvilRecipe
     */
    getRecipe(): Object;
    /** @return the input item stacks used in the recipe. */
    getItems(): import('../item/IItemStack').IItemStack[];
    /** @return true if this is an anvil recipe, false for crafting table. */
    isAnvil(): import('./boolean').boolean;
    readonly recipe: Object;
    readonly items: import('../item/IItemStack').IItemStack[];
    readonly isAnvil: import('./boolean').boolean;
}

export namespace IRecipeEvent {
    
    
    
    /**
     * Fired before a recipe is crafted. Cancelable.
     * @hookName recipeCraftPre
          * @javaFqn noppes.npcs.api.event.IRecipeEvent.Pre
*/
    export interface Pre extends IRecipeEvent {
        /** @param message the denial message shown when canceled. */
        setMessage(message: String): import('./void').void;
        /** @return the denial message. */
        getMessage(): String;
        /**
         * Only for IAnvilRecipe
         *
         * @return the XP Cost of Anvil Recipe
         */
        getXpCost(): import('./int').int;
        /**
         * Sets the XP Cost of Anvil Recipe
         *
         * @param xpCost the XP Cost to set
         */
        setXpCost(xpCost: import('./int').int): import('./void').void;
        /**
         * Only for IAnvilRecipe
         *
         * @return the material usage of Anvil Recipe
         */
        getMaterialUsage(): import('./int').int;
        /**
         * Sets the material usage of Anvil Recipe
         *
         * @param materialUsage the material usage to set
         */
        setMaterialUsage(materialUsage: import('./int').int): import('./void').void;
    }
    /**
     * Fired after a recipe is crafted.
     * @hookName recipeCraftPost
          * @javaFqn noppes.npcs.api.event.IRecipeEvent.Post
*/
    export interface Post extends IRecipeEvent {
        /** @return the crafted item. */
        getCraft(): import('../item/IItemStack').IItemStack;
        /** @param stack the item to set as the crafting result. */
        setResult(stack: import('../item/IItemStack').IItemStack): import('./void').void;
    }
}
