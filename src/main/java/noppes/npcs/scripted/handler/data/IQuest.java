//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.handler.data;

import noppes.npcs.scripted.interfaces.IContainer;
import noppes.npcs.scripted.interfaces.IPlayer;

public interface IQuest {
    int getId();

    String getName();

    void setName(String var1);

    int getType();

    void setType(int var1);

    String getLogText();

    void setLogText(String var1);

    String getCompleteText();

    void setCompleteText(String var1);

    IQuest getNextQuest();

    void setNextQuest(IQuest var1);

    IQuestObjective[] getObjectives(IPlayer var1);

    IQuestCategory getCategory();

    IContainer getRewards();

    String getNpcName();

    void setNpcName(String var1);

    void save();

    boolean getIsRepeatable();

    IQuestInterface getQuestInterface();
}
