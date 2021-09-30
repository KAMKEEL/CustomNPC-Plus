//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.handler.data;

import noppes.npcs.scripted.entity.ICustomNpc;
import noppes.npcs.scripted.entity.IPlayer;

public interface IFaction {
    int getId();

    String getName();

    int getDefaultPoints();

    void setDefaultPoints(int var1);

    int getColor();

    int playerStatus(IPlayer var1);

    boolean hostileToNpc(ICustomNpc var1);

    boolean hostileToFaction(int var1);

    int[] getHostileList();

    void addHostile(int var1);

    void removeHostile(int var1);

    boolean hasHostile(int var1);

    boolean getIsHidden();

    void setIsHidden(boolean var1);

    boolean getAttackedByMobs();

    void setAttackedByMobs(boolean var1);

    void save();
}
