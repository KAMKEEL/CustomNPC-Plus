package noppes.npcs.constants;

import java.lang.annotation.*;

/**
 * Marks an event interface as belonging to a specific script context.
 * 
 * Usage:
 * <pre>
 * {@literal @}ScriptContextMarker("NPC")
 * public interface INpcEvent extends ICustomNPCsEvent {
 *     // ...
 * }
 * </pre>
 * 
 * Multiple interfaces can share the same context name - they will all be
 * registered as namespaces for that context.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptContextMarker {
    /**
     * The script context ID this event interface belongs to.
     * Examples: "NPC", "PLAYER", "BLOCK", "ITEM", "FORGE", "GLOBAL"
     */
    String value();
}
