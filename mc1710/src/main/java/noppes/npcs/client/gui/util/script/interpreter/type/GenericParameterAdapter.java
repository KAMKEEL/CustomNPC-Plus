package noppes.npcs.client.gui.util.script.interpreter.type;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Adapts {@code Object}-typed method parameters to the receiver's generic type arguments
 * for editor display (autocomplete/hover).
 * <p>
 * Java collections use {@code Object} in certain method signatures for backward-compatibility
 * (e.g., {@code Map.get(Object)} instead of {@code Map.get(K)}). This adapter maps those
 * {@code Object} parameters back to the appropriate generic type argument when the receiver
 * is parameterized (e.g., {@code Map<String, Integer>}).
 * <p>
 * <b>Design rules:</b>
 * <ul>
 *   <li>Only contract-level checks using {@code java.util.Map}, {@code java.util.Collection},
 *       {@code java.util.List} assignability — never concrete classes like HashMap/ArrayList.</li>
 *   <li>Only adapts when the reflected parameter type is exactly {@code java.lang.Object}
 *       AND the receiver is parameterized with applied type args.</li>
 *   <li>Table-driven: each rule is a {@link Rule} record in a static list.</li>
 * </ul>
 */
public final class GenericParameterAdapter {

    // ==================== Rule Model ====================

    /**
     * A single adaptation rule.
     *
     * @see #RULES
     */
    private static final class Rule {
        /** The contract interface (e.g., {@code java.util.Map}). */
        final Class<?> contractType;
        /** Method name to match. */
        final String methodName;
        /** Total parameter count the method must have. */
        final int paramCount;
        /** The parameter index to adapt (0-based). */
        final int paramIndex;
        /** Index into the receiver's applied type args (0-based). 0=K/E, 1=V, etc. */
        final int typeArgIndex;

        Rule(Class<?> contractType, String methodName, int paramCount, int paramIndex, int typeArgIndex) {
            this.contractType = contractType;
            this.methodName = methodName;
            this.paramCount = paramCount;
            this.paramIndex = paramIndex;
            this.typeArgIndex = typeArgIndex;
        }
    }

    // ==================== Rule Table ====================

    private static final List<Rule> RULES;

    static {
        List<Rule> rules = new ArrayList<>();

        // Map<K, V>
        rules.add(new Rule(java.util.Map.class, "get",            1, 0, 0));
        rules.add(new Rule(java.util.Map.class, "containsKey",    1, 0, 0));
        rules.add(new Rule(java.util.Map.class, "containsValue",  1, 0, 1));
        rules.add(new Rule(java.util.Map.class, "remove",         1, 0, 0));
        rules.add(new Rule(java.util.Map.class, "remove",         2, 0, 0));
        rules.add(new Rule(java.util.Map.class, "remove",         2, 1, 1));
        rules.add(new Rule(java.util.Map.class, "getOrDefault",   2, 0, 0));

        // ConcurrentMap<K, V> (get/containsKey/containsValue inherited from Map above)
        rules.add(new Rule(java.util.concurrent.ConcurrentMap.class, "remove",       2, 0, 0));
        rules.add(new Rule(java.util.concurrent.ConcurrentMap.class, "remove",       2, 1, 1));
        rules.add(new Rule(java.util.concurrent.ConcurrentMap.class, "getOrDefault", 2, 0, 0));

        // Collection<E>
        rules.add(new Rule(java.util.Collection.class, "contains", 1, 0, 0));
        rules.add(new Rule(java.util.Collection.class, "remove",   1, 0, 0));

        // List<E> (contains/remove covered by Collection above)
        rules.add(new Rule(java.util.List.class, "indexOf",     1, 0, 0));
        rules.add(new Rule(java.util.List.class, "lastIndexOf", 1, 0, 0));

        // Set<E> (contains/remove covered by Collection above)

        // Deque<E>
        rules.add(new Rule(java.util.Deque.class, "contains",              1, 0, 0));
        rules.add(new Rule(java.util.Deque.class, "remove",                1, 0, 0));
        rules.add(new Rule(java.util.Deque.class, "removeFirstOccurrence", 1, 0, 0));
        rules.add(new Rule(java.util.Deque.class, "removeLastOccurrence",  1, 0, 0));

        // BlockingQueue<E> (contains/remove covered by Collection above)
        rules.add(new Rule(java.util.concurrent.BlockingQueue.class, "remove",   1, 0, 0));
        rules.add(new Rule(java.util.concurrent.BlockingQueue.class, "contains", 1, 0, 0));

        // BlockingDeque<E>
        rules.add(new Rule(java.util.concurrent.BlockingDeque.class, "remove",                1, 0, 0));
        rules.add(new Rule(java.util.concurrent.BlockingDeque.class, "contains",              1, 0, 0));
        rules.add(new Rule(java.util.concurrent.BlockingDeque.class, "removeFirstOccurrence", 1, 0, 0));
        rules.add(new Rule(java.util.concurrent.BlockingDeque.class, "removeLastOccurrence",  1, 0, 0));

        RULES = Collections.unmodifiableList(rules);
    }

    // ==================== Public API ====================

    /**
     * Attempt to adapt a parameter type from {@code Object} to the receiver's generic type argument.
     * <p>
     * Returns the adapted {@link TypeInfo} if all conditions are met:
     * <ol>
     *   <li>The receiver is parameterized and has applied type args.</li>
     *   <li>The parameter's current type is exactly {@code java.lang.Object}.</li>
     *   <li>A matching rule exists in the rule table.</li>
     *   <li>The rule's required type arg index is within bounds.</li>
     * </ol>
     * Otherwise returns {@code null} (meaning: keep the original type).
     *
     * @param method        the reflected Java method
     * @param receiverType  the parameterized receiver TypeInfo (e.g., {@code Map<String, Integer>})
     * @param paramIndex    the index of the parameter being checked
     * @param currentType   the current TypeInfo for that parameter (after normal substitution)
     * @return adapted TypeInfo, or {@code null} if no adaptation applies
     */
    public static TypeInfo adaptParameterType(Method method, TypeInfo receiverType,
                                               int paramIndex, TypeInfo currentType) {
        // Guard: only adapt Object params on parameterized receivers
        if (receiverType == null || !receiverType.isParameterized()) {
            return null;
        }
        if (currentType == null || currentType.getJavaClass() != Object.class) {
            return null;
        }

        List<TypeInfo> appliedArgs = receiverType.getAppliedTypeArgs();
        if (appliedArgs == null || appliedArgs.isEmpty()) {
            return null;
        }

        // Determine the declaring class of the method
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName();
        int paramCount = method.getParameterCount();

        // Scan rules for a match
        for (Rule rule : RULES) {
            if (rule.paramIndex != paramIndex) continue;
            if (rule.paramCount != paramCount) continue;
            if (!rule.methodName.equals(methodName)) continue;
            if (!rule.contractType.isAssignableFrom(declaringClass)) continue;

            // Rule matches — check if the type arg index is within bounds
            if (rule.typeArgIndex < appliedArgs.size()) {
                TypeInfo adapted = appliedArgs.get(rule.typeArgIndex);
                if (adapted != null && adapted.isResolved()) {
                    return adapted;
                }
            }
        }

        return null;
    }

    private GenericParameterAdapter() {
        // Utility class — no instantiation
    }
}
