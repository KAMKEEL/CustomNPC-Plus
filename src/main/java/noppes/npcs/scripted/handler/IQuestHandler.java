//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.handler;

import noppes.npcs.scripted.handler.data.IQuest;
import noppes.npcs.scripted.handler.data.IQuestCategory;

import java.util.List;

public interface IQuestHandler {
    List<IQuestCategory> categories();

    IQuest get(int var1);
}
