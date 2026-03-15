/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a custom crafting recipe for the carpentry table or vanilla workbench.
  * @javaFqn noppes.npcs.api.handler.data.IRecipe
*/
export interface IRecipe {
    /** @return the recipe display name. */
    getName(): String;
    /** @return true if this is a global (vanilla workbench) recipe. */
    isGlobal(): import('./boolean').boolean;
    /** @param global true for global; false for carpentry only. */
    setIsGlobal(global: import('./boolean').boolean): import('./void').void;
    /** @return true if NBT data is ignored when matching ingredients. */
    getIgnoreNBT(): import('./boolean').boolean;
    /** @param ignoreNBT true to ignore NBT in ingredient matching. */
    setIgnoreNBT(ignoreNBT: import('./boolean').boolean): import('./void').void;
    /** @return true if damage values are ignored when matching ingredients. */
    getIgnoreDamage(): import('./boolean').boolean;
    /** @param ignoreDamage true to ignore damage in ingredient matching. */
    setIgnoreDamage(ignoreDamage: import('./boolean').boolean): import('./void').void;
    /** @return the recipe grid width. */
    getWidth(): import('./int').int;
    /** @return the recipe grid height. */
    getHeight(): import('./int').int;
    /** @return the resulting item stack. */
    getResult(): import('../../../../../net/minecraft/item/ItemStack').ItemStack;
    /** @return the ingredient item stacks in grid order. */
    getRecipe(): import('../../../../../net/minecraft/item/ItemStack').ItemStack[];
    /** Deletes this recipe from the handler. */
    delete(): import('./void').void;
    /** @return the unique recipe ID. */
    getId(): import('./int').int;
    id: import('./int').int;
    name: String;
    availability: import('./Availability').Availability;
    isGlobal: import('./boolean').boolean;
    ignoreDamage: import('./boolean').boolean;
    ignoreNBT: import('./boolean').boolean;
}
