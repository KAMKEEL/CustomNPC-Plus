package noppes.npcs.controllers.data.action;

import noppes.npcs.EventHooks;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.actions.IEventAction;
import noppes.npcs.controllers.data.DataScript;
import noppes.npcs.controllers.data.INpcScriptHandler;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.ActionTriggerEvent;

import java.util.function.Function;

/**
 * Conditional action that fires a custom event hook when triggered.
 */
public class EventAction extends ConditionalAction implements IEventAction {
    private final INpcScriptHandler handler;
    private String hook;

    public EventAction(ActionManager manager, INpcScriptHandler handler, String hook, Function<IAction, Boolean> condition) {
        super(manager, hook, condition, a -> {});
        this.handler = handler;
        this.hook = hook;
    }

    public EventAction(ActionManager manager, String name, INpcScriptHandler handler, String hook, Function<IAction, Boolean> condition) {
        super(manager, name, condition, a -> {});
        this.handler = handler;
        this.hook = hook;
    }

    @Override
    protected void executeTask() {
        if (handler == null)
            return;

        if (handler instanceof PlayerDataScript) {
            PlayerDataScript pd = (PlayerDataScript) handler;
            IPlayer pl = pd.player != null ? (IPlayer) NpcAPI.Instance().getIEntity(pd.player) : null;
            ActionTriggerEvent.PlayerAction event = new ActionTriggerEvent.PlayerAction(pl, hook);
            EventHooks.onEventAction(handler, event);
        } else if (handler instanceof DataScript) {
            DataScript ds = (DataScript) handler;
            ICustomNpc npc = ds.dummyNpc;
            ActionTriggerEvent.NpcAction event = new ActionTriggerEvent.NpcAction(npc, hook);
            EventHooks.onEventAction(handler, event);
        } else {
            EventHooks.onEventAction(handler, new ActionTriggerEvent(hook));
        }

        count++;
    }

    @Override
    public String getHook() {
        return hook;
    }

    @Override
    public IEventAction setHook(String hook) {
        this.hook = hook;
        return this;
    }
}
