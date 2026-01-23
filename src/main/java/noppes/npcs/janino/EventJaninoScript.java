package noppes.npcs.janino;

import noppes.npcs.constants.ScriptContext;

public class EventJaninoScript extends JaninoScript<Object> {

    public EventJaninoScript(ScriptContext context) {
        super(Object.class, null, false);
        this.context = context;
    }
}
