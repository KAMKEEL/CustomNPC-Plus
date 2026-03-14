package kamkeel.npcs.platform.entity;

/**
 * Platform-independent living entity abstraction.
 * Extends {@link IPlatformEntity} with health and damage operations.
 */
public interface IPlatformLiving extends IPlatformEntity {

    float getHealth();

    void setHealth(float hp);

    float getMaxHealth();

    /**
     * Deal damage to this entity.
     *
     * @param amount the damage amount
     * @param source the damage source, or null for generic damage
     */
    void damage(float amount, IPlatformDamageSource source);
}
