//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.interfaces.entity;

import net.minecraft.entity.EntityLiving;

public interface IEntityLiving<T extends EntityLiving> extends IEntityLivingBase<T> {
    boolean isNavigating();

    void clearNavigation();

    void navigateTo(double var1, double var3, double var5, double var7);

    T getMCEntity();
}
