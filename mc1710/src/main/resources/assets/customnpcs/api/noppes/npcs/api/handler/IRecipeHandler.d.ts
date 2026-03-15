/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handles custom crafting recipes and anvil recipes.
  * @javaFqn noppes.npcs.api.handler.IRecipeHandler
*/
export interface IRecipeHandler {
    /** @return all global (vanilla workbench) recipes. */
    getGlobalList(): import('./data/IRecipe').IRecipe[];
    /** @return all carpentry table recipes. */
    getCarpentryList(): import('./data/IRecipe').IRecipe[];
    /** @return all custom anvil recipes. */
    getAnvilList(): import('./data/IAnvilRecipe').IAnvilRecipe[];
    /**
     * Adds a shaped crafting recipe.
     *
     * @param name   the recipe name.
     * @param global true for global (vanilla workbench); false for carpentry.
     * @param result the resulting item stack.
     * @param recipe the recipe pattern and ingredient mappings.
     */
    addRecipe(name: String, global: import('./boolean').boolean, result: import('../../../../net/minecraft/item/ItemStack').ItemStack, ...recipe: Object[]): import('./void').void;
    /**
     * Adds a shaped crafting recipe with explicit dimensions.
     *
     * @param name   the recipe name.
     * @param global true for global; false for carpentry.
     * @param result the resulting item stack.
     * @param width  the recipe grid width.
     * @param height the recipe grid height.
     * @param items  the ingredient item stacks.
     */
    addRecipe(name: String, global: import('./boolean').boolean, result: import('../../../../net/minecraft/item/ItemStack').ItemStack, width: import('./int').int, height: import('./int').int, ...items: import('../../../../net/minecraft/item/ItemStack').ItemStack[]): import('./void').void;
    /**
     * Deletes a crafting recipe by ID.
     *
     * @param id the recipe ID.
     * @return the deleted recipe, or null if not found.
     */
    delete(id: import('./int').int): import('./data/IRecipe').IRecipe;
    /**
     * Deletes an anvil recipe by ID.
     *
     * @param id the recipe ID.
     * @return the deleted anvil recipe, or null if not found.
     */
    deleteAnvil(id: import('./int').int): import('./data/IAnvilRecipe').IAnvilRecipe;
    /**
     * Adds a custom anvil recipe.
     *
     * @param name             the recipe name.
     * @param global           true for global; false for carpentry.
     * @param itemToRepair     the item to be repaired.
     * @param repairMaterial   the material used for repair.
     * @param xpCost           the experience cost.
     * @param repairPercentage the percentage of durability restored per material.
     */
    addAnvilRecipe(name: String, global: import('./boolean').boolean, itemToRepair: import('../../../../net/minecraft/item/ItemStack').ItemStack, repairMaterial: import('../../../../net/minecraft/item/ItemStack').ItemStack, xpCost: import('./int').int, repairPercentage: import('./float').float): import('./void').void;
}
