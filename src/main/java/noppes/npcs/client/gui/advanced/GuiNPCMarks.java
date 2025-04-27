package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.mainmenu.MainmenuAdvancedMarkDataPacket;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.MarkType;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCMarks extends GuiNPCInterface2 implements ISubGuiListener {
    private final String[] marks = new String[]{"gui.none", "mark.question", "mark.exclamation", "mark.pointer", "mark.skull", "mark.cross", "mark.star"};
    private MarkData data;
    private MarkData.Mark selectedMark;

    public GuiNPCMarks(EntityNPCInterface npc) {
        super(npc);
        this.data = MarkData.get(npc);
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = guiTop + 14;
        for (int i = 0; i < data.marks.size(); i++) {
            MarkData.Mark mark = data.marks.get(i);
            this.addButton(new GuiButtonBiDirectional(1 + i * 10, guiLeft + 6, y, 120, 20, marks, mark.type));

            String color = Integer.toHexString(mark.color);
            while (color.length() < 6)
                color = "0" + color;
            addButton(new GuiNpcButton(2 + i * 10, guiLeft + 128, y, 60, 20, color));
            getButton(2 + i * 10).setTextColor(mark.color);

            this.addButton(new GuiNpcButton(3 + i * 10, guiLeft + 190, y, 120, 20, "availability.options"));
            this.addButton(new GuiNpcButton(4 + i * 10, guiLeft + 312, y, 40, 20, "X"));
            y += 22;
        }

        if (data.marks.size() < 9) {
            this.addButton(new GuiNpcButton(101, guiLeft + 6, y + 2, 60, 20, "gui.add"));
        }
    }

    @Override
    public void buttonEvent(GuiButton button) {
        if (button.id < 90) {
            selectedMark = data.marks.get(button.id / 10);
            if (button.id % 10 == 1) {
                selectedMark.type = ((GuiNpcButton) button).getValue();
            }
            if (button.id % 10 == 2) {
                this.setSubGui(new SubGuiColorSelector(selectedMark.color));
            }
            if (button.id % 10 == 3) {
                setSubGui(new SubGuiNpcAvailability(selectedMark.availability));
            }
            if (button.id % 10 == 4) {
                data.marks.remove(selectedMark);
                initGui();
            }
        }
        if (button.id == 101) {
            data.addMark(MarkType.NONE);
            initGui();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiColorSelector) {
            selectedMark.color = ((SubGuiColorSelector) subgui).color;
            initGui();
        }
    }

    @Override
    public void save() {
        PacketClient.sendClient(new MainmenuAdvancedMarkDataPacket(data.getNBT()));
    }

}
