package noppes.npcs.api.scoreboard;

public interface IScoreboardObjective {

    String getName();

    String getDisplayName();

    void setDisplayName(String name);

    String getCriteria();

    boolean isReadyOnly();
}
