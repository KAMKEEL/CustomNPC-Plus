/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Base interface for all energy ability entities (Projectiles, Barriers).
 * Contains shared display, lightning, owner, and charging properties.
  * @javaFqn noppes.npcs.api.entity.IEnergyAbility
*/
export interface IEnergyAbility<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEntity').IEntity {
    /** @return Entity ID of the caster who spawned this energy ability. */
    getOwnerEntityId(): import('./int').int;
    /** @return The caster entity who spawned this energy ability, or null if not found. */
    getOwner(): import('./IEntity').IEntity;
    /** @return Inner (core) color as a packed RGB integer. */
    getInnerColor(): import('./int').int;
    /** @param color Inner color as a packed RGB integer. */
    setInnerColor(color: import('./int').int): import('./void').void;
    /** @return Alpha (opacity) of the inner color layer (0.0-1.0). */
    getInnerAlpha(): import('./float').float;
    /** @param alpha Inner color opacity (0.0-1.0). */
    setInnerAlpha(alpha: import('./float').float): import('./void').void;
    /** @return Outer (glow) color as a packed RGB integer. */
    getOuterColor(): import('./int').int;
    /** @param color Outer color as a packed RGB integer. */
    setOuterColor(color: import('./int').int): import('./void').void;
    /** @return Whether the outer color layer is rendered. */
    isOuterColorEnabled(): import('./boolean').boolean;
    /** @param enabled Whether to render the outer color layer. */
    setOuterColorEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Width of the outer color layer relative to the entity size. */
    getOuterColorWidth(): import('./float').float;
    /** @param width Outer color layer width. */
    setOuterColorWidth(width: import('./float').float): import('./void').void;
    /** @return Alpha (opacity) of the outer color layer (0.0-1.0). */
    getOuterColorAlpha(): import('./float').float;
    /** @param alpha Outer color opacity (0.0-1.0). */
    setOuterColorAlpha(alpha: import('./float').float): import('./void').void;
    /** @return Whether the lightning visual effect is enabled. */
    hasLightningEffect(): import('./boolean').boolean;
    /** @param enabled Whether to render the lightning effect. */
    setLightningEffect(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Density of lightning arcs (higher = more arcs). */
    getLightningDensity(): import('./float').float;
    /** @param density Lightning arc density. */
    setLightningDensity(density: import('./float').float): import('./void').void;
    /** @return Radius of the lightning effect around the entity, in blocks. */
    getLightningRadius(): import('./float').float;
    /** @param radius Lightning effect radius in blocks. */
    setLightningRadius(radius: import('./float').float): import('./void').void;
    /** @return Fade-out time for lightning arcs in ticks. */
    getLightningFadeTime(): import('./int').int;
    /** @param ticks Lightning fade-out time in ticks. */
    setLightningFadeTime(ticks: import('./int').int): import('./void').void;
    /** @return Whether this energy ability is currently in its charging phase. */
    isCharging(): import('./boolean').boolean;
    /** @return Charge progress as a fraction (0.0-1.0). */
    getChargeProgress(): import('./float').float;
    /** @return Whether this entity ignores target invulnerability frames when dealing damage. */
    isIgnoreIFrames(): import('./boolean').boolean;
    /** @param ignore Whether this entity should ignore target invulnerability frames when dealing damage. */
    setIgnoreIFrames(ignore: import('./boolean').boolean): import('./void').void;
    /**
     * Get the custom damage data attached to this energy entity.
     * Used by addon handlers (e.g. DBC Addon) to carry damage configuration
     * directly on the entity, enabling DBC damage scaling without a sourceAbility.
     * @return Custom damage data as INbt, or null if none set.
     */
    getDamageData(): import('../INbt').INbt;
    /**
     * Set custom damage data on this energy entity.
     * @param data Custom damage data as INbt. Pass null to clear.
     */
    setDamageData(data: import('../INbt').INbt): import('./void').void;
    /**
     * Get this entity's magic data. Defines magic types for outgoing damage splits
     * or barrier defense interactions. Inherited from the source ability on spawn.
     * @return the entity's magic data
     */
    getMagicData(): import('../handler/data/IMagicData').IMagicData;
    /**
     * Set magic data on this energy entity. Overrides any inherited ability magic.
     * @param data Magic data to set.
     */
    setMagicData(data: import('../handler/data/IMagicData').IMagicData): import('./void').void;
}
