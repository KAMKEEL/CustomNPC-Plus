package noppes.npcs.api.ability.data;

/**
 * Configuration for the anchor point of an energy ability projectile.
 * Controls where the projectile spawns relative to the caster.
 */
public interface IEnergyAnchorData {
    /** @return Anchor point ordinal (0=FRONT, 1=CENTER, 2=RIGHT_HAND, 3=LEFT_HAND, 4=ABOVE_HEAD, 5=CHEST, 6=EYE). */
    int getAnchor();

    /**
     * Set the anchor point.
     * @param anchor Anchor ordinal (0=FRONT, 1=CENTER, 2=RIGHT_HAND, 3=LEFT_HAND, 4=ABOVE_HEAD, 5=CHEST, 6=EYE)
     */
    void setAnchor(int anchor);

    /** @return X offset from the anchor point (positive = right). */
    float getAnchorOffsetX();

    /** @param anchorOffsetX X offset from anchor (positive = right). */
    void setAnchorOffsetX(float anchorOffsetX);

    /** @return Y offset from the anchor point (positive = up). */
    float getAnchorOffsetY();

    /** @param anchorOffsetY Y offset from anchor (positive = up). */
    void setAnchorOffsetY(float anchorOffsetY);

    /** @return Z offset from the anchor point (positive = forward). */
    float getAnchorOffsetZ();

    /** @param anchorOffsetZ Z offset from anchor (positive = forward). */
    void setAnchorOffsetZ(float anchorOffsetZ);

    /** @return Whether the projectile launches from the anchor position instead of the default eye/look-vector position. */
    boolean getLaunchFromAnchor();

    /** @param launchFromAnchor When true, the projectile launches from its anchor position; when false, it snaps to the default eye/look-vector position. */
    void setLaunchFromAnchor(boolean launchFromAnchor);
}
