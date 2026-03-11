package noppes.npcs.client.gui.script;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuTopButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.List;

public class SubGuiSelectList extends SubGuiInterface {
    private GuiCustomScroll scroll1;
    private GuiCustomScroll scroll2;

    public String allName, selectedName;
    public List<String> all;
    public List<String> selected;

    public SubGuiSelectList(List<String> all, List<String> selected, String allName, String selectedName) {
        setBackground("menubg.png");
        xSize = 346;
        ySize = 216;
        this.allName = allName;
        this.selectedName = selectedName;

        if (all == null)
            all = new ArrayList<>();
        this.all = all;

        if (selected == null)
            selected = new ArrayList<>();
        this.selected = selected;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (scroll1 == null) {
            scroll1 = new GuiCustomScroll(this, 0);
            scroll1.setSize(140, 180);
        }

        addTopButton(new GuiMenuTopButton(-1, guiLeft + xSize - 22, guiTop - 17, "X"));

        scroll1.guiLeft = guiLeft + 4;
        scroll1.guiTop = guiTop + 14;
        this.addScroll(scroll1);
        addLabel(new GuiNpcLabel(1, allName, guiLeft + 4, guiTop + 4));

        if (scroll2 == null) {
            scroll2 = new GuiCustomScroll(this, 1);
            scroll2.setSize(140, 180);
        }
        scroll2.guiLeft = guiLeft + 200;
        scroll2.guiTop = guiTop + 14;
        this.addScroll(scroll2);
        addLabel(new GuiNpcLabel(2, selectedName, guiLeft + 200, guiTop + 4));

        List<String> tempAll = new ArrayList<>(all);
        tempAll.removeAll(selected);
        scroll1.setList(tempAll);
        scroll2.setList(selected);

        addButton(new GuiNpcButton(1, guiLeft + 145, guiTop + 40, 55, 20, ">"));
        addButton(new GuiNpcButton(2, guiLeft + 145, guiTop + 62, 55, 20, "<"));

        addButton(new GuiNpcButton(3, guiLeft + 145, guiTop + 90, 55, 20, ">>"));
        addButton(new GuiNpcButton(4, guiLeft + 145, guiTop + 112, 55, 20, "<<"));

        addButton(new GuiNpcButton(66, guiLeft + 260, guiTop + 194, 60, 20, "gui.done"));
    }

    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;

        if (button.id == -1) {
            close();
            return;
        }

        if (button.id == 1) {
            if (scroll1.hasSelected()) {
                selected.add(scroll1.getSelected());

                scroll1.selected = -1;
                scroll2.selected = -1;
                initGui();
            }
        }
        if (button.id == 2) {
            if (scroll2.hasSelected()) {
                selected.remove(scroll2.getSelected());
                scroll2.selected = -1;
                initGui();
            }
        }
        if (button.id == 3) {
            selected.clear();
            for (String type : all)
                selected.add(type);

            scroll1.selected = -1;
            scroll2.selected = -1;
            initGui();
        }
        if (button.id == 4) {
            selected.clear();
            scroll1.selected = -1;
            scroll2.selected = -1;
            initGui();
        }
        if (button.id == 66) {
            close();
        }
    }

    @Override
    public void save() {

    }
}
