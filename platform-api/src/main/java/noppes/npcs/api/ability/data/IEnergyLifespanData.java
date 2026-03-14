package noppes.npcs.api.ability.data;

/**
 * Lifespan properties for energy ability projectiles.
 * Controls how far and how long a projectile can travel before expiring.
 */
public interface IEnergyLifespanData {
    /** @return Maximum travel distance in blocks before the projectile expires. */
    float getMaxDistance();

    /** @param maxDistance Maximum travel distance in blocks. */
    void setMaxDistance(float maxDistance);

    /** @return Maximum lifetime in ticks before the projectile expires. */
    int getMaxLifetime();

    /** @param maxLifetime Maximum lifetime in ticks. */
    void setMaxLifetime(int maxLifetime);
}
