package noppes.npcs.client.gui.global;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.SubGuiAbilityDeleteConfirm;
import noppes.npcs.client.gui.advanced.SubGuiAbilityLoad;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.*;

/**
 * Global GUI for managing saved ability presets.
 * Allows viewing, editing, and deleting saved abilities.
 */
public class GuiNpcManageAbilities extends GuiNPCInterface2 implements ICustomScrollListener, ISubGuiListener, IAbilityConfigCallback {

    private GuiCustomScroll scroll;
    private String selectedName = null;
    private List<String> abilityNames = new ArrayList<>();
    private String search = "";

    public GuiNpcManageAbilities(EntityNPCInterface npc) {
        super(npc);
        loadAbilityNames();
    }

    private void loadAbilityNames() {
        Set<String> names = AbilityController.Instance.getSavedAbilityNames();
        abilityNames = new ArrayList<>(names);
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;

        // Title label
        addLabel(new GuiNpcLabel(0, "ability.manager.title", guiLeft + 10, y));
        y += 20;

        // Scroll list of saved abilities
        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
        }
        scroll.setSize(xSize - 90, 160);
        scroll.guiLeft = guiLeft + 10;
        scroll.guiTop = y;
        scroll.setUnsortedList(getFilteredList());
        if (selectedName != null && abilityNames.contains(selectedName)) {
            scroll.setSelected(selectedName);
        }
        addScroll(scroll);

        // Search bar
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 10, y + 163, xSize - 90, 18, search));

        // Right side buttons
        int btnX = guiLeft + xSize - 70;
        addButton(new GuiNpcButton(1, btnX, y, 60, 20, "gui.edit"));
        addButton(new GuiNpcButton(2, btnX, y + 25, 60, 20, "gui.delete"));
        addButton(new GuiNpcButton(3, btnX, y + 60, 60, 20, "selectServer.refresh"));

        // Enable/disable buttons based on selection
        getButton(1).setEnabled(selectedName != null && !selectedName.isEmpty());
        getButton(2).setEnabled(selectedName != null && !selectedName.isEmpty());
    }

    private List<String> getFilteredList() {
        if (search.isEmpty()) {
            return new ArrayList<>(abilityNames);
        }
        List<String> filtered = new ArrayList<>();
        for (String name : abilityNames) {
            if (name.toLowerCase().contains(search.toLowerCase())) {
                filtered.add(name);
            }
        }
        return filtered;
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == 1 && selectedName != null) {
            // Edit
            Ability ability = AbilityController.Instance.getSavedAbility(selectedName);
            if (ability != null) {
                setSubGui(new SubGuiAbilityConfig(ability, this));
            }
        } else if (id == 2 && selectedName != null) {
            // Delete
            setSubGui(new SubGuiAbilityDeleteConfirm(selectedName, null));
        } else if (id == 3) {
            // Refresh
            AbilityController.Instance.load();
            loadAbilityNames();
            selectedName = null;
            initGui();
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0) {
            selectedName = scroll.getSelected();
            initGui();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (scroll.id == 0 && selection != null && !selection.isEmpty()) {
            // Double-click to edit
            Ability ability = AbilityController.Instance.getSavedAbility(selection);
            if (ability != null) {
                setSubGui(new SubGuiAbilityConfig(ability, this));
            }
        }
    }

    @Override
    public void keyTyped(char c, int i) {
        super.keyTyped(c, i);
        if (getTextField(1) != null && getTextField(1).isFocused()) {
            if (!search.equals(getTextField(1).getText())) {
                search = getTextField(1).getText();
                scroll.setUnsortedList(getFilteredList());
                scroll.resetScroll();
            }
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        // Refresh list after any sub gui closes
        loadAbilityNames();
        if (subgui instanceof SubGuiAbilityDeleteConfirm) {
            // After delete, clear selection if it was deleted
            if (selectedName != null && !abilityNames.contains(selectedName)) {
                selectedName = null;
            }
        }
        initGui();
    }

    @Override
    public void onAbilitySaved(Ability ability) {
        // Save the edited ability
        AbilityController.Instance.saveAbility(ability);
        loadAbilityNames();
        initGui();
    }

    @Override
    public void save() {
        // Nothing to save - abilities are saved individually
    }
}
