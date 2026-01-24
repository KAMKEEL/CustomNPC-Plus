package noppes.npcs.client.gui.player;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.AuctionClientConfig;
import noppes.npcs.containers.ContainerAuctionListing;
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

        // Add separator
        tooltip.add("");
        tooltip.add(EnumChatFormatting.DARK_GRAY + "--- " + EnumChatFormatting.GOLD +
            StatCollector.translateToLocal("auction.title") + EnumChatFormatting.DARK_GRAY + " ---");

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

        // Check if it's an active listing
        AuctionListing listing = container.getListingForItem(stack);
        if (listing != null) {
            tooltip.add("");
            tooltip.add(EnumChatFormatting.DARK_GRAY + "--- " + EnumChatFormatting.GOLD +
                StatCollector.translateToLocal("auction.trades.activeListing") + EnumChatFormatting.DARK_GRAY + " ---");

            if (listing.hasBids()) {
                tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.currentBid")
                    .replace("%s", EnumChatFormatting.GOLD + formatCurrency(listing.currentBid)));
                tooltip.add(EnumChatFormatting.GRAY + String.format(StatCollector.translateToLocal("auction.bids"), listing.bidCount));
            } else {
                tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("auction.noBids"));
            }

            long timeRemaining = listing.getTimeRemaining();
            String timeText = StatCollector.translateToLocal("auction.timeLeft")
                .replace("%s", formatTimeRemaining(timeRemaining));
            tooltip.add((timeRemaining < 3600000 ? EnumChatFormatting.RED : EnumChatFormatting.WHITE) + timeText);

            tooltip.add("");
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("auction.trades.rightClickToCancel"));
            return;
        }

        // Check if it's a claim
        AuctionClaim claim = container.getClaimForItem(stack);
        if (claim != null) {
            tooltip.add("");

            String claimTitle;
            switch (claim.type) {
                case ITEM:
                    claimTitle = StatCollector.translateToLocal("auction.claim.item");
                    break;
                case CURRENCY:
                    claimTitle = StatCollector.translateToLocal("auction.claim.currency");
                    break;
                case REFUND:
                    claimTitle = StatCollector.translateToLocal("auction.claim.refund");
                    break;
                default:
                    claimTitle = "Claim";
            }

            tooltip.add(EnumChatFormatting.DARK_GRAY + "--- " + EnumChatFormatting.GOLD +
                claimTitle + EnumChatFormatting.DARK_GRAY + " ---");

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
                    .replace("%s", String.valueOf(daysLeft)));
            }

            tooltip.add("");
            tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("auction.claim.clickToClaim"));
        }
    }

    /**
     * Format currency with commas.
     */
    private static String formatCurrency(long amount) {
        if (amount < 1000) {
            return amount + " " + AuctionClientConfig.getCurrencyName();
        }
        StringBuilder sb = new StringBuilder();
        String str = String.valueOf(amount);
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
