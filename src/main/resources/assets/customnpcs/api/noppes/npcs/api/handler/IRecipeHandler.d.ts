/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IRecipeHandler {

    // Methods
    getGlobalList(): Array<IRecipe;
    getCarpentryList(): Array<IRecipe;
    getAnvilList(): Array<IAnvilRecipe;
    addRecipe(var1: string, var2: boolean, var3: ItemStack, var4: ): void;
    addRecipe(var1: string, var2: boolean, var3: ItemStack, var4: number, var5: number, var6: ): void;
    delete(var1: number): import('./data/IRecipe').IRecipe;
    deleteAnvil(var1: number): import('./data/IAnvilRecipe').IAnvilRecipe;
    addAnvilRecipe(name: string, global: boolean, itemToRepair: ItemStack, repairMaterial: ItemStack, xpCost: number, repairPercentage: number): void;

}
