package noppes.npcs.client.gui.util.script.autocomplete;

import noppes.npcs.client.gui.util.script.autocomplete.weighter.*;

import java.util.Arrays;

public class CompletionWeigherChains {

    public static WeigherChain javaChain() {
        return new WeigherChain(Arrays.asList(
            new MatchQualityWeigher(),
            new MemberKindWeigher(),
            new ContextFitWeigher(),
            new ImportStatusWeigher(),
            new UsageFrequencyWeigher(),
            new DeprecatedWeigher(),
            new AlphabeticalWeigher()
        ));
    }

    public static WeigherChain jsChain() {
        return new WeigherChain(Arrays.asList(
            new MatchQualityWeigher(),
            new MemberKindWeigher(),
            new ContextFitWeigher(),
            new ImportStatusWeigher(),
            new UsageFrequencyWeigher(),
            new InheritanceDepthWeigher(),
            new DeprecatedWeigher(),
            new AlphabeticalWeigher()
        ));
    }
}
