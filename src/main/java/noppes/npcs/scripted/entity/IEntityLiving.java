//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity;

import net.minecraft.entity.EntityLiving;
import noppes.npcs.scripted.entity.IEntityLivingBase;

public interface IEntityLiving<T extends EntityLiving> extends IEntityLivingBase<T> {
    boolean isNavigating();

    void clearNavigation();

    void navigateTo(double var1, double var3, double var5, double var7);

    void jump();

    T getMCEntity();
}
