package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.gui.util.SubGuiNpcCooldownPicker;
import noppes.npcs.constants.EnumStockReset;
import noppes.npcs.roles.RoleTrader;

public class SubGuiNpcTraderStock extends SubGuiInterface implements ISubGuiListener, ITextfieldListener {

    private final RoleTrader role;

    public SubGuiNpcTraderStock(RoleTrader role) {
        this.role = role;
        setBackground("menubg.png");
        xSize = 220;
        ySize = 220;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;

        // Enable Stock
        addLabel(new GuiNpcLabel(1, "stock.enable", guiLeft + 10, y + 5));
        addButton(new GuiNpcButtonYesNo(1, guiLeft + 140, y, role.stock.enableStock));
        getButton(1).setHoverText("stock.enable.hover");

        y += 24;

        // Per-Player Stock
        addLabel(new GuiNpcLabel(2, "stock.perplayer", guiLeft + 10, y + 5));
        addButton(new GuiNpcButtonYesNo(2, guiLeft + 140, y, role.stock.perPlayer));
        getButton(2).setHoverText("stock.perplayer.hover");

        y += 24;

        // Reset Type
        addLabel(new GuiNpcLabel(3, "stock.resettype", guiLeft + 10, y + 5));
        addButton(new GuiNpcButton(3, guiLeft + 90, y, 120, 20, EnumStockReset.getDisplayNames(), role.stock.resetType.ordinal()));
        getButton(3).setHoverText("stock.resettype.hover");

        y += 24;

        // Custom Time button (only visible for custom types)
        EnumStockReset resetType = role.stock.resetType;
        if (resetType == EnumStockReset.MCCUSTOM || resetType == EnumStockReset.RLCUSTOM) {
            addLabel(new GuiNpcLabel(4, "stock.customtime", guiLeft + 10, y + 5));
            addButton(new GuiNpcButton(4, guiLeft + 90, y, 120, 20, "gui.edit"));
            getButton(4).setHoverText("stock.customtime.hover");
            y += 24;
        }

        // Default Max Stock (applies to all slots as default)
        addLabel(new GuiNpcLabel(5, "stock.defaultmax", guiLeft + 10, y + 5));
        GuiNpcTextField defaultStock = new GuiNpcTextField(5, this, fontRendererObj, guiLeft + 140, y, 60, 20, getDefaultStockString());
        defaultStock.setIntegersOnly();
        defaultStock.setMinMaxDefault(-1, Integer.MAX_VALUE, -1);
        addTextField(defaultStock);

        y += 26;

        // Per-Slot Stock button
        addButton(new GuiNpcButton(5, guiLeft + 10, y, 95, 20, "stock.perslot"));
        getButton(5).setHoverText("stock.perslot.hover");

        // Reset Stock button
        addButton(new GuiNpcButton(6, guiLeft + 115, y, 95, 20, "stock.reset"));
        getButton(6).setHoverText("stock.reset.hover");

        y += 24;

        // Reset Cooldown button
        addButton(new GuiNpcButton(7, guiLeft + 10, y, 200, 20, "stock.resetcooldown"));
        getButton(7).setHoverText("stock.resetcooldown.hover");

        // Done button
        addButton(new GuiNpcButton(0, guiLeft + (xSize - 60) / 2, guiTop + ySize - 40, 60, 20, "gui.done"));
    }

    private String getDefaultStockString() {
        // Check if all stocks are the same, if so return that value
        int first = role.stock.maxStock[0];
        for (int i = 1; i < 18; i++) {
            if (role.stock.maxStock[i] != first) {
                return "-1"; // Mixed values, return unlimited
            }
        }
        return String.valueOf(first);
    }

    @Override
    public void buttonEvent(GuiButton button) {
        switch (button.id) {
            case 0: // Done
                close();
                break;
            case 1: // Enable Stock
                role.stock.enableStock = ((GuiNpcButtonYesNo) button).getBoolean();
                break;
            case 2: // Per-Player
                role.stock.perPlayer = ((GuiNpcButtonYesNo) button).getBoolean();
                break;
            case 3: // Reset Type
                int value = ((GuiNpcButton) button).getValue();
                role.stock.resetType = EnumStockReset.values()[value];
                initGui(); // Refresh to show/hide custom time button
                break;
            case 4: // Custom Time
                boolean isMC = role.stock.resetType == EnumStockReset.MCCUSTOM;
                setSubGui(new SubGuiNpcCooldownPicker(isMC, role.stock.customResetTime));
                break;
            case 5: // Per-Slot Stock
                setSubGui(new SubGuiNpcTraderStockSlots(role));
                break;
            case 6: // Reset Stock
                setSubGui(new SubGuiNpcTraderStockReset(role));
                break;
            case 7: // Reset Cooldown
                setSubGui(new SubGuiNpcTraderCooldownReset(role));
                break;
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subGui) {
        if (subGui instanceof SubGuiNpcCooldownPicker) {
            long value = ((SubGuiNpcCooldownPicker) subGui).cooldownValue;
            // Validate minimum: 1 tick for MC time, 1000ms (1 second) for real time
            boolean isMC = role.stock.resetType == EnumStockReset.MCCUSTOM;
            long minimum = isMC ? 1 : 1000;
            role.stock.customResetTime = Math.max(minimum, value);
        }
        // Refresh after per-slot editing
        if (subGui instanceof SubGuiNpcTraderStockSlots) {
            initGui();
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        if (textField.id == 5) {
            int value = textField.getInteger();
            // Apply to all slots
            for (int i = 0; i < 18; i++) {
                role.stock.setMaxStock(i, value);
            }
        }
    }
}
