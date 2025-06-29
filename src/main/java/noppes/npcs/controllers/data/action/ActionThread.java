package noppes.npcs.controllers.data.action;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class ActionThread {
    private final Action action;

    private final Object lock = new Object();
    private Thread thread;
    ExecutorService executor;
    private Future<?> currentExecution;
    private volatile boolean threadPaused, threadSleeping;

    private Supplier<Boolean> pauseUntil;

    public ActionThread(Action action) {
        this.action = action;

        executor = Executors.newSingleThreadExecutor(r -> {
            thread = new Thread(r);
            thread.setName(String.format("ActionThread (%s)", action.getName()));
            return thread;
        });
    }

    public void stop() {
        if (isThreadRunning())
            currentExecution.cancel(true);

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

            if (threadSleeping && currentExecution != null)
                currentExecution.cancel(true);
        }
    }

    public boolean inActionThread() {
        return Thread.currentThread() == thread;
    }

    public boolean isPaused() {
        return threadPaused || threadSleeping;
    }

    public boolean isThreadRunning() {
        return currentExecution != null && !currentExecution.isDone();
    }


    protected void run() {
        if (threadPaused && pauseUntil != null && pauseUntil.get())
            resume();

        if (isThreadRunning())
            return;

        currentExecution = executor.submit(() -> {
            try {
                action.task.accept(action);
                action.count++;
            } catch (Throwable t) {
                System.err.println("Scripted threaded action '" + action.name + "' threw an exception:");
                t.printStackTrace();
                action.markDone();
            }
        });

    }
}
