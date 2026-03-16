package common.minecraft.world;

/**
 * Platform abstraction for Minecraft's explosion.
 * MC 1.7.10: Explosion
 * MC 1.12+: Explosion
 */
public interface IExplosion {
    double getX();
    double getY();
    double getZ();
    float getSize();
}
