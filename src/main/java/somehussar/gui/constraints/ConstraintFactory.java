package somehussar.gui.constraints;

import somehussar.gui.FieldConstraint;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public interface ConstraintFactory<A extends Annotation> {
    FieldConstraint create(Field field, A annotation);
}
