//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;

public interface IFaction {
    int getId();

    String getName();

    void setName(String name);

    void setDefaultPoints(int var1);

    int getDefaultPoints();

    void setFriendlyPoints(int p);

    int getFriendlyPoints();

    void setNeutralPoints(int p);

    int getNeutralPoints();

    void setColor(int c);

    int getColor();

    int playerStatus(IPlayer player);

    boolean isAggressiveToNpc(ICustomNpc npc);

    boolean getIsHidden();

    void setIsHidden(boolean hidden);

    boolean attackedByMobs();

    void setAttackedByMobs(boolean attacked);

    boolean isEnemyFaction(IFaction faction);

    IFaction[] getEnemyFactions();

    void addEnemyFaction(IFaction faction);

    void removeEnemyFaction(IFaction faction);

    void save();
}
