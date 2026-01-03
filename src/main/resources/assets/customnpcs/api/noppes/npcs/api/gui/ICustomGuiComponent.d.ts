/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

export interface ICustomGuiComponent {

    // Methods
    getID(): number;
    setID(id: number): import('./ICustomGuiComponent').ICustomGuiComponent;
    getPosX(): number;
    getPosY(): number;
    setPos(x: number, y: number): import('./ICustomGuiComponent').ICustomGuiComponent;
    hasHoverText(): boolean;
    getHoverText(): string[];
    setHoverText(hoverText: string): import('./ICustomGuiComponent').ICustomGuiComponent;
    setHoverText(hoverTextLines: string[]): import('./ICustomGuiComponent').ICustomGuiComponent;
    getColor(): number;
    setColor(color: number): import('./ICustomGuiComponent').ICustomGuiComponent;
    getAlpha(): number;
    setAlpha(alpha: number): void;
    getRotation(): number;
    setRotation(rotation: number): void;
    toNBT(nbt: NBTTagCompound): NBTTagCompound;
    fromNBT(nbt: NBTTagCompound): import('./ICustomGuiComponent').ICustomGuiComponent;

}
