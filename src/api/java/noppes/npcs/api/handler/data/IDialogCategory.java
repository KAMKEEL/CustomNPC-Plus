//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.handler.data;

import java.util.List;

public interface IDialogCategory {
    List<IDialog> dialogs();

    String getName();

    IDialog create();
}
