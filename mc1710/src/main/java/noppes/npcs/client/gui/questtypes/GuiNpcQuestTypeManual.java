package noppes.npcs.client.gui.questtypes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestManual;

import java.util.TreeMap;

public class GuiNpcQuestTypeManual extends SubGuiInterface implements ITextfieldListener {
    private GuiScreen parent;

    private QuestManual quest;

    private GuiNpcTextField lastSelected;

    public GuiNpcQuestTypeManual(EntityNPCInterface npc, Quest q, GuiScreen parent) {
        this.npc = npc;
        this.parent = parent;
        title = "Quest Manual Setup";

        quest = (QuestManual) q.questInterface;

        setBackground("menubg.png");
        xSize = 356;
        ySize = 216;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();
        int i = 0;
        addLabel(new GuiNpcLabel(0, "You can fill in npc or player names too", guiLeft + 4, guiTop + 50));
        for (String name : quest.manuals.keySet()) {
            this.addTextField(new GuiNpcTextField(i, this, fontRendererObj, guiLeft + 4, guiTop + 70 + i * 22, 180, 20, name));
            this.addTextField(new GuiNpcTextField(i + 3, this, fontRendererObj, guiLeft + 186, guiTop + 70 + i * 22, 24, 20, quest.manuals.get(name) + ""));
            this.getTextField(i + 3).integersOnly = true;
            this.getTextField(i + 3).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
            i++;
        }

        for (; i < 3; i++) {
            this.addTextField(new GuiNpcTextField(i, this, fontRendererObj, guiLeft + 4, guiTop + 70 + i * 22, 180, 20, ""));
            this.addTextField(new GuiNpcTextField(i + 3, this, fontRendererObj, guiLeft + 186, guiTop + 70 + i * 22, 24, 20, "1"));
            this.getTextField(i + 3).integersOnly = true;
            this.getTextField(i + 3).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
        }
        this.addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 140, 98, 20, "gui.back"));

    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            close();
        }
    }

    @Override
    public void save() {
    }

    @Override
    public void unFocused(GuiNpcTextField guiNpcTextField) {
        if (guiNpcTextField.id < 3)
            lastSelected = guiNpcTextField;

        saveTargets();
    }

    private void saveTargets() {
        TreeMap<String, Integer> map = new TreeMap<String, Integer>();
        for (int i = 0; i < 3; i++) {
            String name = getTextField(i).getText();
            if (name.isEmpty())
                continue;
            map.put(name, getTextField(i + 3).getInteger());
        }
        quest.manuals = map;
    }
}
