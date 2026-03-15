package noppes.npcs.api.entity;


/**
 * A directional beam projectile with a trailing path and optional head orb.
 * <p>
 * Beams travel forward from their origin, dealing damage along their length.
 * They can optionally stay attached to their owner and follow the owner's movement.
 * <p>
 * Beams use {@code startFiring} internally rather than {@code startMoving} —
 * this is handled automatically by the fire methods.
 */
public interface IEnergyBeam extends IEnergyProjectile {

    /**
     * Width of the beam trail.
     * @return the beam width in blocks
     */
    float getBeamWidth();

    void setBeamWidth(float width);

    float getHeadSize();

    void setHeadSize(float size);

    boolean isAttachedToOwner();

    void setAttachedToOwner(boolean attached);

    boolean shouldRenderTailOrb();
}
