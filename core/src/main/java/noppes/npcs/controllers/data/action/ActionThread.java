package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ActionThread {
    private Action action;

    private final Object lock = new Object();
    private Thread thread;
    ExecutorService executor;

    private final Queue<Future<?>> runningTasks = new ConcurrentLinkedQueue<>();
    private final Set<Object> runningKeys = ConcurrentHashMap.newKeySet();

    private volatile boolean threadPaused, threadSleeping;

    private Function<IAction, Boolean> pauseUntil;

    public ActionThread(Action action) {
        this.action = action;
        executor = Executors.newSingleThreadExecutor(r -> {
            thread = new Thread(r);
            thread.setName(String.format("ActionThread (%s)", action.getName()));
            return thread;
        });

        if (action.manager.debug)
            action.manager.LOGGER.log("Created Action thread!", this);
    }

    public void stop() {
        runningTasks.forEach((task) -> {
            if (!task.isDone())
                task.cancel(true);
        });

        executor.shutdown();


        if (action.manager.debug)
            action.manager.LOGGER.log("Stopping thread", this);
    }

    public Action getAction() {
        return action;
    }

    public void pauseFor(long millis) {
        if (isPaused() || !inActionThread())
            return;

        try {
            threadSleeping = true;
            if (action.manager.debug)
                action.manager.LOGGER.log(String.format("Sleeping thread for %s ticks", millis / 50), this);

            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            threadSleeping = false;

            if (action.manager.debug)
                action.manager.LOGGER.log(String.format("Woken up thread after sleeping for %s ticks", millis / 50), this);
        }
    }

    public void pauseFor(int ticks) {
        pauseFor(ticks * 50L);
    }

    public void pause() {
        if (!inActionThread() || isPaused())
            return;

        threadPaused = true;
        synchronized (lock) {
            while (threadPaused) {
                try {
                    if (action.manager.debug)
                        action.manager.LOGGER.log(String.format("Pausing thread"), this);

                    lock.wait(); // Wait until notified
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void pauseUntil(Function<IAction, Boolean> until) {
        this.pauseUntil = until;
        pause();
    }

    public void resume() {
        synchronized (lock) {
            if (threadPaused) {
                threadPaused = false;
                lock.notify();

                if (action.manager.debug)
                    action.manager.LOGGER.log(String.format("Resumed thread"), this);
            }

            if (threadSleeping)
                thread.interrupt();
        }
    }

    public boolean isTaskRunning(String taskKey) {
        return runningKeys.contains(taskKey);
    }

    public boolean inActionThread() {
        return Thread.currentThread() == thread;
    }

    public boolean isPaused() {
        return threadPaused || threadSleeping;
    }

    public boolean isThreadRunning() {
        return runningTasks.stream().anyMatch(task -> !task.isDone());
    }

    public void execute(String taskKey, Runnable task) {
        if (threadPaused && pauseUntil != null && pauseUntil.apply(action))
            resume();

        if (!runningKeys.add(taskKey))
            return;

        if (action.manager.debug)
            action.manager.LOGGER.log(String.format("Delegating task '%s' to Action thread...", taskKey), action);

        final AtomicReference<Future<?>> taskRef = new AtomicReference<>();
        taskRef.set(executor.submit(() -> {
            if (action.manager.debug)
                action.manager.LOGGER.log(String.format("Started executing task '%s' on Action thread...", taskKey), this);

            runningTasks.add(taskRef.get());
            try {
                task.run();
            } finally {
                runningTasks.remove(taskRef.get());
                runningKeys.remove(taskKey);

                if (action.manager.debug)
                    action.manager.LOGGER.log(String.format("Finished executing task '%s' on Action thread", taskKey), this);

                if (((ActionQueue) action.queue).finish(action))
                    action.queue.getQueue().remove(action);

            }
        }));
    }
}
