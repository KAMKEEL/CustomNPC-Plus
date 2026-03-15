package noppes.npcs.api.handler.data;

public interface ICustomEffect {

    /**
     * Returns the unique ID of this custom effect.
     *
     * @return the effect ID, or -1 if not yet assigned
     */
    int getID();

    /**
     * Returns the internal name of this custom effect.
     *
     * @return the effect name
     */
    String getName();

    /**
     * Sets the display name shown in menus. Supports color codes using '&amp;' (converted to section signs).
     *
     * @param name the display name to set; ignored if null or empty
     */
    void setMenuName(String name);

    /**
     * Returns the display name shown in menus.
     *
     * @return the menu display name
     */
    String getMenuName();

    /**
     * Sets the internal name of this custom effect.
     *
     * @param name the name to set; ignored if null or empty
     */
    void setName(String name);

    /**
     * Returns the resource path of the icon texture.
     *
     * @return the icon texture path
     */
    String getIcon();

    /**
     * Sets the resource path of the icon texture.
     *
     * @param icon the icon texture path
     */
    void setIcon(String icon);

    /**
     * Returns the tick interval at which this effect fires its script tick event.
     *
     * @return the tick interval (always a multiple of 10, minimum 10)
     */
    int getEveryXTick();

    /**
     * Sets the tick interval at which this effect fires its script tick event.
     * The value is rounded to the nearest multiple of 10 with a minimum of 10.
     *
     * @param everyXTick the desired tick interval
     */
    void setEveryXTick(int everyXTick);

    /**
     * Returns the X offset of the icon within the texture.
     *
     * @return the icon X offset in pixels
     */
    int getIconX();

    /**
     * Sets the X offset of the icon within the texture.
     *
     * @param iconX the icon X offset in pixels
     */
    void setIconX(int iconX);

    /**
     * Returns the Y offset of the icon within the texture.
     *
     * @return the icon Y offset in pixels
     */
    int getIconY();

    /**
     * Sets the Y offset of the icon within the texture.
     *
     * @param iconY the icon Y offset in pixels
     */
    void setIconY(int iconY);

    /**
     * Returns the width of the icon.
     *
     * @return the icon width in pixels
     */
    int getWidth();

    /**
     * Sets the width of the icon.
     *
     * @param width the icon width in pixels
     */
    void setWidth(int width);

    /**
     * Returns the height of the icon.
     *
     * @return the icon height in pixels
     */
    int getHeight();

    /**
     * Sets the height of the icon.
     *
     * @param height the icon height in pixels
     */
    void setHeight(int height);

    /**
     * Returns whether this effect is removed when the player dies.
     *
     * @return true if the effect is lost on death
     */
    boolean isLossOnDeath();

    /**
     * Sets whether this effect is removed when the player dies.
     *
     * @param lossOnDeath true to remove the effect on death
     */
    void setLossOnDeath(boolean lossOnDeath);

    /**
     * Saves this custom effect to the effect controller.
     *
     * @return the saved effect instance
     */
    ICustomEffect save();

    /**
     * Sets the unique ID of this custom effect.
     *
     * @param id the effect ID to assign
     */
    void setID(int id);

    /**
     * Utilized by DBC Addon or other Addons to tell
     * which map of Effects to Match to.
     * Index: 0 - CNPC+
     * Index: 1 - DBC Addon
     *
     * @return the effect's index in the effect list
     */
    int getIndex();

    /**
     * Returns whether the icon texture is animated.
     *
     * @return true if the icon is animated
     */
    boolean isAnimated();

    /**
     * Sets whether the icon texture is animated.
     *
     * @param animated true to enable icon animation
     */
    void setAnimated(boolean animated);

    /**
     * Returns the number of frames in the icon animation.
     *
     * @return the frame count (minimum 1)
     */
    int getFrameCount();

    /**
     * Sets the number of frames in the icon animation.
     * Values less than 1 are clamped to 1.
     *
     * @param frameCount the number of animation frames
     */
    void setFrameCount(int frameCount);

    /**
     * Returns the duration of each animation frame in ticks.
     *
     * @return the frame time in ticks (minimum 1)
     */
    int getFrameTime();

    /**
     * Sets the duration of each animation frame in ticks.
     * Values less than 1 are clamped to 1.
     *
     * @param frametime the frame duration in ticks
     */
    void setFrameTime(int frametime);
}


