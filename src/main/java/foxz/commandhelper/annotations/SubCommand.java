package foxz.commandhelper.annotations;

import java.lang.annotation.*;

@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {

    String name() default "";

    String usage() default "";
    
    boolean hasEmptyCall() default false;

    String desc();

    Class[] permissions() default {};

}
