/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.scoreboard
 */

export interface IScoreboardTeam {

    // Methods
    getName(): string;
    getDisplayName(): string;
    setDisplayName(name: string): void;
    addPlayer(player: string): void;
    addPlayer(player: import('../entity/IPlayer').IPlayer): void;
    removePlayer(player: string): void;
    removePlayer(player: import('../entity/IPlayer').IPlayer): void;
    getPlayers(): string[];
    getTeamsize(): number;
    clearPlayers(): void;
    getFriendlyFire(): boolean;
    setFriendlyFire(bo: boolean): void;
    setColor(color: string): void;
    getColor(): string;
    setSeeInvisibleTeamPlayers(bo: boolean): void;
    getSeeInvisibleTeamPlayers(): boolean;

}
