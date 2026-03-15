package noppes.npcs.api.handler.data;


/**
 * Represents a custom crafting recipe for the carpentry table or vanilla workbench.
 */
public interface IRecipe {

    /** @return the recipe display name. */
    String getName();

    /** @return true if this is a global (vanilla workbench) recipe. */
    boolean isGlobal();

    /** @param global true for global; false for carpentry only. */
    void setIsGlobal(boolean global);

    /** @return true if NBT data is ignored when matching ingredients. */
    boolean getIgnoreNBT();

    /** @param ignoreNBT true to ignore NBT in ingredient matching. */
    void setIgnoreNBT(boolean ignoreNBT);

    /** @return true if damage values are ignored when matching ingredients. */
    boolean getIgnoreDamage();

    /** @param ignoreDamage true to ignore damage in ingredient matching. */
    void setIgnoreDamage(boolean ignoreDamage);

    /** @return the recipe grid width. */
    int getWidth();

    /** @return the recipe grid height. */
    int getHeight();

    /** @return the resulting item stack. */
    Object getResult();

    /** @return the ingredient item stacks in grid order. */
    Object[] getRecipe();

    /** Deletes this recipe from the handler. */
    void delete();

    /** @return the unique recipe ID. */
    int getId();
}
