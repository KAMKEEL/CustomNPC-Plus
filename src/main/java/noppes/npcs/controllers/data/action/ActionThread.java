package noppes.npcs.controllers.data.action;

import noppes.npcs.api.handler.data.IAction;

public class ActionThread {
    private Action action;
    private Thread thread;
    private volatile boolean threadPaused;
    private boolean threadRunning;

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

    public IAction pauseFor(long millis) {
        if (Thread.currentThread() == thread) {
            try {
                threadPaused = true;
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                threadPaused = false;
                return action;
            }
        } else
            return action.pauseFor((int) (millis / 50));
    }

    public IAction pauseFor(int ticks) {
        if (Thread.currentThread() == thread) {
            try {
                threadPaused = true;
                Thread.sleep(ticks * 50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                threadPaused = false;
                return action;
            }
        } else
            return action.pauseFor(ticks);
    }

    public boolean isPaused() {
        return threadPaused;
    }

    public void run() {
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
