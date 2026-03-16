package common.minecraft.potion;

/**
 * Platform abstraction for Minecraft's potion type (the potion definition, not the effect instance).
 * MC 1.7.10: Potion
 * MC 1.12+: Potion (registry-based)
 */
public interface IPotionType {
    int getId();
    String getName();
    boolean isBadEffect();
}
