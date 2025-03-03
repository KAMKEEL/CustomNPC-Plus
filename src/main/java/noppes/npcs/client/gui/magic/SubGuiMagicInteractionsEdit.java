package noppes.npcs.client.gui.magic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kamkeel.npcs.network.PacketClient;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Magic;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiMagicInteractionsEdit extends SubGuiInterface implements ITextfieldListener {

    private Magic magic;
    private GuiCustomScroll interactionsScroll;
    private List<String> interactionsList = new ArrayList<>();

    public SubGuiMagicInteractionsEdit(Magic magic) {
        this.magic = magic;
        setBackground("menubg.png");
        xSize = 360;
        ySize = 280;
    }

    @Override
    public void initGui() {
        super.initGui();
        addLabel(new GuiNpcLabel(0, "Edit Interactions: " + magic.name, guiLeft + 4, guiTop + 4));

        // Build interactions list from magic.interactions map.
        interactionsList.clear();
        for (Map.Entry<Integer, Float> entry : magic.interactions.entrySet()) {
            interactionsList.add("MagicID: " + entry.getKey() + " | Percent: " + entry.getValue());
        }
        if (interactionsScroll == null) {
            interactionsScroll = new GuiCustomScroll(this, 0, 0);
        }
        interactionsScroll.guiLeft = guiLeft + 4;
        interactionsScroll.guiTop = guiTop + 20;
        interactionsScroll.setSize(200, 100);
        interactionsScroll.setList(interactionsList);
        this.addScroll(interactionsScroll);

        // Text fields for adding/updating an interaction
        addLabel(new GuiNpcLabel(1, "Target Magic ID:", guiLeft + 210, guiTop + 30));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 310, guiTop + 26, 40, 20, ""));
        addLabel(new GuiNpcLabel(2, "Percent:", guiLeft + 210, guiTop + 60));
        addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 310, guiTop + 56, 40, 20, ""));

        // Buttons to add, update, or remove an interaction
        addButton(new GuiNpcButton(10, guiLeft + 210, guiTop + 90, 70, 20, "Add"));
        addButton(new GuiNpcButton(11, guiLeft + 290, guiTop + 90, 70, 20, "Update"));
        addButton(new GuiNpcButton(12, guiLeft + 210, guiTop + 115, 70, 20, "Remove"));

        // Done button to send changes
        addButton(new GuiNpcButton(99, guiLeft + 4, guiTop + 240, 60, 20, "Done"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) { // Add interaction
            try {
                int targetId = Integer.parseInt(getTextField(1).getText());
                float percent = Float.parseFloat(getTextField(2).getText());
                magic.interactions.put(targetId, percent);
            } catch (NumberFormatException e) { }
            initGui();
        }
        if (button.id == 11) { // Update interaction
            String selected = interactionsScroll.getSelected();
            if (selected != null && !selected.isEmpty()) {
                try {
                    int targetId = Integer.parseInt(selected.split(" ")[1]);
                    float percent = Float.parseFloat(getTextField(2).getText());
                    magic.interactions.put(targetId, percent);
                } catch (NumberFormatException e) { }
                initGui();
            }
        }
        if (button.id == 12) { // Remove interaction
            String selected = interactionsScroll.getSelected();
            if (selected != null && !selected.isEmpty()) {
                try {
                    int targetId = Integer.parseInt(selected.split(" ")[1]);
                    magic.interactions.remove(targetId);
                } catch (NumberFormatException e) { }
                initGui();
            }
        }

        if (button.id == 99) {
            close();
        }
    }

    @Override
    public void unFocused(noppes.npcs.client.gui.util.GuiNpcTextField textField) {
    }
}
