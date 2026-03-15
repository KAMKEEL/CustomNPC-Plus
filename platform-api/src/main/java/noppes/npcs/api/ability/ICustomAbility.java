package noppes.npcs.api.ability;

/**
 * A custom (script-driven) ability whose behavior is entirely defined by script event hooks.
 * Supports both instant execution mode and toggle mode.
 */
public interface ICustomAbility extends IAbility {

    /**
     * Get the duration in ticks for instant mode (non-toggle).
     * @return duration in ticks
     */
    int getDurationTicks();

    /**
     * Set the duration in ticks for instant mode.
     * @param ticks duration in ticks
     */
    void setDurationTicks(int ticks);

    /**
     * Get the telegraph shape type ordinal.
     * @return ordinal of TelegraphType: 0=CIRCLE, 1=RING, 2=LINE, 3=CONE, 4=POINT, 5=SQUARE, 6=NONE
     */
    int getTelegraphShapeType();

    /**
     * Set the telegraph shape type by ordinal.
     * @param type ordinal of TelegraphType
     */
    void setTelegraphShapeType(int type);

    /**
     * Get the targeting mode ordinal.
     * @return ordinal of TargetingMode: 0=AGGRO_TARGET, 1=SELF, 2=AOE_SELF, 3=AOE_TARGET
     */
    int getTargetingModeType();

    /**
     * Set the targeting mode by ordinal.
     * @param type ordinal of TargetingMode
     */
    void setTargetingModeType(int type);

    /**
     * Get how many ticks the telegraph persists during the active phase.
     * When syncTelegraphWithDuration is true, this returns the duration ticks.
     * @return telegraph active ticks
     */
    int getTelegraphActiveTicks();

    /**
     * Set how many ticks the telegraph persists during the active phase.
     * Only used when syncTelegraphWithDuration is false.
     * @param ticks telegraph active ticks
     */
    void setTelegraphActiveTicks(int ticks);

    /**
     * Whether telegraph active ticks are synced to the ability duration.
     * @return true if synced
     */
    boolean isSyncTelegraphWithDuration();

    /**
     * Set whether telegraph active ticks should sync to the ability duration.
     * @param sync whether to sync
     */
    void setSyncTelegraphWithDuration(boolean sync);

    /**
     * Get the telegraph radius (used by CIRCLE, RING, SQUARE shapes).
     * @return the telegraph radius
     */
    float getTelegraphRadius();

    /**
     * Set the telegraph radius.
     * @param radius the telegraph radius
     */
    void setTelegraphRadius(float radius);

    /**
     * Get the telegraph inner radius (used by RING, CONE shapes).
     * @return the inner radius
     */
    float getTelegraphInnerRadius();

    /**
     * Set the telegraph inner radius.
     * @param innerRadius the inner radius
     */
    void setTelegraphInnerRadius(float innerRadius);

    /**
     * Get the telegraph length (used by LINE, CONE shapes).
     * @return the telegraph length
     */
    float getTelegraphLength();

    /**
     * Set the telegraph length.
     * @param length the telegraph length
     */
    void setTelegraphLength(float length);

    /**
     * Get the telegraph width (used by LINE shape).
     * @return the telegraph width
     */
    float getTelegraphWidth();

    /**
     * Set the telegraph width.
     * @param width the telegraph width
     */
    void setTelegraphWidth(float width);

    /**
     * Get the telegraph angle in degrees (used by CONE shape).
     * @return the telegraph angle in degrees
     */
    float getTelegraphAngle();

    /**
     * Set the telegraph angle in degrees.
     * @param angle the angle in degrees
     */
    void setTelegraphAngle(float angle);
}
