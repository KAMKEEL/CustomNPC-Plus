package noppes.npcs.controllers.data.action;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ActionThread {
    private final Object lock = new Object();
    private Thread thread;
    ExecutorService executor;
    private final Queue<Future<?>> runningTasks = new ConcurrentLinkedQueue<>();

    private volatile boolean threadPaused, threadSleeping;

    private Supplier<Boolean> pauseUntil;

    public ActionThread(Action action) {
        executor = Executors.newSingleThreadExecutor(r -> {
            thread = new Thread(r);
            thread.setName(String.format("ActionThread (%s)", action.getName()));
            return thread;
        });
    }

    public void stop() {
        runningTasks.forEach((task) -> {
            if (!task.isDone())
                task.cancel(true);
        });

        executor.shutdown();
    }

    public void pauseFor(long millis) {
        if (isPaused() || !inActionThread())
            return;

        try {
            threadSleeping = true;
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            threadSleeping = false;
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
                    lock.wait(); // Wait until notified
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void pauseUntil(Supplier<Boolean> until) {
        this.pauseUntil = until;
        pause();
    }

    public void resume() {
        synchronized (lock) {
            if (threadPaused) {
                threadPaused = false;
                lock.notify();
            }

            if (threadSleeping)
                thread.interrupt();
        }
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

    protected void execute(Runnable task) {
        if (threadPaused && pauseUntil != null && pauseUntil.get())
            resume();

        if (isThreadRunning())
            return;

        final AtomicReference<Future<?>> taskRef = new AtomicReference<>();
        taskRef.set(executor.submit(() -> {
            runningTasks.add(taskRef.get());
            try {
                task.run();
            } finally {
                runningTasks.remove(taskRef.get());
            }
        }));
    }
}
