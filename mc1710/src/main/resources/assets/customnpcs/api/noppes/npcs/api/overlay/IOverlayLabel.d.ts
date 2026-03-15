/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.overlay
 */

/**
 * @javaFqn noppes.npcs.api.overlay.IOverlayLabel
 */
export interface IOverlayLabel extends import('./ICustomOverlayComponent').ICustomOverlayComponent {
    getText(): String;
    setText(var1: String): import('./IOverlayLabel').IOverlayLabel;
    getWidth(): import('./int').int;
    getHeight(): import('./int').int;
    setSize(var1: import('./int').int, var2: import('./int').int): import('./IOverlayLabel').IOverlayLabel;
    getScale(): import('./float').float;
    setScale(var1: import('./float').float): import('./IOverlayLabel').IOverlayLabel;
    getShadow(): import('./boolean').boolean;
    setShadow(shadow: import('./boolean').boolean): import('./void').void;
}
