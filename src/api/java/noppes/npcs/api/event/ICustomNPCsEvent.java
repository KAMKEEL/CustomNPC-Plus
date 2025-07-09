package noppes.npcs.api.event;

/**
 * Base interface for all CustomNPCs events.
 */
public interface ICustomNPCsEvent {
    /**
     * @return name of the hook that triggered the event
     */
    String getHookName();
}
