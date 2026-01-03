/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

export interface ICustomGui {

    // Methods
    getID(): number;
    getWidth(): number;
    getHeight(): number;
    getComponents(): Array<ICustomGuiComponent;
    clear(): void;
    getSlots(): Array<IItemSlot;
    setSize(width: number, height: number): void;
    setDoesPauseGame(pauseGame: boolean): void;
    doesPauseGame(): boolean;
    setBackgroundTexture(resourceLocation: string): void;
    getBackgroundTexture(): string;
    addButton(id: number, text: string, x: number, y: number): import('./IButton').IButton;
    addButton(id: number, text: string, x: number, y: number, width: number, height: number): import('./IButton').IButton;
    addTexturedButton(id: number, text: string, x: number, y: number, width: number, height: number, texture: string): import('./IButton').IButton;
    addTexturedButton(id: number, text: string, x: number, y: number, width: number, height: number, texture: string, textureX: number, textureY: number): import('./IButton').IButton;
    addLabel(id: number, text: string, x: number, y: number, width: number, height: number): import('./ILabel').ILabel;
    addLabel(id: number, text: string, x: number, y: number, width: number, height: number, color: number): import('./ILabel').ILabel;
    addTextField(id: number, x: number, y: number, width: number, height: number): import('./ITextField').ITextField;
    addTexturedRect(id: number, texture: string, x: number, y: number, width: number, height: number): import('./ITexturedRect').ITexturedRect;
    addTexturedRect(id: number, texture: string, x: number, y: number, width: number, height: number, textureX: number, textureY: number): import('./ITexturedRect').ITexturedRect;
    addItemSlot(id: number, x: number, y: number): import('./IItemSlot').IItemSlot;
    addItemSlot(id: number, x: number, y: number, itemStack: import('../item/IItemStack').IItemStack): import('./IItemSlot').IItemSlot;
    addItemSlot(x: number, y: number): import('./IItemSlot').IItemSlot;
    addItemSlot(x: number, y: number, itemStack: import('../item/IItemStack').IItemStack): import('./IItemSlot').IItemSlot;
    addScroll(id: number, x: number, y: number, width: number, height: number, list: string[]): import('./IScroll').IScroll;
    addLine(id: number, x1: number, y1: number, x2: number, y2: number, color: number, thickness: number): import('../handler/data/ILine').ILine;
    addLine(id: number, x1: number, y1: number, x2: number, y2: number): import('../handler/data/ILine').ILine;
    showPlayerInventory(x: number, y: number): void;
    getComponent(id: number): import('./ICustomGuiComponent').ICustomGuiComponent;
    removeComponent(id: number): void;
    updateComponent(component: import('./ICustomGuiComponent').ICustomGuiComponent): void;
    update(player: import('../entity/IPlayer').IPlayer): void;
    getShowPlayerInv(): boolean;
    getPlayerInvX(): number;
    getPlayerInvY(): number;
    fromNBT(tag: NBTTagCompound): import('./ICustomGui').ICustomGui;
    toNBT(): NBTTagCompound;

}
