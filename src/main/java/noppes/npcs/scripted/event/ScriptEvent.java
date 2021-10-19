package noppes.npcs.scripted.event;

public
class ScriptEvent {
    private boolean isCancelled = false;

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean bo) {
        isCancelled = bo;
    }
}
