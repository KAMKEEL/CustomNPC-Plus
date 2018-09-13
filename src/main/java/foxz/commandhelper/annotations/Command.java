package foxz.commandhelper.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Command {

    String name();

    String desc();

    String usage() default "";

    Class[] sub() default {};
}
