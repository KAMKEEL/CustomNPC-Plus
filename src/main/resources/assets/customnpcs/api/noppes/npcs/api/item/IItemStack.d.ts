/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.item
 */

export interface IItemStack {

    // Methods
    getName(): string;
    getStackSize(): number;
    hasCustomName(): boolean;
    setCustomName(name: string): void;
    getDisplayName(): string;
    getItemName(): string;
    setStackSize(size: number): void;
    getMaxStackSize(): number;
    getItemDamage(): number;
    setItemDamage(value: number): void;
    setTag(key: string, value: any): void;
    hasTag(key: string): boolean;
    getTag(key: string): any;
    removeTags(): import('../INbt').INbt;
    isEnchanted(): boolean;
    hasEnchant(id: number): boolean;
    addEnchant(id: number, strength: number): void;
    setAttribute(name: string, value: number): void;
    getAttribute(name: string): number;
    hasAttribute(name: string): boolean;
    setCustomAttribute(key: string, value: number): void;
    hasCustomAttribute(key: string): boolean;
    getCustomAttribute(key: string): number;
    removeCustomAttribute(key: string): void;
    setMagicAttribute(key: string, magicId: number, value: number): void;
    hasMagicAttribute(key: string, magicId: number): boolean;
    getMagicAttribute(key: string, magicId: number): number;
    removeMagicAttribute(key: string, magicId: number): void;
    setRequirement(reqKey: string, value: any): void;
    hasRequirement(reqKey: string): boolean;
    getRequirement(reqKey: string): any;
    removeRequirement(reqKey: string): void;
    getCustomAttributeKeys(): string[];
    getMagicAttributeKeys(key: string): string[];
    getRequirementKeys(): string[];
    getLore(): string[];
    hasLore(): boolean;
    setLore(lore: string[]): void;
    copy(): import('./IItemStack').IItemStack;
    getMaxItemDamage(): number;
    isWrittenBook(): boolean;
    getBookTitle(): string;
    getBookAuthor(): string;
    getBookText(): string[];
    isBlock(): boolean;
    getNbt(): import('../INbt').INbt;
    getItemNbt(): import('../INbt').INbt;
    getMCItemStack(): ItemStack;
    itemHash(): number;
    getMCNbt(): NBTTagCompound;
    setMCNbt(compound: NBTTagCompound): void;
    compare(item: import('./IItemStack').IItemStack, ignoreNBT: boolean): boolean;
    compare(item: import('./IItemStack').IItemStack, ignoreDamage: boolean, ignoreNBT: boolean): boolean;

}
