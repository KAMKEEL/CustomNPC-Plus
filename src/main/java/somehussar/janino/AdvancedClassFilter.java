package somehussar.janino;

import io.github.somehussar.janinoloader.api.delegates.LoadClassCondition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AdvancedClassFilter implements LoadClassCondition {

    private final Set<String> allowedClasses = new HashSet<>();
    private final Set<Pattern> allowedWildCards = new HashSet<>();
    private final Set<String> bannedClasses = new HashSet<>();
    private final Set<Pattern> bannedWildCards = new HashSet<>();

    public AdvancedClassFilter() {
        addClasses("java.io.Serializable", "java.util.Iterator");
        addRegexes("java\\.lang\\..*");
    }

    public AdvancedClassFilter addRegexes(String... classRegexes) {
        allowedWildCards.addAll(Arrays.stream(classRegexes).map(Pattern::compile).collect(Collectors.toList()));
        return this;
    }
    public AdvancedClassFilter banRegexes(String... classRegexes) {
        bannedWildCards.addAll(Arrays.stream(classRegexes).map(Pattern::compile).collect(Collectors.toList()));
        return this;
    }
    public AdvancedClassFilter addClasses(String... classPath) {
        allowedClasses.addAll(Arrays.asList(classPath));
        return this;
    }
    public AdvancedClassFilter banClasses(String... classPath) {
        bannedClasses.addAll(Arrays.asList(classPath));
        return this;
    }


    @Override
    public boolean isValid(String name) {
        if (bannedClasses.contains(name))
            return false;
        if (bannedWildCards.stream().anyMatch(pattern -> pattern.matcher(name).matches()))
            return false;

        if (name.startsWith("java.lang")) return true;

        if (allowedClasses.contains(name)) return true;

        return allowedWildCards.stream().anyMatch(pattern -> pattern.matcher(name).matches());
    }
}
