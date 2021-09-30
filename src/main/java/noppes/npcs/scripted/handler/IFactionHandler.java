//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.handler;

import java.util.List;
import noppes.npcs.scripted.handler.data.IFaction;

public interface IFactionHandler {
    List<IFaction> list();

    IFaction delete(int var1);

    IFaction create(String var1, int var2);

    IFaction get(int var1);
}
