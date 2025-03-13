package noppes.npcs.controllers;

import kamkeel.npcs.controllers.SyncController;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.handler.data.IAnvilRecipe;
import noppes.npcs.api.handler.data.IRecipe;
import noppes.npcs.controllers.data.RecipeAnvil;
import noppes.npcs.controllers.data.RecipeCarpentry;
import noppes.npcs.controllers.data.RecipesDefault;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class RecipeController implements IRecipeHandler {
    private static Collection<RecipeCarpentry> prevRecipes;

    public HashMap<Integer, RecipeCarpentry> globalRecipes = new HashMap<Integer, RecipeCarpentry>();
    public HashMap<Integer, RecipeCarpentry> carpentryRecipes = new HashMap<Integer, RecipeCarpentry>();
    public HashMap<Integer, RecipeAnvil> anvilRecipes = new HashMap<Integer, RecipeAnvil>();

    public static RecipeController Instance;

    public static final int version = 1;
    public int nextId = 1;
    public int nextAnvilId = 1;

    public static HashMap<Integer, RecipeCarpentry> syncRecipes = new HashMap<Integer, RecipeCarpentry>();
    public static HashMap<Integer, RecipeAnvil> syncAnvilRecipes = new HashMap<Integer, RecipeAnvil>();

    public RecipeController() {
        Instance = this;
    }

    public void load() {
        loadCategories();
        reloadGlobalRecipes(globalRecipes);
    }

    public static void reloadGlobalRecipes(HashMap<Integer, RecipeCarpentry> globalRecipes) {
        List list = CraftingManager.getInstance().getRecipeList();
        if (prevRecipes != null) {
            list.removeAll(prevRecipes);
        }

        prevRecipes = new HashSet<RecipeCarpentry>();
        for (RecipeCarpentry recipe : globalRecipes.values()) {
            if (recipe.isValid())
                prevRecipes.add(recipe);
        }
        list.addAll(prevRecipes);
    }

    private void loadCategories() {
        File saveDir = CustomNpcs.getWorldSaveDirectory();
        try {
            File file = new File(saveDir, "recipes.dat");
            if (file.exists()) {
                loadCategories(file);
            } else {
                globalRecipes.clear();
                carpentryRecipes.clear();
                anvilRecipes.clear();
                loadDefaultRecipes(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                File file = new File(saveDir, "recipes.dat_old");
                if (file.exists()) {
                    loadCategories(file);
                }
            } catch (Exception ee) {
                e.printStackTrace();
            }
        }
    }

    private void loadDefaultRecipes(int i) {
        if (i == version)
            return;
        RecipesDefault.loadDefaultRecipes(i);
        saveCategories();
    }

    private void loadCategories(File file) throws Exception {
        NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
        nextId = nbttagcompound1.getInteger("LastId");
        nextAnvilId = nbttagcompound1.getInteger("LastAnvilId");
        NBTTagList list = nbttagcompound1.getTagList("Data", 10);
        HashMap<Integer, RecipeCarpentry> globalRecipes = new HashMap<Integer, RecipeCarpentry>();
        HashMap<Integer, RecipeCarpentry> carpentryRecipes = new HashMap<Integer, RecipeCarpentry>();
        HashMap<Integer, RecipeAnvil> anvilRecipes = new HashMap<Integer, RecipeAnvil>();
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound compound = list.getCompoundTagAt(i);
                if (compound.hasKey("IsAnvil")) {
                    RecipeAnvil anvil = RecipeAnvil.read(compound);
                    anvilRecipes.put(anvil.id, anvil);
                    if (anvil.id > nextAnvilId)
                        nextAnvilId = anvil.id;
                } else {
                    RecipeCarpentry recipe = RecipeCarpentry.read(compound);
                    if (recipe.isGlobal)
                        globalRecipes.put(recipe.id, recipe);
                    else
                        carpentryRecipes.put(recipe.id, recipe);
                    if (recipe.id > nextId)
                        nextId = recipe.id;
                }
            }
        }
        this.carpentryRecipes = carpentryRecipes;
        this.globalRecipes = globalRecipes;
        this.anvilRecipes = anvilRecipes;
        loadDefaultRecipes(nbttagcompound1.getInteger("Version"));
    }

    private void saveCategories() {
        try {
            File saveDir = CustomNpcs.getWorldSaveDirectory();
            NBTTagList list = new NBTTagList();
            for (RecipeCarpentry recipe : globalRecipes.values()) {
                list.appendTag(recipe.writeNBT());
            }
            for (RecipeCarpentry recipe : carpentryRecipes.values()) {
                list.appendTag(recipe.writeNBT());
            }
            for (RecipeAnvil recipe : anvilRecipes.values()) {
                list.appendTag(recipe.writeNBT());
            }
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("Data", list);
            nbttagcompound.setInteger("LastId", nextId);
            nbttagcompound.setInteger("LastAnvilId", nextAnvilId);
            nbttagcompound.setInteger("Version", version);
            File file = new File(saveDir, "recipes.dat_new");
            File file1 = new File(saveDir, "recipes.dat_old");
            File file2 = new File(saveDir, "recipes.dat");
            CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file));
            if (file1.exists()) {
                file1.delete();
            }
            file2.renameTo(file1);
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RecipeCarpentry findMatchingRecipe(InventoryCrafting par1InventoryCrafting) {
        for (RecipeCarpentry recipe : carpentryRecipes.values()) {
            if (recipe.isValid() && recipe.matches(par1InventoryCrafting, null))
                return recipe;
        }
        return null;
    }

    public RecipeCarpentry getRecipe(int id) {
        if (globalRecipes.containsKey(id))
            return globalRecipes.get(id);
        if (carpentryRecipes.containsKey(id))
            return carpentryRecipes.get(id);
        return null;
    }

    public RecipeAnvil getAnvilRecipe(int id) {
        if (anvilRecipes.containsKey(id))
            return anvilRecipes.get(id);
        return null;
    }

    public RecipeCarpentry saveRecipe(NBTTagCompound compound) throws IOException {
        RecipeCarpentry recipe = RecipeCarpentry.read(compound);

        RecipeCarpentry current = getRecipe(recipe.id);
        if (current != null && !current.name.equals(recipe.name)) {
            while (containsRecipeName(recipe.name))
                recipe.name += "_";
        }

        if (recipe.id == -1) {
            recipe.id = getUniqueId();
            while (containsRecipeName(recipe.name))
                recipe.name += "_";
        }
        if (recipe.isGlobal) {
            carpentryRecipes.remove(recipe.id);
            globalRecipes.put(recipe.id, recipe);
        } else {
            globalRecipes.remove(recipe.id);
            carpentryRecipes.put(recipe.id, recipe);
        }
        saveCategories();
        reloadGlobalRecipes(globalRecipes);
        return recipe;
    }

    public RecipeAnvil saveAnvilRecipe(NBTTagCompound compound) throws IOException {
        RecipeAnvil recipe = RecipeAnvil.read(compound);

        RecipeAnvil current = getAnvilRecipe(recipe.id);
        if (current != null && !current.name.equals(recipe.name)) {
            while (containsAnvilRecipeName(recipe.name))
                recipe.name += "_";
        }

        if (recipe.id == -1) {
            recipe.id = getUniqueAnvilId();
            while (containsAnvilRecipeName(recipe.name))
                recipe.name += "_";
        }

        anvilRecipes.put(recipe.id, recipe);
        saveCategories();
        return recipe;
    }

    private int getUniqueId() {
        return nextId++;
    }

    private int getUniqueAnvilId() {
        return nextAnvilId++;
    }

    private boolean containsRecipeName(String name) {
        name = name.toLowerCase();
        for (RecipeCarpentry recipe : globalRecipes.values()) {
            if (recipe.name.toLowerCase().equals(name))
                return true;
        }
        for (RecipeCarpentry recipe : carpentryRecipes.values()) {
            if (recipe.name.toLowerCase().equals(name))
                return true;
        }
        return false;
    }

    private boolean containsAnvilRecipeName(String name) {
        name = name.toLowerCase();
        for (RecipeAnvil recipe : anvilRecipes.values()) {
            if (recipe.name.toLowerCase().equals(name))
                return true;
        }
        return false;
    }

    public RecipeCarpentry delete(int id) {
        RecipeCarpentry recipe = getRecipe(id);
        if (recipe == null)
            return null;
        RecipeCarpentry globalRecipe = globalRecipes.remove(recipe.id);
        if (globalRecipe != null)
            SyncController.syncAllWorkbenchRecipes();

        RecipeCarpentry carpentry = carpentryRecipes.remove(recipe.id);
        if (carpentry != null)
            SyncController.syncAllCarpentryRecipes();

        saveCategories();
        reloadGlobalRecipes(globalRecipes);
        return recipe;
    }

    public RecipeAnvil deleteAnvil(int id) {
        RecipeAnvil recipe = getAnvilRecipe(id);
        if (recipe == null)
            return null;

        RecipeAnvil anvilRecipe = anvilRecipes.remove(recipe.id);
        if (anvilRecipe != null)
            SyncController.syncAllAnvilRecipes();

        saveCategories();
        return recipe;
    }

    public void addRecipe(RecipeCarpentry recipeCarpentry) {
        recipeCarpentry.id = getUniqueId();
        if (!recipeCarpentry.isGlobal)
            RecipeController.Instance.carpentryRecipes.put(recipeCarpentry.id, recipeCarpentry);
        else {
            RecipeController.Instance.globalRecipes.put(recipeCarpentry.id, recipeCarpentry);
        }
    }

    public void addAnvilRecipe(RecipeAnvil recipeAnvil) {
        recipeAnvil.id = getUniqueAnvilId();
        anvilRecipes.put(recipeAnvil.id, recipeAnvil);
    }

    @Override
    public List<IRecipe> getGlobalList() {
        return new ArrayList(this.globalRecipes.values());
    }

    @Override
    public List<IRecipe> getCarpentryList() {
        return new ArrayList(this.carpentryRecipes.values());
    }

    @Override
    public List<IAnvilRecipe> getAnvilList() {
        List<IAnvilRecipe> list = new ArrayList<>();
        list.addAll(anvilRecipes.values());
        return list;
    }

    @Override
    public void addRecipe(String name, boolean global, ItemStack result, Object... objects) {
        RecipeCarpentry recipe = new RecipeCarpentry(name);
        recipe.isGlobal = global;
        recipe = RecipeCarpentry.saveRecipe(recipe, result, objects);

        try {
            this.saveRecipe(recipe.writeNBT());
        } catch (Exception var7) {
            var7.printStackTrace();
        }
    }

    @Override
    public void addRecipe(String name, boolean global, ItemStack result, int width, int height, ItemStack... objects) {
        ArrayList<ItemStack> list = new ArrayList<>();
        int var9 = objects.length;

        for (int var10 = 0; var10 < var9; ++var10) {
            ItemStack item = objects[var10];
            if (item.stackSize > 0) {
                list.add(item);
            }
        }

        RecipeCarpentry recipe = new RecipeCarpentry(width, height, list.toArray(new ItemStack[0]), result);
        recipe.isGlobal = global;
        recipe.name = name;

        try {
            this.saveRecipe(recipe.writeNBT());
        } catch (IOException var12) {
            var12.printStackTrace();
        }
    }

    @Override
    public void addAnvilRecipe(String name, boolean global, ItemStack itemToRepair, ItemStack repairMaterial, int xpCost, float repairPercentage) {
        RecipeAnvil recipe = new RecipeAnvil(name, itemToRepair, repairMaterial, xpCost, repairPercentage);
        try {
            this.saveAnvilRecipe(recipe.writeNBT());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
