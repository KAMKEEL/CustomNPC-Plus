package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;
import noppes.npcs.api.handler.data.IActionListener;
import noppes.npcs.api.handler.data.IActionQueue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ActionListener implements IActionListener {
    private static final ActionManager manager = ActionManager.GLOBAL;

    private final Object object; //object listener is attached to
    private final Map<String, Hook> hooks = new ConcurrentHashMap<>();

    public ActionListener(Object target) {
        this.object = target;
    }

    public void tick() {
        Iterator<Map.Entry<String, Hook>> it = hooks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Hook> entry = it.next();
            Hook hook = entry.getValue();
            ActionQueue hookQueue = (ActionQueue) hook.queue;
            hookQueue.tick();

            if (hookQueue.isDead()) {
                hookQueue.clear();
                if (manager.debug)
                    manager.LOGGER.log(String.format("Removing queue '%s'", hookQueue.getName()), manager);
                it.remove();
            }
        }
    }

    public Object getObject() {
        return object;
    }

//    @Override
    public Hook getOrCreateHook(String hookName) {
        return hooks.computeIfAbsent(hookName, Hook::new);
    }

//    @Override
    public Hook getHook(String hookName) {
        return hooks.get(hookName);
    }

//    @Override
    public boolean fire(String hookName) {
        Hook hook = hooks.get(hookName);
        if (hook != null) {
            hook.fire();
            return true;
        }

        return false;
    }

    public IAction schedule(String hookName, IAction action) {
        return getOrCreateHook(hookName).schedule(action);
    }

    public class Hook {
        public final String name;
        public final IActionQueue queue;

        public Hook(String hookName) {
            this.name = hookName;

            String queueName = String.format("ActionListener#%s:%s", object, hookName);
            this.queue = manager.createQueue(queueName).stopWhenEmpty(true).killWhenEmpty(false);
        }

        public String getName() {
            return name;
        }

        public void fire() {
            queue.getManager().start(); // just in case, ensures the manager is ticking
            queue.start();
        }

        public IAction schedule(IAction action) {
            return queue.schedule(action);
        }
    }
}
