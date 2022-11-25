//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.handler.data;

import java.util.List;

public interface IQuestCategory {
    List<IQuest> quests();

    String getName();

    IQuest create();
}
