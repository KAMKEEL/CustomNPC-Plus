/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface IAnimationHandler {

    // Methods
    saveAnimation(animation: import('./data/IAnimation').IAnimation): import('./data/IAnimation').IAnimation;
    delete(name: string): void;
    delete(id: number): void;
    has(name: string): boolean;
    get(name: string): import('./data/IAnimation').IAnimation;
    get(id: number): import('./data/IAnimation').IAnimation;
    getAnimations(): import('./data/IAnimation').IAnimation[];

}
