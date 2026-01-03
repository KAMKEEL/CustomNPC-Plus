/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.overlay
 */

export interface ICustomOverlayComponent {

    // Methods
    getID(): number;
    setID(id: number): import('./ICustomOverlayComponent').ICustomOverlayComponent;
    getPosX(): number;
    getPosY(): number;
    setPos(x: number, y: number): import('./ICustomOverlayComponent').ICustomOverlayComponent;
    getAlignment(): number;
    setAlignment(alignment: number): void;
    getColor(): number;
    setColor(color: number): import('./ICustomOverlayComponent').ICustomOverlayComponent;
    getAlpha(): number;
    setAlpha(alpha: number): void;
    getRotation(): number;
    setRotation(rotation: number): void;
    toNBT(nbt: NBTTagCompound): NBTTagCompound;
    fromNBT(nbt: NBTTagCompound): import('./ICustomOverlayComponent').ICustomOverlayComponent;

}
