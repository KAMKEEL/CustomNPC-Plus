package noppes.npcs.client.gui.select;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuiDialogSelection extends SubGuiInterface implements ICustomScrollListener {
    private HashMap<String, DialogCategory> categoryData = new HashMap<String, DialogCategory>();
    private HashMap<String, Dialog> dialogData = new HashMap<String, Dialog>();

    private GuiCustomScroll scrollCategories;
    private GuiCustomScroll scrollDialogs;

    private DialogCategory selectedCategory;
    public Dialog selectedDialog;

    private GuiSelectionListener listener;
    private String catSearch = "";
    private String dialogSearch = "";

    public GuiDialogSelection(int dialog) {
        drawDefaultBackground = false;
        title = "";
        setBackground("menubg.png");
        xSize = 366;
        ySize = 226;
        this.selectedDialog = DialogController.Instance.dialogs.get(dialog);
        if (selectedDialog != null) {
            selectedCategory = selectedDialog.category;
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        if (parent instanceof GuiSelectionListener) {
            listener = (GuiSelectionListener) parent;
        }
        this.addLabel(new GuiNpcLabel(0, "gui.categories", guiLeft + 8, guiTop + 4));
        this.addLabel(new GuiNpcLabel(1, "dialog.dialogs", guiLeft + 184, guiTop + 4));

        this.addButton(new GuiNpcButton(2, guiLeft + xSize - 56, guiTop + ySize - 35, 50, 20, "gui.done"));
        this.addButton(new GuiNpcButton(3, guiLeft + xSize - 108, guiTop + ySize - 35, 50, 20, "gui.cancel"));

        HashMap<String, DialogCategory> categoryData = new HashMap<String, DialogCategory>();
        HashMap<String, Dialog> dialogData = new HashMap<String, Dialog>();

        for (DialogCategory category : DialogController.Instance.categories.values()) {
            categoryData.put(category.title, category);
        }
        this.categoryData = categoryData;

        if (selectedCategory != null) {
            for (Dialog dialog : selectedCategory.dialogs.values()) {
                dialogData.put(dialog.title, dialog);
            }
        }
        this.dialogData = dialogData;

        if (scrollCategories == null) {
            scrollCategories = new GuiCustomScroll(this, 0, 0);
            scrollCategories.setSize(177, 153);
        }
        scrollCategories.setList(getCatSearch());
        if (selectedCategory != null) {
            scrollCategories.setSelected(selectedCategory.title);
        }
        scrollCategories.guiLeft = guiLeft + 4;
        scrollCategories.guiTop = guiTop + 14;
        this.addScroll(scrollCategories);
        addTextField(new GuiNpcTextField(33, this, fontRendererObj, guiLeft + 4, guiTop + 169, 177, 20, catSearch));

        if (scrollDialogs == null) {
            scrollDialogs = new GuiCustomScroll(this, 1, 0);
            scrollDialogs.setSize(177, 153);
        }
        scrollDialogs.setList(getDialogSearch());
        if (selectedDialog != null) {
            scrollDialogs.setSelected(selectedDialog.title);
        }
        scrollDialogs.guiLeft = guiLeft + 182;
        scrollDialogs.guiTop = guiTop + 14;
        this.addScroll(scrollDialogs);
        addTextField(new GuiNpcTextField(44, this, fontRendererObj, guiLeft + 182, guiTop + 169, 177, 20, dialogSearch));
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(33) != null) {
            if (getTextField(33).isFocused()) {
                if (catSearch.equals(getTextField(33).getText()))
                    return;
                catSearch = getTextField(33).getText().toLowerCase();
                scrollCategories.resetScroll();
                scrollCategories.setList(getCatSearch());
            }
        }
        if (getTextField(44) != null) {
            if (getTextField(44).isFocused()) {
                if (dialogSearch.equals(getTextField(44).getText()))
                    return;
                dialogSearch = getTextField(44).getText().toLowerCase();
                scrollDialogs.resetScroll();
                scrollDialogs.setList(getDialogSearch());
            }
        }
    }

    private List<String> getCatSearch() {
        if (catSearch.isEmpty()) {
            return Lists.newArrayList(categoryData.keySet());
        }
        List<String> list = new ArrayList<String>();
        for (String name : Lists.newArrayList(categoryData.keySet())) {
            if (name.toLowerCase().contains(catSearch))
                list.add(name);
        }
        return list;
    }

    private List<String> getDialogSearch() {
        if (selectedCategory == null) {
            return Lists.newArrayList(dialogData.keySet());
        }

        if (dialogSearch.isEmpty()) {
            return new ArrayList<String>(Lists.newArrayList(dialogData.keySet()));
        }
        List<String> list = new ArrayList<String>();
        for (String name : Lists.newArrayList(dialogData.keySet())) {
            if (name.toLowerCase().contains(dialogSearch))
                list.add(name);
        }
        return list;
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            selectedCategory = categoryData.get(scrollCategories.getSelected());
            selectedDialog = null;
            scrollDialogs.selected = -1;
            scrollDialogs.resetScroll();
            getTextField(44).setText("");
            dialogSearch = "";
        }
        if (guiCustomScroll.id == 1) {
            selectedDialog = dialogData.get(scrollDialogs.getSelected());
        }
        initGui();
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (selectedDialog == null)
            return;
        if (listener != null) {
            listener.selected(selectedDialog.id, selectedDialog.title);
        }
        close();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 2) {
            if (selectedDialog != null) {
                customScrollDoubleClicked(null, null);
            } else {
                close();
            }
        }
        if (id == 3) {
            close();
        }
    }
}
