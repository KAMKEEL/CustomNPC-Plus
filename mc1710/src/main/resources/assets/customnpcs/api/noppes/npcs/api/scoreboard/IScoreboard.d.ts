/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.scoreboard
 */

/**
 * @javaFqn noppes.npcs.api.scoreboard.IScoreboard
 */
export interface IScoreboard {
    getObjectives(): import('./IScoreboardObjective').IScoreboardObjective[];
    getObjective(name: String): import('./IScoreboardObjective').IScoreboardObjective;
    hasObjective(objective: String): import('./boolean').boolean;
    removeObjective(objective: String): import('./void').void;
    addObjective(objective: String, criteria: String): import('./IScoreboardObjective').IScoreboardObjective;
    setPlayerScore(player: String, objective: String, score: import('./int').int, datatag: String): import('./void').void;
    setPlayerScore(player: import('../entity/IPlayer').IPlayer, objective: String, score: import('./int').int, datatag: String): import('./void').void;
    getPlayerScore(player: String, objective: String, datatag: String): import('./int').int;
    getPlayerScore(player: import('../entity/IPlayer').IPlayer, objective: String, datatag: String): import('./int').int;
    hasPlayerObjective(player: String, objective: String, datatag: String): import('./boolean').boolean;
    hasPlayerObjective(player: import('../entity/IPlayer').IPlayer, objective: String, datatag: String): import('./boolean').boolean;
    deletePlayerScore(player: String, objective: String, datatag: String): import('./void').void;
    deletePlayerScore(player: import('../entity/IPlayer').IPlayer, objective: String, datatag: String): import('./void').void;
    getTeams(): import('./IScoreboardTeam').IScoreboardTeam[];
    getTeamByName(name: String): import('./IScoreboardTeam').IScoreboardTeam;
    hasTeam(name: String): import('./boolean').boolean;
    addTeam(name: String): import('./IScoreboardTeam').IScoreboardTeam;
    getTeam(name: String): import('./IScoreboardTeam').IScoreboardTeam;
    removeTeam(name: String): import('./void').void;
}
