//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.entity;

import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.scripted.entity.IEntity;
import noppes.npcs.scripted.item.IItemStack;

public interface IEntityLivingBase<T extends EntityLivingBase> extends IEntity<T> {
    float getHealth();

    void setHealth(float var1);

    float getMaxHealth();

    void setMaxHealth(float var1);

    boolean isAttacking();

    void setAttackTarget(IEntityLivingBase var1);

    IEntityLivingBase getAttackTarget();

    IEntityLivingBase getLastAttacked();

    int getLastAttackedTime();

    boolean canSeeEntity(IEntity var1);

    void swingMainhand();

    void swingOffhand();

    IItemStack getMainhandItem();

    void setMainhandItem(IItemStack var1);

    IItemStack getOffhandItem();

    void setOffhandItem(IItemStack var1);

    IItemStack getArmor(int var1);

    void setArmor(int var1, IItemStack var2);

    void addPotionEffect(int var1, int var2, int var3, boolean var4);

    void clearPotionEffects();

    int getPotionEffect(int var1);

    boolean isChild();

    T getMCEntity();

    float getMoveForward();

    void setMoveForward(float var1);

    float getMoveStrafing();

    void setMoveStrafing(float var1);

    float getMoveVertical();

    void setMoveVertical(float var1);
}
