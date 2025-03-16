package noppes.npcs.client.gui;

import kamkeel.npcs.network.packets.request.quest.QuestGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Quest;

public class SubGuiNpcAvailabilityQuest extends SubGuiInterface implements GuiSelectionListener, IGuiData {
    private Availability availabitily;
    private int slot = 0;

    public SubGuiNpcAvailabilityQuest(Availability availabitily) {
        this.availabitily = availabitily;
        setBackground("menubg.png");
        xSize = 316;
        ySize = 216;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();
        addLabel(new GuiNpcLabel(1, "availability.available", guiLeft, guiTop + 4));
        getLabel(1).center(xSize);

        int y = guiTop + 12;
        this.addButton(new GuiNpcButton(0, guiLeft + 4, y, 90, 20, new String[]{"availability.always", "availability.after", "availability.before", "availability.whenactive", "availability.whennotactive", "availability.acceptable", "availability.notacceptable"}, availabitily.questAvailable.ordinal()));
        this.addButton(new GuiNpcButton(10, guiLeft + 96, y, 192, 20, "availability.selectquest"));
        getButton(10).setEnabled(availabitily.questAvailable != EnumAvailabilityQuest.Always);
        this.addButton(new GuiNpcButton(20, guiLeft + 290, y, 20, 20, "X"));

        y += 23;
        this.addButton(new GuiNpcButton(1, guiLeft + 4, y, 90, 20, new String[]{"availability.always", "availability.after", "availability.before", "availability.whenactive", "availability.whennotactive", "availability.acceptable", "availability.notacceptable"}, availabitily.quest2Available.ordinal()));
        this.addButton(new GuiNpcButton(11, guiLeft + 96, y, 192, 20, "availability.selectquest"));
        getButton(11).setEnabled(availabitily.quest2Available != EnumAvailabilityQuest.Always);
        this.addButton(new GuiNpcButton(21, guiLeft + 290, y, 20, 20, "X"));

        y += 23;
        this.addButton(new GuiNpcButton(2, guiLeft + 4, y, 90, 20, new String[]{"availability.always", "availability.after", "availability.before", "availability.whenactive", "availability.whennotactive", "availability.acceptable", "availability.notacceptable"}, availabitily.quest3Available.ordinal()));
        this.addButton(new GuiNpcButton(12, guiLeft + 96, y, 192, 20, "availability.selectquest"));
        getButton(12).setEnabled(availabitily.quest3Available != EnumAvailabilityQuest.Always);
        this.addButton(new GuiNpcButton(22, guiLeft + 290, y, 20, 20, "X"));

        y += 23;
        this.addButton(new GuiNpcButton(3, guiLeft + 4, y, 90, 20, new String[]{"availability.always", "availability.after", "availability.before", "availability.whenactive", "availability.whennotactive", "availability.acceptable", "availability.notacceptable"}, availabitily.quest4Available.ordinal()));
        this.addButton(new GuiNpcButton(13, guiLeft + 96, y, 192, 20, "availability.selectquest"));
        getButton(13).setEnabled(availabitily.quest4Available != EnumAvailabilityQuest.Always);
        this.addButton(new GuiNpcButton(23, guiLeft + 290, y, 20, 20, "X"));


        this.addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 192, 98, 20, "gui.done"));

        updateGuiButtons();
    }


    private void updateGuiButtons() {
        getButton(10).setDisplayText("availability.selectquest");
        getButton(11).setDisplayText("availability.selectquest");
        getButton(12).setDisplayText("availability.selectquest");
        getButton(13).setDisplayText("availability.selectquest");

        if (availabitily.questId >= 0) {
            QuestGetPacket.getQuest(availabitily.questId);
        }
        if (availabitily.quest2Id >= 0) {
            QuestGetPacket.getQuest(availabitily.quest2Id);
        }
        if (availabitily.quest3Id >= 0) {
            QuestGetPacket.getQuest(availabitily.quest3Id);
        }
        if (availabitily.quest4Id >= 0) {
            QuestGetPacket.getQuest(availabitily.quest4Id);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;

        if (button.id == 0) {
            availabitily.questAvailable = EnumAvailabilityQuest.values()[button.getValue()];
            if (availabitily.questAvailable == EnumAvailabilityQuest.Always)
                availabitily.questId = -1;
            initGui();
        }
        if (button.id == 1) {
            availabitily.quest2Available = EnumAvailabilityQuest.values()[button.getValue()];
            if (availabitily.quest2Available == EnumAvailabilityQuest.Always)
                availabitily.quest2Id = -1;
            initGui();
        }
        if (button.id == 2) {
            availabitily.quest3Available = EnumAvailabilityQuest.values()[button.getValue()];
            if (availabitily.quest3Available == EnumAvailabilityQuest.Always)
                availabitily.quest3Id = -1;
            initGui();
        }
        if (button.id == 3) {
            availabitily.quest4Available = EnumAvailabilityQuest.values()[button.getValue()];
            if (availabitily.quest4Available == EnumAvailabilityQuest.Always)
                availabitily.quest4Id = -1;
            initGui();
        }
        if (button.id == 10) {
            slot = 1;
            setSubGui(new GuiQuestSelection(availabitily.questId));
        }
        if (button.id == 11) {
            slot = 2;
            setSubGui(new GuiQuestSelection(availabitily.quest2Id));
        }
        if (button.id == 12) {
            slot = 3;
            setSubGui(new GuiQuestSelection(availabitily.quest3Id));
        }
        if (button.id == 13) {
            slot = 4;
            setSubGui(new GuiQuestSelection(availabitily.quest4Id));
        }

        if (button.id == 20) {
            availabitily.questId = -1;
            getButton(10).setDisplayText("availability.selectquest");
        }
        if (button.id == 21) {
            availabitily.quest2Id = -1;
            getButton(11).setDisplayText("availability.selectquest");
        }
        if (button.id == 22) {
            availabitily.quest3Id = -1;
            getButton(12).setDisplayText("availability.selectquest");
        }
        if (button.id == 23) {
            availabitily.quest4Id = -1;
            getButton(13).setDisplayText("availability.selectquest");
        }

        if (button.id == 66) {
            close();
        }
    }

    @Override
    public void selected(int id, String name) {
        if (slot == 1)
            availabitily.questId = id;
        if (slot == 2)
            availabitily.quest2Id = id;
        if (slot == 3)
            availabitily.quest3Id = id;
        if (slot == 4)
            availabitily.quest4Id = id;
        updateGuiButtons();
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        Quest quest = new Quest();
        quest.readNBT(compound);
        if (availabitily.questId == quest.id)
            getButton(10).setDisplayText(quest.title);
        if (availabitily.quest2Id == quest.id)
            getButton(11).setDisplayText(quest.title);
        if (availabitily.quest3Id == quest.id)
            getButton(12).setDisplayText(quest.title);
        if (availabitily.quest4Id == quest.id)
            getButton(13).setDisplayText(quest.title);

    }

}
