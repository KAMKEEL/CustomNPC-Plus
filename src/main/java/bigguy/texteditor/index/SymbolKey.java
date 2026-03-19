package bigguy.texteditor.index;

import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.Objects;

/**
 * Uniquely identifies a symbol within the document by name, kind, and range.
 * Replaces magic integer offset keys used in the legacy ScriptDocument.
 *
 * <p>Two keys are equal if and only if they share the same name, kind, and range.
 * This guarantees uniqueness even for overloaded methods or shadowed variables
 * at different scopes.</p>
 */
public final class SymbolKey {

    private final String name;
    private final SymbolKind kind;
    private final TextSpan range;

    /**
     * @param name  the symbol's simple name
     * @param kind  the classification of the symbol
     * @param range the source range of the symbol's declaration
     */
    public SymbolKey(String name, SymbolKind kind, TextSpan range) {
        this.name = Objects.requireNonNull(name, "name");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.range = Objects.requireNonNull(range, "range");
    }

    /** @return the symbol's simple name */
    public String name() { return name; }

    /** @return the classification of the symbol */
    public SymbolKind kind() { return kind; }

    /** @return the source range of the symbol's declaration */
    public TextSpan range() { return range; }

    /**
     * Returns a dot-qualified name for nested type display (e.g. "Outer.Inner").
     * For non-nested symbols this returns the simple name.
     *
     * @return the qualified name
     */
    public String qualifiedName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SymbolKey)) return false;
        SymbolKey that = (SymbolKey) obj;
        return name.equals(that.name) && kind == that.kind && range.equals(that.range);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + kind.hashCode();
        result = 31 * result + range.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return kind.displayName() + " '" + name + "' at " + range;
    }
}
