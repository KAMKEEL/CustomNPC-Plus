package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.AuctionClientConfig;
import noppes.npcs.client.gui.util.GuiAuctionNavButton;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.containers.ContainerAuctionBidding;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for viewing and bidding on a specific auction listing.
 * Shows item, auction info, and bid/buyout buttons.
 */
public class GuiAuctionBidding extends GuiAuctionInterface implements ISubGuiListener, IGuiData {
    // Icons
    private static final ResourceLocation ICON_BID = new ResourceLocation("customnpcs", "textures/items/npcCoinDiamond.png");
    private static final ResourceLocation ICON_BUYOUT = new ResourceLocation("customnpcs", "textures/items/npcCoinEmerald.png");

    // Layout - non-final for testing/positioning
    protected int itemSlotX = 56;
    protected int itemSlotY = 52;
    protected int btnBidX = 56;
    protected int btnBidY = 80;
    protected int btnBuyoutX = 80;
    protected int btnBuyoutY = 80;
    protected int infoX = 110;
    protected int infoY = 50;
    protected int infoWidth = 130;

    // Button IDs
    private static final int BTN_BID = 200;
    private static final int BTN_BUYOUT = 201;

    private final ContainerAuctionBidding biddingContainer;
    private AuctionListing listing;
    private long playerBalance = 0;
    private boolean dataLoaded = false;
    private boolean isOwnListing = false;

    // Buttons
    private GuiAuctionNavButton btnBid;
    private GuiAuctionNavButton btnBuyout;

    public GuiAuctionBidding(EntityNPCInterface npc, ContainerAuctionBidding container) {
        super(npc, container);
        this.biddingContainer = container;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Bid button (diamond coin)
        btnBid = new GuiAuctionNavButton(BTN_BID, guiLeft + btnBidX, guiTop + btnBidY,
            "auction.bid.place", ICON_BID);
        addButton(btnBid);

        // Buy Now button (emerald coin) - only visible if listing has buyout
        btnBuyout = new GuiAuctionNavButton(BTN_BUYOUT, guiLeft + btnBuyoutX, guiTop + btnBuyoutY,
            "auction.bid.buyout", ICON_BUYOUT);
        addButton(btnBuyout);

        updateButtonState();
    }

    @Override
    protected int getCurrentPage() {
        // Not a standard page - return -1 so nav buttons aren't selected
        return -1;
    }

    /** Update button visibility based on listing state */
    private void updateButtonState() {
        if (listing == null) {
            if (btnBid != null) btnBid.setVisible(false);
            if (btnBuyout != null) btnBuyout.setVisible(false);
            return;
        }

        // Hide buttons for own listings - can view but not bid/buy
        if (isOwnListing) {
            if (btnBid != null) btnBid.setVisible(false);
            if (btnBuyout != null) btnBuyout.setVisible(false);
            return;
        }

        // Bid button visible for other players' listings
        if (btnBid != null) btnBid.setVisible(true);

        // Buyout button only if listing has buyout
        if (btnBuyout != null) {
            btnBuyout.setVisible(listing.hasBuyout());
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        if (listing == null) return;

        switch (button.id) {
            case BTN_BID:
                openBidSubGui();
                break;
            case BTN_BUYOUT:
                if (listing.hasBuyout()) {
                    openBuyNowSubGui();
                }
                break;
        }
    }

    private void openBidSubGui() {
        if (listing == null) return;
        long minBid = listing.getMinimumBid(AuctionClientConfig.getMinBidIncrement());
        setSubGui(new SubGuiAuctionBid(listing.id, minBid, playerBalance));
    }

    private void openBuyNowSubGui() {
        if (listing == null || !listing.hasBuyout()) return;
        setSubGui(new SubGuiAuctionBuyNow(listing.id, listing.buyoutPrice, playerBalance));
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiAuctionBid) {
            SubGuiAuctionBid bidGui = (SubGuiAuctionBid) subgui;
            if (bidGui.wasSuccessful()) {
                // Bid placed - go to listings
                AuctionActionPacket.openPage(PAGE_LISTINGS);
            }
        } else if (subgui instanceof SubGuiAuctionBuyNow) {
            SubGuiAuctionBuyNow buyGui = (SubGuiAuctionBuyNow) subgui;
            if (buyGui.wasSuccessful()) {
                // Bought out - go to my trades
                AuctionActionPacket.openPage(PAGE_CLAIMS);
            }
        }
    }

    // ========== Data Handling ==========

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("BiddingData")) {
            playerBalance = compound.getLong("Balance");
            isOwnListing = compound.getBoolean("IsOwnListing");

            if (compound.hasKey("Listing")) {
                listing = AuctionListing.fromNBT(compound.getCompoundTag("Listing"));
                biddingContainer.setListing(listing);
            }

            dataLoaded = true;
            updateButtonState();
        }
    }

    // ========== Drawing ==========

    @Override
    protected void drawAuctionContent(float partialTicks, int mouseX, int mouseY) {
        // Draw item slot background
        drawAuctionSlot(guiLeft + itemSlotX, guiTop + itemSlotY);

        if (!dataLoaded || listing == null) {
            // Loading text
            String loading = StatCollector.translateToLocal("gui.loading");
            fontRendererObj.drawString(loading, guiLeft + infoX, guiTop + infoY, 0xFFFFFF);
            return;
        }

        // Draw auction info on the right side
        drawAuctionInfo();
    }

    /** Draw auction listing information on the right side */
    private void drawAuctionInfo() {
        int x = guiLeft + infoX;
        int y = guiTop + infoY;
        int lineHeight = 12;
        String currencyName = AuctionClientConfig.getCurrencyName();

        // Seller
        String sellerLabel = StatCollector.translateToLocal("auction.seller").replace("%s", "");
        fontRendererObj.drawString(EnumChatFormatting.GRAY + sellerLabel, x, y, 0xFFFFFF);
        y += lineHeight;
        fontRendererObj.drawString(EnumChatFormatting.WHITE + listing.sellerName, x + 10, y, 0xFFFFFF);
        y += lineHeight + 4;

        // Current Bid or Starting Price
        if (listing.hasBids()) {
            String bidLabel = StatCollector.translateToLocal("auction.info.currentBid");
            fontRendererObj.drawString(EnumChatFormatting.YELLOW + bidLabel, x, y, 0xFFFFFF);
            y += lineHeight;
            fontRendererObj.drawString(EnumChatFormatting.GOLD + formatCurrency(listing.currentBid) + " " + currencyName, x + 10, y, 0xFFFFFF);
            y += lineHeight + 4;

            // Current Bidder
            String bidderLabel = StatCollector.translateToLocal("auction.info.highBidder");
            fontRendererObj.drawString(EnumChatFormatting.GRAY + bidderLabel, x, y, 0xFFFFFF);
            y += lineHeight;
            String bidderName = listing.highBidderName != null ? listing.highBidderName : "???";
            fontRendererObj.drawString(EnumChatFormatting.WHITE + bidderName, x + 10, y, 0xFFFFFF);
            y += lineHeight + 4;
        } else {
            String startLabel = StatCollector.translateToLocal("auction.info.startingPrice");
            fontRendererObj.drawString(EnumChatFormatting.YELLOW + startLabel, x, y, 0xFFFFFF);
            y += lineHeight;
            fontRendererObj.drawString(EnumChatFormatting.GOLD + formatCurrency(listing.startingPrice) + " " + currencyName, x + 10, y, 0xFFFFFF);
            y += lineHeight + 4;
        }

        // Buyout Price (if available)
        if (listing.hasBuyout()) {
            String buyoutLabel = StatCollector.translateToLocal("auction.info.buyoutPrice");
            fontRendererObj.drawString(EnumChatFormatting.GREEN + buyoutLabel, x, y, 0xFFFFFF);
            y += lineHeight;
            fontRendererObj.drawString(EnumChatFormatting.GREEN + formatCurrency(listing.buyoutPrice) + " " + currencyName, x + 10, y, 0xFFFFFF);
            y += lineHeight + 4;
        }

        // Time Remaining
        String timeLabel = StatCollector.translateToLocal("auction.info.timeLeft");
        fontRendererObj.drawString(EnumChatFormatting.GRAY + timeLabel, x, y, 0xFFFFFF);
        y += lineHeight;
        long timeRemaining = listing.getTimeRemaining();
        EnumChatFormatting timeColor = timeRemaining < 3600000 ? EnumChatFormatting.RED : EnumChatFormatting.WHITE;
        fontRendererObj.drawString(timeColor + formatTimeRemaining(timeRemaining), x + 10, y, 0xFFFFFF);
        y += lineHeight + 4;

        // Bid Count
        String bidCountLabel = StatCollector.translateToLocal("auction.info.bidCount");
        fontRendererObj.drawString(EnumChatFormatting.GRAY + bidCountLabel, x, y, 0xFFFFFF);
        y += lineHeight;
        fontRendererObj.drawString(EnumChatFormatting.WHITE + String.valueOf(listing.bidCount), x + 10, y, 0xFFFFFF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (hasSubGui()) return;

        // Draw button tooltips
        drawBiddingPageTooltips(mouseX, mouseY);
    }

    private void drawBiddingPageTooltips(int mouseX, int mouseY) {
        List<String> tooltip = null;

        if (btnBid != null && btnBid.visible && btnBid.isHovered()) {
            tooltip = new ArrayList<>();
            tooltip.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("auction.bid.place"));
            if (listing != null) {
                long minBid = listing.getMinimumBid(AuctionClientConfig.getMinBidIncrement());
                tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.bid.minimum")
                    .replace("%s", EnumChatFormatting.WHITE + formatCurrency(minBid) + " " + AuctionClientConfig.getCurrencyName()));
            }
        } else if (btnBuyout != null && btnBuyout.visible && btnBuyout.isHovered()) {
            tooltip = new ArrayList<>();
            tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.bid.buyout"));
            if (listing != null && listing.hasBuyout()) {
                tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.info.buyoutPrice") + ":");
                tooltip.add(EnumChatFormatting.GREEN + formatCurrency(listing.buyoutPrice) + " " + AuctionClientConfig.getCurrencyName());
            }
        }

        if (tooltip != null && !tooltip.isEmpty()) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            drawHoveringText(tooltip, mouseX - guiLeft, mouseY - guiTop, fontRendererObj);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    // ========== Slot Handling ==========

    @Override
    protected void handleMouseClick(Slot slot, int slotIndex, int mouseButton, int clickType) {
        // Block all slot interactions - view only
    }

    public AuctionListing getListing() { return listing; }
}
