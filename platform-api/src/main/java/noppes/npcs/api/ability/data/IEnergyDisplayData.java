package noppes.npcs.api.ability.data;

/**
 * Visual display properties for energy ability projectiles.
 * Controls colors, transparency, and rotation speed.
 */
public interface IEnergyDisplayData {
    /** @return Inner (core) color as a packed RGB integer. */
    int getInnerColor();

    /** @param innerColor Inner color as a packed RGB integer. */
    void setInnerColor(int innerColor);

    /** @return Outer (glow) color as a packed RGB integer. */
    int getOuterColor();

    /** @param outerColor Outer color as a packed RGB integer. */
    void setOuterColor(int outerColor);

    /** @return Whether the outer color layer is rendered. */
    boolean isOuterColorEnabled();

    /** @param outerColorEnabled Whether to render the outer color layer. */
    void setOuterColorEnabled(boolean outerColorEnabled);

    /** @return Width of the outer color layer relative to the projectile size. */
    float getOuterColorWidth();

    /** @param outerColorWidth Outer color layer width. */
    void setOuterColorWidth(float outerColorWidth);

    /** @return Alpha (opacity) of the outer color layer (0.0-1.0). */
    float getOuterColorAlpha();

    /** @param outerColorAlpha Outer color opacity (0.0-1.0). */
    void setOuterColorAlpha(float outerColorAlpha);

    /** @return Alpha (opacity) of the inner color layer (0.0-1.0). */
    float getInnerAlpha();

    /** @param innerAlpha Inner color opacity (0.0-1.0). */
    void setInnerAlpha(float innerAlpha);

    /** @return Visual rotation speed in degrees per tick. */
    float getRotationSpeed();

    /** @param rotationSpeed Rotation speed in degrees per tick. */
    void setRotationSpeed(float rotationSpeed);
}
