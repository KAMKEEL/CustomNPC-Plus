package somehussar.gui;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GuiEditable {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Field {
        String value();

        /**
         * Display order
         * Higher value = higher priority.
         */
        int order() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Group {
        String value();
    }
}
