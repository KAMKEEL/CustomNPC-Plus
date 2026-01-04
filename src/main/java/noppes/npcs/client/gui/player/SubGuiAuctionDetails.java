package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.controllers.data.AuctionListing;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Sub GUI for viewing auction listing details.
 */
public class SubGuiAuctionDetails extends SubGuiInterface {
    private AuctionListing listing;
    private long lastUpdate = 0;

    public SubGuiAuctionDetails(GuiAuction parent, AuctionListing listing) {
        this.listing = listing;
        setBackground("menubg.png");
        xSize = 240;
        ySize = 200;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 8;
        int labelColor = 0x404040;
        int valueColor = 0x000000;

        // Title
        addLabel(new GuiNpcLabel(0, "Auction Details", guiLeft + 80, y, labelColor));
        y += 16;

        // Item name
        addLabel(new GuiNpcLabel(1, "Item:", guiLeft + 20, y, labelColor));
        addLabel(new GuiNpcLabel(2, listing.item.getDisplayName(), guiLeft + 80, y, valueColor));
        y += 14;

        // Quantity
        addLabel(new GuiNpcLabel(3, "Quantity:", guiLeft + 20, y, labelColor));
        addLabel(new GuiNpcLabel(4, String.valueOf(listing.item.stackSize), guiLeft + 80, y, valueColor));
        y += 14;

        // Seller
        addLabel(new GuiNpcLabel(5, "Seller:", guiLeft + 20, y, labelColor));
        addLabel(new GuiNpcLabel(6, listing.sellerName, guiLeft + 80, y, valueColor));
        y += 14;

        // Starting price
        addLabel(new GuiNpcLabel(10, "Starting Price:", guiLeft + 20, y, labelColor));
        addLabel(new GuiNpcLabel(11, formatCurrency(listing.startingPrice), guiLeft + 100, y, valueColor));
        y += 14;

        // Current bid
        if (listing.currentBid > 0) {
            addLabel(new GuiNpcLabel(12, "Current Bid:", guiLeft + 20, y, labelColor));
            addLabel(new GuiNpcLabel(13, formatCurrency(listing.currentBid), guiLeft + 100, y, 0x008800));
            y += 14;

            // High bidder
            addLabel(new GuiNpcLabel(14, "High Bidder:", guiLeft + 20, y, labelColor));
            addLabel(new GuiNpcLabel(15, listing.highBidderName, guiLeft + 100, y, valueColor));
            y += 14;

            // Bid count
            addLabel(new GuiNpcLabel(16, "Total Bids:", guiLeft + 20, y, labelColor));
            addLabel(new GuiNpcLabel(17, String.valueOf(listing.bidCount), guiLeft + 100, y, valueColor));
            y += 14;
        } else {
            addLabel(new GuiNpcLabel(12, "Current Bid:", guiLeft + 20, y, labelColor));
            addLabel(new GuiNpcLabel(13, "No bids yet", guiLeft + 100, y, 0x888888));
            y += 14;
        }

        // Buyout price
        if (listing.buyoutPrice > 0) {
            addLabel(new GuiNpcLabel(20, "Buyout Price:", guiLeft + 20, y, labelColor));
            addLabel(new GuiNpcLabel(21, formatCurrency(listing.buyoutPrice), guiLeft + 100, y, 0xAA6600));
            y += 14;
        }

        // Minimum bid
        addLabel(new GuiNpcLabel(22, "Minimum Bid:", guiLeft + 20, y, labelColor));
        addLabel(new GuiNpcLabel(23, formatCurrency(listing.getMinimumBid()), guiLeft + 100, y, 0x0066AA));
        y += 14;

        // Time remaining (will be updated)
        addLabel(new GuiNpcLabel(30, "Time Left:", guiLeft + 20, y, labelColor));
        addLabel(new GuiNpcLabel(31, formatTimeRemaining(listing.getTimeRemaining()), guiLeft + 100, y,
            getTimeColor(listing.getTimeRemaining())));
        y += 20;

        // Close button
        addButton(new GuiNpcButton(50, guiLeft + 80, guiTop + 175, 80, 20, "Close"));
    }

    private String formatCurrency(long amount) {
        return String.format("%,d %s", amount, ConfigMarket.CurrencyName);
    }

    private String formatTimeRemaining(long millis) {
        if (millis <= 0) return "Auction Ended";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private int getTimeColor(long millis) {
        if (millis <= 0) return 0xAA0000;
        if (millis < 5 * 60 * 1000) return 0xFF0000;  // < 5 minutes - red
        if (millis < 30 * 60 * 1000) return 0xFF6600; // < 30 minutes - orange
        if (millis < 60 * 60 * 1000) return 0xAAAA00; // < 1 hour - yellow
        return 0x008800; // green
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        // Update time remaining every second
        long now = System.currentTimeMillis();
        if (now - lastUpdate >= 1000) {
            lastUpdate = now;

            GuiNpcLabel timeLabel = getLabel(31);
            if (timeLabel != null) {
                long remaining = listing.getTimeRemaining();
                timeLabel.label = formatTimeRemaining(remaining);
                timeLabel.color = getTimeColor(remaining);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw item icon
        if (listing.item != null) {
            int itemX = guiLeft + 200;
            int itemY = guiTop + 30;

            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glScalef(2.0f, 2.0f, 2.0f);

            itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.renderEngine,
                listing.item, itemX / 2, itemY / 2);
            itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.renderEngine,
                listing.item, itemX / 2, itemY / 2);

            GL11.glScalef(0.5f, 0.5f, 0.5f);
            RenderHelper.disableStandardItemLighting();

            // Tooltip on hover
            if (mouseX >= itemX && mouseX < itemX + 32 && mouseY >= itemY && mouseY < itemY + 32) {
                renderToolTip(listing.item, mouseX, mouseY);
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 50) {
            close();
        }
    }

    @Override
    public void save() {
        // Nothing to save
    }
}
