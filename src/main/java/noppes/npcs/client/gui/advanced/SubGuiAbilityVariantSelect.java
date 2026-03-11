package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * SubGui for selecting a variant/template when creating a new ability.
 * Uses a scroll list with group headers for external addon variants.
 */
public class SubGuiAbilityVariantSelect extends SubGuiInterface implements ICustomScrollListener {

    private final List<AbilityVariant> variants;
    private int selectedIndex = -1;

    // Maps each scroll list index to a variant list index (-1 for non-selectable entries)
    private final List<Integer> scrollToVariantIndex = new ArrayList<>();

    public SubGuiAbilityVariantSelect(List<AbilityVariant> variants) {
        this.variants = variants;
        setBackground("menubg.png");
        xSize = 200;
        ySize = 216;
    }

    @Override
    public void initGui() {
        super.initGui();

        addLabel(new GuiNpcLabel(0, "ability.selectVariant", guiLeft + 10, guiTop + 8));

        // Build the unsorted scroll list with group headers
        List<String> scrollList = new ArrayList<>();
        scrollToVariantIndex.clear();

        boolean hasBaseVariants = false;
        boolean hasGroupedVariants = false;

        for (AbilityVariant v : variants) {
            if (v.getGroup() == null)
                hasBaseVariants = true;
            else
                hasGroupedVariants = true;
        }

        // Add base (non-grouped) variants first
        for (int i = 0; i < variants.size(); i++) {
            if (variants.get(i).getGroup() == null) {
                scrollList.add(variants.get(i).getDisplayKey());
                scrollToVariantIndex.add(i);
            }
        }

        // Blank separator between base and grouped variants
        if (hasBaseVariants && hasGroupedVariants) {
            scrollList.add("");
            scrollToVariantIndex.add(-1);
        }

        // Add grouped variants with group headers
        String lastGroup = null;
        for (int i = 0; i < variants.size(); i++) {
            AbilityVariant v = variants.get(i);
            if (v.getGroup() == null)
                continue;

            if (!v.getGroup().equals(lastGroup)) {
                scrollList.add(v.getGroup());
                scrollToVariantIndex.add(-1);
                lastGroup = v.getGroup();
            }

            scrollList.add(v.getDisplayKey());
            scrollToVariantIndex.add(i);
        }

        GuiCustomScroll scroll = new GuiCustomScroll(this, 0);
        scroll.guiLeft = guiLeft + 5;
        scroll.guiTop = guiTop + 22;
        scroll.setUnsortedList(scrollList);
        scroll.setSize(190, 155);

        // Mark blank separators and group headers as non-interactive colored entries
        scroll.nonInteractive.add("");
        scroll.colors.put("", 0x000000);
        if (lastGroup != null) {
            for (int i = 0; i < variants.size(); i++) {
                String group = variants.get(i).getGroup();
                if (group != null) {
                    scroll.nonInteractive.add(group);
                    scroll.colors.put(group, 0xFFFF00); // Yellow
                }
            }
        }

        // Restore selection in scroll
        if (selectedIndex >= 0) {
            for (int s = 0; s < scrollToVariantIndex.size(); s++) {
                if (scrollToVariantIndex.get(s) == selectedIndex) {
                    scroll.selected = s;
                    break;
                }
            }
        }

        addScroll(scroll);

        // Select / Cancel buttons
        addButton(new GuiNpcButton(0, guiLeft + 5, guiTop + 188, 90, 20, "gui.select"));
        getButton(0).setEnabled(selectedIndex >= 0);
        addButton(new GuiNpcButton(1, guiLeft + 105, guiTop + 188, 90, 20, "gui.cancel"));
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (scroll.id == 0 && scroll.selected >= 0 && scroll.selected < scrollToVariantIndex.size()) {
            int variantIdx = scrollToVariantIndex.get(scroll.selected);
            if (variantIdx >= 0) {
                selectedIndex = variantIdx;
                getButton(0).setEnabled(true);
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {
        if (selectedIndex >= 0) {
            close();
        }
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 0 && selectedIndex >= 0) {
            close();
        } else if (id == 1) {
            selectedIndex = -1;
            close();
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public List<AbilityVariant> getVariants() {
        return variants;
    }
}
