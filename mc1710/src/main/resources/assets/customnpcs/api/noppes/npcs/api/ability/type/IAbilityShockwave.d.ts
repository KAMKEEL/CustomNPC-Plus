/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.type
 */

/**
 * API interface for Shockwave abilities.
 * Pushes targets away from the caster.
  * @javaFqn noppes.npcs.api.ability.type.IAbilityShockwave
*/
export interface IAbilityShockwave extends import('../IAbility').IAbility {
    /** @return Radius of the push effect in blocks. */
    getPushRadius(): import('./float').float;
    /** @param radius Push radius in blocks. */
    setPushRadius(radius: import('./float').float): import('./void').void;
    /** @return Strength of the outward push force. */
    getPushStrength(): import('./float').float;
    /** @param strength Push force strength. */
    setPushStrength(strength: import('./float').float): import('./void').void;
    /** @return Damage dealt to entities caught in the shockwave. */
    getDamage(): import('./float').float;
    /** @param damage Shockwave damage. */
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return Whether the shockwave hits all entities in radius (true) or only the target (false). */
    isAoe(): import('./boolean').boolean;
    /** @param aoe Whether the shockwave is area-of-effect. */
    setAoe(aoe: import('./boolean').boolean): import('./void').void;
    getDisplayDamage: import('./float').float;
}
