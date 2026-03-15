/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.item
 */

/**
 * @javaFqn noppes.npcs.api.item.IItemCustomizable
 */
export interface IItemCustomizable extends import('./IItemStack').IItemStack {
    getScriptHandler(): Object;
    getMaxStackSize(): import('./int').int;
    /**
     * Gets the armor type for the scripted item.
     *
     * @return The armor type
     */
    getArmorType(): import('./int').int;
    /**
     * Checks if the scripted item is a tool. Allows for enchanting
     *
     * @return True if the item is a tool, false otherwise
     */
    isTool(): import('./boolean').boolean;
    /**
     * Checks if the scripted item is a normal item.
     *
     * @return True if the item is a normal item, false otherwise
     */
    isNormalItem(): import('./boolean').boolean;
    /**
     * Gets the dig speed for the scripted item.
     *
     * @return The dig speed
     */
    getDigSpeed(): import('./int').int;
    /**
     * Gets the current durability value for the scripted item.
     *
     * @return The durability value
     */
    getDurabilityValue(): import('./double').double;
    /**
     * Gets the maximum item use duration for the scripted item.
     *
     * @return The maximum item use duration
     */
    getMaxItemUseDuration(): import('./int').int;
    /**
     * Gets the item use action for the scripted item.
     *
     * @return The item use action
     */
    getItemUseAction(): import('./int').int;
    /**
     * Gets the enchantability for the scripted item.
     *
     * @return The enchantability
     */
    getEnchantability(): import('./int').int;
    /**
     * Gets the attack speed for the scripted item.
     * Speed is the max hurt resistant time in ticks (20 ticks = 1 second)
     *
     * @return The attack speed
     */
    getAttackSpeed(): import('./int').int;
    /**
     * Gets the texture path for the scripted item.
     *
     * @return The texture path
     */
    getTexture(): String;
    /**
     * Sets the texture path for the scripted item. Can be a URL
     *
     * @param texture The texture path
     */
    setTexture(texture: String): import('./void').void;
    /**
     * Checks if the durability bar should be shown for the scripted item.
     *
     * @return True if the durability bar should be shown, false otherwise
     */
    getDurabilityShow(): Boolean;
    /**
     * Sets whether the durability bar should be shown for the scripted item.
     *
     * @param durabilityShow True if the durability bar should be shown, false otherwise
     */
    setDurabilityShow(durabilityShow: Boolean): import('./void').void;
    /**
     * Gets the color of the durability bar for the scripted item.
     *
     * @return The durability bar color
     */
    getDurabilityColor(): Integer;
    /**
     * Sets the color of the durability bar for the scripted item.
     *
     * @param durabilityColor The durability bar color
     */
    setDurabilityColor(durabilityColor: Integer): import('./void').void;
    /**
     * Gets the color of the scripted item.
     *
     * @return The item color
     */
    getColor(): Integer;
    /**
     * Sets the color of the scripted item.
     *
     * @param color The item color
     */
    setColor(color: Integer): import('./void').void;
    /**
     * Sets the rotation values for the scripted item.
     *
     * @param rotationX The X-axis rotation
     * @param rotationY The Y-axis rotation
     * @param rotationZ The Z-axis rotation
     */
    setRotation(rotationX: Float, rotationY: Float, rotationZ: Float): import('./void').void;
    /**
     * Sets the rotation rate values for the scripted item. Spinning Speed
     *
     * @param rotationXRate The X-axis rotation rate
     * @param rotationYRate The Y-axis rotation rate
     * @param rotationZRate The Z-axis rotation rate
     */
    setRotationRate(rotationXRate: Float, rotationYRate: Float, rotationZRate: Float): import('./void').void;
    /**
     * Sets the scale values for the scripted item.
     *
     * @param scaleX The X-axis scale
     * @param scaleY The Y-axis scale
     * @param scaleZ The Z-axis scale
     */
    setScale(scaleX: Float, scaleY: Float, scaleZ: Float): import('./void').void;
    /**
     * Sets the translation values for the scripted item.
     *
     * @param translateX The X-axis translation
     * @param translateY The Y-axis translation
     * @param translateZ The Z-axis translation
     */
    setTranslate(translateX: Float, translateY: Float, translateZ: Float): import('./void').void;
    /**
     * Gets the X-axis rotation for the scripted item.
     *
     * @return The X-axis rotation
     */
    getRotationX(): Float;
    /**
     * Gets the Y-axis rotation for the scripted item.
     *
     * @return The Y-axis rotation
     */
    getRotationY(): Float;
    /**
     * Gets the Z-axis rotation for the scripted item.
     *
     * @return The Z-axis rotation
     */
    getRotationZ(): Float;
    /**
     * Gets the X-axis rotation rate for the scripted item.
     *
     * @return The X-axis rotation rate
     */
    getRotationXRate(): Float;
    /**
     * Gets the Y-axis rotation rate for the scripted item.
     *
     * @return The Y-axis rotation rate
     */
    getRotationYRate(): Float;
    /**
     * Gets the Z-axis rotation rate for the scripted item.
     *
     * @return The Z-axis rotation rate
     */
    getRotationZRate(): Float;
    /**
     * Gets the X-axis scale for the scripted item.
     *
     * @return The X-axis scale
     */
    getScaleX(): Float;
    /**
     * Gets the Y-axis scale for the scripted item.
     *
     * @return The Y-axis scale
     */
    getScaleY(): Float;
    /**
     * Gets the Z-axis scale for the scripted item.
     *
     * @return The Z-axis scale
     */
    getScaleZ(): Float;
    /**
     * Gets the X-axis translation for the scripted item.
     *
     * @return The X-axis translation
     */
    getTranslateX(): Float;
    /**
     * Gets the Y-axis translation for the scripted item.
     *
     * @return The Y-axis translation
     */
    getTranslateY(): Float;
    /**
     * Gets the Z-axis translation for the scripted item.
     *
     * @return The Z-axis translation
     */
    getTranslateZ(): Float;
    /**
     * Returns whether the item texture has animation enabled.
     *
     * @return True if the texture is animated, false otherwise
     */
    isTextureAnimated(): Boolean;
    /**
     * Sets whether the item texture should be animated.
     *
     * @param animated True to enable animation
     */
    setTextureAnimated(animated: Boolean): import('./void').void;
    /**
     * Gets the number of animation frames in the texture strip.
     *
     * @return The frame count
     */
    getFrameCount(): Integer;
    /**
     * Sets the number of animation frames.
     *
     * @param frameCount The frame count
     */
    setFrameCount(frameCount: Integer): import('./void').void;
    /**
     * Gets the ticks per animation frame.
     *
     * @return The frame time in ticks
     */
    getFrameTime(): Integer;
    /**
     * Sets the ticks per animation frame.
     *
     * @param frametime The frame time in ticks
     */
    setFrameTime(frametime: Integer): import('./void').void;
    readonly itemDisplay: import('./ItemDisplayData').ItemDisplayData;
    getMaxStackSize: import('./abstract int').abstract int;
    getArmorType: import('./abstract int').abstract int;
    isTool: import('./abstract boolean').abstract boolean;
    isNormalItem: import('./abstract boolean').abstract boolean;
    getDigSpeed: import('./abstract int').abstract int;
    getDurabilityValue: import('./abstract double').abstract double;
    getMaxItemUseDuration: import('./abstract int').abstract int;
    getItemUseAction: import('./abstract int').abstract int;
    getEnchantability: import('./abstract int').abstract int;
    getAttackSpeed: import('./abstract int').abstract int;
}
