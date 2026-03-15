package noppes.npcs.api.entity;


/**
 * A continuous laser beam that extends outward from its origin along a direction vector.
 * <p>
 * Unlike other projectiles, lasers don't move through the world — they extend from their
 * origin point along a direction, hitting everything in their path up to {@link #getMaxLength()}.
 * The direction can be set explicitly via {@link #setDirection(double, double, double)}.
 * <p>
 * The coordinate-based {@code fireAt(x, y, z)} calculates and sets the direction vector
 * automatically, then uses the laser's internal launch sequence.
 */
public interface IEnergyLaser extends IEnergyProjectile {

    /**
     * Width of the laser beam.
     * @return the laser width in blocks
     */
    float getLaserWidth();

    void setLaserWidth(float width);

    float getExpansionSpeed();

    void setExpansionSpeed(float speed);

    float getMaxLength();

    void setMaxLength(float maxLength);

    float getCurrentLength();

    boolean isFullyExtended();

    double getDirX();

    double getDirY();

    double getDirZ();

    void setDirection(double x, double y, double z);

    double getEndX();

    double getEndY();

    double getEndZ();
}
