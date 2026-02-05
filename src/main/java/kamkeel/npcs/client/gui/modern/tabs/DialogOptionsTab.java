package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.components.ModernButton;
import kamkeel.npcs.client.gui.components.ModernDropdown;
import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Options tab for the dialog editor.
 * Contains up to 6 dialog option editors.
 */
public class DialogOptionsTab extends DialogEditorTab {

    private List<OptionEditorV2> optionEditors = new ArrayList<>();
    private ModernButton addOptionBtn;
    private int componentGap = 3;

    public DialogOptionsTab(DialogEditorPanel parent) {
        super(parent);
        initComponents();
    }

    private void initComponents() {
        addOptionBtn = new ModernButton(200, 0, 0, 80, 16, "+ Add Option");
    }

    @Override
    public void loadFromDialog(Dialog dialog) {
        this.dialog = dialog;
        rebuildOptionEditors();
    }

    @Override
    public void saveToDialog(Dialog dialog) {
        if (dialog == null) return;

        for (OptionEditorV2 editor : optionEditors) {
            DialogOption opt = dialog.options.get(editor.getSlot());
            if (opt != null) {
                opt.title = editor.getTitleField().getText();
                int typeIdx = editor.getTypeIndex();
                opt.optionType = EnumOptionType.values()[typeIdx];
                opt.optionColor = editor.getColorBtn().getColor();
                opt.dialogId = editor.getTargetDialogId();
                opt.command = editor.getCommand();
            }
        }
    }

    public void rebuildOptionEditors() {
        optionEditors.clear();

        if (dialog == null) return;

        for (int i = 0; i < 6; i++) {
            DialogOption opt = dialog.options.get(i);
            if (opt != null && opt.optionType != EnumOptionType.Disabled) {
                OptionEditorV2 editor = new OptionEditorV2(1000 + i * 20, i, opt);
                optionEditors.add(editor);
            }
        }
    }

    @Override
    public int draw(int cx, int cw, int startY, int mouseX, int mouseY, FontRenderer fr) {
        int y = startY;

        if (optionEditors.isEmpty()) {
            fr.drawString("No options defined.", cx, y, ModernColors.TEXT_DARK);
            y += 14;
        } else {
            for (OptionEditorV2 editor : optionEditors) {
                editor.setBounds(cx, y, cw);
                editor.draw(mouseX, mouseY, fr);
                y += editor.getHeight() + componentGap;
            }
        }

        // Add option button
        if (optionEditors.size() < 6) {
            y += 4;
            addOptionBtn.xPosition = cx;
            addOptionBtn.yPosition = y;
            addOptionBtn.width = 100;
            addOptionBtn.height = 18;
            addOptionBtn.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
            y += 22;
        }

        return y - startY;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        for (OptionEditorV2 editor : optionEditors) {
            if (editor.mouseClicked(mouseX, mouseY, button, parent)) {
                markDirty();
                return true;
            }
        }
        if (addOptionBtn.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            addNewOption();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char c, int keyCode) {
        for (OptionEditorV2 editor : optionEditors) {
            if (editor.keyTyped(c, keyCode)) { markDirty(); return true; }
        }
        return false;
    }

    @Override
    public void updateScreen() {
        for (OptionEditorV2 editor : optionEditors) {
            editor.updateCursorCounter();
        }
    }

    @Override
    public List<ModernDropdown> getDropdowns() {
        List<ModernDropdown> dropdowns = new ArrayList<>();
        for (OptionEditorV2 editor : optionEditors) {
            dropdowns.add(editor.getTypeDropdown());
        }
        return dropdowns;
    }

    // === Option Management ===

    private void addNewOption() {
        if (dialog == null || optionEditors.size() >= 6) return;

        for (int i = 0; i < 6; i++) {
            DialogOption opt = dialog.options.get(i);
            if (opt == null || opt.optionType == EnumOptionType.Disabled) {
                DialogOption newOpt = new DialogOption();
                newOpt.title = "New Option";
                newOpt.optionType = EnumOptionType.QuitOption;
                dialog.options.put(i, newOpt);
                rebuildOptionEditors();
                markDirty();
                return;
            }
        }
    }

    public void removeOption(int slot) {
        if (dialog == null) return;
        DialogOption opt = dialog.options.get(slot);
        if (opt != null) {
            opt.optionType = EnumOptionType.Disabled;
            rebuildOptionEditors();
            markDirty();
        }
    }

    // === Accessors ===

    public List<OptionEditorV2> getOptionEditors() {
        return optionEditors;
    }

    public void onOptionDialogSelected(int optionSlot, int dialogId, String dialogName) {
        for (OptionEditorV2 editor : optionEditors) {
            if (editor.getSlot() == optionSlot) {
                editor.setTargetDialog(dialogId, dialogName);
                markDirty();
                return;
            }
        }
    }
}
