package common.minecraft.potion;

/**
 * Platform abstraction for Minecraft's active potion effect instance.
 * MC 1.7.10: PotionEffect
 * MC 1.12+: PotionEffect / EffectInstance (1.14+)
 */
public interface IPotionEffect {
    int getPotionID();
    int getDuration();
    int getAmplifier();
    boolean getIsAmbient();
}
