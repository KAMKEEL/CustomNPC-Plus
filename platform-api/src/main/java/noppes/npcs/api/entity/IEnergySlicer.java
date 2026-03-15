package noppes.npcs.api.entity;


/**
 * A thin, wide blade projectile that slices through targets.
 * <p>
 * Slicers travel in a straight line, cutting through entities in their path.
 */
public interface IEnergySlicer extends IEnergyProjectile {

    /**
     * Width of the slicer blade.
     * @return the width of each slice in blocks
     */
    float getSliceWidth();

    void setSliceWidth(float width);

    float getSliceThickness();

    void setSliceThickness(float thickness);
}
