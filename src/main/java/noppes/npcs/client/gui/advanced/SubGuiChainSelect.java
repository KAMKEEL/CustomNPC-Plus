package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.AbilityController;
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
 * Selector for chained ability presets. Lists chain names from synced data.
 * Returns the selected chain name via {@link #getSelectedName()}.
 */
public class SubGuiChainSelect extends SubGuiInterface implements ICustomScrollListener {

    private GuiCustomScroll scroll;
    private String selectedName = null;

    public SubGuiChainSelect() {
        setBackground("menubg.png");
        xSize = 220;
        ySize = 200;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 5;
        addLabel(new GuiNpcLabel(0, "ability.loadChain", guiLeft + 10, y));
        y += 14;

        List<String> names = getChainNames();

        if (scroll == null) {
            scroll = new GuiCustomScroll(this, 0);
            scroll.setSize(200, 130);
        }
        scroll.guiLeft = guiLeft + 10;
        scroll.guiTop = y;
        scroll.setList(names);
        if (selectedName != null) {
            scroll.setSelected(selectedName);
        }
        addScroll(scroll);

        y += 133;
        addButton(new GuiNpcButton(0, guiLeft + 10, y, 95, 20, "gui.select"));
        addButton(new GuiNpcButton(1, guiLeft + 115, y, 95, 20, "gui.cancel"));
        getButton(0).setEnabled(selectedName != null);
    }

    private List<String> getChainNames() {
        Set<String> names = AbilityController.Instance.getChainedAbilityNamesSet();
        return new ArrayList<>(names);
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        if (guibutton.id == 0 && selectedName != null) {
            close();
        } else if (guibutton.id == 1) {
            selectedName = null;
            close();
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            selectedName = guiCustomScroll.getSelected();
            initGui();
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0 && selection != null && !selection.isEmpty()) {
            selectedName = selection;
            close();
        }
    }

    public String getSelectedName() {
        return selectedName;
    }
}
