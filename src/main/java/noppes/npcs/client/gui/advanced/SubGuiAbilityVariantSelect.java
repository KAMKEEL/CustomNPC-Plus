package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.AbilityVariant;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiScrollWindow;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * SubGui for selecting a variant/template when creating a new ability.
 * Uses a scroll window so external mods can register many variants.
 */
public class SubGuiAbilityVariantSelect extends SubGuiInterface {

    private static final int VARIANT_BTN_START = 10;
    private static final int BTN_H = 20;
    private static final int BTN_PAD = 2;

    private final List<AbilityVariant> variants;
    private final List<String> displayNames = new ArrayList<>();
    private int selectedIndex = -1;

    public SubGuiAbilityVariantSelect(List<AbilityVariant> variants) {
        this.variants = variants;
        for (AbilityVariant variant : variants) {
            displayNames.add(StatCollector.translateToLocal(variant.getDisplayKey()));
        }
        setBackground("menubg.png");
        xSize = 200;
        ySize = 216;
    }

    @Override
    public void initGui() {
        super.initGui();

        addLabel(new GuiNpcLabel(0, "ability.selectVariant", guiLeft + 10, guiTop + 8));

        // Scroll window for variant buttons
        int swX = guiLeft + 5;
        int swY = guiTop + 22;
        int swW = 190;
        int swH = 155;

        GuiScrollWindow sw = new GuiScrollWindow(this, swX, swY, swW, swH, 0);
        addScrollableGui(0, sw); // Must register BEFORE adding components (initGui clears them)

        int localY = 2;
        int btnW = swW - 14; // Leave room for scrollbar
        for (int i = 0; i < displayNames.size(); i++) {
            GuiNpcButton btn = new GuiNpcButton(VARIANT_BTN_START + i, 2, localY, btnW, BTN_H, displayNames.get(i));
            if (i == selectedIndex) {
                btn.packedFGColour = 0x55FF55;
            }
            sw.addButton(btn);
            localY += BTN_H + BTN_PAD;
        }
        sw.maxScrollY = Math.max(localY - swH, 0);

        // Select / Cancel buttons
        addButton(new GuiNpcButton(0, guiLeft + 5, guiTop + 188, 90, 20, "gui.select"));
        getButton(0).setEnabled(selectedIndex >= 0);
        addButton(new GuiNpcButton(1, guiLeft + 105, guiTop + 188, 90, 20, "gui.cancel"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 0 && selectedIndex >= 0) {
            close();
        } else if (id == 1) {
            selectedIndex = -1;
            close();
        } else if (id >= VARIANT_BTN_START && id < VARIANT_BTN_START + displayNames.size()) {
            selectedIndex = id - VARIANT_BTN_START;
            initGui();
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public List<AbilityVariant> getVariants() {
        return variants;
    }
}
