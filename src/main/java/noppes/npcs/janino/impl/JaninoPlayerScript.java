package noppes.npcs.janino.impl;

import noppes.npcs.janino.JaninoScript;

/**
 * Janino (Java) script implementation for player events.
 * Hook methods are resolved dynamically by name - no interface definition required.
 */
public class JaninoPlayerScript extends JaninoScript<JaninoPlayerScript.Functions> {

    public JaninoPlayerScript() {
        super(Functions.class, null, false);
    }

    @Override
    protected String getHookContext() {
        return "player";
    }

    public interface Functions {}
}
