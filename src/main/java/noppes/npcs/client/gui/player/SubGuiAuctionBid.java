package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.controllers.data.AuctionListing;

/**
 * Sub GUI for placing a bid on an auction.
 */
public class SubGuiAuctionBid extends SubGuiInterface {
    private AuctionListing listing;
    private long playerBalance;
    private GuiNpcTextField bidField;
    private long lastUpdate = 0;

    public SubGuiAuctionBid(GuiAuction parent, AuctionListing listing, long playerBalance) {
        this.listing = listing;
        this.playerBalance = playerBalance;
        setBackground("menubg.png");
        xSize = 200;
        ySize = 150;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 10;
        int labelColor = 0x404040;

        // Title
        addLabel(new GuiNpcLabel(0, "Place Bid", guiLeft + 70, y, labelColor));
        y += 18;

        // Item name
        addLabel(new GuiNpcLabel(1, "Item: " + listing.item.getDisplayName(), guiLeft + 15, y, 0x000000));
        y += 14;

        // Current price
        long currentPrice = listing.currentBid > 0 ? listing.currentBid : listing.startingPrice;
        addLabel(new GuiNpcLabel(2, "Current Price:", guiLeft + 15, y, labelColor));
        addLabel(new GuiNpcLabel(3, formatCurrency(currentPrice), guiLeft + 100, y, 0x008800));
        y += 14;

        // Minimum bid
        long minBid = listing.getMinimumBid();
        addLabel(new GuiNpcLabel(4, "Minimum Bid:", guiLeft + 15, y, labelColor));
        addLabel(new GuiNpcLabel(5, formatCurrency(minBid), guiLeft + 100, y, 0x0066AA));
        y += 14;

        // Your balance
        addLabel(new GuiNpcLabel(6, "Your Balance:", guiLeft + 15, y, labelColor));
        addLabel(new GuiNpcLabel(7, formatCurrency(playerBalance), guiLeft + 100, y,
            playerBalance >= minBid ? 0x008800 : 0xAA0000));
        y += 14;

        // Time remaining
        addLabel(new GuiNpcLabel(8, "Time Left:", guiLeft + 15, y, labelColor));
        addLabel(new GuiNpcLabel(9, formatTimeRemaining(listing.getTimeRemaining()), guiLeft + 100, y,
            getTimeColor(listing.getTimeRemaining())));
        y += 18;

        // Bid input
        addLabel(new GuiNpcLabel(10, "Your Bid:", guiLeft + 15, y + 3, labelColor));
        bidField = new GuiNpcTextField(11, this, guiLeft + 80, y, 100, 16, String.valueOf(minBid));
        bidField.setNumbersOnly();
        bidField.setMinMaxDefault(minBid, 999999999, minBid);
        addTextField(bidField);
        y += 24;

        // Quick bid buttons
        addButton(new GuiNpcButton(20, guiLeft + 15, y, 55, 16, "Min Bid"));
        addButton(new GuiNpcButton(21, guiLeft + 73, y, 55, 16, "+10%"));
        addButton(new GuiNpcButton(22, guiLeft + 131, y, 55, 16, "+25%"));
        y += 22;

        // Place bid button
        GuiNpcButton bidButton = new GuiNpcButton(30, guiLeft + 15, y, 80, 20, "Place Bid");
        bidButton.setEnabled(playerBalance >= minBid && listing.isActive());
        addButton(bidButton);

        // Cancel button
        addButton(new GuiNpcButton(31, guiLeft + 105, y, 80, 20, "Cancel"));

        // Error message area
        if (playerBalance < minBid) {
            addLabel(new GuiNpcLabel(40, "Insufficient funds!", guiLeft + 50, guiTop + ySize - 15, 0xAA0000));
        } else if (!listing.isActive()) {
            addLabel(new GuiNpcLabel(40, "Auction has ended!", guiLeft + 50, guiTop + ySize - 15, 0xAA0000));
        }
    }

    private String formatCurrency(long amount) {
        return String.format("%,d %s", amount, ConfigMarket.CurrencyName);
    }

    private String formatTimeRemaining(long millis) {
        if (millis <= 0) return "Ended";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private int getTimeColor(long millis) {
        if (millis <= 0) return 0xAA0000;
        if (millis < 5 * 60 * 1000) return 0xFF0000;
        if (millis < 30 * 60 * 1000) return 0xFF6600;
        return 0x008800;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        // Update time remaining every second
        long now = System.currentTimeMillis();
        if (now - lastUpdate >= 1000) {
            lastUpdate = now;

            GuiNpcLabel timeLabel = getLabel(9);
            if (timeLabel != null) {
                long remaining = listing.getTimeRemaining();
                timeLabel.label = formatTimeRemaining(remaining);
                timeLabel.color = getTimeColor(remaining);
            }

            // Disable bid button if auction ended
            GuiNpcButton bidButton = getButton(30);
            if (bidButton != null && !listing.isActive()) {
                bidButton.setEnabled(false);
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        int id = button.id;

        long minBid = listing.getMinimumBid();

        if (id == 20) {
            // Min bid
            bidField.setText(String.valueOf(minBid));
        } else if (id == 21) {
            // +10%
            long newBid = (long) (minBid * 1.10);
            if (newBid <= playerBalance) {
                bidField.setText(String.valueOf(newBid));
            }
        } else if (id == 22) {
            // +25%
            long newBid = (long) (minBid * 1.25);
            if (newBid <= playerBalance) {
                bidField.setText(String.valueOf(newBid));
            }
        } else if (id == 30) {
            // Place bid
            placeBid();
        } else if (id == 31) {
            // Cancel
            close();
        }
    }

    private void placeBid() {
        long bidAmount = bidField.getInteger();
        long minBid = listing.getMinimumBid();

        if (bidAmount < minBid) {
            return;
        }

        if (bidAmount > playerBalance) {
            return;
        }

        if (!listing.isActive()) {
            return;
        }

        // Send bid to server
        AuctionActionPacket.PlaceBid(listing.id, bidAmount);
        close();
    }

    @Override
    public void save() {
        // Nothing to save
    }
}
