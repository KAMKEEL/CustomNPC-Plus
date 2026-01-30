package noppes.npcs.client.gui.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.AuctionClientConfig;
import noppes.npcs.constants.EnumClaimType;
import noppes.npcs.containers.ContainerAuctionListing;
import noppes.npcs.containers.ContainerAuctionSell;
import noppes.npcs.containers.ContainerAuctionTrades;
import noppes.npcs.controllers.data.AuctionClaim;
import noppes.npcs.controllers.data.AuctionListing;

import java.util.List;

/**
 * Handles adding auction information to item tooltips when viewing auction GUIs.
 * Called from MixinItem when Item.addInformation is invoked.
 */
@SideOnly(Side.CLIENT)
public class AuctionTooltipHandler {

    /**
     * Add auction information to an item's tooltip if we're in an auction GUI.
     */
    public static void addAuctionInfo(ItemStack stack, List<String> tooltip) {
        if (stack == null || tooltip == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen == null) return;

        GuiScreen screen = mc.currentScreen;

        // Check if we're in an auction listing GUI
        if (screen instanceof GuiAuctionListing) {
            addListingTooltip((GuiAuctionListing) screen, stack, tooltip);
        }
        // Check if we're in the trades GUI
        else if (screen instanceof GuiAuctionTrades) {
            addTradesTooltip((GuiAuctionTrades) screen, stack, tooltip);
        }
        // Check if we're in the sell GUI
        else if (screen instanceof GuiAuctionSell) {
            addSellTooltip((GuiAuctionSell) screen, stack, tooltip);
        }
    }

    /**
     * Add auction info when viewing the main listings page.
     */
    private static void addListingTooltip(GuiAuctionListing gui, ItemStack stack, List<String> tooltip) {
        ContainerAuctionListing container = (ContainerAuctionListing) gui.inventorySlots;
        if (container == null) return;

        // Find the listing that matches this ItemStack
        AuctionListing listing = container.getListingForItem(stack);
        if (listing == null) return;

        tooltip.add("");

        // Seller
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.seller")
            .replace("%s", EnumChatFormatting.WHITE + listing.sellerName));

        // Current bid or starting price
        if (listing.hasBids()) {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.currentBid")
                .replace("%s", EnumChatFormatting.GOLD + formatCurrency(listing.currentBid)));
            tooltip.add(EnumChatFormatting.GRAY + String.format(StatCollector.translateToLocal("auction.bids"), listing.bidCount));
        } else {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.startingPrice")
                .replace("%s", EnumChatFormatting.GOLD + formatCurrency(listing.startingPrice)));
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.noBids"));
        }

        // Buyout price
        if (listing.hasBuyout()) {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.buyout")
                .replace("%s", EnumChatFormatting.GREEN + formatCurrency(listing.buyoutPrice)));
        }

        // Time remaining
        long timeRemaining = listing.getTimeRemaining();
        String timeText = StatCollector.translateToLocal("auction.timeLeft")
            .replace("%s", formatTimeRemaining(timeRemaining));
        tooltip.add((timeRemaining < 3600000 ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + timeText);
    }

    /**
     * Add auction info when viewing the trades page.
     */
    private static void addTradesTooltip(GuiAuctionTrades gui, ItemStack stack, List<String> tooltip) {
        ContainerAuctionTrades container = (ContainerAuctionTrades) gui.inventorySlots;
        if (container == null) return;

        // Check if it's an item I'm selling
        AuctionListing sellingListing = container.getSellingListingForItem(stack);
        if (sellingListing != null) {
            tooltip.add("");
            tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.selling"));

            if (sellingListing.hasBids()) {
                tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.currentBid")
                    .replace("%s", EnumChatFormatting.GOLD + formatCurrency(sellingListing.currentBid)));
                tooltip.add(EnumChatFormatting.GRAY + String.format(StatCollector.translateToLocal("auction.bids"), sellingListing.bidCount));
            } else {
                tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.noBids"));
            }

            long timeRemaining = sellingListing.getTimeRemaining();
            String timeText = StatCollector.translateToLocal("auction.timeLeft")
                .replace("%s", formatTimeRemaining(timeRemaining));
            tooltip.add((timeRemaining < 3600000 ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + timeText);

            tooltip.add("");
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.rightClickToCancel"));
            return;
        }

        // Check if it's an item I'm bidding on
        AuctionListing biddingListing = container.getBiddingListingForItem(stack);
        if (biddingListing != null) {
            tooltip.add("");
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.bidding"));

            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.currentBid")
                .replace("%s", EnumChatFormatting.GOLD + formatCurrency(biddingListing.currentBid)));
            tooltip.add(EnumChatFormatting.GRAY + String.format(StatCollector.translateToLocal("auction.bids"), biddingListing.bidCount));

            long timeRemaining = biddingListing.getTimeRemaining();
            String timeText = StatCollector.translateToLocal("auction.timeLeft")
                .replace("%s", formatTimeRemaining(timeRemaining));
            tooltip.add((timeRemaining < 3600000 ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + timeText);

            tooltip.add("");
            tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.trades.clickToRebid"));
            return;
        }

        // Check if it's a claim
        AuctionClaim claim = container.getClaimForItem(stack);
        if (claim != null) {
            tooltip.add("");

            // Title based on claim type
            switch (claim.type) {
                case ITEM:
                    if (claim.isReturned) {
                        // Expired - returned to seller
                        tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("auction.trades.expired"));
                    } else {
                        // Won item
                        tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.won"));
                    }
                    break;
                case CURRENCY:
                    // Sold - money to claim
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
                    // Outbid - refund available
                    tooltip.add(EnumChatFormatting.RED + StatCollector.translateToLocal("auction.trades.outbid"));
                    if (claim.otherPlayerName != null && !claim.otherPlayerName.isEmpty()) {
                        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.claim.outbidBy")
                            .replace("%s", EnumChatFormatting.WHITE + claim.otherPlayerName));
                    }
                    break;
            }

            // Amount for currency claims (including REFUND)
            if (claim.type.isCurrency()) {
                tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.claim.amount")
                    .replace("%s", EnumChatFormatting.GOLD + formatCurrency(claim.currency)));
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

            // Show rebid option for REFUND claims with item (active auction)
            if (claim.type == EnumClaimType.REFUND && claim.item != null) {
                tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.trades.leftClickRebid"));
                tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.rightClickRefund"));
            } else {
                tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.claim.clickToClaim"));
            }
        }
    }

    /**
     * Add sell page tooltip info.
     */
    private static void addSellTooltip(GuiAuctionSell gui, ItemStack stack, List<String> tooltip) {
        ContainerAuctionSell container = (ContainerAuctionSell) gui.inventorySlots;
        if (container == null) return;

        // Check if this is the sell slot item
        ItemStack sellItem = container.getItemToSell();
        if (sellItem != null && ItemStack.areItemStacksEqual(stack, sellItem)) {
            // This is the item in the sell slot
            tooltip.add("");
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.sell.leftClear"));
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.sell.rightRemove"));
            return;
        }

        // This is an inventory item - show add controls
        tooltip.add("");
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.sell.leftAdd"));
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.sell.rightAddOne"));
    }

    /**
     * Format currency with commas.
     */
    private static String formatCurrency(long amount) {
        if (amount < 1000) {
            return amount + " " + AuctionClientConfig.getCurrencyName();
        }
        StringBuilder sb = new StringBuilder();
        String str = "" + amount;
        int count = 0;
        for (int i = str.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) {
                sb.insert(0, ',');
            }
            sb.insert(0, str.charAt(i));
            count++;
        }
        return sb.toString() + " " + AuctionClientConfig.getCurrencyName();
    }

    /**
     * Format time remaining.
     */
    private static String formatTimeRemaining(long milliseconds) {
        if (milliseconds <= 0) {
            return StatCollector.translateToLocal("auction.ended");
        }

        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / 60000) % 60;
        long hours = (milliseconds / 3600000) % 24;
        long days = milliseconds / 86400000;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m");
        } else {
            sb.append(seconds).append("s");
        }

        return sb.toString();
    }
}
