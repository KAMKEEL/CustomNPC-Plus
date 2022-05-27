//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.handler.data.IDialog;
import noppes.npcs.scripted.interfaces.handler.data.IDialogCategory;

import java.util.List;

public interface IDialogHandler {
    List<IDialogCategory> categories();

    IDialog get(int var1);
}
