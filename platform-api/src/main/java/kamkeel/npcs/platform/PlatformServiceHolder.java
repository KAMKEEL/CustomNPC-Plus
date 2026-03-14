package kamkeel.npcs.platform;

/**
 * Static accessor for the active {@link PlatformService} implementation.
 *
 * The mc* module sets this once during mod initialization:
 * <pre>
 *     PlatformServiceHolder.set(new MC1710PlatformService());
 * </pre>
 *
 * CORE code retrieves it via:
 * <pre>
 *     PlatformService platform = PlatformServiceHolder.get();
 * </pre>
 */
public final class PlatformServiceHolder {

    private static PlatformService instance;

    private PlatformServiceHolder() {}

    /**
     * Sets the active platform service. Must be called exactly once during mod initialization.
     *
     * @throws IllegalStateException if already set
     */
    public static void set(PlatformService service) {
        if (instance != null) {
            throw new IllegalStateException("PlatformService has already been initialized");
        }
        instance = service;
    }

    /**
     * @return the active platform service
     * @throws IllegalStateException if not yet initialized
     */
    public static PlatformService get() {
        if (instance == null) {
            throw new IllegalStateException("PlatformService has not been initialized yet");
        }
        return instance;
    }

    /**
     * @return true if the platform service has been initialized
     */
    public static boolean isInitialized() {
        return instance != null;
    }
}
