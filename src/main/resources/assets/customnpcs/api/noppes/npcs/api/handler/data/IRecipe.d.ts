/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface IRecipe {

    // Methods
    getName(): string;
    isGlobal(): boolean;
    setIsGlobal(var1: boolean): void;
    getIgnoreNBT(): boolean;
    setIgnoreNBT(var1: boolean): void;
    getIgnoreDamage(): boolean;
    setIgnoreDamage(var1: boolean): void;
    getWidth(): number;
    getHeight(): number;
    getResult(): ItemStack;
    getRecipe(): ItemStack[];
    delete(): void;
    getId(): number;

}
