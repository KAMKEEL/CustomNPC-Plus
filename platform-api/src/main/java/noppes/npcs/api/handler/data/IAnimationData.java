package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IAnimatable;

/**
 * Holds and manages animation data for an animatable entity.
 * This includes the current animation, its timing, and the ability to update
 * the animation state on clients.
 */
public interface IAnimationData {

    /**
     * Retrieves the animatable entity this data is associated with.
     *
     * @return the animatable entity.
     */
    IAnimatable getEntity();

    /**
     * Updates the animation data on the client side.
     */
    void updateClient();

    /**
     * Checks whether the current animation is active.
     *
     * @return true if an animation is active; false otherwise.
     */
    boolean isActive();

    /**
     * Checks whether the client is currently animating.
     *
     * @return true if client animation is active; false otherwise.
     */
    boolean isClientAnimating();

    /**
     * Enables or disables animations for the entity.
     *
     * @param enabled true to enable animations; false to disable.
     */
    void setEnabled(boolean enabled);

    /**
     * Checks if animations are enabled.
     *
     * @return true if enabled; false otherwise.
     */
    boolean enabled();

    /**
     * Sets the current animation.
     *
     * @param animation the animation to set.
     */
    void setAnimation(IAnimation animation);

    /**
     * Returns the current animation.
     *
     * @return the animation, or null if none is set.
     */
    IAnimation getAnimation();

    /**
     * Returns the total time the current animation has been running.
     *
     * @return the animating time in ticks.
     */
    long getAnimatingTime();
}
