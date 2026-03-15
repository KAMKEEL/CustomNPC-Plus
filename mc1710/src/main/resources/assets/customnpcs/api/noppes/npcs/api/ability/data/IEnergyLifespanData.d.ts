/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.data
 */

/**
 * Lifespan properties for energy ability projectiles.
 * Controls how far and how long a projectile can travel before expiring.
  * @javaFqn noppes.npcs.api.ability.data.IEnergyLifespanData
*/
export interface IEnergyLifespanData {
    /** @return Maximum travel distance in blocks before the projectile expires. */
    getMaxDistance(): import('./float').float;
    /** @param maxDistance Maximum travel distance in blocks. */
    setMaxDistance(maxDistance: import('./float').float): import('./void').void;
    /** @return Maximum lifetime in ticks before the projectile expires. */
    getMaxLifetime(): import('./int').int;
    /** @param maxLifetime Maximum lifetime in ticks. */
    setMaxLifetime(maxLifetime: import('./int').int): import('./void').void;
}
