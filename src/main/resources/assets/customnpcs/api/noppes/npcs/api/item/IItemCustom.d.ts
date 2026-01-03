/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.item
 */

export interface IItemCustom extends IItemCustomizable {

    // Methods
    getEnabled(): boolean;
    setEnabled(enable: boolean): void;
    setArmorType(armorType: number): void;
    setIsTool(isTool: boolean): void;
    setIsNormalItem(normalItem: boolean): void;
    setDigSpeed(digSpeed: number): void;
    setMaxStackSize(maxStackSize: number): void;
    setDurabilityValue(durabilityValue: number): void;
    setMaxItemUseDuration(duration: number): void;
    setItemUseAction(action: number): void;
    setEnchantability(enchantability: number): void;

}
