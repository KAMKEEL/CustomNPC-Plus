/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IOverlayHandler {

    // Methods
    add(id: number, overlay: import('../ISkinOverlay').ISkinOverlay): void;
    get(id: number): import('../ISkinOverlay').ISkinOverlay;
    has(id: number): boolean;
    remove(id: number): boolean;
    size(): number;
    clear(): void;

}
