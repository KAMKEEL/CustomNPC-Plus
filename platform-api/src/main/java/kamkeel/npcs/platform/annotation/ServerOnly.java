package kamkeel.npcs.platform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks code that should only exist on the dedicated server.
 *
 * This is a documentation-only annotation in core — it does NOT trigger
 * class stripping. See {@link ClientOnly} for details.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface ServerOnly {
}
