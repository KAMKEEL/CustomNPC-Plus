package noppes.npcs.scripted;

import noppes.npcs.api.handler.IActionManager;
import noppes.npcs.api.handler.data.IAction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class ScriptedActionManager implements IActionManager {
    private boolean isWorking;
    private final Queue<IAction> actionQueue = new LinkedList<>();

    @Override
    public IAction create(String name, int maxDuration, int startAfterTicks, Consumer<IAction> action) {
        return new CustomAction(name, maxDuration, startAfterTicks, action);
    }

    @Override
    public void start() {
        isWorking = true;
    }

    @Override
    public void stop() {
        isWorking = false;
    }

    @Override
    public void scheduleAction(IAction action) {
        actionQueue.add(action);
    }

    @Override
    public void scheduleAction(String name, int maxDuration, int startAfterTicks, Consumer<IAction> action) {
        scheduleAction(create(name, maxDuration, startAfterTicks, action));
    }

    @Override
    public void scheduleActionAt(int index, IAction action) {
        ((LinkedList) actionQueue).add(index, action);
    }

    @Override
    public int getIndex(IAction action) {
        if (action == null)
            return -1;
        return ((LinkedList) actionQueue).indexOf(action);
    }

    @Override
    public IAction getCurrentAction() {
        return actionQueue.peek();
    }

    @Override
    public Queue<IAction> getActionQueue() {
        return actionQueue;
    }

    public void clear() {
        actionQueue.clear();
    }

    public void tick(int ticksExisted) {
        if (isWorking) {
            CustomAction current = (CustomAction) getCurrentAction();
            if (current != null) {
                current.tick(ticksExisted);
                if (current.isDone || current.duration >= current.maxDuration) {
                    current.destroy();
                    actionQueue.poll();
                }
            }
        }
    }

    //Nested not static to access outer class instance
    public class CustomAction implements IAction {
        Consumer<IAction> action;
        private final HashMap<String, Object> data = new HashMap<>();

        private final String name;
        private int startAfterTicks; //number of ticks to start action after
        private int count; //number of times action ran
        private int duration;
        private final int maxDuration;  //duration since action began
        private int updateEveryXTick = 5;

        private boolean isDone;

        public CustomAction(String name, int maxDuration, int startAfterTicks, Consumer<IAction> task) {
            this.name = name;
            this.maxDuration = maxDuration;
            this.startAfterTicks = startAfterTicks;
            this.action = task;
        }

        private void tick(int ticksExisted) {
            if (startAfterTicks <= 0) {
                if (ticksExisted % updateEveryXTick == 0) {
                    action.accept(this);
                    count++;
                }
                duration++;
            } else
                startAfterTicks--;
        }

        private void destroy() {
            data.clear();
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public int getDuration() {
            return duration;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getMaxDuration() {
            return maxDuration;
        }

        @Override
        public void markDone() {
            isDone = true;
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public Object getData(String key) {
            return data.get(key);
        }

        @Override
        public void addData(String key, Object value) {
            data.put(key, value);
        }

        @Override
        public int getUpdateEveryXTick() {
            return updateEveryXTick;
        }

        @Override
        public void setUpdateEveryXTick(int X) {
            this.updateEveryXTick = X;
        }

        @Override
        public int getStartAfterTicks() {
            return startAfterTicks;
        }

        @Override
        public IAction create(String name, int maxDuration, int startAfterTicks, Consumer<IAction> action) {
            return ScriptedActionManager.this.create(name, maxDuration, startAfterTicks, action);
        }

        @Override
        public IAction getNext() {
            int index = getIndex(this);
            if (index != -1 && index + 1 < actionQueue.size())
                return ((LinkedList<IAction>) actionQueue).get(index + 1);

            return null;
        }

        @Override
        public IAction getPrevious() {
            int index = getIndex(this);

            if (index == 0)
                return null;
            else if (index != -1)
                return ((LinkedList<IAction>) actionQueue).get(index - 1);

            return null;
        }

        @Override
        public void scheduleAfter(IAction after) {
            int index = getIndex(this);
            if (index != -1)
                scheduleActionAt(index + 1, after);
        }

        @Override
        public void scheduleBefore(IAction before) {
            int index = getIndex(this);
            if (index != -1)
                scheduleActionAt(index == 0 ? 0 : index - 1, before);
        }
    }
}



