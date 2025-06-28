package noppes.npcs.controllers.data.action;

import java.util.function.Supplier;

public class ActionThread {
    private final Action action;

    private final Object lock = new Object();
    private Thread thread;
    private volatile boolean threadPaused, threadSleeping;
    private boolean threadRunning;

    private Supplier<Boolean> pauseUntil;

    public ActionThread(Action action) {
        this.action = action;
        createThread();
    }

    private void createThread() {
        thread = new Thread(() -> {
            threadRunning = true;
            action.task.accept(action);
            threadRunning = false;
            action.count++;
        });
    }


    public void pauseFor(long millis) {
        if (isPaused())
            return;

        if (inActionThread()) {
            try {
                threadSleeping = true;
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                threadSleeping = false;
            }
        } else
            action.pauseFor((int) (millis / 50));
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

    public boolean isPaused() {
        return threadPaused || threadSleeping;
    }

    public boolean inActionThread() {
        return Thread.currentThread() == thread;
    }

    protected void run() {
        if (threadPaused && pauseUntil != null && pauseUntil.get())
            resume();

        if (threadRunning)
            return;

        try {
            thread.start();
        } catch (Throwable t) {
            System.err.println("Scripted threaded action '" + action.name + "' threw an exception:");
            t.printStackTrace();
            action.markDone();
        }
    }
}
