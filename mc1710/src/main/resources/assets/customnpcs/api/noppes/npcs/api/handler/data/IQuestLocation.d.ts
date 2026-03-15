/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Quest objective interface for location-based quests.
 * Players must visit up to three named locations to complete the quest.
  * @javaFqn noppes.npcs.api.handler.data.IQuestLocation
*/
export interface IQuestLocation extends import('./IQuestInterface').IQuestInterface {
    /** @param loc1 the name of the first location objective. */
    setLocation1(loc1: String): import('./void').void;
    /** @return the name of the first location objective. */
    getLocation1(): String;
    /** @param loc2 the name of the second location objective. */
    setLocation2(loc2: String): import('./void').void;
    /** @return the name of the second location objective. */
    getLocation2(): String;
    /** @param loc3 the name of the third location objective. */
    setLocation3(loc3: String): import('./void').void;
    /** @return the name of the third location objective. */
    getLocation3(): String;
    location: String;
    location2: String;
    location3: String;
}
