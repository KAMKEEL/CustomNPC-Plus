package noppes.npcs.api.scoreboard;

public interface IScoreboard {
    IScoreboardObjective[] getObjectives();

    IScoreboardObjective getObjective(String name);

    boolean hasObjective(String objective);

    void removeObjective(String objective);

    IScoreboardObjective addObjective(String objective, String criteria);

    void setPlayerScore(String player, String objective, int score, String datatag);
    
    int getPlayerScore(String player, String objective, String datatag);

    boolean hasPlayerObjective(String player, String objective, String datatag);

    void deletePlayerScore(String player, String objective, String datatag);

    IScoreboardTeam[] getTeams();

    IScoreboardTeam getTeamByName(String name);

    boolean hasTeam(String name);

    IScoreboardTeam addTeam(String name);

    IScoreboardTeam getTeam(String name);

    void removeTeam(String name);
}
