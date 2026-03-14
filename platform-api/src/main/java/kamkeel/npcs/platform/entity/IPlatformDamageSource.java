package kamkeel.npcs.platform.entity;

/**
 * Platform-independent damage source abstraction.
 */
public interface IPlatformDamageSource {

    /**
     * @return the damage type identifier (e.g., "player", "mob", "generic")
     */
    String getType();

    /**
     * @return the entity that caused this damage, or null if none (e.g., fall damage)
     */
    IPlatformEntity getSourceEntity();

    /**
     * Returns the underlying MC DamageSource object.
     * Core code should NEVER call this.
     *
     * @return the raw MC DamageSource
     */
    Object getHandle();
}
