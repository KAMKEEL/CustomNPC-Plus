/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.ICustomEffect
 */
export interface ICustomEffect {
    /**
     * Returns the unique ID of this custom effect.
     *
     * @return the effect ID, or -1 if not yet assigned
     */
    getID(): import('./int').int;
    /**
     * Returns the internal name of this custom effect.
     *
     * @return the effect name
     */
    getName(): String;
    /**
     * Sets the display name shown in menus. Supports color codes using '&amp;' (converted to section signs).
     *
     * @param name the display name to set; ignored if null or empty
     */
    setMenuName(name: String): import('./void').void;
    /**
     * Returns the display name shown in menus.
     *
     * @return the menu display name
     */
    getMenuName(): String;
    /**
     * Sets the internal name of this custom effect.
     *
     * @param name the name to set; ignored if null or empty
     */
    setName(name: String): import('./void').void;
    /**
     * Returns the resource path of the icon texture.
     *
     * @return the icon texture path
     */
    getIcon(): String;
    /**
     * Sets the resource path of the icon texture.
     *
     * @param icon the icon texture path
     */
    setIcon(icon: String): import('./void').void;
    /**
     * Returns the tick interval at which this effect fires its script tick event.
     *
     * @return the tick interval (always a multiple of 10, minimum 10)
     */
    getEveryXTick(): import('./int').int;
    /**
     * Sets the tick interval at which this effect fires its script tick event.
     * The value is rounded to the nearest multiple of 10 with a minimum of 10.
     *
     * @param everyXTick the desired tick interval
     */
    setEveryXTick(everyXTick: import('./int').int): import('./void').void;
    /**
     * Returns the X offset of the icon within the texture.
     *
     * @return the icon X offset in pixels
     */
    getIconX(): import('./int').int;
    /**
     * Sets the X offset of the icon within the texture.
     *
     * @param iconX the icon X offset in pixels
     */
    setIconX(iconX: import('./int').int): import('./void').void;
    /**
     * Returns the Y offset of the icon within the texture.
     *
     * @return the icon Y offset in pixels
     */
    getIconY(): import('./int').int;
    /**
     * Sets the Y offset of the icon within the texture.
     *
     * @param iconY the icon Y offset in pixels
     */
    setIconY(iconY: import('./int').int): import('./void').void;
    /**
     * Returns the width of the icon.
     *
     * @return the icon width in pixels
     */
    getWidth(): import('./int').int;
    /**
     * Sets the width of the icon.
     *
     * @param width the icon width in pixels
     */
    setWidth(width: import('./int').int): import('./void').void;
    /**
     * Returns the height of the icon.
     *
     * @return the icon height in pixels
     */
    getHeight(): import('./int').int;
    /**
     * Sets the height of the icon.
     *
     * @param height the icon height in pixels
     */
    setHeight(height: import('./int').int): import('./void').void;
    /**
     * Returns whether this effect is removed when the player dies.
     *
     * @return true if the effect is lost on death
     */
    isLossOnDeath(): import('./boolean').boolean;
    /**
     * Sets whether this effect is removed when the player dies.
     *
     * @param lossOnDeath true to remove the effect on death
     */
    setLossOnDeath(lossOnDeath: import('./boolean').boolean): import('./void').void;
    /**
     * Saves this custom effect to the effect controller.
     *
     * @return the saved effect instance
     */
    save(): import('./ICustomEffect').ICustomEffect;
    /**
     * Sets the unique ID of this custom effect.
     *
     * @param id the effect ID to assign
     */
    setID(id: import('./int').int): import('./void').void;
    /**
     * Utilized by DBC Addon or other Addons to tell
     * which map of Effects to Match to.
     * Index: 0 - CNPC+
     * Index: 1 - DBC Addon
     *
     * @return the effect's index in the effect list
     */
    getIndex(): import('./int').int;
    /**
     * Returns whether the icon texture is animated.
     *
     * @return true if the icon is animated
     */
    isAnimated(): import('./boolean').boolean;
    /**
     * Sets whether the icon texture is animated.
     *
     * @param animated true to enable icon animation
     */
    setAnimated(animated: import('./boolean').boolean): import('./void').void;
    /**
     * Returns the number of frames in the icon animation.
     *
     * @return the frame count (minimum 1)
     */
    getFrameCount(): import('./int').int;
    /**
     * Sets the number of frames in the icon animation.
     * Values less than 1 are clamped to 1.
     *
     * @param frameCount the number of animation frames
     */
    setFrameCount(frameCount: import('./int').int): import('./void').void;
    /**
     * Returns the duration of each animation frame in ticks.
     *
     * @return the frame time in ticks (minimum 1)
     */
    getFrameTime(): import('./int').int;
    /**
     * Sets the duration of each animation frame in ticks.
     * Values less than 1 are clamped to 1.
     *
     * @param frametime the frame duration in ticks
     */
    setFrameTime(frametime: import('./int').int): import('./void').void;
    id: import('./int').int;
    name: String;
    lossOnDeath: import('./boolean').boolean;
    length: import('./int').int;
    everyXTick: import('./int').int;
    icon: String;
    iconX: import('./int').int;
    iconY: import('./int').int;
    menuName: String;
    width: import('./int').int;
    height: import('./int').int;
    animated: import('./boolean').boolean;
    frameCount: import('./int').int;
    frametime: import('./int').int;
    index: import('./int').int;
    tagUUIDs: import('./UUID').UUID[];
}
