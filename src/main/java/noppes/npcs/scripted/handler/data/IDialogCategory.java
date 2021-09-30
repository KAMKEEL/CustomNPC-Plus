//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.handler.data;

import java.util.List;
import noppes.npcs.scripted.handler.data.IDialog;

public interface IDialogCategory {
    List<IDialog> dialogs();

    String getName();

    IDialog create();
}
