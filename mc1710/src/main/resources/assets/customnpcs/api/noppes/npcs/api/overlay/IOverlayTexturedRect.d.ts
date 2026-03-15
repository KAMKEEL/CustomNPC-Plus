/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.overlay
 */

/**
 * @javaFqn noppes.npcs.api.overlay.IOverlayTexturedRect
 */
export interface IOverlayTexturedRect extends import('./ICustomOverlayComponent').ICustomOverlayComponent {
    getTexture(): String;
    setTexture(var1: String): import('./IOverlayTexturedRect').IOverlayTexturedRect;
    getWidth(): import('./int').int;
    getHeight(): import('./int').int;
    setSize(var1: import('./int').int, var2: import('./int').int): import('./IOverlayTexturedRect').IOverlayTexturedRect;
    getScale(): import('./float').float;
    setScale(var1: import('./float').float): import('./IOverlayTexturedRect').IOverlayTexturedRect;
    getTextureX(): import('./int').int;
    getTextureY(): import('./int').int;
    setTextureOffset(var1: import('./int').int, var2: import('./int').int): import('./IOverlayTexturedRect').IOverlayTexturedRect;
}
