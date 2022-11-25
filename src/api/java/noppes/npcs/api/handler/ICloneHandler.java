//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.handler;

import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

public interface ICloneHandler {

    IEntity spawn(double x, double y, double z, int tab, String name, IWorld world, boolean ignoreProtection);
    IEntity spawn(IPos pos, int tab, String name, IWorld world, boolean ignoreProtection);

    IEntity spawn(double x, double y, double z, int tab, String name, IWorld world);
    IEntity spawn(IPos pos, int tab, String name, IWorld world);

    IEntity[] getTab(int tab, IWorld world);

    IEntity get(int tab, String name, IWorld world);

    void set(int tab, String name, IEntity entity);

    void remove(int tab, String name);
}
