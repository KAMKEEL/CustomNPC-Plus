package noppes.npcs.api.entity;

/**
 * Platform-api version (MC-free). The src/api shadow adds generic type parameter.
 * All methods are clean — no MC types needed.
 */
public interface IEntityLiving extends IEntityLivingBase {

    boolean isNavigating();

    void clearNavigation();

    void navigateTo(double x, double y, double z, double speed);

    Object getMCEntity();

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

    boolean canBeSteered();
}
