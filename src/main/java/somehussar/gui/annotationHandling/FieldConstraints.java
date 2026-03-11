package somehussar.gui.annotationHandling;

public final class FieldConstraints {

    private static final FieldConstraints EMPTY =
        new FieldConstraints(new FieldConstraint[0]);

    private final FieldConstraint[] constraints;

    public FieldConstraints(FieldConstraint[] constraints) {
        this.constraints = constraints;
    }

    public static FieldConstraints empty() {
        return EMPTY;
    }

    public void validate(Object value) {
        for (FieldConstraint c : constraints) {
            c.validate(value);
        }
    }

    public boolean isEmpty() {
        return constraints.length == 0;
    }
}
