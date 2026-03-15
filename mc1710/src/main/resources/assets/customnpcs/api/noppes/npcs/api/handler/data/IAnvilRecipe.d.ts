/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IAnvilRecipe
 */
export interface IAnvilRecipe {
    /**
     * Returns the unique name of this anvil recipe.
     *
     * @return the recipe name
     */
    getName(): String;
    /**
     * The XP cost per repair tick (e.g., 10 levels per tick).
     *
     * @return the experience level cost
     */
    getXpCost(): import('./int').int;
    /**
     * The repair percentage (e.g., 0.1f repairs 10% of max damage per tick).
     *
     * @return the repair percentage (0.0 to 1.0)
     */
    getRepairPercentage(): import('./float').float;
    /**
     * Checks whether the provided item and repair material match this recipe.
     *
     * @param itemToRepair   the damaged item
     * @param repairMaterial the material used to repair
     * @return true if the items match the recipe requirements
     */
    matches(itemToRepair: import('../../../../../net/minecraft/item/ItemStack').ItemStack, repairMaterial: import('../../../../../net/minecraft/item/ItemStack').ItemStack): import('./boolean').boolean;
    /**
     * Returns the repaired item based on the input damaged item.
     *
     * @param itemToRepair the input damaged item
     * @return a copy with reduced damage
     */
    getResult(itemToRepair: import('../../../../../net/minecraft/item/ItemStack').ItemStack): import('../../../../../net/minecraft/item/ItemStack').ItemStack;
    getID(): import('./int').int;
    id: import('./int').int;
    name: String;
    availability: import('./Availability').Availability;
    ignoreRepairItemNBT: import('./boolean').boolean;
    ignoreRepairMaterialNBT: import('./boolean').boolean;
    ignoreRepairMaterialDamage: import('./boolean').boolean;
    itemToRepair: import('../../../../../net/minecraft/item/ItemStack').ItemStack;
    repairMaterial: import('../../../../../net/minecraft/item/ItemStack').ItemStack;
    xpCost: import('./int').int;
    repairPercentage: import('./float').float;
}
