//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.handler.data;

import java.util.List;
import noppes.npcs.scripted.handler.data.IAvailability;
import noppes.npcs.scripted.handler.data.IDialogCategory;
import noppes.npcs.scripted.handler.data.IDialogOption;
import noppes.npcs.scripted.handler.data.IQuest;

public interface IDialog {
    int getId();

    String getName();

    void setName(String var1);

    String getText();

    void setText(String var1);

    IQuest getQuest();

    void setQuest(IQuest var1);

    String getCommand();

    void setCommand(String var1);

    List<IDialogOption> getOptions();

    IDialogOption getOption(int var1);

    IAvailability getAvailability();

    IDialogCategory getCategory();

    void save();
}
