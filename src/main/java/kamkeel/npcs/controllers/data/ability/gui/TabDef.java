package kamkeel.npcs.controllers.data.ability.gui;

/**
 * Simple data class representing a tab in the ability config GUI.
 * Built automatically from the FieldDef list.
 */
public class TabDef {

    public final String name;
    public final TabTarget target;
    public int index;

    public TabDef(String name, TabTarget target, int index) {
        this.name = name;
        this.target = target;
        this.index = index;
    }

    /**
     * Returns a unique key combining target and custom name for deduplication.
     */
    public String getKey() {
        if (target == TabTarget.CUSTOM && name != null) {
            return "CUSTOM:" + name;
        }
        return target.name();
    }
}
