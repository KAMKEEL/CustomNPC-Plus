//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted;

import noppes.npcs.scripted.IScoreboardScore;

public interface IScoreboardObjective {
    String getName();

    String getDisplayName();

    void setDisplayName(String var1);

    String getCriteria();

    boolean isReadyOnly();

    IScoreboardScore[] getScores();

    IScoreboardScore getScore(String var1);

    boolean hasScore(String var1);

    IScoreboardScore createScore(String var1);

    void removeScore(String var1);
}
