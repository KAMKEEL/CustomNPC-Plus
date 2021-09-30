//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

import noppes.npcs.scripted.IScoreboardObjective;
import noppes.npcs.scripted.IScoreboardTeam;

public interface IScoreboard {
    IScoreboardObjective[] getObjectives();

    IScoreboardObjective getObjective(String var1);

    boolean hasObjective(String var1);

    void removeObjective(String var1);

    IScoreboardObjective addObjective(String var1, String var2);

    void setPlayerScore(String var1, String var2, int var3, String var4);

    int getPlayerScore(String var1, String var2, String var3);

    boolean hasPlayerObjective(String var1, String var2, String var3);

    void deletePlayerScore(String var1, String var2, String var3);

    IScoreboardTeam[] getTeams();

    boolean hasTeam(String var1);

    IScoreboardTeam addTeam(String var1);

    IScoreboardTeam getTeam(String var1);

    void removeTeam(String var1);

    IScoreboardTeam getPlayerTeam(String var1);

    void removePlayerTeam(String var1);

    String[] getPlayerList();
}
