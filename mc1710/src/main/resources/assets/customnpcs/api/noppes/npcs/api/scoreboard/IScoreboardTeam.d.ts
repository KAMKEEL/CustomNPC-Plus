/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.scoreboard
 */

/**
 * @javaFqn noppes.npcs.api.scoreboard.IScoreboardTeam
 */
export interface IScoreboardTeam {
    getName(): String;
    getDisplayName(): String;
    setDisplayName(name: String): import('./void').void;
    addPlayer(player: String): import('./void').void;
    addPlayer(player: import('../entity/IPlayer').IPlayer): import('./void').void;
    removePlayer(player: String): import('./void').void;
    removePlayer(player: import('../entity/IPlayer').IPlayer): import('./void').void;
    getPlayers(): String[];
    getTeamsize(): import('./int').int;
    clearPlayers(): import('./void').void;
    getFriendlyFire(): import('./boolean').boolean;
    setFriendlyFire(bo: import('./boolean').boolean): import('./void').void;
    setColor(color: String): import('./void').void;
    getColor(): String;
    setSeeInvisibleTeamPlayers(bo: import('./boolean').boolean): import('./void').void;
    getSeeInvisibleTeamPlayers(): import('./boolean').boolean;
}
