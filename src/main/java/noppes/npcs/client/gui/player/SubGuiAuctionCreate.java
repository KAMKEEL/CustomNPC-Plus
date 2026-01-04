package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
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

    private static final String[] DURATION_NAMES = {"Short", "Medium", "Long", "Very Long"};

    public SubGuiAuctionCreate(GuiAuction parent, ContainerAuction container) {
        this.container = container;
        setBackground("menubg.png");
        xSize = 220;
        ySize = 180;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Title
        addLabel(new GuiNpcLabel(0, "Create Auction Listing", guiLeft + 50, guiTop + 8, 0x404040));

        // Item slot instructions
        addLabel(new GuiNpcLabel(1, "Place item in slot below:", guiLeft + 20, guiTop + 28, 0x404040));

        // Item info (shows when item is in slot)
        ItemStack item = container.getListingItem();
        if (item != null) {
            addLabel(new GuiNpcLabel(2, "Item: " + item.getDisplayName(), guiLeft + 20, guiTop + 50, 0x008800));
        } else {
            addLabel(new GuiNpcLabel(2, "(No item selected)", guiLeft + 20, guiTop + 50, 0x888888));
        }

        // Starting price
        addLabel(new GuiNpcLabel(10, "Starting Price:", guiLeft + 20, guiTop + 70, 0x404040));
        startingPriceField = new GuiNpcTextField(11, this, guiLeft + 100, guiTop + 68, 80, 14, "100");
        startingPriceField.setNumbersOnly();
        startingPriceField.setMinMaxDefault(1, 999999999, 100);
        addTextField(startingPriceField);

        // Buyout price (optional)
        addLabel(new GuiNpcLabel(20, "Buyout Price:", guiLeft + 20, guiTop + 90, 0x404040));
        buyoutPriceField = new GuiNpcTextField(21, this, guiLeft + 100, guiTop + 88, 80, 14, "0");
        buyoutPriceField.setNumbersOnly();
        buyoutPriceField.setMinMaxDefault(0, 999999999, 0);
        addTextField(buyoutPriceField);
        addLabel(new GuiNpcLabel(22, "(0 = no buyout)", guiLeft + 185, guiTop + 90, 0x888888));

        // Duration selection
        addLabel(new GuiNpcLabel(30, "Duration:", guiLeft + 20, guiTop + 112, 0x404040));
        addButton(new GuiNpcButton(31, guiLeft + 100, guiTop + 108, 100, 20, getDurationDisplay()));

        // Fee display
        long fee = getListingFee();
        addLabel(new GuiNpcLabel(40, "Listing Fee: " + fee + " " + ConfigMarket.CurrencyName,
            guiLeft + 20, guiTop + 135, fee > 0 ? 0xAA0000 : 0x008800));

        // Create button
        addButton(new GuiNpcButton(50, guiLeft + 20, guiTop + 155, 80, 20, "Create Listing"));
        getButton(50).setEnabled(item != null);

        // Cancel button
        addButton(new GuiNpcButton(51, guiLeft + 120, guiTop + 155, 80, 20, "Cancel"));
    }

    private String getDurationDisplay() {
        EnumAuctionDuration duration = EnumAuctionDuration.values()[selectedDuration];
        return DURATION_NAMES[selectedDuration] + " (" + duration.getHours() + "h)";
    }

    private long getListingFee() {
        EnumAuctionDuration duration = EnumAuctionDuration.values()[selectedDuration];
        return duration.getFee();
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

        long startingPrice = startingPriceField.getInteger();
        long buyoutPrice = buyoutPriceField.getInteger();

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

    @Override
    public void updateScreen() {
        super.updateScreen();

        // Update item display if it changed
        ItemStack item = container.getListingItem();
        GuiNpcLabel itemLabel = getLabel(2);
        GuiNpcButton createButton = getButton(50);

        if (item != null) {
            if (itemLabel != null) {
                itemLabel.label = "Item: " + item.getDisplayName();
                itemLabel.color = 0x008800;
            }
            if (createButton != null) {
                createButton.setEnabled(true);
            }
        } else {
            if (itemLabel != null) {
                itemLabel.label = "(No item selected)";
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
