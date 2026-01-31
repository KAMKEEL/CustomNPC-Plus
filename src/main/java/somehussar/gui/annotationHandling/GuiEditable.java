package somehussar.gui.annotationHandling;

import java.lang.annotation.Annotation;
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

    @Retention(RetentionPolicy.RUNTIME)
    @interface Size {
        int labelWidth() default 60;
        int fieldWidth() default 50;

        GuiEditable.Size DEFAULT_SIZE = new GuiEditable.Size() {
            @Override
            public int labelWidth() {
                return 60;
            }

            @Override
            public int fieldWidth() {
                return 50;
            }
            @Override public Class<? extends Annotation> annotationType() { return GuiEditable.Size.class; }
        };

    }
}
