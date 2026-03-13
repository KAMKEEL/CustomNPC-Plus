package noppes.npcs.api.handler.data;

public interface IMagicData {

    /**
     * @param id The ID of the Magic
     */
    void removeMagic(int id);

    /**
     * @param id The ID of the Magic
     * @return If the Magic exists
     */
    boolean hasMagic(int id);

    /**
     * Clears the Magics
     */
    void clear();

    /**
     * @return If the Magics are empty
     */
    boolean isEmpty();

    /**
     * @param id     The ID of the Magic
     * @param damage The bonus damage for the Magic
     * @param split  The split of the Magic
     */
    void addMagic(int id, float damage, float split);

    /**
     * @param id The ID of the Magic
     * @return The bonus damage for the Magic
     */
    float getMagicDamage(int id);

    /**
     * @param id The ID of the Magic
     * @return The split of the Magic
     */
    float getMagicSplit(int id);
}
