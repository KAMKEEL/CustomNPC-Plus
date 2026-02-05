package kamkeel.npcs.client.gui.modern;

import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;

/**
 * Modern styled quest selector SubGui.
 * Features two-column layout (categories | quests) with draggable divider, search fields, and preview.
 */
public class ModernQuestSelector extends ModernCategorySelector<QuestCategory, Quest> {

    // Keep public field for backward compatibility
    public Quest selectedQuest;

    public ModernQuestSelector(int questId) {
        super();
        setHeaderTitle("Select Quest");

        // Load quest data
        loadData();

        // Find initial selection
        if (questId > 0) {
            selectedQuest = QuestController.Instance.quests.get(questId);
            selectedItem = selectedQuest;
            if (selectedQuest != null) {
                selectedCategory = selectedQuest.category;
                loadItemsForSelectedCategory();
            }
        }
    }

    @Override
    protected void loadAllCategories() {
        for (QuestCategory cat : QuestController.Instance.categories.values()) {
            categoryData.put(cat.title, cat);
        }
    }

    @Override
    protected void loadItemsForCategory(QuestCategory category) {
        if (category != null) {
            for (Quest q : category.quests.values()) {
                itemData.put(q.title, q);
            }
        }
    }

    @Override
    protected String getCategoryTitle(QuestCategory category) {
        return category.title;
    }

    @Override
    protected String getItemTitle(Quest item) {
        return item.title;
    }

    @Override
    protected int getItemId(Quest item) {
        return item.id;
    }

    @Override
    protected String getHeaderTitle() {
        return "Select Quest";
    }

    @Override
    protected String getCategoryColumnTitle() {
        return "Categories";
    }

    @Override
    protected String getItemColumnTitle() {
        return "Quests";
    }

    @Override
    protected void confirm() {
        // Sync with backward-compatible public field
        selectedQuest = selectedItem;
        super.confirm();
    }

    /**
     * Get the selected quest, or null if none selected.
     */
    public Quest getSelectedQuest() {
        return selectedItem;
    }
}
