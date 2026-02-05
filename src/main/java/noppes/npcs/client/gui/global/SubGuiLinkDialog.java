package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;

/**
 * Sub-GUI shown during link mode: pick which option slot to use and set the title.
 */
public class SubGuiLinkDialog extends SubGuiInterface implements ITextfieldListener {

    private final Dialog sourceDialog;
    private final Dialog targetDialog;

    private int selectedSlot = -1;
    private String optionTitle;
    public boolean confirmed = false;

    // Available slots (0-5)
    private static final int MAX_SLOTS = 6;

    public SubGuiLinkDialog(Dialog source, Dialog target) {
        this.sourceDialog = source;
        this.targetDialog = target;
        this.optionTitle = target.title;
        setBackground("menubg.png");
        xSize = 250;
        ySize = 160;
    }

    @Override
    public void initGui() {
        super.initGui();

        addLabel(new GuiNpcLabel(0, "Link: " + sourceDialog.title + " -> " + targetDialog.title,
            guiLeft + 4, guiTop + 6, 0xFFFFFF));

        // Slot selector buttons
        addLabel(new GuiNpcLabel(1, "dialog.options", guiLeft + 4, guiTop + 26));

        String[] slotLabels = new String[MAX_SLOTS];
        for (int i = 0; i < MAX_SLOTS; i++) {
            DialogOption existing = sourceDialog.options.get(i);
            if (existing != null && existing.optionType != EnumOptionType.Disabled) {
                slotLabels[i] = i + ": " + existing.title;
            } else {
                slotLabels[i] = i + ": (empty)";
            }
        }

        // Find first available empty slot
        if (selectedSlot < 0) {
            for (int i = 0; i < MAX_SLOTS; i++) {
                DialogOption existing = sourceDialog.options.get(i);
                if (existing == null || existing.optionType == EnumOptionType.Disabled) {
                    selectedSlot = i;
                    break;
                }
            }
            if (selectedSlot < 0) selectedSlot = 0;
        }

        addButton(new GuiNpcButton(1, guiLeft + 70, guiTop + 22, 170, 20, slotLabels, selectedSlot));

        // Option title field
        addLabel(new GuiNpcLabel(2, "gui.title", guiLeft + 4, guiTop + 52));
        addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 70, guiTop + 48, 170, 20, optionTitle));

        // Confirm / Cancel
        addButton(new GuiNpcButton(10, guiLeft + 40, guiTop + 130, 80, 20, "gui.done"));
        addButton(new GuiNpcButton(11, guiLeft + 130, guiTop + 130, 80, 20, "gui.cancel"));
    }

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 1) {
            selectedSlot = ((GuiNpcButton) guibutton).getValue();
        }
        if (id == 10) {
            // Confirm
            confirmed = true;
            applyLink();
            close();
        }
        if (id == 11) {
            // Cancel
            close();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        if (textField.id == 2) {
            optionTitle = textField.getText();
        }
    }

    private void applyLink() {
        if (selectedSlot < 0 || selectedSlot >= MAX_SLOTS) return;

        DialogOption option = sourceDialog.options.get(selectedSlot);
        if (option == null) {
            option = new DialogOption();
            sourceDialog.options.put(selectedSlot, option);
        }
        option.optionType = EnumOptionType.DialogOption;
        option.dialogId = targetDialog.id;
        option.title = optionTitle;
    }
}
