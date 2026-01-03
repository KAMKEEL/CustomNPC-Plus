/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.overlay
 */

export interface ICustomOverlay {

    // Methods
    getID(): number;
    getComponents(): Array<ICustomOverlayComponent;
    getDefaultAlignment(): number;
    setDefaultAlignment(defaultAlignment: number): void;
    addTexturedRect(id: number, texture: string, x: number, y: number, width: number, height: number): import('./IOverlayTexturedRect').IOverlayTexturedRect;
    addTexturedRect(id: number, texture: string, x: number, y: number, width: number, height: number, textureX: number, textureY: number): import('./IOverlayTexturedRect').IOverlayTexturedRect;
    addLabel(id: number, label: string, x: number, y: number, width: number, height: number): import('./IOverlayLabel').IOverlayLabel;
    addLabel(id: number, label: string, x: number, y: number, width: number, height: number, color: number): import('./IOverlayLabel').IOverlayLabel;
    addLine(id: number, x1: number, y1: number, x2: number, y2: number, color: number, thickness: number): import('./IOverlayLine').IOverlayLine;
    addLine(id: number, x1: number, y1: number, x2: number, y2: number): import('./IOverlayLine').IOverlayLine;
    getComponent(componentID: number): import('./ICustomOverlayComponent').ICustomOverlayComponent;
    removeComponent(componentID: number): void;
    updateComponent(component: import('./ICustomOverlayComponent').ICustomOverlayComponent): void;
    update(player: import('../entity/IPlayer').IPlayer): void;
    fromNBT(tag: NBTTagCompound): import('./ICustomOverlay').ICustomOverlay;
    toNBT(): NBTTagCompound;

}
