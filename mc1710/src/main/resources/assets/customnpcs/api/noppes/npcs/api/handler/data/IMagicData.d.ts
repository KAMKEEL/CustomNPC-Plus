/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IMagicData
 */
export interface IMagicData {
    /**
     * @param id The ID of the Magic
     */
    removeMagic(id: import('./int').int): import('./void').void;
    /**
     * @param id The ID of the Magic
     * @return If the Magic exists
     */
    hasMagic(id: import('./int').int): import('./boolean').boolean;
    /**
     * Clears the Magics
     */
    clear(): import('./void').void;
    /**
     * @return If the Magics are empty
     */
    isEmpty(): import('./boolean').boolean;
    /**
     * @param id     The ID of the Magic
     * @param damage The bonus damage for the Magic
     * @param split  The split of the Magic
     */
    addMagic(id: import('./int').int, damage: import('./float').float, split: import('./float').float): import('./void').void;
    /**
     * @param id The ID of the Magic
     * @return The bonus damage for the Magic
     */
    getMagicDamage(id: import('./int').int): import('./float').float;
    /**
     * @param id The ID of the Magic
     * @return The split of the Magic
     */
    getMagicSplit(id: import('./int').int): import('./float').float;
}
