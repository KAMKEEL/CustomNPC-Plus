package noppes.npcs.client.gui.player;

import kamkeel.npcs.network.packets.player.AuctionActionPacket;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.AuctionClientConfig;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.containers.ContainerAuctionTrades;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for viewing player's auction trades.
 * Shows active listings, bids, and claims with confirmation system.
 */
public class GuiAuctionTrades extends GuiAuctionInterface implements IGuiData {
    // Icon textures
    private static final ResourceLocation ICON_X = new ResourceLocation("customnpcs", "textures/gui/auction/x_icon.png");
    private static final ResourceLocation ICON_CHECK = new ResourceLocation("customnpcs", "textures/gui/auction/check_icon.png");
    private static final ResourceLocation ICON_COIN = new ResourceLocation("customnpcs", "textures/items/npcCoinGold.png");

    // Grid layout
    private static final int GRID_X = 56;
    private static final int GRID_Y = 46;
    private static final int COLS = 9;
    private static final int ROWS = 5;
    private static final int TOTAL_SLOTS = COLS * ROWS;

    // Slot tint colors (ARGB)
    private static final int TINT_BLUE = 0x303060FF;   // Active listing/bid (selling)
    private static final int TINT_GREEN = 0x3030FF30;  // Sold (currency claim) or Won (item claim)
    private static final int TINT_YELLOW = 0x30FFFF30; // Outbid (refund claim)
    private static final int TINT_RED = 0x30FF3030;    // Expired/Returned (item returned to seller)

    private final ContainerAuctionTrades tradesContainer;
    private int maxTradeSlots;

    // Pending operation state
    private int pendingSlot = -1;
    private PendingOp pendingOp = PendingOp.NONE;

    private enum PendingOp { NONE, CANCEL, CLAIM }

    public GuiAuctionTrades(EntityNPCInterface npc, ContainerAuctionTrades container) {
        super(npc, container);
        this.tradesContainer = container;
        this.maxTradeSlots = AuctionClientConfig.getMaxActiveListings() * 2;
    }

    @Override
    protected int getCurrentPage() {
        return PAGE_CLAIMS;
    }

    // ========== Mouse Handling ==========

    @Override
    public void mouseEvent(int mouseX, int mouseY, int mouseButton) {
        if (hasSubGui()) return;

        int slot = getSlotAt(mouseX, mouseY);
        if (slot < 0 || slot >= TOTAL_SLOTS) {
            clearPending();
            return;
        }

        // Clicking on pending slot - confirm or cancel
        if (slot == pendingSlot && pendingOp != PendingOp.NONE) {
            handlePendingClick(mouseButton);
            return;
        }

        // Clear existing pending, start new operation
        clearPending();

        AuctionListing listing = tradesContainer.getListingAt(slot);
        AuctionClaim claim = tradesContainer.getClaimAt(slot);

        if (listing != null && tradesContainer.isSellingAt(slot) && mouseButton == 1) {
            // Right-click on own listing = show cancel
            setPending(slot, PendingOp.CANCEL);
            NoppesUtil.clickSound();
        } else if (listing != null && tradesContainer.isBiddingAt(slot) && mouseButton == 0) {
            // Left-click on active bid = open bidding GUI to increase bid
            AuctionActionPacket.openBidding(listing.id);
            NoppesUtil.clickSound();
        } else if (claim != null && mouseButton == 0) {
            // Left-click on claim = show claim
            setPending(slot, PendingOp.CLAIM);
            NoppesUtil.clickSound();
        }
    }

    /** Handle click on pending operation slot */
    private void handlePendingClick(int mouseButton) {
        if (pendingOp == PendingOp.CANCEL && mouseButton == 1) {
            // Right-click confirms cancel
            AuctionListing listing = tradesContainer.getListingAt(pendingSlot);
            if (listing != null) {
                AuctionActionPacket.cancelListing(listing.id);
                playConfirmSound();
            }
            clearPending();
        } else if (pendingOp == PendingOp.CLAIM && mouseButton == 0) {
            // Left-click confirms claim
            AuctionClaim claim = tradesContainer.getClaimAt(pendingSlot);
            if (claim != null) {
                if (claim.type.isItem()) {
                    AuctionActionPacket.claimItem(claim.id);
                } else {
                    AuctionActionPacket.claimCurrency(claim.id);
                }
                playConfirmSound();
            }
            clearPending();
        } else {
            // Wrong button - cancel operation
            clearPending();
            NoppesUtil.clickSound();
        }
    }

    /** Set pending operation and hide slot item */
    private void setPending(int slot, PendingOp op) {
        pendingSlot = slot;
        pendingOp = op;
        tradesContainer.setHiddenSlot(slot);
    }

    /** Clear pending operation and restore slot */
    private void clearPending() {
        if (pendingSlot >= 0) {
            tradesContainer.clearHiddenSlot();
        }
        pendingSlot = -1;
        pendingOp = PendingOp.NONE;
    }

    /** Play confirmation sound */
    private void playConfirmSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(
            new ResourceLocation("random.orb"), 1.0F));
    }

    /** Get slot index at mouse position */
    private int getSlotAt(int mouseX, int mouseY) {
        int relX = mouseX - guiLeft - GRID_X;
        int relY = mouseY - guiTop - GRID_Y;
        if (relX < 0 || relY < 0) return -1;

        int col = relX / 18;
        int row = relY / 18;
        if (col >= COLS || row >= ROWS) return -1;

        return col + row * COLS;
    }

    // ========== Drawing ==========

    @Override
    protected void drawAuctionContent(float partialTicks, int mouseX, int mouseY) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int slot = col + row * COLS;
                int x = guiLeft + GRID_X + col * 18;
                int y = guiTop + GRID_Y + row * 18;

                drawAuctionSlot(x, y);

                // Color tint based on slot contents
                int tint = getSlotTint(slot);
                if (tint != 0 && slot != pendingSlot) {
                    drawColoredOverlay(x, y, tint);
                }

                // Darken unavailable slots
                if (slot >= maxTradeSlots) {
                    drawDarkenedOverlay(x, y);
                }

                // Pending operation icon or currency claim coin
                if (slot == pendingSlot) {
                    if (pendingOp == PendingOp.CANCEL) {
                        drawIconOverlay(x, y, ICON_X);
                    } else if (pendingOp == PendingOp.CLAIM) {
                        drawIconOverlay(x, y, ICON_CHECK);
                    }
                } else {
                    // Draw coin icon for currency/refund claims (no item in slot)
                    AuctionClaim claim = tradesContainer.getClaimAt(slot);
                    if (claim != null && (claim.type == EnumClaimType.CURRENCY || claim.type == EnumClaimType.REFUND)) {
                        drawIconOverlay(x, y, ICON_COIN);
                    }
                }
            }
        }
    }

    /** Get tint color for slot based on contents */
    private int getSlotTint(int slot) {
        AuctionListing listing = tradesContainer.getListingAt(slot);
        AuctionClaim claim = tradesContainer.getClaimAt(slot);

        if (listing != null) {
            // Active listing or bid - Blue
            return TINT_BLUE;
        } else if (claim != null) {
            switch (claim.type) {
                case CURRENCY:
                    // Sold item - Green
                    return TINT_GREEN;
                case REFUND:
                    // Outbid - Yellow
                    return TINT_YELLOW;
                case ITEM:
                    // Won item (green) or Returned/Expired (red)
                    return claim.isReturned ? TINT_RED : TINT_GREEN;
                default:
                    return TINT_GREEN;
            }
        }
        return 0;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (hasSubGui()) return;

        int slot = getSlotAt(mouseX, mouseY);
        if (slot >= 0 && slot < TOTAL_SLOTS) {
            drawTradeTooltip(slot, mouseX - guiLeft, mouseY - guiTop);
        }
    }

    /** Draw tooltip for a trade slot */
    private void drawTradeTooltip(int slot, int x, int y) {
        List<String> tooltip = new ArrayList<>();

        if (slot == pendingSlot) {
            // Pending operation tooltip
            if (pendingOp == PendingOp.CANCEL) {
                tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("auction.trades.confirmCancel"));
                tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.trades.rightClickConfirm"));
                tooltip.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("auction.trades.otherClickCancel"));
            } else if (pendingOp == PendingOp.CLAIM) {
                tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.confirmClaim"));
                tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.trades.leftClickConfirm"));
                tooltip.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("auction.trades.otherClickCancel"));
            }
        } else {
            // Normal slot tooltip
            AuctionListing listing = tradesContainer.getListingAt(slot);
            AuctionClaim claim = tradesContainer.getClaimAt(slot);

            if (listing != null) {
                if (tradesContainer.isSellingAt(slot)) {
                    tooltip.add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("auction.trades.activeListing"));
                    tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.rightClickToCancel"));
                } else if (tradesContainer.isBiddingAt(slot)) {
                    tooltip.add(EnumChatFormatting.AQUA + StatCollector.translateToLocal("auction.trades.activeBid"));
                    tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.trades.winningBid"));
                    tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.leftClickToIncrease"));
                }
            } else if (claim != null) {
                switch (claim.type) {
                    case CURRENCY:
                        tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.soldClaim"));
                        if (!claim.itemName.isEmpty()) {
                            tooltip.add(EnumChatFormatting.WHITE + claim.itemName);
                        }
                        if (!claim.otherPlayerName.isEmpty()) {
                            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.trades.buyer") + ": " + EnumChatFormatting.AQUA + claim.otherPlayerName);
                        }
                        tooltip.add(EnumChatFormatting.GOLD + String.format("%,d", claim.currency) + " " + AuctionClientConfig.getCurrencyName());
                        break;
                    case REFUND:
                        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.refundClaim"));
                        if (!claim.itemName.isEmpty()) {
                            tooltip.add(EnumChatFormatting.WHITE + claim.itemName);
                        }
                        if (!claim.otherPlayerName.isEmpty()) {
                            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.trades.outbidBy") + ": " + EnumChatFormatting.AQUA + claim.otherPlayerName);
                        }
                        tooltip.add(EnumChatFormatting.GOLD + String.format("%,d", claim.currency) + " " + AuctionClientConfig.getCurrencyName());
                        break;
                    case ITEM:
                        if (claim.isReturned) {
                            tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("auction.trades.expiredClaim"));
                        } else {
                            tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.wonClaim"));
                        }
                        break;
                }
                tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.leftClickToClaim"));
            } else if (slot >= maxTradeSlots) {
                tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("auction.trades.unavailable"));
            }
        }

        if (!tooltip.isEmpty()) {
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            drawHoveringText(tooltip, x, y, fontRendererObj);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    // ========== Slot Handling ==========

    @Override
    protected void handleMouseClick(Slot slot, int slotIndex, int mouseButton, int clickType) {
        // Block all slot interactions
    }

    // ========== Server Updates ==========

    @Override
    public void setGuiData(NBTTagCompound compound) {
        if (compound.hasKey("TradesData")) {
            // Full trades data received from server
            tradesContainer.setTradesData(compound);
            clearPending();
        } else if (compound.hasKey("TradesUpdate")) {
            // Legacy flag - should not happen now but keep for safety
            tradesContainer.refreshData();
        }
    }

    public ContainerAuctionTrades getTradesContainer() {
        return tradesContainer;
    }
}
