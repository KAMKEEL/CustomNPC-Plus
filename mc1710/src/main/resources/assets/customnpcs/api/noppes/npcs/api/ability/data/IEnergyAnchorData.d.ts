/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.data
 */

/**
 * Configuration for the anchor point of an energy ability projectile.
 * Controls where the projectile spawns relative to the caster.
  * @javaFqn noppes.npcs.api.ability.data.IEnergyAnchorData
*/
export interface IEnergyAnchorData {
    /** @return Anchor point ordinal (0=FRONT, 1=CENTER, 2=RIGHT_HAND, 3=LEFT_HAND, 4=ABOVE_HEAD, 5=CHEST, 6=EYE). */
    getAnchor(): import('./int').int;
    /**
     * Set the anchor point.
     * @param anchor Anchor ordinal (0=FRONT, 1=CENTER, 2=RIGHT_HAND, 3=LEFT_HAND, 4=ABOVE_HEAD, 5=CHEST, 6=EYE)
     */
    setAnchor(anchor: import('./int').int): import('./void').void;
    /** @return X offset from the anchor point (positive = right). */
    getAnchorOffsetX(): import('./float').float;
    /** @param anchorOffsetX X offset from anchor (positive = right). */
    setAnchorOffsetX(anchorOffsetX: import('./float').float): import('./void').void;
    /** @return Y offset from the anchor point (positive = up). */
    getAnchorOffsetY(): import('./float').float;
    /** @param anchorOffsetY Y offset from anchor (positive = up). */
    setAnchorOffsetY(anchorOffsetY: import('./float').float): import('./void').void;
    /** @return Z offset from the anchor point (positive = forward). */
    getAnchorOffsetZ(): import('./float').float;
    /** @param anchorOffsetZ Z offset from anchor (positive = forward). */
    setAnchorOffsetZ(anchorOffsetZ: import('./float').float): import('./void').void;
    /** @return Whether the projectile launches from the anchor position instead of the default eye/look-vector position. */
    getLaunchFromAnchor(): import('./boolean').boolean;
    /** @param launchFromAnchor When true, the projectile launches from its anchor position; when false, it snaps to the default eye/look-vector position. */
    setLaunchFromAnchor(launchFromAnchor: import('./boolean').boolean): import('./void').void;
}
