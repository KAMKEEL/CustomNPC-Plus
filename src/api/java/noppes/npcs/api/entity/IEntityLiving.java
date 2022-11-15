//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.api.entity;

import net.minecraft.entity.EntityLiving;

public interface IEntityLiving<T extends EntityLiving> extends IEntityLivingBase<T> {
    boolean isNavigating();

    void clearNavigation();

    void navigateTo(double var1, double var3, double var5, double var7);

    T getMCEntity();

     void playLivingSound();

     void spawnExplosionParticle();

     void setMoveForward(float speed);

     void faceEntity(IEntity entity, float pitch, float yaw);

     boolean canPickUpLoot();

     void setCanPickUpLoot(boolean pickUp);

     boolean isPersistent();

     void enablePersistence();

     void setCustomNameTag(String text);

     String getCustomNameTag();

     boolean hasCustomNameTag();

     void setAlwaysRenderNameTag(boolean alwaysRender);

     boolean getAlwaysRenderNameTag();

     void clearLeashed(boolean sendPacket, boolean dropLeash);

     boolean allowLeashing();

     boolean getLeashed();

     IEntity getLeashedTo();

     void setLeashedTo(IEntity entity, boolean sendPacket);
}
