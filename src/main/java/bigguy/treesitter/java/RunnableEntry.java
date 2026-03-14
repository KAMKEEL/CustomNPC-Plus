package bigguy.treesitter.java;

import java.util.Collections;
import java.util.Map;

/**
 * A runnable code location detected by the runnables query.
 *
 * <p>Corresponds to Zed's runnable tags: {@code java-main},
 * {@code java-test-method}, {@code java-test-class},
 * {@code java-test-method-nested}, and {@code java-test-class-nested}.</p>
 *
 * @see JavaQueryEngine#runnables(SyntaxTree, String)
 */
public final class RunnableEntry {

    /**
     * The type of runnable detected, mapping to Zed's {@code #set! tag} values.
     */
    public enum Tag {
        /** {@code public static void main(String[] args)} */
        JAVA_MAIN("java-main"),
        /** A method annotated with {@code @Test} */
        JAVA_TEST_METHOD("java-test-method"),
        /** A class containing {@code @Test} methods */
        JAVA_TEST_CLASS("java-test-class"),
        /** {@code @Test} method inside a {@code @Nested} class */
        JAVA_TEST_METHOD_NESTED("java-test-method-nested"),
        /** {@code @Nested} class containing {@code @Test} methods */
        JAVA_TEST_CLASS_NESTED("java-test-class-nested"),
        UNKNOWN("unknown");

        private final String value;
        Tag(String value) { this.value = value; }

        /** @return the tag string as used in Zed's runnables.scm */
        public String getValue() { return value; }

        public static Tag fromValue(String value) {
            for (Tag t : values()) {
                if (t.value.equals(value)) return t;
            }
            return UNKNOWN;
        }
    }

    private final Tag tag;
    private final TextSpan span;
    private final String className;
    private final String methodName;
    private final String packageName;
    private final Map<String, String> metadata;

    public RunnableEntry(Tag tag, TextSpan span, String className, String methodName,
                         String packageName, Map<String, String> metadata) {
        this.tag = tag;
        this.span = span;
        this.className = className;
        this.methodName = methodName;
        this.packageName = packageName;
        this.metadata = metadata != null
                ? Collections.unmodifiableMap(metadata)
                : Collections.<String, String>emptyMap();
    }

    /** @return the runnable tag identifying the entry type */
    public Tag getTag() { return tag; }

    /** @return the source span of the runnable element */
    public TextSpan getSpan() { return span; }

    /** @return the enclosing class name, or {@code null} */
    public String getClassName() { return className; }

    /** @return the method name (for test methods / main), or {@code null} */
    public String getMethodName() { return methodName; }

    /** @return the Java package name, or {@code null} */
    public String getPackageName() { return packageName; }

    /** @return any additional metadata from the match (e.g. from {@code #set!} directives) */
    public Map<String, String> getMetadata() { return metadata; }

    /**
     * @return a fully-qualified identifier suitable for test runner invocation,
     *         e.g. {@code "com.example.MyTest#testFoo"}
     */
    public String getFullyQualifiedName() {
        StringBuilder sb = new StringBuilder();
        if (packageName != null && !packageName.isEmpty()) {
            sb.append(packageName).append('.');
        }
        if (className != null) {
            sb.append(className);
        }
        if (methodName != null && !methodName.isEmpty()) {
            sb.append('#').append(methodName);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Runnable{%s %s %s}", tag.getValue(), getFullyQualifiedName(), span);
    }
}
