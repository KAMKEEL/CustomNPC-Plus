/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.overlay
 */

/**
 * @javaFqn noppes.npcs.api.overlay.ICustomOverlay
 */
export interface ICustomOverlay {
    getID(): import('./int').int;
    getComponents(): import('./ICustomOverlayComponent').ICustomOverlayComponent[];
    getDefaultAlignment(): import('./int').int;
    setDefaultAlignment(defaultAlignment: import('./int').int): import('./void').void;
    addTexturedRect(id: import('./int').int, texture: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int): import('./IOverlayTexturedRect').IOverlayTexturedRect;
    addTexturedRect(id: import('./int').int, texture: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int, textureX: import('./int').int, textureY: import('./int').int): import('./IOverlayTexturedRect').IOverlayTexturedRect;
    addLabel(id: import('./int').int, label: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int): import('./IOverlayLabel').IOverlayLabel;
    addLabel(id: import('./int').int, label: String, x: import('./int').int, y: import('./int').int, width: import('./int').int, height: import('./int').int, color: import('./int').int): import('./IOverlayLabel').IOverlayLabel;
    addLine(id: import('./int').int, x1: import('./int').int, y1: import('./int').int, x2: import('./int').int, y2: import('./int').int, color: import('./int').int, thickness: import('./int').int): import('./IOverlayLine').IOverlayLine;
    addLine(id: import('./int').int, x1: import('./int').int, y1: import('./int').int, x2: import('./int').int, y2: import('./int').int): import('./IOverlayLine').IOverlayLine;
    getComponent(componentID: import('./int').int): import('./ICustomOverlayComponent').ICustomOverlayComponent;
    removeComponent(componentID: import('./int').int): import('./void').void;
    updateComponent(component: import('./ICustomOverlayComponent').ICustomOverlayComponent): import('./void').void;
    update(player: import('../entity/IPlayer').IPlayer): import('./void').void;
    fromNBT(tag: import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound): import('./ICustomOverlay').ICustomOverlay;
    toNBT(): import('../../../../net/minecraft/nbt/NBTTagCompound').NBTTagCompound;
}
