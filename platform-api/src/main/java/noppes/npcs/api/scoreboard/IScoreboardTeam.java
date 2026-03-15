package noppes.npcs.api.scoreboard;

import noppes.npcs.api.entity.IPlayer;

public interface IScoreboardTeam {
    String getName();

    String getDisplayName();

    void setDisplayName(String name);

    void addPlayer(String player);

    void addPlayer(IPlayer player);

    void removePlayer(String player);

    void removePlayer(IPlayer player);

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
