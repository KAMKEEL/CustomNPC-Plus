package noppes.npcs.constants;

public enum EnumDiagramLayout {
    CIRCULAR("diagram.circular"),
    SQUARE("diagram.square"),
    TREE("diagram.tree"),
    GENERATED("diagram.generated"),
    CIRCULAR_MANUAL("diagram.circular_manual"),
    SQUARE_MANUAL("diagram.square_manual"),
    TREE_MANUAL("diagram.tree_manual"),
    CHART("diagram.chart");

    private String lang;
    EnumDiagramLayout(String langName){
        this.lang = langName;
    }

    public String getLang(){
        return lang;
    }
}
