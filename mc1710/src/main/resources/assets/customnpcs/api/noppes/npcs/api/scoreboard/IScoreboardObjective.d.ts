/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.scoreboard
 */

/**
 * @javaFqn noppes.npcs.api.scoreboard.IScoreboardObjective
 */
export interface IScoreboardObjective {
    getName(): String;
    getDisplayName(): String;
    setDisplayName(name: String): import('./void').void;
    getCriteria(): String;
    isReadyOnly(): import('./boolean').boolean;
}
