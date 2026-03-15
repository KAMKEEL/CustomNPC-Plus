package noppes.npcs.api.entity;


/**
 * A flat spinning disc projectile with optional boomerang behavior.
 * <p>
 * Discs can be oriented vertically or horizontally, and can return to their owner
 * like a boomerang after a configurable delay.
 */
public interface IEnergyDisc extends IEnergyProjectile {

    /**
     * Radius of the disc.
     * @return the disc radius in blocks
     */
    float getDiscRadius();

    void setDiscRadius(float radius);

    float getDiscThickness();

    void setDiscThickness(float thickness);

    boolean isVertical();

    void setVertical(boolean vertical);

    boolean isBoomerang();

    void setBoomerang(boolean boomerang);

    int getBoomerangDelay();

    void setBoomerangDelay(int ticks);

    boolean isReturning();
}
