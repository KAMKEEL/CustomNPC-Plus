package somehussar.gui.annotationHandling.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NumericConstraint {
    double min() default Double.NEGATIVE_INFINITY;
    double max() default Double.POSITIVE_INFINITY;
}
