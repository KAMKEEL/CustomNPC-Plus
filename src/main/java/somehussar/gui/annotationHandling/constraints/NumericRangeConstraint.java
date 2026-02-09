package somehussar.gui.annotationHandling.constraints;

import somehussar.gui.annotationHandling.FieldConstraint;

public final class NumericRangeConstraint implements FieldConstraint {

    private final double min;
    private final double max;

    public NumericRangeConstraint(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void validate(Object value) {
        if (!(value instanceof Number)) {
            throw new ConstraintViolationException("Expected number, got " + value);
        }

        double v = ((Number) value).doubleValue();
        if (v < min || v > max) {
            throw new ConstraintViolationException(
                "Value " + v + " must be between " + min + " and " + max
            );
        }
    }

    public static final class ConstraintViolationException extends RuntimeException {
        public ConstraintViolationException(String message) {
            super(message);
        }
    }

}
