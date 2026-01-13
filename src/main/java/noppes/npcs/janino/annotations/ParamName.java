package noppes.npcs.janino.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the parameter name and type to use when generating method stubs.
 * Applied to interface method parameters to preserve their names and types in generated code.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParamName {
    /** The parameter name (e.g., "event") */
    String value();
    
    /** The fully qualified type name (e.g., "noppes.npcs.api.event.INpcEvent.InitEvent") */
    String type() default "";
}
