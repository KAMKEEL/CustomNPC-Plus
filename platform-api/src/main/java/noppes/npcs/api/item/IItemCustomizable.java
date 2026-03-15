package noppes.npcs.api.item;

public interface IItemCustomizable extends IItemStack {

    Object getScriptHandler();

    int getMaxStackSize();

    /**
     * Gets the armor type for the scripted item.
     *
     * @return The armor type
     */
    int getArmorType();

    /**
     * Checks if the scripted item is a tool. Allows for enchanting
     *
     * @return True if the item is a tool, false otherwise
     */
    boolean isTool();

    /**
     * Checks if the scripted item is a normal item.
     *
     * @return True if the item is a normal item, false otherwise
     */
    boolean isNormalItem();

    /**
     * Gets the dig speed for the scripted item.
     *
     * @return The dig speed
     */
    int getDigSpeed();

    /**
     * Gets the current durability value for the scripted item.
     *
     * @return The durability value
     */
    double getDurabilityValue();

    /**
     * Gets the maximum item use duration for the scripted item.
     *
     * @return The maximum item use duration
     */
    int getMaxItemUseDuration();

    /**
     * Gets the item use action for the scripted item.
     *
     * @return The item use action
     */
    int getItemUseAction();

    /**
     * Gets the enchantability for the scripted item.
     *
     * @return The enchantability
     */
    int getEnchantability();

    /**
     * Gets the attack speed for the scripted item.
     * Speed is the max hurt resistant time in ticks (20 ticks = 1 second)
     *
     * @return The attack speed
     */
    int getAttackSpeed();

    /**
     * Gets the texture path for the scripted item.
     *
     * @return The texture path
     */
    String getTexture();

    /**
     * Sets the texture path for the scripted item. Can be a URL
     *
     * @param texture The texture path
     */
    void setTexture(String texture);

    /**
     * Checks if the durability bar should be shown for the scripted item.
     *
     * @return True if the durability bar should be shown, false otherwise
     */
    Boolean getDurabilityShow();

    /**
     * Sets whether the durability bar should be shown for the scripted item.
     *
     * @param durabilityShow True if the durability bar should be shown, false otherwise
     */
    void setDurabilityShow(Boolean durabilityShow);

    /**
     * Gets the color of the durability bar for the scripted item.
     *
     * @return The durability bar color
     */
    Integer getDurabilityColor();

    /**
     * Sets the color of the durability bar for the scripted item.
     *
     * @param durabilityColor The durability bar color
     */
    void setDurabilityColor(Integer durabilityColor);

    /**
     * Gets the color of the scripted item.
     *
     * @return The item color
     */
    Integer getColor();

    /**
     * Sets the color of the scripted item.
     *
     * @param color The item color
     */
    void setColor(Integer color);

    /**
     * Sets the rotation values for the scripted item.
     *
     * @param rotationX The X-axis rotation
     * @param rotationY The Y-axis rotation
     * @param rotationZ The Z-axis rotation
     */
    void setRotation(Float rotationX, Float rotationY, Float rotationZ);

    /**
     * Sets the rotation rate values for the scripted item. Spinning Speed
     *
     * @param rotationXRate The X-axis rotation rate
     * @param rotationYRate The Y-axis rotation rate
     * @param rotationZRate The Z-axis rotation rate
     */
    void setRotationRate(Float rotationXRate, Float rotationYRate, Float rotationZRate);

    /**
     * Sets the scale values for the scripted item.
     *
     * @param scaleX The X-axis scale
     * @param scaleY The Y-axis scale
     * @param scaleZ The Z-axis scale
     */
    void setScale(Float scaleX, Float scaleY, Float scaleZ);

    /**
     * Sets the translation values for the scripted item.
     *
     * @param translateX The X-axis translation
     * @param translateY The Y-axis translation
     * @param translateZ The Z-axis translation
     */
    void setTranslate(Float translateX, Float translateY, Float translateZ);

    /**
     * Gets the X-axis rotation for the scripted item.
     *
     * @return The X-axis rotation
     */
    Float getRotationX();

    /**
     * Gets the Y-axis rotation for the scripted item.
     *
     * @return The Y-axis rotation
     */
    Float getRotationY();

    /**
     * Gets the Z-axis rotation for the scripted item.
     *
     * @return The Z-axis rotation
     */
    Float getRotationZ();

    /**
     * Gets the X-axis rotation rate for the scripted item.
     *
     * @return The X-axis rotation rate
     */
    Float getRotationXRate();

    /**
     * Gets the Y-axis rotation rate for the scripted item.
     *
     * @return The Y-axis rotation rate
     */
    Float getRotationYRate();

    /**
     * Gets the Z-axis rotation rate for the scripted item.
     *
     * @return The Z-axis rotation rate
     */
    Float getRotationZRate();

    /**
     * Gets the X-axis scale for the scripted item.
     *
     * @return The X-axis scale
     */
    Float getScaleX();

    /**
     * Gets the Y-axis scale for the scripted item.
     *
     * @return The Y-axis scale
     */
    Float getScaleY();

    /**
     * Gets the Z-axis scale for the scripted item.
     *
     * @return The Z-axis scale
     */
    Float getScaleZ();

    /**
     * Gets the X-axis translation for the scripted item.
     *
     * @return The X-axis translation
     */
    Float getTranslateX();

    /**
     * Gets the Y-axis translation for the scripted item.
     *
     * @return The Y-axis translation
     */
    Float getTranslateY();

    /**
     * Gets the Z-axis translation for the scripted item.
     *
     * @return The Z-axis translation
     */
    Float getTranslateZ();

    /**
     * Returns whether the item texture has animation enabled.
     *
     * @return True if the texture is animated, false otherwise
     */
    Boolean isTextureAnimated();

    /**
     * Sets whether the item texture should be animated.
     *
     * @param animated True to enable animation
     */
    void setTextureAnimated(Boolean animated);

    /**
     * Gets the number of animation frames in the texture strip.
     *
     * @return The frame count
     */
    Integer getFrameCount();

    /**
     * Sets the number of animation frames.
     *
     * @param frameCount The frame count
     */
    void setFrameCount(Integer frameCount);

    /**
     * Gets the ticks per animation frame.
     *
     * @return The frame time in ticks
     */
    Integer getFrameTime();

    /**
     * Sets the ticks per animation frame.
     *
     * @param frametime The frame time in ticks
     */
    void setFrameTime(Integer frametime);
}
