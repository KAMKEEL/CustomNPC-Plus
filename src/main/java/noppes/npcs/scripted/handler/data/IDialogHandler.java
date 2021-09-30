//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.handler;

import java.util.List;
import noppes.npcs.scripted.handler.data.IDialog;
import noppes.npcs.scripted.handler.data.IDialogCategory;

public interface IDialogHandler {
    List<IDialogCategory> categories();

    IDialog get(int var1);
}
