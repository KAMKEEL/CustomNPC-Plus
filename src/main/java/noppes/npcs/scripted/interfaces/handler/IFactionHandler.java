//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.handler.data.IFaction;

import java.util.List;

public interface IFactionHandler {
    List<IFaction> list();

    IFaction delete(int var1);

    IFaction create(String var1, int var2);

    IFaction get(int var1);
}
