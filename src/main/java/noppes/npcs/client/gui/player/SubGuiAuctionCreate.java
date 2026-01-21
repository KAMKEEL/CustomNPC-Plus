package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumAuctionDuration;
import noppes.npcs.containers.ContainerAuction;

/**
 * Sub GUI for creating a new auction listing.
 */
public class SubGuiAuctionCreate extends SubGuiInterface {
    private ContainerAuction container;

    private GuiNpcTextField startingPriceField;
    private GuiNpcTextField buyoutPriceField;
    private int selectedDuration = 1; // Default to MEDIUM

    public SubGuiAuctionCreate(GuiAuction parent, ContainerAuction container) {
        this.container = container;
        setBackground("menubg.png");
        xSize = 260;
        ySize = 190;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Title
        addLabel(new GuiNpcLabel(0, StatCollector.translateToLocal("auction.createListing"), guiLeft + 80, guiTop + 8, 0x404040));

        // Item slot instructions
        addLabel(new GuiNpcLabel(1, StatCollector.translateToLocal("auction.placeItemBelow"), guiLeft + 20, guiTop + 28, 0x404040));

        // Item info (shows when item is in slot)
        ItemStack item = container.getListingItem();
        if (item != null) {
            addLabel(new GuiNpcLabel(2, StatCollector.translateToLocal("auction.item") + " " + item.getDisplayName(), guiLeft + 20, guiTop + 50, 0x008800));
        } else {
            addLabel(new GuiNpcLabel(2, StatCollector.translateToLocal("auction.noItemSelected"), guiLeft + 20, guiTop + 50, 0x888888));
        }

        // Starting price
        addLabel(new GuiNpcLabel(10, StatCollector.translateToLocal("auction.startingPrice"), guiLeft + 20, guiTop + 74, 0x404040));
        startingPriceField = new GuiNpcTextField(11, this, guiLeft + 120, guiTop + 72, 90, 14, "100");
        startingPriceField.setIntegersOnly();
        startingPriceField.setMinMaxDefault(1, 999999999, 100);
        addTextField(startingPriceField);

        // Buyout price (optional)
        addLabel(new GuiNpcLabel(20, StatCollector.translateToLocal("auction.buyoutPrice"), guiLeft + 20, guiTop + 96, 0x404040));
        buyoutPriceField = new GuiNpcTextField(21, this, guiLeft + 120, guiTop + 94, 90, 14, "0");
        buyoutPriceField.setIntegersOnly();
        buyoutPriceField.setMinMaxDefault(0, 999999999, 0);
        addTextField(buyoutPriceField);
        addLabel(new GuiNpcLabel(22, StatCollector.translateToLocal("auction.noBuyoutHint"), guiLeft + 215, guiTop + 96, 0x888888));

        // Duration selection
        addLabel(new GuiNpcLabel(30, StatCollector.translateToLocal("auction.duration"), guiLeft + 20, guiTop + 118, 0x404040));
        addButton(new GuiNpcButton(31, guiLeft + 120, guiTop + 114, 110, 20, getDurationDisplay()));

        // Fee display
        long fee = getListingFee();
        addLabel(new GuiNpcLabel(40, StatCollector.translateToLocal("auction.listingFee") + " " + fee + " " + ConfigMarket.CurrencyName,
            guiLeft + 20, guiTop + 142, fee > 0 ? 0xAA0000 : 0x008800));

        // Create button
        addButton(new GuiNpcButton(50, guiLeft + 30, guiTop + 162, 90, 20, StatCollector.translateToLocal("auction.create")));
        getButton(50).setEnabled(item != null);

        // Cancel button
        addButton(new GuiNpcButton(51, guiLeft + 140, guiTop + 162, 90, 20, StatCollector.translateToLocal("gui.cancel")));
    }

    private String getDurationDisplay() {
        EnumAuctionDuration duration = EnumAuctionDuration.values()[selectedDuration];
        return duration.getDisplayName();
    }

    private long getListingFee() {
        EnumAuctionDuration duration = EnumAuctionDuration.values()[selectedDuration];
        return duration.getListingFee();
    }

    @Override
    public void actionPerformed(GuiButton button) {
        int id = button.id;

        if (id == 31) {
            // Cycle duration
            selectedDuration = (selectedDuration + 1) % EnumAuctionDuration.values().length;
            initGui();
        } else if (id == 50) {
            // Create listing
            createListing();
        } else if (id == 51) {
            // Cancel
            close();
        }
    }

    private void createListing() {
        ItemStack item = container.getListingItem();
        if (item == null) {
            return;
        }

        long startingPrice = parsePrice(startingPriceField, 1);
        long buyoutPrice = parsePrice(buyoutPriceField, 0);

        if (startingPrice < 1) {
            startingPrice = 1;
        }

        if (buyoutPrice > 0 && buyoutPrice < startingPrice) {
            // Show error - buyout must be higher than starting
            return;
        }

        EnumAuctionDuration duration = EnumAuctionDuration.values()[selectedDuration];

        // Send to server
        AuctionActionPacket.CreateListing(item, startingPrice, buyoutPrice, duration);

        // Clear slot and close
        container.clearListingSlot();
        close();
    }

    private long parsePrice(GuiNpcTextField field, long defaultValue) {
        try {
            return Long.parseLong(field.getText());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        // Update item display if it changed
        ItemStack item = container.getListingItem();
        GuiNpcLabel itemLabel = getLabel(2);
        GuiNpcButton createButton = getButton(50);

        if (item != null) {
            if (itemLabel != null) {
                itemLabel.label = StatCollector.translateToLocal("auction.item") + " " + item.getDisplayName();
                itemLabel.color = 0x008800;
            }
            if (createButton != null) {
                createButton.setEnabled(true);
            }
        } else {
            if (itemLabel != null) {
                itemLabel.label = StatCollector.translateToLocal("auction.noItemSelected");
                itemLabel.color = 0x888888;
            }
            if (createButton != null) {
                createButton.setEnabled(false);
            }
        }
    }

    @Override
    public void save() {
        // Nothing to save
    }
}
