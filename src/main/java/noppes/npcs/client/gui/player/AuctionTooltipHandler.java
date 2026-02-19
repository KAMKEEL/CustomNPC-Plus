package noppes.npcs.client.gui.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import noppes.npcs.client.AuctionClientConfig;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.containers.ContainerAuctionListing;
import noppes.npcs.containers.ContainerAuctionSell;
import noppes.npcs.containers.ContainerAuctionTrades;
import noppes.npcs.containers.SlotAuctionDisplay;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.util.AuctionFormatUtil;

import java.util.List;

/**
 * Handles adding auction information to item tooltips when viewing auction GUIs.
 * Called from ItemTooltipEvent in ClientEventHandler.
 * <p>
 * Uses slot-based lookup (slot index) instead of ItemStack comparison to correctly
 * handle duplicate items with different auction data (e.g., two porkchops at different prices).
 */
@SideOnly(Side.CLIENT)
public class AuctionTooltipHandler {

    /**
     * Handle ItemTooltipEvent - append auction info to the end of the tooltip.
     */
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.itemStack == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen == null) return;

        GuiScreen screen = mc.currentScreen;

        if (screen instanceof GuiAuctionListing) {
            addListingTooltip((GuiAuctionListing) screen, event.itemStack, event.toolTip);
        } else if (screen instanceof GuiAuctionTrades) {
            addTradesTooltip((GuiAuctionTrades) screen, event.itemStack, event.toolTip);
        } else if (screen instanceof GuiAuctionSell) {
            addSellTooltip((GuiAuctionSell) screen, event.itemStack, event.toolTip);
        }
    }

    /**
     * Find the slot in the container whose getStack() returns the exact same ItemStack reference.
     * This avoids the duplicate item bug where ItemStack.areItemStacksEqual matches the wrong listing.
     */
    private static Slot findHoveredSlot(GuiContainer gui, ItemStack stack) {
        if (stack == null) return null;
        for (Object obj : gui.inventorySlots.inventorySlots) {
            Slot slot = (Slot) obj;
            if (slot.getHasStack() && slot.getStack() == stack) {
                return slot;
            }
        }
        return null;
    }

    // ========== Listing Page ==========

    private static void addListingTooltip(GuiAuctionListing gui, ItemStack stack, List<String> tooltip) {
        Slot slot = findHoveredSlot(gui, stack);
        if (slot == null || !(slot instanceof SlotAuctionDisplay)) return;

        ContainerAuctionListing container = gui.getListingContainer();
        if (container == null) return;

        int displayIndex = slot.getSlotIndex();
        AuctionListing listing = container.getListingAt(displayIndex);
        if (listing == null) return;

        tooltip.add("");

        // Seller
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.seller")
            .replace("%s", EnumChatFormatting.WHITE + listing.sellerName));

        // Current bid or starting price
        if (listing.hasBids()) {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.currentBid")
                .replace("%s", EnumChatFormatting.GOLD + AuctionFormatUtil.formatCurrencyWithName(listing.currentBid)));
            tooltip.add(EnumChatFormatting.GRAY + String.format(StatCollector.translateToLocal("auction.bids"), listing.bidCount));
        } else {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.startingPrice")
                .replace("%s", EnumChatFormatting.GOLD + AuctionFormatUtil.formatCurrencyWithName(listing.startingPrice)));
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.noBids"));
        }

        // Buyout price
        if (listing.hasBuyout()) {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.buyout")
                .replace("%s", EnumChatFormatting.GREEN + AuctionFormatUtil.formatCurrencyWithName(listing.buyoutPrice)));
        }

        // Time remaining
        long timeRemaining = listing.getTimeRemaining();
        String timeText = StatCollector.translateToLocal("auction.timeLeft")
            .replace("%s", AuctionFormatUtil.formatTimeRemaining(timeRemaining));
        tooltip.add((AuctionFormatUtil.isTimeUrgent(timeRemaining) ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + timeText);
    }

    // ========== Trades Page ==========

    private static void addTradesTooltip(GuiAuctionTrades gui, ItemStack stack, List<String> tooltip) {
        Slot slot = findHoveredSlot(gui, stack);
        if (slot == null || !(slot instanceof SlotAuctionDisplay)) return;

        ContainerAuctionTrades container = gui.getTradesContainer();
        if (container == null) return;

        int displayIndex = slot.getSlotIndex();

        // Check listings first (selling or bidding)
        AuctionListing listing = container.getListingAt(displayIndex);
        if (listing != null) {
            if (container.isSellingAt(displayIndex)) {
                addSellingTooltip(listing, tooltip);
            } else if (container.isBiddingAt(displayIndex)) {
                addBiddingTooltip(listing, tooltip);
            }
            return;
        }

        // Check claims
        AuctionClaim claim = container.getClaimAt(displayIndex);
        if (claim != null) {
            addClaimTooltip(claim, tooltip);
        }
    }

    private static void addSellingTooltip(AuctionListing listing, List<String> tooltip) {
        tooltip.add("");
        tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.selling"));

        if (listing.hasBids()) {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.currentBid")
                .replace("%s", EnumChatFormatting.GOLD + AuctionFormatUtil.formatCurrencyWithName(listing.currentBid)));
            tooltip.add(EnumChatFormatting.GRAY + String.format(StatCollector.translateToLocal("auction.bids"), listing.bidCount));
        } else {
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.noBids"));
        }

        long timeRemaining = listing.getTimeRemaining();
        String timeText = StatCollector.translateToLocal("auction.timeLeft")
            .replace("%s", AuctionFormatUtil.formatTimeRemaining(timeRemaining));
        tooltip.add((AuctionFormatUtil.isTimeUrgent(timeRemaining) ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + timeText);

        tooltip.add("");
        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.rightClickToCancel"));
    }

    private static void addBiddingTooltip(AuctionListing listing, List<String> tooltip) {
        tooltip.add("");
        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.bidding"));

        tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.currentBid")
            .replace("%s", EnumChatFormatting.GOLD + AuctionFormatUtil.formatCurrencyWithName(listing.currentBid)));
        tooltip.add(EnumChatFormatting.GRAY + String.format(StatCollector.translateToLocal("auction.bids"), listing.bidCount));

        long timeRemaining = listing.getTimeRemaining();
        String timeText = StatCollector.translateToLocal("auction.timeLeft")
            .replace("%s", AuctionFormatUtil.formatTimeRemaining(timeRemaining));
        tooltip.add((AuctionFormatUtil.isTimeUrgent(timeRemaining) ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + timeText);

        tooltip.add("");
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.trades.clickToRebid"));
    }

    private static void addClaimTooltip(AuctionClaim claim, List<String> tooltip) {
        tooltip.add("");

        switch (claim.type) {
            case ITEM:
                if (claim.isReturned) {
                    tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("auction.trades.expired"));
                } else {
                    tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.won"));
                }
                break;
            case CURRENCY:
                tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.sold"));
                if (claim.itemName != null && !claim.itemName.isEmpty()) {
                    tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.claim.soldItem")
                        .replace("%s", EnumChatFormatting.WHITE + claim.itemName));
                }
                if (claim.otherPlayerName != null && !claim.otherPlayerName.isEmpty()) {
                    tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.claim.buyer")
                        .replace("%s", EnumChatFormatting.WHITE + claim.otherPlayerName));
                }
                break;
            case REFUND:
                tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("auction.trades.outbid"));
                if (claim.otherPlayerName != null && !claim.otherPlayerName.isEmpty()) {
                    tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.claim.outbidBy")
                        .replace("%s", EnumChatFormatting.WHITE + claim.otherPlayerName));
                }
                break;
        }

        // Amount for currency claims
        if (claim.type.isCurrency()) {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.claim.amount")
                .replace("%s", EnumChatFormatting.GOLD + AuctionFormatUtil.formatCurrencyWithName(claim.currency)));
        }

        // Expiration
        int expirationDays = AuctionClientConfig.getClaimExpirationDays();
        long daysLeft = claim.getDaysUntilExpiration(expirationDays);
        if (daysLeft <= 1) {
            tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("auction.claim.expiresSoon"));
        } else {
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.claim.expires")
                .replace("%s", "" + daysLeft));
        }

        tooltip.add("");

        // Rebid option for REFUND claims with item
        if (claim.type == EnumClaimType.REFUND && claim.item != null) {
            tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.leftClickRebid"));
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.rightClickRefund"));
        } else {
            tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.claim.clickToClaim"));
        }
    }

    // ========== Sell Page ==========

    private static void addSellTooltip(GuiAuctionSell gui, ItemStack stack, List<String> tooltip) {
        Slot slot = findHoveredSlot(gui, stack);
        if (slot == null) return;

        ContainerAuctionSell container = (ContainerAuctionSell) gui.inventorySlots;
        if (container == null) return;

        if (container.isSellSlot(slot.slotNumber)) {
            tooltip.add("");
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.sell.leftClear"));
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.sell.rightRemove"));
        } else if (container.isPlayerInventorySlot(slot.slotNumber)) {
            tooltip.add("");
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.sell.leftAdd"));
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.sell.rightAddOne"));
        }
    }

}
