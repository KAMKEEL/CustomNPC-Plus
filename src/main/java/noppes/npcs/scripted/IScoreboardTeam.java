//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

public interface IScoreboardTeam {
    String getName();

    String getDisplayName();

    void setDisplayName(String var1);

    void addPlayer(String var1);

    boolean hasPlayer(String var1);

    void removePlayer(String var1);

    String[] getPlayers();

    void clearPlayers();

    boolean getFriendlyFire();

    void setFriendlyFire(boolean var1);

    void setColor(String var1);

    String getColor();

    void setSeeInvisibleTeamPlayers(boolean var1);

    boolean getSeeInvisibleTeamPlayers();
}
