/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * @javaFqn noppes.npcs.api.handler.IAnimationHandler
 */
export interface IAnimationHandler {
    saveAnimation(animation: import('./data/IAnimation').IAnimation): import('./data/IAnimation').IAnimation;
    delete(name: String): import('./void').void;
    delete(id: import('./int').int): import('./void').void;
    has(name: String): import('./boolean').boolean;
    get(name: String): import('./data/IAnimation').IAnimation;
    get(id: import('./int').int): import('./data/IAnimation').IAnimation;
    getAnimations(): import('./data/IAnimation').IAnimation[];
    /**
     * Get all built-in animations.
     * Built-in animations are read-only and cannot be modified or deleted.
     *
     * @return array of all built-in animations
     */
    getBuiltInAnimations(): import('./data/IAnimation').IAnimation[];
    /**
     * Get all animations (both built-in and user-created).
     *
     * @return array of all animations (built-in and custom)
     */
    getAllAnimations(): import('./data/IAnimation').IAnimation[];
    /**
     * Check if an animation name is a built-in animation.
     *
     * @param name The animation name to check
     * @return true if this is a built-in animation
     */
    isBuiltIn(name: String): import('./boolean').boolean;
    /**
     * Get names of all built-in animations.
     *
     * @return array of all built-in animation names
     */
    getBuiltInAnimationNames(): String[];
}
