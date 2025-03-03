package noppes.npcs.client.gui.magic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.magic.MagicCycleSavePacket;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.MagicAssociation;
import noppes.npcs.controllers.data.MagicCycle;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiMagicCycleEdit extends SubGuiInterface implements ITextfieldListener {

    private MagicCycle cycle;
    private GuiCustomScroll assocScroll;
    private List<String> assocList = new ArrayList<>();

    public SubGuiMagicCycleEdit(MagicCycle cycle) {
        this.cycle = cycle;
        setBackground("menubg.png");
        xSize = 360;
        ySize = 240;
    }

    @Override
    public void initGui() {
        super.initGui();
        addLabel(new GuiNpcLabel(0, "Cycle: " + cycle.title, guiLeft + 4, guiTop + 4));

        // Build list of associations in the cycle (format: "MagicID: X | Index: Y | Priority: Z")
        assocList.clear();
        for (Map.Entry<Integer, MagicAssociation> entry : cycle.associations.entrySet()) {
            MagicAssociation assoc = entry.getValue();
            assocList.add("MagicID: " + entry.getKey() + " | Index: " + assoc.index + " | Priority: " + assoc.priority);
        }
        if (assocScroll == null) {
            assocScroll = new GuiCustomScroll(this, 0, 0);
        }
        assocScroll.guiLeft = guiLeft + 4;
        assocScroll.guiTop = guiTop + 20;
        assocScroll.setSize(200, 100);
        assocScroll.setList(assocList);
        this.addScroll(assocScroll);

        // Text fields for editing the selected association’s index and priority
        addLabel(new GuiNpcLabel(1, "Index:", guiLeft + 210, guiTop + 30));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 250, guiTop + 26, 50, 20, ""));
        addLabel(new GuiNpcLabel(2, "Priority:", guiLeft + 210, guiTop + 60));
        addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 250, guiTop + 56, 50, 20, ""));

        // Buttons to update or remove the selected association
        addButton(new GuiNpcButton(10, guiLeft + 210, guiTop + 90, 70, 20, "Update"));
        addButton(new GuiNpcButton(11, guiLeft + 290, guiTop + 90, 70, 20, "Remove"));

        // Done button to save cycle changes via packet
        addButton(new GuiNpcButton(99, guiLeft + 4, guiTop + 130, 60, 20, "Done"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) { // Update association
            String selected = assocScroll.getSelected();
            if (selected != null && !selected.isEmpty()) {
                try {
                    String[] parts = selected.split(" ");
                    int magicId = Integer.parseInt(parts[1]);
                    MagicAssociation assoc = cycle.associations.get(magicId);
                    if (assoc != null) {
                        int newIndex = Integer.parseInt(getTextField(1).getText());
                        int newPriority = Integer.parseInt(getTextField(2).getText());
                        assoc.index = newIndex;
                        assoc.priority = newPriority;
                    }
                } catch (NumberFormatException e) {
                    // Handle parse error if needed.
                }
                initGui();
            }
        }
        if (button.id == 11) { // Remove association
            String selected = assocScroll.getSelected();
            if (selected != null && !selected.isEmpty()) {
                try {
                    String[] parts = selected.split(" ");
                    int magicId = Integer.parseInt(parts[1]);
                    cycle.associations.remove(magicId);
                } catch (NumberFormatException e) {
                    // Handle error if needed.
                }
                initGui();
            }
        }
        if (button.id == 99) { // Done: send save packet
            NBTTagCompound compound = new NBTTagCompound();
            cycle.writeNBT(compound);
            PacketClient.sendClient(new MagicCycleSavePacket(compound));
            close();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {

    }
}
