package noppes.npcs.constants;

/**
 * Identifies which controller a category operation targets.
 * Used by category packets to route to the correct CategoryManager.
 */
public class EnumCategoryType {
    public static final int EFFECT = 1;
    public static final int ANIMATION = 2;
    public static final int LINKED_ITEM = 3;
    public static final int ABILITY = 4;
    public static final int CHAINED_ABILITY = 5;
}
