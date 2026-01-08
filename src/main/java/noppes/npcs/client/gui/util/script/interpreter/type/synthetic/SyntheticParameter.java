package noppes.npcs.client.gui.util.script.interpreter.type.synthetic;

public class SyntheticParameter {
    public final String name;
    public final String typeName;
    public final String documentation;

    SyntheticParameter(String name, String typeName, String documentation) {
        this.name = name;
        this.typeName = typeName;
        this.documentation = documentation;
    }
}
