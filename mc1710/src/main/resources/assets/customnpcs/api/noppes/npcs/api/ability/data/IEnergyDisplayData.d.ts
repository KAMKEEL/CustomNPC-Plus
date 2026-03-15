/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability.data
 */

/**
 * Visual display properties for energy ability projectiles.
 * Controls colors, transparency, and rotation speed.
  * @javaFqn noppes.npcs.api.ability.data.IEnergyDisplayData
*/
export interface IEnergyDisplayData {
    /** @return Inner (core) color as a packed RGB integer. */
    getInnerColor(): import('./int').int;
    /** @param innerColor Inner color as a packed RGB integer. */
    setInnerColor(innerColor: import('./int').int): import('./void').void;
    /** @return Outer (glow) color as a packed RGB integer. */
    getOuterColor(): import('./int').int;
    /** @param outerColor Outer color as a packed RGB integer. */
    setOuterColor(outerColor: import('./int').int): import('./void').void;
    /** @return Whether the outer color layer is rendered. */
    isOuterColorEnabled(): import('./boolean').boolean;
    /** @param outerColorEnabled Whether to render the outer color layer. */
    setOuterColorEnabled(outerColorEnabled: import('./boolean').boolean): import('./void').void;
    /** @return Width of the outer color layer relative to the projectile size. */
    getOuterColorWidth(): import('./float').float;
    /** @param outerColorWidth Outer color layer width. */
    setOuterColorWidth(outerColorWidth: import('./float').float): import('./void').void;
    /** @return Alpha (opacity) of the outer color layer (0.0-1.0). */
    getOuterColorAlpha(): import('./float').float;
    /** @param outerColorAlpha Outer color opacity (0.0-1.0). */
    setOuterColorAlpha(outerColorAlpha: import('./float').float): import('./void').void;
    /** @return Alpha (opacity) of the inner color layer (0.0-1.0). */
    getInnerAlpha(): import('./float').float;
    /** @param innerAlpha Inner color opacity (0.0-1.0). */
    setInnerAlpha(innerAlpha: import('./float').float): import('./void').void;
    /** @return Visual rotation speed in degrees per tick. */
    getRotationSpeed(): import('./float').float;
    /** @param rotationSpeed Rotation speed in degrees per tick. */
    setRotationSpeed(rotationSpeed: import('./float').float): import('./void').void;
}
