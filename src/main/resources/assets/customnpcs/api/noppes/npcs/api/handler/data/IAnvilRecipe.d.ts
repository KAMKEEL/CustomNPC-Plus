/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IAnvilRecipe {

    // Methods
    getName(): string;
    getXpCost(): number;
    getRepairPercentage(): number;
    matches(itemToRepair: ItemStack, repairMaterial: ItemStack): boolean;
    getResult(itemToRepair: ItemStack): ItemStack;
    getID(): number;

}
