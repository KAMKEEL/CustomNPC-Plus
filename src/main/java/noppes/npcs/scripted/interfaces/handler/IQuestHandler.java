//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.handler.data.IQuest;
import noppes.npcs.scripted.interfaces.handler.data.IQuestCategory;

import java.util.List;

public interface IQuestHandler {
    List<IQuestCategory> categories();

    IQuest get(int var1);
}
