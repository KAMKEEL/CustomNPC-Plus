package noppes.npcs.janino;

import noppes.npcs.constants.EnumScriptType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark script hook methods and associate them with their EnumScriptType.
 * Used by JaninoScript to automatically build a map of hook types to methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ScriptHook {
    /**
     * The script type(s) this method handles.
     * Multiple types can be specified for methods that handle multiple hooks.
     */
    EnumScriptType[] value();
}
