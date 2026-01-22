package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.roles.RoleTrader;

/**
 * Per-slot stock management dialog.
 * Shows all 18 slots with their max stock values.
 */
public class SubGuiNpcTraderStockSlots extends SubGuiInterface implements ITextfieldListener {

    private final RoleTrader role;

    public SubGuiNpcTraderStockSlots(RoleTrader role) {
        this.role = role;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Title
        addLabel(new GuiNpcLabel(0, "stock.perslot.title", guiLeft + 10, guiTop + 6));

        // 18 slots in 3 columns x 6 rows
        int startY = guiTop + 22;
        int colWidth = 80;

        for (int i = 0; i < 18; i++) {
            int col = i % 3;
            int row = i / 3;

            int x = guiLeft + 10 + col * colWidth;
            int y = startY + row * 28;

            // Slot label
            addLabel(new GuiNpcLabel(i + 1, "Slot " + (i + 1) + ":", x, y + 5));

            // Max stock textfield
            int maxStock = role.stock.maxStock[i];
            String value = maxStock < 0 ? "-1" : String.valueOf(maxStock);
            GuiNpcTextField field = new GuiNpcTextField(i, this, fontRendererObj, x + 42, y, 32, 18, value);
            field.setIntegersOnly();
            field.setMinMaxDefault(-1, Integer.MAX_VALUE, -1);
            addTextField(field);
        }

        // Done button
        addButton(new GuiNpcButton(0, guiLeft + (xSize - 60) / 2, guiTop + ySize - 26, 60, 20, "gui.done"));
    }

    @Override
    public void buttonEvent(GuiButton button) {
        if (button.id == 0) {
            close();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        int slot = textField.id;
        if (slot >= 0 && slot < 18) {
            int value = textField.getInteger();
            role.stock.setMaxStock(slot, value);
        }
    }
}
