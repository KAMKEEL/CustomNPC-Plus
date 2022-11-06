//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.IWorld;
import noppes.npcs.scripted.interfaces.entity.IEntity;

public interface ICloneHandler {

    IEntity spawn(double x, double y, double z, int tab, String name, IWorld world, boolean ignoreProtection);

    IEntity spawn(double x, double y, double z, int tab, String name, IWorld world);

    IEntity[] getTab(int tab, IWorld world);

    IEntity get(int tab, String name, IWorld world);

    void set(int tab, String name, IEntity entity);

    void remove(int tab, String name);
}
