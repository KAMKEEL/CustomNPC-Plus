package noppes.npcs.scripted.event;

public class ScriptEventTimer extends ScriptEvent {
    private int id;

    public ScriptEventTimer(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
