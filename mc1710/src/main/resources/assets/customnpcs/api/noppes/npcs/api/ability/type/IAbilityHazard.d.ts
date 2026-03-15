/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Hazard abilities.
 * Persistent ground effect zones placed around the caster.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityHazard
*/
export interface IAbilityHazard extends import('./IAbilityZone').IAbilityZone {
    /** @return Damage radius of the hazard zone in blocks. */
    getRadius(): import('./float').float;
    /** @param radius Damage radius in blocks. */
    setRadius(radius: import('./float').float): import('./void').void;
    /** @return Damage dealt per damage interval tick. */
    getDamagePerSecond(): import('./float').float;
    /** @param damage Damage per interval. */
    setDamagePerSecond(damage: import('./float').float): import('./void').void;
    /** @return Interval in ticks between damage applications. */
    getDamageInterval(): import('./int').int;
    /** @param interval Damage interval in ticks. */
    setDamageInterval(interval: import('./int').int): import('./void').void;
    /** @return Whether the hazard also damages the caster. */
    isAffectsCaster(): import('./boolean').boolean;
    /** @param affects Whether the hazard affects the caster. */
    setAffectsCaster(affects: import('./boolean').boolean): import('./void').void;
    getDisplayDamage: import('./float').float;
    isDisplayDamageDPS: import('./boolean').boolean;
}
