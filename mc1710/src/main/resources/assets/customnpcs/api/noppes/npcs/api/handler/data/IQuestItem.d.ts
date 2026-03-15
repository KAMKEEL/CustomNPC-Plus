/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Quest objective interface for item-collection quests.
 * Configures whether items are consumed on turn-in and matching criteria.
  * @javaFqn noppes.npcs.api.handler.data.IQuestItem
*/
export interface IQuestItem extends import('./IQuestInterface').IQuestInterface {
    /** @param leaveItems true to leave items in the player's inventory on turn-in. */
    setLeaveItems(leaveItems: import('./boolean').boolean): import('./void').void;
    /** @return true if items remain in the player's inventory on turn-in. */
    getLeaveItems(): import('./boolean').boolean;
    /** @param ignoreDamage true to ignore item damage when matching. */
    setIgnoreDamage(ignoreDamage: import('./boolean').boolean): import('./void').void;
    /** @return true if item damage is ignored when matching. */
    getIgnoreDamage(): import('./boolean').boolean;
    /** @param ignoreNbt true to ignore NBT data when matching. */
    setIgnoreNbt(ignoreNbt: import('./boolean').boolean): import('./void').void;
    /** @return true if NBT data is ignored when matching. */
    getIgnoreNbt(): import('./boolean').boolean;
    items: import('./NpcMiscInventory').NpcMiscInventory;
    leaveItems: import('./boolean').boolean;
    ignoreDamage: import('./boolean').boolean;
    ignoreNBT: import('./boolean').boolean;
}
