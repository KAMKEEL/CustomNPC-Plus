//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity;

import net.minecraft.entity.projectile.EntityThrowable;
import noppes.npcs.scripted.entity.IEntity;
import noppes.npcs.scripted.entity.IThrowable;
import noppes.npcs.scripted.item.IItemStack;

public interface IProjectile<T extends EntityThrowable> extends IThrowable<T> {
    IItemStack getItem();

    void setItem(IItemStack var1);

    boolean getHasGravity();

    void setHasGravity(boolean var1);

    int getAccuracy();

    void setAccuracy(int var1);

    void setHeading(IEntity var1);

    void setHeading(double var1, double var3, double var5);

    void setHeading(float var1, float var2);

    void enableEvents();
}
