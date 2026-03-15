package somehussar.gui.annotationHandling.constraints;

import somehussar.gui.annotationHandling.FieldConstraint;

import java.lang.reflect.Field;

public class NumericConstraintFactory implements ConstraintFactory<NumericConstraint> {

    @Override
    public FieldConstraint create(Field field, NumericConstraint ann) {
        return new NumericRangeConstraint(ann.min(), ann.max());
    }
}
