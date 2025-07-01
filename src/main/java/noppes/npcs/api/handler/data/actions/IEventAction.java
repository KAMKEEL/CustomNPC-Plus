package noppes.npcs.api.handler.data.actions;

/**
 * EventAction is a ConditionalAction that will fire a custom script hook when its
 * condition evaluates to true.
 */
public interface IEventAction extends IConditionalAction {
    /**
     * @return hook name used when firing this action
     */
    String getHook();

    /**
     * Set the hook name to use.
     * @param hook hook name
     * @return this action
     */
    IEventAction setHook(String hook);
}
