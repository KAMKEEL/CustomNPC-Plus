package somehussar.gui;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GuiEditable {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Field {
        String name() default "";
    }
}
