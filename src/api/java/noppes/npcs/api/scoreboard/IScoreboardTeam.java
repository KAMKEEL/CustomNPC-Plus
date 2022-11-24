package noppes.npcs.api.scoreboard;

public interface IScoreboardTeam {
    String getName();

    String getDisplayName();

    void setDisplayName(String name);

    void addPlayer(String player);

    void removePlayer(String player);

    String[] getPlayers();

    int getTeamsize();

    void clearPlayers();

    boolean getFriendlyFire();

    void setFriendlyFire(boolean bo);

    void setColor(String color);

    String getColor();

    void setSeeInvisibleTeamPlayers(boolean bo);

    boolean getSeeInvisibleTeamPlayers();
}
