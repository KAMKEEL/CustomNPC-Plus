package noppes.npcs.janino.impl;

import noppes.npcs.janino.JaninoScript;

/**
 * Janino (Java) script implementation for NPC events.
 * Hook methods are resolved dynamically by name - no interface definition required.
 */
public class JaninoNpcScript extends JaninoScript<JaninoNpcScript.Functions> {

    public JaninoNpcScript() {
        super(Functions.class, null);
    }

    @Override
    protected String getHookContext() {
        return "npc";
    }

    public interface Functions {}
}
