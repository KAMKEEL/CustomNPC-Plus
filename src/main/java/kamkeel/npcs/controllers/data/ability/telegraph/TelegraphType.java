package kamkeel.npcs.controllers.data.ability.telegraph;

/**
 * Types of telegraph shapes for visual attack warnings.
 */
public enum TelegraphType {
    /**
     * Filled circle (uses radius)
     */
    CIRCLE,

    /**
     * Donut shape (uses radius and innerRadius)
     */
    RING,

    /**
     * Rectangle (uses length, width)
     */
    LINE,

    /**
     * Fan/wedge shape (uses length, angle)
     */
    CONE,

    /**
     * Single point indicator
     */
    POINT,

    /**
     * No telegraph
     */
    NONE
}
