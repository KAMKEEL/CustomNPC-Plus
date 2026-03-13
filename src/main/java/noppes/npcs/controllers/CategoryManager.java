package noppes.npcs.controllers;

import noppes.npcs.controllers.data.Category;

import java.io.File;
import java.util.*;
import java.util.Arrays;

/**
 * Filesystem-based category manager for organizing items into named groups.
 * Categories are subdirectories within the controller's base directory.
 * Items without a category mapping are "Uncategorized" (root-level files).
 *
 * One instance per controller (Effect, Animation, LinkedItem, Form, Aura, Outline, etc.)
 */
public class CategoryManager {
    public static final int UNCATEGORIZED_ID = 0;
    public static final String UNCATEGORIZED_NAME = "Uncategorized";

    private File baseDir;
    private final HashMap<Integer, Category> categories = new HashMap<>();
    private final HashMap<Integer, Integer> itemCategoryMap = new HashMap<>();
    private int lastUsedCatID = 0;

    /**
     * Scan the base directory for subdirectories and create Category objects.
     * Call at the start of controller loading, before loading items.
     */
    public void loadCategories(File baseDir) {
        loadCategories(baseDir, new String[0]);
    }

    /**
     * Scan the base directory for subdirectories and create Category objects,
     * excluding any subdirectories whose names match excludeDirs.
     */
    public void loadCategories(File baseDir, String... excludeDirs) {
        this.baseDir = baseDir;
        categories.clear();
        itemCategoryMap.clear();
        lastUsedCatID = 0;

        if (!baseDir.exists()) return;

        Set<String> excludeSet = new HashSet<>(Arrays.asList(excludeDirs));
        File[] files = baseDir.listFiles();
        if (files == null) return;
        for (File sub : files) {
            if (!sub.isDirectory()) continue;
            if (excludeSet.contains(sub.getName())) continue;
            lastUsedCatID++;
            categories.put(lastUsedCatID, new Category(lastUsedCatID, sub.getName()));
        }
    }

    /**
     * Register an item as belonging to a category.
     * Call when loading items from a subdirectory.
     */
    public void registerItem(int itemId, int catId) {
        if (catId > UNCATEGORIZED_ID) {
            itemCategoryMap.put(itemId, catId);
        }
    }

    public void removeItem(int itemId) {
        itemCategoryMap.remove(itemId);
    }

    public boolean isLoaded() {
        return baseDir != null;
    }

    public File getItemDir(int itemId) {
        int catId = itemCategoryMap.getOrDefault(itemId, UNCATEGORIZED_ID);
        return getCategoryDir(catId);
    }

    public File getCategoryDir(int catId) {
        if (catId == UNCATEGORIZED_ID) return baseDir;
        Category cat = categories.get(catId);
        if (cat != null) return new File(baseDir, cat.title);
        return baseDir;
    }

    public HashMap<Integer, Category> getCategories() {
        return categories;
    }

    // ========== Category CRUD ==========

    public Category createCategory(String name) {
        if (baseDir == null) return null;
        while (hasCategoryName(name)) name += "_";
        lastUsedCatID++;
        Category cat = new Category(lastUsedCatID, name);
        categories.put(cat.id, cat);
        File dir = new File(baseDir, name);
        if (!dir.exists()) dir.mkdirs();
        return cat;
    }

    public void saveCategory(Category cat) {
        if (cat.id <= 0 || baseDir == null) return;
        Category existing = categories.get(cat.id);
        if (existing != null && !existing.title.equals(cat.title)) {
            String newTitle = cat.title;
            while (hasCategoryName(newTitle)) newTitle += "_";
            File oldDir = new File(baseDir, existing.title);
            File newDir = new File(baseDir, newTitle);
            if (oldDir.exists() && !newDir.exists()) {
                oldDir.renameTo(newDir);
            }
            cat.title = newTitle;
        }
        categories.put(cat.id, cat);
    }

    public boolean removeCategory(int catId) {
        return removeCategory(catId, null);
    }

    public boolean removeCategory(int catId, Set<Integer> allItemIds) {
        if (baseDir == null) return false;
        if (catId <= UNCATEGORIZED_ID) return false;
        Category cat = categories.get(catId);
        if (cat == null) return false;

        // Server-side guard: refuse to delete if category has items
        if (allItemIds != null) {
            if (!isCategoryEmpty(catId, allItemIds)) return false;
        } else {
            // Without item set, check if any items are mapped to this category
            if (itemCategoryMap.containsValue(catId)) return false;
        }

        // Also check filesystem — directory must be empty or non-existent
        File dir = new File(baseDir, cat.title);
        if (dir.exists()) {
            String[] contents = dir.list();
            if (contents != null && contents.length > 0) {
                return false;
            }
            dir.delete();
        }

        categories.remove(catId);
        Iterator<Map.Entry<Integer, Integer>> it = itemCategoryMap.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() == catId) {
                it.remove();
            }
        }
        return true;
    }

    public void moveItem(int itemId, String fileName, int destCatId) {
        if (baseDir == null) return;
        int oldCatId = itemCategoryMap.getOrDefault(itemId, UNCATEGORIZED_ID);
        if (oldCatId == destCatId) return;

        File oldDir = getCategoryDir(oldCatId);
        File newDir = getCategoryDir(destCatId);
        if (!newDir.exists()) newDir.mkdirs();

        File oldFile = new File(oldDir, fileName);
        File newFile = new File(newDir, fileName);
        if (oldFile.exists()) {
            oldFile.renameTo(newFile);
        }

        if (destCatId == UNCATEGORIZED_ID) {
            itemCategoryMap.remove(itemId);
        } else {
            itemCategoryMap.put(itemId, destCatId);
        }
    }

    // ========== Query ==========

    public boolean hasCategoryName(String name) {
        for (Category cat : categories.values()) {
            if (cat.title.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public boolean isCategoryEmpty(int catId, Set<Integer> allItemIds) {
        if (catId == UNCATEGORIZED_ID) {
            for (int id : allItemIds) {
                if (!itemCategoryMap.containsKey(id)) return false;
            }
            return true;
        }
        for (Map.Entry<Integer, Integer> entry : itemCategoryMap.entrySet()) {
            if (entry.getValue() == catId && allItemIds.contains(entry.getKey())) return false;
        }
        return true;
    }

    public Category getCategory(int catId) {
        return categories.get(catId);
    }

    public int getItemCategory(int itemId) {
        return itemCategoryMap.getOrDefault(itemId, UNCATEGORIZED_ID);
    }

    public List<Integer> getItemsInCategory(int catId, Set<Integer> allItemIds) {
        List<Integer> result = new ArrayList<>();
        for (int itemId : allItemIds) {
            int assignedCat = itemCategoryMap.getOrDefault(itemId, UNCATEGORIZED_ID);
            if (assignedCat == catId) {
                result.add(itemId);
            }
        }
        return result;
    }

    // ========== Scroll Data ==========

    public Map<String, Integer> getCategoryScrollData() {
        Map<String, Integer> map = new HashMap<>();
        map.put(UNCATEGORIZED_NAME, UNCATEGORIZED_ID);
        for (Category cat : categories.values()) {
            map.put(cat.title, cat.id);
        }
        return map;
    }

    /**
     * Search all directories (root + subdirectories) for a file and delete it.
     */
    public void deleteFile(String fileName) {
        if (baseDir == null || !baseDir.exists()) return;

        File file = new File(baseDir, fileName);
        if (file.exists()) {
            file.delete();
            return;
        }

        File[] subs = baseDir.listFiles();
        if (subs != null) {
            for (File sub : subs) {
                if (!sub.isDirectory()) continue;
                File subFile = new File(sub, fileName);
                if (subFile.exists()) {
                    subFile.delete();
                    return;
                }
            }
        }
    }
}
