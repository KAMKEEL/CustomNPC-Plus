/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.overlay
 */

/**
 * @javaFqn noppes.npcs.api.overlay.ICustomOverlayComponent
 */
export interface ICustomOverlayComponent {
    getID(): import('./int').int;
    setID(id: import('./int').int): import('./ICustomOverlayComponent').ICustomOverlayComponent;
    getPosX(): import('./int').int;
    getPosY(): import('./int').int;
    setPos(x: import('./int').int, y: import('./int').int): import('./ICustomOverlayComponent').ICustomOverlayComponent;
    getAlignment(): import('./int').int;
    setAlignment(alignment: import('./int').int): import('./void').void;
    getColor(): import('./int').int;
    setColor(color: import('./int').int): import('./ICustomOverlayComponent').ICustomOverlayComponent;
    getAlpha(): import('./float').float;
    setAlpha(alpha: import('./float').float): import('./void').void;
    getRotation(): import('./float').float;
    setRotation(rotation: import('./float').float): import('./void').void;
    toNBT(nbt: import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound): import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound;
    fromNBT(nbt: import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound): import('./ICustomOverlayComponent').ICustomOverlayComponent;
    getType: import('./abstract int').abstract int;
}
