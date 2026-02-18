package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityAction;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Picker for selecting an NPC ability slot to consume into a chain entry.
 * Shows standalone (non-chain) ability slots from the NPC's current list.
 */
public class SubGuiNpcSlotPicker extends SubGuiInterface implements ICustomScrollListener {

    private final List<AbilityAction> npcSlots;
    private final List<Integer> validIndices = new ArrayList<>();
    private GuiCustomScroll scroll;
    private int selectedIndex = -1;

    public SubGuiNpcSlotPicker(List<AbilityAction> npcSlots) {
        this.npcSlots = npcSlots;
        setBackground("menubg.png");
        xSize = 220;
        ySize = 200;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 5;
        addLabel(new GuiNpcLabel(0, "ability.selectSlot", guiLeft + 10, y));
        y += 14;

        List<String> displayList = new ArrayList<>();
        validIndices.clear();

        for (int i = 0; i < npcSlots.size(); i++) {
            AbilityAction slot = npcSlots.get(i);
            // Only show standalone abilities (not chains)
            if (slot.isChain()) continue;

            Ability ability = slot.getAbility();
            if (ability == null) continue;

            String typeName = I18n.format(ability.getTypeId());
            String customName = ability.getName();
            String display;
            if (customName != null && !customName.isEmpty() && !customName.equals(typeName)) {
                display = (i + 1) + ". " + customName + " (" + typeName + ")";
            } else {
                display = (i + 1) + ". " + typeName;
            }

            if (slot.isReference()) {
                display = "\u00A7e" + display + " (Ref)";
            }

            displayList.add(display);
            validIndices.add(i);
        }

        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(200, 130);
        }
        scroll.guiLeft = guiLeft + 10;
        scroll.guiTop = y;
        scroll.setList(displayList);
        addScroll(scroll);

        y += 133;
        addButton(new GuiNpcButton(0, guiLeft + 10, y, 95, 20, "gui.select"));
        addButton(new GuiNpcButton(1, guiLeft + 115, y, 95, 20, "gui.cancel"));
        getButton(0).setEnabled(selectedIndex >= 0);
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0 && selectedIndex >= 0) {
            close();
        } else if (guibutton.id == 1) {
            selectedIndex = -1;
            close();
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0 && guiCustomScroll.selected >= 0 && guiCustomScroll.selected < validIndices.size()) {
            selectedIndex = validIndices.get(guiCustomScroll.selected);
            initGui();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0 && selectedIndex >= 0) {
            close();
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}
