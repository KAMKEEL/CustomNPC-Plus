/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

export interface IRecipeEvent extends IPlayerEvent {

    // Methods
    getRecipe(): any;
    getItems(): import('../item/IItemStack').IItemStack[];
    isAnvil(): boolean;
    setMessage(message: string): void;
    getMessage(): string;
    getXpCost(): number;
    setXpCost(xpCost: number): void;
    getMaterialUsage(): number;
    setMaterialUsage(materialUsage: number): void;

    // Nested interfaces
    interface Pre extends IRecipeEvent {
        setMessage(message: string): void;
        getMessage(): string;
        getXpCost(): number;
        setXpCost(xpCost: number): void;
        getMaterialUsage(): number;
        setMaterialUsage(materialUsage: number): void;
    }
    interface Post extends IRecipeEvent {
        getCraft(): import('../item/IItemStack').IItemStack;
        setResult(stack: import('../item/IItemStack').IItemStack): void;
    }

}
