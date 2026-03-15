/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Guard abilities.
 * Defensive stance that reduces incoming damage.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityGuard
*/
export interface IAbilityGuard extends import('./IAbilityDefend').IAbilityDefend {
    /** @return Damage reduction multiplier while guarding (0.0 = full block, 1.0 = no reduction). */
    getDamageReduction(): import('./float').float;
    /** @param reduction Damage reduction multiplier (0.0 = full block, 1.0 = no reduction). */
    setDamageReduction(reduction: import('./float').float): import('./void').void;
}
