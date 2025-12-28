package noppes.npcs.client.gui.util.script.interpreter.field;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeChecker;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Metadata for assignment validation.
 * Tracks assignment statements like "varName = expr" or "obj.field = expr"
 * and validates type compatibility, access modifiers, and final status.
 * 
 * Each AssignmentInfo represents a single assignment to a field.
 * The positions are:
 * - lhsStart/lhsEnd: The target variable/field position (left-hand side)
 * - rhsStart/rhsEnd: From first token of RHS to the semicolon (inclusive)
 */
public class AssignmentInfo {

    /**
     * Error type for assignment validation.
     */
    public enum ErrorType {
        NONE,
        TYPE_MISMATCH,          // Assigned value type doesn't match target type
        FINAL_REASSIGNMENT,     // Attempting to reassign a final field
        PRIVATE_ACCESS,         // Attempting to access private field
        PROTECTED_ACCESS,       // Attempting to access protected field from invalid context
        UNRESOLVED_TARGET,      // Target variable/field doesn't exist
        STATIC_CONTEXT_ERROR,   // Accessing instance field from static context
        DUPLICATE_DECLARATION   // Variable is already defined in the scope
    }

    // Statement position
    private final int statementStart;       // Absolute start of statement (includes modifiers/type for declarations)
    
    // Target/LHS info
    private final String targetName;
    private final int lhsStart;             // Position of first char of target variable/field
    private final int lhsEnd;               // Position after last char of target
    private final TypeInfo targetType;      // Declared type of target
    
    // Source/RHS info (from first token of RHS expression to semicolon)
    private final int rhsStart;             // Position of first non-whitespace char after '='
    private final int rhsEnd;               // Position of ';' (inclusive in the range for underlining)
    private final TypeInfo sourceType;      // Resolved type of RHS expression
    private final String sourceExpr;        // The RHS expression text (for display)
    
    // For chained field access (e.g., obj.field = value)
    private final TypeInfo receiverType;    // Type of receiver (null for simple variables)
    private final Field reflectionField; // Java reflection field (for modifier checks)
    
    // Flag for script-defined final variables
    private final boolean isFinal;
    
    // Validation
    private ErrorType errorType = ErrorType.NONE;
    private String errorMessage;
    private String requiredType;
    private String providedType;

    public AssignmentInfo(String targetName, int statementStart, int lhsStart, int lhsEnd,
                          TypeInfo targetType, int rhsStart, int rhsEnd,
                          TypeInfo sourceType, String sourceExpr,
                          TypeInfo receiverType, java.lang.reflect.Field reflectionField,
                          boolean isFinal) {
        this.targetName = targetName;
        this.statementStart = statementStart;
        this.lhsStart = lhsStart;
        this.lhsEnd = lhsEnd;
        this.targetType = targetType;
        this.rhsStart = rhsStart;
        this.rhsEnd = rhsEnd;
        this.sourceType = sourceType;
        this.sourceExpr = sourceExpr;
        this.receiverType = receiverType;
        this.reflectionField = reflectionField;
        this.isFinal = isFinal;
    }
    
    /**
     * Factory method to create an AssignmentInfo representing a duplicate declaration error.
     * Only the LHS (variable name) position is relevant for underlining.
     */
    public static AssignmentInfo duplicateDeclaration(String varName, int nameStart, int nameEnd, String errorMessage) {
        AssignmentInfo info = new AssignmentInfo(
            varName,
            nameStart,      // statementStart = nameStart for underline positioning
            nameStart,      // lhsStart
            nameEnd,        // lhsEnd
            null,           // targetType
            -1,             // rhsStart (not applicable)
            -1,             // rhsEnd (not applicable)
            null,           // sourceType
            null,           // sourceExpr
            null,           // receiverType
            null,           // reflectionField
            false           // isFinal
        );
        info.setError(ErrorType.DUPLICATE_DECLARATION, errorMessage);
        return info;
    }

    // ==================== VALIDATION ====================

    /**
     * Validate this assignment.
     * Checks type compatibility, final status, and access modifiers.
     */
    public void validate() {
        // Check for final field reassignment from reflection
        if (reflectionField != null && Modifier.isFinal(reflectionField.getModifiers())) {
            setError(ErrorType.FINAL_REASSIGNMENT, 
                    "Cannot assign a value to final variable '" + targetName + "'");
            return;
        }
        
        // Check for final field reassignment from FieldInfo (script-defined finals)
        if (isFinal) {
            setError(ErrorType.FINAL_REASSIGNMENT, 
                    "Cannot assign a value to final variable '" + targetName + "'");
            return;
        }

        // Check access modifiers for chained field access
        if (reflectionField != null && receiverType != null) {
            int mods = reflectionField.getModifiers();
            if (Modifier.isPrivate(mods)) {
                setError(ErrorType.PRIVATE_ACCESS,
                        "'" + targetName + "' has private access in '" + receiverType.getFullName() + "'");
                return;
            }
        }

        // Check type compatibility
        if (targetType != null && sourceType != null) {
            if (!TypeChecker.isTypeCompatible(targetType, sourceType)) {
                this.requiredType = targetType.getSimpleName();
                this.providedType = sourceType.getSimpleName();
                setError(ErrorType.TYPE_MISMATCH, buildTypeMismatchMessage());
            }
        }
    }

    /**
     * Build a formatted type mismatch error message (IntelliJ style).
     */
    private String buildTypeMismatchMessage() {
        return "Provided type:     " + providedType + "\nRequired:             " + requiredType;
    }

    private void setError(ErrorType type, String message) {
        this.errorType = type;
        this.errorMessage = message;
    }

    // ==================== POSITION CHECKS ====================

    /**
     * Check if the given global position falls within the LHS (target) of this assignment.
     */
    public boolean containsLhsPosition(int position) {
        return position >= statementStart && position < lhsEnd;
    }

    /**
     * Check if the given global position falls within the RHS (source) of this assignment.
     */
    public boolean containsRhsPosition(int position) {
        return position >= rhsStart && position <= rhsEnd;
    }

    /**
     * Check if the given global position falls anywhere in this assignment (LHS or RHS).
     */
    public boolean containsPosition(int position) {
        return position >= statementStart && position <= rhsEnd;
    }

    // ==================== ERROR TYPE CHECKS ====================

    /**
     * Check if this is an LHS error (final reassignment, access errors, duplicate declarations).
     * These errors should underline the LHS.
     */
    public boolean isLhsError() {
        return errorType == ErrorType.FINAL_REASSIGNMENT ||
               errorType == ErrorType.PRIVATE_ACCESS ||
               errorType == ErrorType.PROTECTED_ACCESS ||
               errorType == ErrorType.STATIC_CONTEXT_ERROR ||
               errorType == ErrorType.DUPLICATE_DECLARATION;
    }

    /**
     * Check if this is an RHS error (type mismatch).
     * These errors should underline the RHS.
     */
    public boolean isRhsError() {
        return false;
    }

    public boolean isFullLineError() {
        return errorType == ErrorType.TYPE_MISMATCH;
    }

    // ==================== GETTERS ====================

    public String getTargetName() { return targetName; }
    public int getStatementStart() { return statementStart; }
    public int getLhsStart() { return lhsStart; }
    public int getLhsEnd() { return lhsEnd; }
    public TypeInfo getTargetType() { return targetType; }
    public int getRhsStart() { return rhsStart; }
    public int getRhsEnd() { return rhsEnd; }
    public TypeInfo getSourceType() { return sourceType; }
    public String getSourceExpr() { return sourceExpr; }
    public TypeInfo getReceiverType() { return receiverType; }
    public java.lang.reflect.Field getReflectionField() { return reflectionField; }
    
    public ErrorType getErrorType() { return errorType; }
    public String getErrorMessage() { return errorMessage; }
    public String getRequiredType() { return requiredType; }
    public String getProvidedType() { return providedType; }

    public boolean hasError() { return errorType != ErrorType.NONE; }
    public boolean hasTypeMismatch() { return errorType == ErrorType.TYPE_MISMATCH; }
    public boolean hasFinalReassignment() { return errorType == ErrorType.FINAL_REASSIGNMENT; }
    public boolean hasAccessError() { return errorType == ErrorType.PRIVATE_ACCESS || errorType == ErrorType.PROTECTED_ACCESS; }

    @Override
    public String toString() {
        return "AssignmentInfo{" +
                "target='" + targetName + "', " +
                "stmt=" + statementStart + ", " +
                "lhs=[" + lhsStart + "-" + lhsEnd + "], " +
                "rhs=[" + rhsStart + "-" + rhsEnd + "], " +
                "targetType=" + targetType + ", " +
                "sourceType=" + sourceType + ", " +
                "error=" + errorType +
                '}';
    }
}
