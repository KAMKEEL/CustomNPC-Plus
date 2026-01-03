/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.scoreboard
 */

export interface IScoreboard {

    // Methods
    getObjectives(): import('./IScoreboardObjective').IScoreboardObjective[];
    getObjective(name: string): import('./IScoreboardObjective').IScoreboardObjective;
    hasObjective(objective: string): boolean;
    removeObjective(objective: string): void;
    addObjective(objective: string, criteria: string): import('./IScoreboardObjective').IScoreboardObjective;
    setPlayerScore(player: string, objective: string, score: number, datatag: string): void;
    setPlayerScore(player: import('../entity/IPlayer').IPlayer, objective: string, score: number, datatag: string): void;
    getPlayerScore(player: string, objective: string, datatag: string): number;
    getPlayerScore(player: import('../entity/IPlayer').IPlayer, objective: string, datatag: string): number;
    hasPlayerObjective(player: string, objective: string, datatag: string): boolean;
    hasPlayerObjective(player: import('../entity/IPlayer').IPlayer, objective: string, datatag: string): boolean;
    deletePlayerScore(player: string, objective: string, datatag: string): void;
    deletePlayerScore(player: import('../entity/IPlayer').IPlayer, objective: string, datatag: string): void;
    getTeams(): import('./IScoreboardTeam').IScoreboardTeam[];
    getTeamByName(name: string): import('./IScoreboardTeam').IScoreboardTeam;
    hasTeam(name: string): boolean;
    addTeam(name: string): import('./IScoreboardTeam').IScoreboardTeam;
    getTeam(name: string): import('./IScoreboardTeam').IScoreboardTeam;
    removeTeam(name: string): void;

}
