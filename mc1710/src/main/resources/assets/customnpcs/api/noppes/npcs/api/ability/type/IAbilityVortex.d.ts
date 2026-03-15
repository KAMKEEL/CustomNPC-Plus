/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Vortex abilities.
 * Pulls targets toward the caster.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityVortex
*/
export interface IAbilityVortex extends import('../IAbility').IAbility {
    /** @return Radius of the pull effect in blocks. */
    getPullRadius(): import('./float').float;
    /** @param radius Pull effect radius in blocks. */
    setPullRadius(radius: import('./float').float): import('./void').void;
    /** @return Strength of the inward pull force. */
    getPullStrength(): import('./float').float;
    /** @param strength Pull force strength. */
    setPullStrength(strength: import('./float').float): import('./void').void;
    /** @return Damage dealt on initial vortex hit. */
    getDamage(): import('./float').float;
    /** @param damage Initial hit damage. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Knockback strength applied on initial hit. */
    getKnockback(): import('./float').float;
    /** @param knockback Knockback strength. */
    setKnockback(knockback: import('./float').float): import('./void').void;
    /** @return Whether the vortex hits all entities in radius (true) or only the target (false). */
    isAoe(): import('./boolean').boolean;
    /** @param aoe Whether the vortex is area-of-effect. */
    setAoe(aoe: import('./boolean').boolean): import('./void').void;
    /** @return Whether continuous damage is dealt while pulling entities. */
    isDamageOnPull(): import('./boolean').boolean;
    /** @param damage Whether to deal damage during pull. */
    setDamageOnPull(damage: import('./boolean').boolean): import('./void').void;
    /** @return Damage dealt per tick while pulling entities. */
    getPullDamage(): import('./float').float;
    /** @param damage Per-tick pull damage. */
    setPullDamage(damage: import('./float').float): import('./void').void;
    getDisplayDamage: import('./float').float;
}
