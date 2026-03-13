package noppes.npcs.client.gui.util.script.autocomplete.weighter;

import noppes.npcs.client.gui.util.script.autocomplete.AutocompleteItem;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

public class MemberKindWeigher extends CompletionWeigher {

    public enum MemberKind {
        ENUM_CONSTANT,
        LOCAL_VARIABLE,
        INSTANCE_FIELD,
        INSTANCE_METHOD,
        TYPE_CLASS,
        TYPE_ENUM,
        TYPE_INTERFACE,
        STATIC_FIELD,
        STATIC_METHOD,
        KEYWORD,
        SNIPPET
    }

    public MemberKindWeigher() {
        super("memberKind");
    }

    @Override
    public Comparable<?> weigh(AutocompleteItem item, ScoringContext context) {
        switch (item.getKind()) {
            case ENUM_CONSTANT: return MemberKind.ENUM_CONSTANT;
            case PARAMETER:     return MemberKind.LOCAL_VARIABLE;
            case VARIABLE:      return MemberKind.LOCAL_VARIABLE;
            case METHOD:
                return item.isStatic() ? MemberKind.STATIC_METHOD : MemberKind.INSTANCE_METHOD;
            case FIELD:
                return item.isStatic() ? MemberKind.STATIC_FIELD : MemberKind.INSTANCE_FIELD;
            case CLASS:         return resolveTypeTier(item);
            case ENUM:          return MemberKind.TYPE_ENUM;
            case KEYWORD:       return MemberKind.KEYWORD;
            case SNIPPET:       return MemberKind.SNIPPET;
            default:            return MemberKind.INSTANCE_FIELD;
        }
    }

    private MemberKind resolveTypeTier(AutocompleteItem item) {
        Object source = item.getSourceData();
        if (source instanceof TypeInfo) {
            TypeInfo typeInfo = (TypeInfo) source;
            switch (typeInfo.getKind()) {
                case INTERFACE: return MemberKind.TYPE_INTERFACE;
                case ENUM:      return MemberKind.TYPE_ENUM;
                default:        return MemberKind.TYPE_CLASS;
            }
        }
        return MemberKind.TYPE_CLASS;
    }
}
