package noppes.npcs.api.ability.type;

/**
 * API interface for Laser abilities.
 * Sweeping beam that follows the caster's look vector.
 */
public interface IAbilityLaser extends IAbilityEnergyProjectile {

    /** @return Width of the laser beam in blocks. */
    float getLaserWidth();

    /** @param width Laser beam width in blocks. */
    void setLaserWidth(float width);

    /** @return Speed at which the laser extends to its max length, in blocks per tick. */
    float getExpansionSpeed();

    /** @param speed Expansion speed in blocks per tick. */
    void setExpansionSpeed(float speed);

    /** @return Maximum length the laser can reach in blocks. */
    float getMaxLength();

    /** @param maxLength Maximum laser length in blocks. */
    void setMaxLength(float maxLength);
}
