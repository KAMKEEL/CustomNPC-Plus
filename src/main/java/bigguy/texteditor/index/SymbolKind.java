package bigguy.texteditor.index;

/**
 * Classifies symbols within a source document for indexing, navigation, and
 * icon display.
 *
 * <p>Follows the LSP {@code SymbolKind} enumeration and IntelliJ IDEA's
 * {@code IElementType} hierarchy, covering the Java language constructs
 * relevant to the script editor.</p>
 */
public enum SymbolKind {

    CLASS("Class", true, false, false),
    INTERFACE("Interface", true, false, false),
    ENUM("Enum", true, false, false),
    ANNOTATION("Annotation", true, false, false),
    METHOD("Method", false, true, false),
    CONSTRUCTOR("Constructor", false, true, false),
    FIELD("Field", false, false, true),
    PARAMETER("Parameter", false, false, true),
    LOCAL_VARIABLE("Local Variable", false, false, true),
    IMPORT("Import", false, false, false),
    PACKAGE("Package", false, false, false),
    TYPE_PARAMETER("Type Parameter", false, false, false),
    LAMBDA("Lambda", false, true, false),
    ENUM_CONSTANT("Enum Constant", false, false, true),
    STATIC_INITIALIZER("Static Initializer", false, false, false);

    private final String displayName;
    private final boolean type;
    private final boolean callable;
    private final boolean variable;

    SymbolKind(String displayName, boolean type, boolean callable, boolean variable) {
        this.displayName = displayName;
        this.type = type;
        this.callable = callable;
        this.variable = variable;
    }

    /**
     * Returns a human-readable name suitable for UI display (e.g. "Local Variable").
     *
     * @return the display name
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns {@code true} for type-declaring symbols:
     * {@link #CLASS}, {@link #INTERFACE}, {@link #ENUM}, {@link #ANNOTATION}.
     *
     * @return whether this kind represents a type declaration
     */
    public boolean isType() {
        return type;
    }

    /**
     * Returns {@code true} for callable symbols:
     * {@link #METHOD}, {@link #CONSTRUCTOR}, {@link #LAMBDA}.
     *
     * @return whether this kind represents something that can be invoked
     */
    public boolean isCallable() {
        return callable;
    }

    /**
     * Returns {@code true} for variable-like symbols:
     * {@link #FIELD}, {@link #PARAMETER}, {@link #LOCAL_VARIABLE}, {@link #ENUM_CONSTANT}.
     *
     * @return whether this kind represents a named value holder
     */
    public boolean isVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
