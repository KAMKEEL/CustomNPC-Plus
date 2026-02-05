package kamkeel.npcs.client.gui.modern;

import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;

/**
 * Modern styled dialog selector SubGui.
 * Features two-column layout (categories | dialogs) with draggable divider, search fields, and preview.
 */
public class ModernDialogSelector extends ModernCategorySelector<DialogCategory, Dialog> {

    // Keep public field for backward compatibility
    public Dialog selectedDialog;

    public ModernDialogSelector(int dialogId) {
        super();
        setHeaderTitle("Select Dialog");

        // Load dialog data
        loadData();

        // Find initial selection
        if (dialogId > 0) {
            selectedDialog = DialogController.Instance.dialogs.get(dialogId);
            selectedItem = selectedDialog;
            if (selectedDialog != null) {
                selectedCategory = selectedDialog.category;
                loadItemsForSelectedCategory();
            }
        }
    }

    @Override
    protected void loadAllCategories() {
        for (DialogCategory cat : DialogController.Instance.categories.values()) {
            categoryData.put(cat.title, cat);
        }
    }

    @Override
    protected void loadItemsForCategory(DialogCategory category) {
        if (category != null) {
            for (Dialog d : category.dialogs.values()) {
                itemData.put(d.title, d);
            }
        }
    }

    @Override
    protected String getCategoryTitle(DialogCategory category) {
        return category.title;
    }

    @Override
    protected String getItemTitle(Dialog item) {
        return item.title;
    }

    @Override
    protected int getItemId(Dialog item) {
        return item.id;
    }

    @Override
    protected String getHeaderTitle() {
        return "Select Dialog";
    }

    @Override
    protected String getCategoryColumnTitle() {
        return "Categories";
    }

    @Override
    protected String getItemColumnTitle() {
        return "Dialogs";
    }

    @Override
    protected void confirm() {
        // Sync with backward-compatible public field
        selectedDialog = selectedItem;
        super.confirm();
    }

    /**
     * Get the selected dialog, or null if none selected.
     */
    public Dialog getSelectedDialog() {
        return selectedItem;
    }
}
