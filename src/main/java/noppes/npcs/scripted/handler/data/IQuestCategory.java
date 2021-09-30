//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.handler.data;

import java.util.List;
import noppes.npcs.scripted.handler.data.IQuest;

public interface IQuestCategory {
    List<IQuest> quests();

    String getName();

    IQuest create();
}
