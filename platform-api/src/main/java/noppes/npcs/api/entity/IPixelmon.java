package noppes.npcs.api.entity;


/**
 * Represents a Pixelmon (a tameable creature) with additional attributes
 * such as shiny state, level, IV/EV values, nature, moves, and more.
 *
 */
public interface IPixelmon extends IAnimal {

    /**
     * @return true if the Pixelmon is shiny.
     */
    boolean getIsShiny();

    /**
     * Sets whether the Pixelmon is shiny.
     *
     * @param bo true for shiny.
     */
    void setIsShiny(boolean bo);

    /**
     * @return The Pixelmon's level.
     */
    int getLevel();

    /**
     * Sets the Pixelmon's level.
     *
     * @param level the new level.
     */
    void setLevel(int level);

    /**
     * Gets the Individual Value (IV) for the specified stat.
     *
     * @param type 0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @return the IV value, or -1 if invalid.
     */
    int getIV(int type);

    /**
     * Sets the Individual Value (IV) for the specified stat.
     *
     * @param type  0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @param value the new IV value.
     */
    void setIV(int type, int value);

    /**
     * Gets the Effort Value (EV) for the specified stat.
     *
     * @param type 0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @return the EV value, or -1 if invalid.
     */
    int getEV(int type);

    /**
     * Sets the Effort Value (EV) for the specified stat.
     *
     * @param type  0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @param value the new EV value.
     */
    void setEV(int type, int value);

    /**
     * Gets the calculated stat for the specified stat type.
     *
     * @param type 0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @return the stat value, or -1 if invalid.
     */
    int getStat(int type);

    /**
     * Sets the calculated stat for the specified stat type.
     *
     * @param type  0: HP, 1: Attack, 2: Defense, 3: SpAttack, 4: SpDefense, 5: Speed
     * @param value the new stat value.
     */
    void setStat(int type, int value);

    /**
     * @return The Pixelmon's size type (0: Pygmy, 1: Runt, 2: Small, 3: Normal, 4: Huge, 5: Giant,
     * 6: Enormous, 7: Ginormous, 8: Microscopic).
     */
    int getSize();

    /**
     * Sets the Pixelmon's size type.
     *
     * @param type the size type.
     */
    void setSize(int type);

    /**
     * @return The Pixelmon's happiness (0-255).
     */
    int getHapiness();

    /**
     * Sets the Pixelmon's happiness.
     *
     * @param value a value between 0 and 255.
     */
    void setHapiness(int value);

    /**
     * @return The Pixelmon's nature as an integer (see nature definitions).
     */
    int getNature();

    /**
     * Sets the Pixelmon's nature.
     *
     * @param type the nature value.
     */
    void setNature(int type);

    /**
     * @return The type of Poké Ball in which the Pixelmon is contained
     * (-1: Uncaught, 0: Pokéball, 1: Great Ball, etc.).
     */
    int getPokeball();

    /**
     * Sets the type of Poké Ball for this Pixelmon.
     *
     * @param type -1 for Uncaught, 0 for Pokéball, etc.
     */
    void setPokeball(int type);

    /**
     * @return The Pixelmon's nickname.
     */
    String getNickname();

    /**
     * @return true if the Pixelmon has a nickname.
     */
    boolean hasNickname();

    /**
     * Sets the Pixelmon's nickname.
     *
     * @param name the new nickname.
     */
    void setNickname(String name);

    /**
     * Returns the name of the move in the specified slot.
     *
     * @param slot the move slot.
     * @return the move name.
     */
    String getMove(int slot);

    /**
     * Sets the move in the specified slot.
     *
     * @param slot the move slot.
     * @param move the move name.
     */
    void setMove(int slot, String move);
}
