package noppes.npcs.core;

/**
 * Static config values accessible to core module code.
 * Values are populated by the platform-specific config system at startup.
 * Defaults match the original ConfigMain defaults.
 */
public final class CoreConfig {
    // NPC Hitbox
    public static int HitBoxScaleMax = 15;

    // Party defaults
    public static int DefaultMinPartySize = 1;
    public static int DefaultMaxPartySize = 4;

    // Dialog
    public static int DialogImageLimit = 10;

    private CoreConfig() {}
}
