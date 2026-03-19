package bigguy.texteditor.index;

import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Hierarchy representing all symbol types in the document.
 * Each variant carries type-specific metadata.
 *
 * <p>Follows IntelliJ's {@code PsiElement} specialization pattern: a common
 * interface with concrete final implementations for each symbol category.</p>
 */
public interface Symbol {

    /** @return the unique key identifying this symbol */
    SymbolKey key();

    /** @return the source range of the symbol's declaration */
    TextSpan range();

    /** @return the symbol's simple name */
    String name();

    /** @return the classification of this symbol */
    SymbolKind kind();

    /**
     * A type declaration (class, interface, enum, annotation).
     */
    final class TypeSymbol implements Symbol {

        private final SymbolKey key;
        private final TextSpan range;
        private final String simpleName;
        private final String fullName;
        private final String dotName;

        /**
         * @param key        the unique key for this symbol
         * @param range      the declaration's source range
         * @param simpleName the unqualified type name
         * @param fullName   the fully qualified name (e.g. "java.util.List")
         * @param dotName    the dot-path from enclosing scope (e.g. "Outer.Inner")
         */
        public TypeSymbol(SymbolKey key, TextSpan range, String simpleName,
                          String fullName, String dotName) {
            this.key = Objects.requireNonNull(key, "key");
            this.range = Objects.requireNonNull(range, "range");
            this.simpleName = Objects.requireNonNull(simpleName, "simpleName");
            this.fullName = Objects.requireNonNull(fullName, "fullName");
            this.dotName = Objects.requireNonNull(dotName, "dotName");
        }

        @Override public SymbolKey key() { return key; }
        @Override public TextSpan range() { return range; }
        @Override public String name() { return simpleName; }
        @Override public SymbolKind kind() { return key.kind(); }

        /** @return the fully qualified name (e.g. "java.util.List") */
        public String fullName() { return fullName; }

        /** @return the dot-path from enclosing scope (e.g. "Outer.Inner") */
        public String dotName() { return dotName; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TypeSymbol)) return false;
            TypeSymbol that = (TypeSymbol) obj;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() { return key.hashCode(); }

        @Override
        public String toString() {
            return kind().displayName() + " " + fullName + " at " + range;
        }
    }

    /**
     * A method or constructor declaration.
     */
    final class MethodSymbol implements Symbol {

        private final SymbolKey key;
        private final TextSpan range;
        private final String name;
        private final TextSpan bodyRange;
        private final List<String> parameterNames;

        /**
         * @param key            the unique key for this symbol
         * @param range          the declaration's source range (including signature)
         * @param name           the method name (or {@code "<init>"} for constructors)
         * @param bodyRange      the range of the method body (may be {@code null} for abstract methods)
         * @param parameterNames the ordered list of parameter names
         */
        public MethodSymbol(SymbolKey key, TextSpan range, String name,
                            TextSpan bodyRange, List<String> parameterNames) {
            this.key = Objects.requireNonNull(key, "key");
            this.range = Objects.requireNonNull(range, "range");
            this.name = Objects.requireNonNull(name, "name");
            this.bodyRange = bodyRange;
            this.parameterNames = Collections.unmodifiableList(
                    new java.util.ArrayList<>(Objects.requireNonNull(parameterNames, "parameterNames")));
        }

        @Override public SymbolKey key() { return key; }
        @Override public TextSpan range() { return range; }
        @Override public String name() { return name; }
        @Override public SymbolKind kind() { return key.kind(); }

        /** @return the range of the method body, or {@code null} for abstract methods */
        public TextSpan bodyRange() { return bodyRange; }

        /** @return an unmodifiable list of parameter names in declaration order */
        public List<String> parameterNames() { return parameterNames; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof MethodSymbol)) return false;
            MethodSymbol that = (MethodSymbol) obj;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() { return key.hashCode(); }

        @Override
        public String toString() {
            return kind().displayName() + " " + name + "(" + String.join(", ", parameterNames) + ") at " + range;
        }
    }

    /**
     * A field, parameter, local variable, or enum constant declaration.
     */
    final class FieldSymbol implements Symbol {

        private final SymbolKey key;
        private final TextSpan range;
        private final String name;
        private final String typeName;
        private final boolean isStatic;
        private final boolean isFinal;

        /**
         * @param key      the unique key for this symbol
         * @param range    the declaration's source range
         * @param name     the field name
         * @param typeName the declared type name (may be {@code null} if unresolved)
         * @param isStatic whether the field is static
         * @param isFinal  whether the field is final
         */
        public FieldSymbol(SymbolKey key, TextSpan range, String name,
                           String typeName, boolean isStatic, boolean isFinal) {
            this.key = Objects.requireNonNull(key, "key");
            this.range = Objects.requireNonNull(range, "range");
            this.name = Objects.requireNonNull(name, "name");
            this.typeName = typeName;
            this.isStatic = isStatic;
            this.isFinal = isFinal;
        }

        @Override public SymbolKey key() { return key; }
        @Override public TextSpan range() { return range; }
        @Override public String name() { return name; }
        @Override public SymbolKind kind() { return key.kind(); }

        /** @return the declared type name, or {@code null} if unresolved */
        public String typeName() { return typeName; }

        /** @return whether the field is static */
        public boolean isStatic() { return isStatic; }

        /** @return whether the field is final */
        public boolean isFinal() { return isFinal; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof FieldSymbol)) return false;
            FieldSymbol that = (FieldSymbol) obj;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() { return key.hashCode(); }

        @Override
        public String toString() {
            String modifiers = (isStatic ? "static " : "") + (isFinal ? "final " : "");
            return kind().displayName() + " " + modifiers
                    + (typeName != null ? typeName + " " : "") + name + " at " + range;
        }
    }

    /**
     * An import declaration.
     */
    final class ImportSymbol implements Symbol {

        private final SymbolKey key;
        private final TextSpan range;
        private final String name;
        private final String fullPath;
        private final boolean isWildcard;
        private final boolean isStatic;

        /**
         * @param key        the unique key for this symbol
         * @param range      the import statement's source range
         * @param name       the simple name (last segment of the import path)
         * @param fullPath   the full import path (e.g. "java.util.List")
         * @param isWildcard whether this is a wildcard import (e.g. "java.util.*")
         * @param isStatic   whether this is a static import
         */
        public ImportSymbol(SymbolKey key, TextSpan range, String name,
                            String fullPath, boolean isWildcard, boolean isStatic) {
            this.key = Objects.requireNonNull(key, "key");
            this.range = Objects.requireNonNull(range, "range");
            this.name = Objects.requireNonNull(name, "name");
            this.fullPath = Objects.requireNonNull(fullPath, "fullPath");
            this.isWildcard = isWildcard;
            this.isStatic = isStatic;
        }

        @Override public SymbolKey key() { return key; }
        @Override public TextSpan range() { return range; }
        @Override public String name() { return name; }
        @Override public SymbolKind kind() { return key.kind(); }

        /** @return the full import path (e.g. "java.util.List") */
        public String fullPath() { return fullPath; }

        /** @return whether this is a wildcard import (e.g. "java.util.*") */
        public boolean isWildcard() { return isWildcard; }

        /** @return whether this is a static import */
        public boolean isStatic() { return isStatic; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ImportSymbol)) return false;
            ImportSymbol that = (ImportSymbol) obj;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() { return key.hashCode(); }

        @Override
        public String toString() {
            String prefix = isStatic ? "static import " : "import ";
            return prefix + fullPath + (isWildcard ? ".*" : "") + " at " + range;
        }
    }
}
