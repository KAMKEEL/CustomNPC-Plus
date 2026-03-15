/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Represents a Pixelmon (a tameable creature) with additional attributes
 * such as shiny state, level, IV/EV values, nature, moves, and more.
 *
 * @param <T> The underlying Minecraft EntityTameable type.
  * @javaFqn noppes.npcs.api.entity.IPixelmon
*/
export interface IPixelmon<T extends EntityTameable /* net.minecraft.entity.passive.EntityTameable */> extends import('./IAnimal').IAnimal {
    /**
     * @return true if the Pixelmon is shiny.
     */
    getIsShiny(): import('./boolean').boolean;
    /**
     * Sets whether the Pixelmon is shiny.
     *
     * @param bo true for shiny.
     */
    setIsShiny(bo: import('./boolean').boolean): import('./void').void;
    /**
     * @return The Pixelmon's level.
     */
    getLevel(): import('./int').int;
    /**
     * Sets the Pixelmon's level.
     *
     * @param level the new level.
     */
    setLevel(level: import('./int').int): import('./void').void;
    /**
     * Gets the Individual Value (IV) for the specified stat.
     *
     * @param type 0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @return the IV value, or -1 if invalid.
     */
    getIV(type: import('./int').int): import('./int').int;
    /**
     * Sets the Individual Value (IV) for the specified stat.
     *
     * @param type  0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @param value the new IV value.
     */
    setIV(type: import('./int').int, value: import('./int').int): import('./void').void;
    /**
     * Gets the Effort Value (EV) for the specified stat.
     *
     * @param type 0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @return the EV value, or -1 if invalid.
     */
    getEV(type: import('./int').int): import('./int').int;
    /**
     * Sets the Effort Value (EV) for the specified stat.
     *
     * @param type  0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @param value the new EV value.
     */
    setEV(type: import('./int').int, value: import('./int').int): import('./void').void;
    /**
     * Gets the calculated stat for the specified stat type.
     *
     * @param type 0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @return the stat value, or -1 if invalid.
     */
    getStat(type: import('./int').int): import('./int').int;
    /**
     * Sets the calculated stat for the specified stat type.
     *
     * @param type  0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @param value the new stat value.
     */
    setStat(type: import('./int').int, value: import('./int').int): import('./void').void;
    /**
     * @return The Pixelmon's size type (0: Pygmy, 1: Runt, 2: Small, 3: Normal, 4: Huge, 5: Giant,
     * 6: Enormous, 7: Ginormous, 8: Microscopic).
     */
    getSize(): import('./int').int;
    /**
     * Sets the Pixelmon's size type.
     *
     * @param type the size type.
     */
    setSize(type: import('./int').int): import('./void').void;
    /**
     * @return The Pixelmon's happiness (0-255).
     */
    getHapiness(): import('./int').int;
    /**
     * Sets the Pixelmon's happiness.
     *
     * @param value a value between 0 and 255.
     */
    setHapiness(value: import('./int').int): import('./void').void;
    /**
     * @return The Pixelmon's nature as an integer (see nature definitions).
     */
    getNature(): import('./int').int;
    /**
     * Sets the Pixelmon's nature.
     *
     * @param type the nature value.
     */
    setNature(type: import('./int').int): import('./void').void;
    /**
     * @return The type of Poké Ball in which the Pixelmon is contained
     * (-1: Uncaught, 0: Pokéball, 1: Great Ball, etc.).
     */
    getPokeball(): import('./int').int;
    /**
     * Sets the type of Poké Ball for this Pixelmon.
     *
     * @param type -1 for Uncaught, 0 for Pokéball, etc.
     */
    setPokeball(type: import('./int').int): import('./void').void;
    /**
     * @return The Pixelmon's nickname.
     */
    getNickname(): String;
    /**
     * @return true if the Pixelmon has a nickname.
     */
    hasNickname(): import('./boolean').boolean;
    /**
     * Sets the Pixelmon's nickname.
     *
     * @param name the new nickname.
     */
    setNickname(name: String): import('./void').void;
    /**
     * Returns the name of the move in the specified slot.
     *
     * @param slot the move slot.
     * @return the move name.
     */
    getMove(slot: import('./int').int): String;
    /**
     * Sets the move in the specified slot.
     *
     * @param slot the move slot.
     * @param move the move name.
     */
    setMove(slot: import('./int').int, move: String): import('./void').void;
}
