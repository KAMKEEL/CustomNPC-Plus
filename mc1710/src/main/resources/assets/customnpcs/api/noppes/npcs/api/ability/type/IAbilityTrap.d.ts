/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Trap abilities.
 * Proximity-triggered traps placed around the caster.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityTrap
*/
export interface IAbilityTrap extends import('./IAbilityZone').IAbilityZone {
    /** @return Radius in blocks at which entities trigger the trap. */
    getTriggerRadius(): import('./float').float;
    /** @param radius Trigger detection radius in blocks. */
    setTriggerRadius(radius: import('./float').float): import('./void').void;
    /** @return Time in ticks before the trap becomes active after placement. */
    getArmTime(): import('./int').int;
    /** @param ticks Arming time in ticks. */
    setArmTime(ticks: import('./int').int): import('./void').void;
    /** @return Maximum number of times the trap can trigger before expiring. */
    getMaxTriggers(): import('./int').int;
    /** @param max Maximum trigger count. */
    setMaxTriggers(max: import('./int').int): import('./void').void;
    /** @return Cooldown in ticks between consecutive triggers. */
    getTriggerCooldown(): import('./int').int;
    /** @param cooldown Trigger cooldown in ticks. */
    setTriggerCooldown(cooldown: import('./int').int): import('./void').void;
    /** @return Damage dealt when the trap triggers. */
    getDamage(): import('./float').float;
    /** @param damage Trigger damage amount. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Radius of the damage area when triggered, in blocks. */
    getDamageRadius(): import('./float').float;
    /** @param radius Damage area radius in blocks. */
    setDamageRadius(radius: import('./float').float): import('./void').void;
    /** @return Knockback strength applied to entities caught in the trigger. */
    getKnockback(): import('./float').float;
    /** @param knockback Knockback strength. */
    setKnockback(knockback: import('./float').float): import('./void').void;
    /** @return Whether the trap is visible to entities. */
    isVisible(): import('./boolean').boolean;
    /** @param visible Whether the trap is visible. */
    setVisible(visible: import('./boolean').boolean): import('./void').void;
    getDisplayDamage: import('./float').float;
}
