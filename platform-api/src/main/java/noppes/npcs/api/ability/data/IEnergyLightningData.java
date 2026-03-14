package noppes.npcs.api.ability.data;

/**
 * Lightning visual effect properties for energy ability projectiles.
 * Controls the electric arc effect rendered around the projectile.
 */
public interface IEnergyLightningData {
    /** @return Whether the lightning visual effect is enabled. */
    boolean isLightningEffect();

    /** @param lightningEffect Whether to enable the lightning effect. */
    void setLightningEffect(boolean lightningEffect);

    /** @return Density of lightning arcs (higher = more arcs). */
    float getLightningDensity();

    /** @param lightningDensity Lightning arc density. */
    void setLightningDensity(float lightningDensity);

    /** @return Radius of the lightning effect around the projectile, in blocks. */
    float getLightningRadius();

    /** @param lightningRadius Lightning effect radius in blocks. */
    void setLightningRadius(float lightningRadius);

    /** @return Fade-out time for lightning arcs in ticks. */
    int getLightningFadeTime();

    /** @param lightningFadeTime Fade-out time in ticks. */
    void setLightningFadeTime(int lightningFadeTime);
}
