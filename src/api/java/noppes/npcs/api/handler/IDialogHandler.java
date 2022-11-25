//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;

import java.util.List;

public interface IDialogHandler {
    List<IDialogCategory> categories();

    IDialog get(int var1);
}
