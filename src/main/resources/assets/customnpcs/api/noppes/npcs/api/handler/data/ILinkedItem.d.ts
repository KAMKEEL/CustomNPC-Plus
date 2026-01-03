/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

export interface ILinkedItem {

    // Methods
    save(): import('./ILinkedItem').ILinkedItem;
    createStack(): import('../../item/IItemStack').IItemStack;
    getId(): number;
    setId(id: number): void;
    getVersion(): number;
    setVersion(version: number): void;
    getName(): string;
    setName(name: string): void;
    getDurabilityValue(): number;
    setDurabilityValue(durabilityValue: number): void;
    getStackSize(): number;
    setStackSize(stackSize: number): void;
    getMaxItemUseDuration(): number;
    setMaxItemUseDuration(maxItemUseDuration: number): void;
    getItemUseAction(): number;
    setItemUseAction(itemUseAction: number): void;
    isNormalItem(): boolean;
    setNormalItem(normalItem: boolean): void;
    isTool(): boolean;
    setTool(tool: boolean): void;
    getDigSpeed(): number;
    setDigSpeed(digSpeed: number): void;
    getArmorType(): number;
    setArmorType(armorType: number): void;
    getEnchantability(): number;
    setEnchantability(enchantability: number): void;

}
