/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Charge abilities.
 * Rush forward attack.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityCharge
*/
export interface IAbilityCharge extends import('../IAbility').IAbility {
    /** @return Movement speed during the charge in blocks per tick. */
    getChargeSpeed(): import('./float').float;
    /** @param speed Movement speed in blocks per tick. */
    setChargeSpeed(speed: import('./float').float): import('./void').void;
    /** @return Damage dealt to entities hit during the charge. */
    getDamage(): import('./float').float;
    /** @param damage Damage on charge impact. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Knockback strength applied to entities hit during the charge. */
    getKnockback(): import('./float').float;
    /** @param knockback Knockback strength on impact. */
    setKnockback(knockback: import('./float').float): import('./void').void;
    /** @return Width of the charge hit area in blocks. */
    getHitWidth(): import('./float').float;
    /** @param width Hit area width in blocks. */
    setHitWidth(width: import('./float').float): import('./void').void;
    getDisplayDamage: import('./float').float;
}
