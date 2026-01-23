package noppes.npcs.controllers.data;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptHookController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface IScriptHandler {
    default void callScript(EnumScriptType type, Event event) {
        if (type != null)
            callScript(type.function, event);

    }

    void callScript(String hookName, Event event);

    /**
     * Call a function by name and return its result.
     * <p>
     * Default implementation calls the function on each script unit (in order) and returns
     * the first non-null value.
     */
    default Object callFunction(String hookName, Object... args) {
        if (hookName == null || hookName.isEmpty())
            return null;

        if (!getEnabled())
            return null;

        List<IScriptUnit> scripts = getScripts();
        if (scripts == null || scripts.isEmpty())
            return null;

        for (IScriptUnit script : scripts) {
            if (script == null || script.hasErrored() || !script.hasCode())
                continue;

            Object result = script.callFunction(hookName, args);
            if (result != null)
                return result;
        }

        return null;
    }

    /**
     * Typed convenience wrapper around {@link #callFunction(String, Object...)}.
     */
    default <S> S callFunction(String hookName, Class<S> returnType, Object... args) {
        Object result = callFunction(hookName, args);
        if (result == null || returnType == null)
            return null;

        if (returnType.isInstance(result))
            return returnType.cast(result);

        return null;
    }

    default boolean isClient() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    boolean getEnabled();

    void setEnabled(boolean enabled);

    String getLanguage();

    void setLanguage(String language);

    void setScripts(List<IScriptUnit> list);

    List<IScriptUnit> getScripts();

    default String noticeString() {
        return "";
    }

    default Map<Long, String> getConsoleText() {
        Map<Long, String> map = new TreeMap<>();
        int tab = 0;

        for (IScriptUnit script : getScripts()) {
            ++tab;
            for (Map.Entry<Long, String> entry : script.getConsole().entrySet()) {
                map.put(entry.getKey(), " tab " + tab + ":\n" + entry.getValue());
            }
        }
        return map;
    }

    default void clearConsole() {
        for (IScriptUnit script : getScripts())
            script.clearConsole();

    }

    /**
     * Create a new Janino (Java) script unit for this handler.
     * Handlers that don't support Janino scripts should return null.
     *
     * @return A new JaninoScript instance, or null if not supported
     */
    default IScriptUnit createJaninoScriptUnit() {
        return null; // Default: Janino not supported
    }

    /**
     * Check if this handler supports Janino (Java) scripts.
     *
     * @return true if createJaninoScriptUnit() can create valid Janino scripts
     */
    default boolean supportsJanino() {
        return createJaninoScriptUnit() != null;
    }

    /**
     * Get the script context for autocomplete and type resolution.
     *
     * @return The script context (default: GLOBAL)
     */
    default ScriptContext getContext() {
        return ScriptContext.GLOBAL;
    }

    /**
     * Get the hook context identifier for this handler.
     * Used by ScriptHookController to look up available hooks.
     * <p>
     * By default, derives from getContext().hookContext.
     * Override if a different hook context is needed.
     *
     * @return The context string (e.g., "effect", "npc"), or empty if none
     */
    default String getHookContext() {
        ScriptContext ctx = getContext();
        return ctx != null ? ctx.hookContext : "";
    }

    /**
     * Get the list of available hooks for this handler.
     * Uses getHookContext() to look up hooks from ScriptHookController.
     *
     * @return List of hook names, or empty list if no context
     */
    default List<String> getHooks() {
        String context = getHookContext();
        if (context == null || context.isEmpty())
            return Collections.emptyList();

        return ScriptHookController.Instance.getAllHooks(context);
    }

    /**
     * Check if this handler manages only a single script container.
     * Single-container handlers use simplified UI (no numbered tabs).
     *
     * @return true if this handler uses a single container
     */
    default boolean isSingleContainer() {
        return false;
    }

    /**
     * Get the single script unit for single-container handlers.
     * Convenience method that returns the first script or null.
     *
     * @return The single script unit, or null if none exists
     */
    default IScriptUnit getSingleScript() {
        List<IScriptUnit> scripts = getScripts();
        return (scripts != null && !scripts.isEmpty()) ? scripts.get(0) : null;
    }

    /**
     * Add a script unit to this handler.
     * For single-container handlers, replaces the current unit.
     */
    default void addScriptUnit(IScriptUnit unit) {
        if (unit != null)
            getScripts().add(unit);
    }

    /**
     * Replace a script unit at the given index.
     * For single-container handlers, replaces the current unit.
     */
    default void replaceScriptUnit(int index, IScriptUnit unit) {
        List<IScriptUnit> scripts = getScripts();
        if (index < 0 || index >= scripts.size())
            scripts.add(unit);
        else
            scripts.set(index, unit);
    }

    /**
     * Remove a script unit at the given index.
     * For single-container handlers, clears the unit.
     */
    default void removeScriptUnit(int index) {
        List<IScriptUnit> scripts = getScripts();
        if (index >= 0 && index < scripts.size())
            scripts.remove(index);
    }
}
