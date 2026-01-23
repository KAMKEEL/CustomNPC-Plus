package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Dialog for loading a saved ability preset.
 * Can be opened from SubGuiAbilityConfig (to load into editing ability)
 * or from GuiNPCAbilities (to add ability directly to NPC).
 */
public class SubGuiAbilityLoad extends SubGuiInterface implements ICustomScrollListener {

    private final SubGuiAbilityConfig parentConfig;
    private final GuiNPCAbilities parentAbilities;
    private String selectedName = null;
    private List<String> abilityNames = new ArrayList<>();

    public SubGuiAbilityLoad(SubGuiAbilityConfig parentConfig) {
        this.parentConfig = parentConfig;
        this.parentAbilities = null;

        setBackground("menubg.png");
        xSize = 220;
        ySize = 180;
    }

    public SubGuiAbilityLoad(GuiNPCAbilities parentAbilities) {
        this.parentConfig = null;
        this.parentAbilities = parentAbilities;

        setBackground("menubg.png");
        xSize = 220;
        ySize = 180;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Load ability names
        Set<String> names = AbilityController.Instance.getSavedAbilityNames();
        abilityNames = new ArrayList<>(names);

        int y = guiTop + 5;

        addLabel(new GuiNpcLabel(0, "ability.load.select", guiLeft + 10, y));
        y += 14;

        // Scroll list of saved abilities
        GuiCustomScroll scroll = new GuiCustomScroll(this, 0);
        scroll.setSize(200, 110);
        scroll.guiLeft = guiLeft + 10;
        scroll.guiTop = y;
        scroll.setUnsortedList(abilityNames);
        if (selectedName != null && abilityNames.contains(selectedName)) {
            scroll.setSelected(selectedName);
        }
        addScroll(scroll);

        y += 115;

        // Buttons
        addButton(new GuiNpcButton(0, guiLeft + 10, y, 60, 20, "gui.load"));
        addButton(new GuiNpcButton(1, guiLeft + 80, y, 60, 20, "gui.delete"));
        addButton(new GuiNpcButton(2, guiLeft + 150, y, 60, 20, "gui.cancel"));

        // Disable load/delete if nothing selected
        getButton(0).setEnabled(selectedName != null && !selectedName.isEmpty());
        getButton(1).setEnabled(selectedName != null && !selectedName.isEmpty());
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        if (id == 0 && selectedName != null) {
            // Load the selected ability
            Ability loaded = AbilityController.Instance.getSavedAbility(selectedName);
            if (loaded != null) {
                if (parentConfig != null) {
                    parentConfig.loadAbility(loaded);
                } else if (parentAbilities != null) {
                    parentAbilities.loadAbility(loaded);
                }
            }
            close();
        } else if (id == 1 && selectedName != null) {
            // Delete confirmation
            setSubGui(new SubGuiAbilityDeleteConfirm(selectedName, this));
        } else if (id == 2) {
            close();
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
            // Double-click to load immediately
            Ability loaded = AbilityController.Instance.getSavedAbility(selection);
            if (loaded != null) {
                if (parentConfig != null) {
                    parentConfig.loadAbility(loaded);
                } else if (parentAbilities != null) {
                    parentAbilities.loadAbility(loaded);
                }
            }
            close();
        }
    }

    /**
     * Called after an ability is deleted to refresh the list.
     */
    public void onAbilityDeleted(String name) {
        if (name.equals(selectedName)) {
            selectedName = null;
        }
        initGui();
    }
}
