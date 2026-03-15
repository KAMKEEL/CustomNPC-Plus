/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * A flat spinning disc projectile with optional boomerang behavior.
 * <p>
 * Discs can be oriented vertically or horizontally, and can return to their owner
 * like a boomerang after a configurable delay.
  * @javaFqn noppes.npcs.api.entity.IEnergyDisc
*/
export interface IEnergyDisc<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyProjectile').IEnergyProjectile {
    /**
     * Radius of the disc.
     * @return the disc radius in blocks
     */
    getDiscRadius(): import('./float').float;
    setDiscRadius(radius: import('./float').float): import('./void').void;
    getDiscThickness(): import('./float').float;
    setDiscThickness(thickness: import('./float').float): import('./void').void;
    isVertical(): import('./boolean').boolean;
    setVertical(vertical: import('./boolean').boolean): import('./void').void;
    isBoomerang(): import('./boolean').boolean;
    setBoomerang(boomerang: import('./boolean').boolean): import('./void').void;
    getBoomerangDelay(): import('./int').int;
    setBoomerangDelay(ticks: import('./int').int): import('./void').void;
    isReturning(): import('./boolean').boolean;
}
