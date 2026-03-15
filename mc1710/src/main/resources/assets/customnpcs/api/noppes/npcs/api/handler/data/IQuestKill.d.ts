/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Quest objective interface for kill-type quests.
 * Configures the target type for kill counting.
  * @javaFqn noppes.npcs.api.handler.data.IQuestKill
*/
export interface IQuestKill extends import('./IQuestInterface').IQuestInterface {
    /**
     * Sets the target matching type.
     *
     * @param type 0: by entity name, 1: by faction.
     */
    setTargetType(type: import('./int').int): import('./void').void;
    /**
     * @return the target matching type (0: entity name, 1: faction).
     */
    getTargetType(): import('./int').int;
    targets: Java.java.util.TreeMap<String, Integer>;
    targetType: import('./int').int;
    customTargetType: String;
}
