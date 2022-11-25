//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IPlayer;

public interface IAvailability {
    boolean isAvailable(IPlayer var1);

    int getDaytime();

    void setDaytime(int var1);

    int getMinPlayerLevel();

    void setMinPlayerLevel(int var1);

    int getDialog(int var1);

    void setDialog(int var1, int var2, int var3);

    void removeDialog(int var1);

    int getQuest(int var1);

    void setQuest(int var1, int var2, int var3);

    void removeQuest(int var1);

    void setFaction(int var1, int var2, int var3, int var4);

    void removeFaction(int var1);
}
