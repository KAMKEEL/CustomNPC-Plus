package kamkeel.npcs.platform.entity;

/**
 * Platform-independent living entity abstraction.
 * Extends {@link IMob} with health and damage operations.
 */
public interface ILiving extends IMob {

    float getHealth();

    void setHealth(float hp);

    float getMaxHealth();

    /**
     * Deal damage to this entity.
     *
     * @param amount the damage amount
     * @param source the damage source, or null for generic damage
     */
    void damage(float amount, IDamage source);
}
