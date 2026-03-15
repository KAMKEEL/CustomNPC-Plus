/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Heavy Hit abilities.
 * AOE rectangle melee attack in front of the caster.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityHeavyHit
*/
export interface IAbilityHeavyHit extends import('../IAbility').IAbility {
    /** @return Damage dealt to entities in the hit area. */
    getDamage(): import('./float').float;
    /** @param damage Damage amount. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Knockback strength applied to entities hit. */
    getKnockback(): import('./float').float;
    /** @param knockback Knockback strength. */
    setKnockback(knockback: import('./float').float): import('./void').void;
    /** @return Length of the rectangular hit area in blocks (forward from caster). */
    getHitLength(): import('./float').float;
    /** @param hitLength Hit area length in blocks. */
    setHitLength(hitLength: import('./float').float): import('./void').void;
    /** @return Width of the rectangular hit area in blocks. */
    getHitWidth(): import('./float').float;
    /** @param hitWidth Hit area width in blocks. */
    setHitWidth(hitWidth: import('./float').float): import('./void').void;
    getDisplayDamage: import('./float').float;
}
