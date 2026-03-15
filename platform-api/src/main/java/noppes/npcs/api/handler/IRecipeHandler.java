package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IAnvilRecipe;
import noppes.npcs.api.handler.data.IRecipe;

import java.util.List;

/**
 * Handles custom crafting recipes and anvil recipes.
 */
public interface IRecipeHandler {

    /** @return all global (vanilla workbench) recipes. */
    List<IRecipe> getGlobalList();

    /** @return all carpentry table recipes. */
    List<IRecipe> getCarpentryList();

    /** @return all custom anvil recipes. */
    List<IAnvilRecipe> getAnvilList();

    /**
     * Adds a shaped crafting recipe.
     *
     * @param name   the recipe name.
     * @param global true for global (vanilla workbench); false for carpentry.
     * @param result the resulting item stack.
     * @param recipe the recipe pattern and ingredient mappings.
     */
    void addRecipe(String name, boolean global, Object result, Object... recipe);

    /**
     * Adds a shaped crafting recipe with explicit dimensions.
     *
     * @param name   the recipe name.
     * @param global true for global; false for carpentry.
     * @param result the resulting item stack.
     * @param width  the recipe grid width.
     * @param height the recipe grid height.
     * @param items  the ingredient item stacks.
     */
    void addRecipe(String name, boolean global, Object result, int width, int height, Object... items);

    /**
     * Deletes a crafting recipe by ID.
     *
     * @param id the recipe ID.
     * @return the deleted recipe, or null if not found.
     */
    IRecipe delete(int id);

    /**
     * Deletes an anvil recipe by ID.
     *
     * @param id the recipe ID.
     * @return the deleted anvil recipe, or null if not found.
     */
    IAnvilRecipe deleteAnvil(int id);

    /**
     * Adds a custom anvil recipe.
     *
     * @param name             the recipe name.
     * @param global           true for global; false for carpentry.
     * @param itemToRepair     the item to be repaired.
     * @param repairMaterial   the material used for repair.
     * @param xpCost           the experience cost.
     * @param repairPercentage the percentage of durability restored per material.
     */
    void addAnvilRecipe(String name, boolean global, Object itemToRepair, Object repairMaterial, int xpCost, float repairPercentage);
}
