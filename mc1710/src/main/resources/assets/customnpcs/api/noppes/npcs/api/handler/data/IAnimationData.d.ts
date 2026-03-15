/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Holds and manages animation data for an animatable entity.
 * This includes the current animation, its timing, and the ability to update
 * the animation state on clients.
  * @javaFqn noppes.npcs.api.handler.data.IAnimationData
*/
export interface IAnimationData {
    /**
     * Retrieves the animatable entity this data is associated with.
     *
     * @return the animatable entity.
     */
    getEntity(): import('../../entity/IAnimatable').IAnimatable;
    /**
     * Updates the animation data on the client side.
     */
    updateClient(): import('./void').void;
    /**
     * Checks whether the current animation is active.
     *
     * @return true if an animation is active; false otherwise.
     */
    isActive(): import('./boolean').boolean;
    /**
     * Checks whether the client is currently animating.
     *
     * @return true if client animation is active; false otherwise.
     */
    isClientAnimating(): import('./boolean').boolean;
    /**
     * Enables or disables animations for the entity.
     *
     * @param enabled true to enable animations; false to disable.
     */
    setEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * Checks if animations are enabled.
     *
     * @return true if enabled; false otherwise.
     */
    enabled(): import('./boolean').boolean;
    /**
     * Sets the current animation.
     *
     * @param animation the animation to set.
     */
    setAnimation(animation: import('./IAnimation').IAnimation): import('./void').void;
    /**
     * Returns the current animation.
     *
     * @return the animation, or null if none is set.
     */
    getAnimation(): import('./IAnimation').IAnimation;
    /**
     * Returns the total time the current animation has been running.
     *
     * @return the animating time in ticks.
     */
    getAnimatingTime(): import('./long').long;
    parent: Object;
    animation: import('./Animation').Animation;
    allowAnimation: import('./boolean').boolean;
    animatingTime: import('./long').long;
    currentClientAnimation: import('./Animation').Animation;
    finishedTime: import('./int').int;
    finishedFrame: import('./int').int;
}
