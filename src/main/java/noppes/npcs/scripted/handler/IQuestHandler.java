//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.handler;

import java.util.List;
import noppes.npcs.scripted.handler.data.IQuest;
import noppes.npcs.scripted.handler.data.IQuestCategory;

public interface IQuestHandler {
    List<IQuestCategory> categories();

    IQuest get(int var1);
}
