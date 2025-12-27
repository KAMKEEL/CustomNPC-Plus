package noppes.npcs.client.gui.util.script.interpreter;

/**
 * Metadata for field access validation.
 * Parallel to MethodCallInfo but for field accesses like `obj.field` or `Class.staticField`.
 */
public class FieldAccessInfo {

    /**
     * Validation error type for field accesses.
     */
    public enum ErrorType {
        NONE,
        TYPE_MISMATCH,        // Field type doesn't match expected type (e.g., assignment LHS)
        UNRESOLVED_FIELD,     // Field doesn't exist on the receiver type
        STATIC_ACCESS_ERROR   // Trying to access instance field statically or vice versa
    }

    private final String fieldName;
    private final int fieldNameStart;
    private final int fieldNameEnd;
    private final TypeInfo receiverType;    // The type on which this field is accessed
    private final FieldInfo resolvedField;  // The resolved field (null if unresolved)
    private final boolean isStaticAccess;   // True if this is Class.field style access
    private TypeInfo expectedType;          // Expected field type (from assignment LHS, etc.)

    private ErrorType errorType = ErrorType.NONE;
    private String errorMessage;

    public FieldAccessInfo(String fieldName, int fieldNameStart, int fieldNameEnd,
                           TypeInfo receiverType, FieldInfo resolvedField, boolean isStaticAccess) {
        this.fieldName = fieldName;
        this.fieldNameStart = fieldNameStart;
        this.fieldNameEnd = fieldNameEnd;
        this.receiverType = receiverType;
        this.resolvedField = resolvedField;
        this.isStaticAccess = isStaticAccess;
    }

    // Getters
    public String getFieldName() {
        return fieldName;
    }

    public int getFieldNameStart() {
        return fieldNameStart;
    }

    public int getFieldNameEnd() {
        return fieldNameEnd;
    }

    public TypeInfo getReceiverType() {
        return receiverType;
    }

    public FieldInfo getResolvedField() {
        return resolvedField;
    }

    public boolean isStaticAccess() {
        return isStaticAccess;
    }

    public TypeInfo getExpectedType() {
        return expectedType;
    }

    public void setExpectedType(TypeInfo expectedType) {
        this.expectedType = expectedType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Validate this field access.
     * Checks type compatibility with expected type.
     */
    public void validate() {
        // Check if field was resolved
        if (resolvedField == null) {
            setError(ErrorType.UNRESOLVED_FIELD, "Cannot resolve field '" + fieldName + "'");
            return;
        }

        // Check return type compatibility with expected type (e.g., assignment LHS)
        if (expectedType != null && resolvedField != null) {
            TypeInfo fieldType = resolvedField.getDeclaredType();
            if (fieldType != null && !isTypeCompatible(fieldType, expectedType)) {
                //extra space is necessary for alignment
                //setError(ErrorType.TYPE_MISMATCH,  "Provided type:      " + fieldType.getSimpleName()+
                        //"\nRequired:                " + expectedType.getSimpleName());
            }
        }
    }

    /**
     * Check if sourceType can be assigned to targetType.
     */
    private boolean isTypeCompatible(TypeInfo sourceType, TypeInfo targetType) {
        if (sourceType == null || targetType == null) {
            return true; // Can't validate, assume compatible
        }

        // Same type
        if (sourceType.getFullName().equals(targetType.getFullName())) {
            return true;
        }

        // Check if sourceType is a subtype of targetType
        if (sourceType.isResolved() && targetType.isResolved()) {
            Class<?> sourceClass = sourceType.getJavaClass();
            Class<?> targetClass = targetType.getJavaClass();

            if (sourceClass != null && targetClass != null) {
                return targetClass.isAssignableFrom(sourceClass);
            }
        }

        // Primitive widening/boxing conversions would go here
        // For now, just check direct equality
        return false;
    }

    private void setError(ErrorType type, String message) {
        this.errorType = type;
        this.errorMessage = message;
    }

    /**
     * Check if this field access has any validation error.
     */
    public boolean hasError() {
        return errorType != ErrorType.NONE;
    }

    /**
     * Check if this is a type mismatch error.
     */
    public boolean hasTypeMismatch() {
        return errorType == ErrorType.TYPE_MISMATCH;
    }

    /**
     * Check if this is an unresolved field error.
     */
    public boolean hasUnresolvedField() {
        return errorType == ErrorType.UNRESOLVED_FIELD;
    }

    /**
     * Check if this is a static access error.
     */
    public boolean hasStaticAccessError() {
        return errorType == ErrorType.STATIC_ACCESS_ERROR;
    }

    @Override
    public String toString() {
        return "FieldAccessInfo{" +
                "fieldName='" + fieldName + "', " +
                "receiverType=" + receiverType + ", " +
                "resolvedField=" + resolvedField + ", " +
                "errorType=" + errorType +
                '}';
    }
}
