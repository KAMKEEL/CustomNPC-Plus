package noppes.npcs.constants;

import java.util.ArrayList;

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

    public static String[] names(){
        ArrayList<String> list = new ArrayList<String>();
        for(EnumDiagramLayout e : values())
            list.add(e.lang);

        return list.toArray(new String[list.size()]);
    }

    public boolean isManual(){
        return this == CIRCULAR_MANUAL || this == SQUARE_MANUAL || this == TREE_MANUAL;
    }
}
