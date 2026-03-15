package noppes.npcs.api.entity;


/**
 * Represents an energy dome barrier - a spherical shield centered on the caster.
 */
public interface IEnergyDome extends IEnergyBarrier {

    /** @return The radius of the dome in blocks. */
    float getDomeRadius();

    /** @param radius The radius of the dome in blocks. */
    void setDomeRadius(float radius);
}
