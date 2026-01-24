package kamkeel.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.AuctionListing;
import noppes.npcs.controllers.data.PlayerData;

import java.util.List;
import java.util.UUID;

import static kamkeel.npcs.util.ColorUtil.sendError;
import static kamkeel.npcs.util.ColorUtil.sendMessage;
import static kamkeel.npcs.util.ColorUtil.sendResult;

public class AuctionCommand extends CommandKamkeelBase {

    @Override
    public String getCommandName() {
        return "auction";
    }

    @Override
    public String getDescription() {
        return "Auction House operations";
    }

    @SubCommand(
        desc = "List active auctions",
        usage = "[page]"
    )
    public void list(ICommandSender sender, String[] args) throws CommandException {
        if (!ConfigMarket.AuctionEnabled) {
            sendError(sender, "Auction system is disabled");
            return;
        }

        AuctionController controller = AuctionController.getInstance();
        if (controller == null) {
            sendError(sender, "Auction system is not available");
            return;
        }

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                sendError(sender, "Invalid page number: " + args[0]);
                return;
            }
        }

        List<AuctionListing> listings = controller.getActiveListings(null, page - 1, 10);
        int total = controller.getTotalActiveListings(null);
        int totalPages = Math.max(1, (int) Math.ceil((double) total / 10));

        if (listings.isEmpty()) {
            sendMessage(sender, "\u00A77No active auctions found");
            return;
        }

        sendMessage(sender, String.format("\u00A76=== Active Auctions (Page %d/%d) ===", page, totalPages));
        for (AuctionListing listing : listings) {
            String itemName = listing.item != null ? listing.item.getDisplayName() : "Unknown";
            String price = listing.hasBids() ?
                String.format("%,d (bid)", listing.currentBid) :
                String.format("%,d (start)", listing.startingPrice);
            sendMessage(sender, String.format("\u00A7e%s \u00A77by \u00A7b%s\u00A77 - \u00A76%s %s",
                itemName, listing.sellerName, price, ConfigMarket.CurrencyName));
        }
    }

    @SubCommand(
        desc = "View player's auction activity",
        usage = "<player>"
    )
    public void view(ICommandSender sender, String[] args) throws CommandException {
        if (!ConfigMarket.AuctionEnabled) {
            sendError(sender, "Auction system is disabled");
            return;
        }

        AuctionController controller = AuctionController.getInstance();
        if (controller == null) {
            sendError(sender, "Auction system is not available");
            return;
        }

        String playername = args[0];
        List<PlayerData> dataList = PlayerDataController.Instance.getPlayersData(sender, playername);

        if (dataList.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        for (PlayerData playerdata : dataList) {
            UUID playerUUID = UUID.fromString(playerdata.uuid);
            List<AuctionListing> activeListings = controller.getPlayerActiveListings(playerUUID);
            List<AuctionListing> activeBids = controller.getPlayerActiveBids(playerUUID);
            int claimCount = controller.getPlayerClaims(playerUUID).size();

            sendMessage(sender, String.format("\u00A76=== Auction Activity for %s ===", playerdata.playername));
            sendMessage(sender, String.format("\u00A77Active Listings: \u00A7e%d", activeListings.size()));
            sendMessage(sender, String.format("\u00A77Active Bids: \u00A7e%d", activeBids.size()));
            sendMessage(sender, String.format("\u00A77Pending Claims: \u00A7e%d", claimCount));

            if (!activeListings.isEmpty()) {
                sendMessage(sender, "\u00A7a--- Selling ---");
                for (AuctionListing listing : activeListings) {
                    String itemName = listing.item != null ? listing.item.getDisplayName() : "Unknown";
                    String price = listing.hasBids() ?
                        String.format("%,d (bid)", listing.currentBid) :
                        String.format("%,d (start)", listing.startingPrice);
                    sendMessage(sender, String.format("  \u00A7e%s \u00A77- \u00A76%s", itemName, price));
                }
            }

            if (!activeBids.isEmpty()) {
                sendMessage(sender, "\u00A7e--- Bidding ---");
                for (AuctionListing bid : activeBids) {
                    String itemName = bid.item != null ? bid.item.getDisplayName() : "Unknown";
                    sendMessage(sender, String.format("  \u00A7e%s \u00A77- \u00A76%,d %s",
                        itemName, bid.currentBid, ConfigMarket.CurrencyName));
                }
            }
        }
    }

    @SubCommand(
        desc = "Open the Auction House GUI",
        usage = "",
        permission = 0
    )
    public void open(ICommandSender sender, String[] args) throws CommandException {
        if (!ConfigMarket.AuctionEnabled) {
            sendError(sender, "Auction system is disabled");
            return;
        }

        if (!(sender instanceof EntityPlayer)) {
            sendError(sender, "This command can only be used by players");
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        // Open the auction GUI without requiring an NPC
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerAuction, null);
        sendResult(sender, "Opening Auction House...");
    }

    @Override
    public List addTabCompletionOptions(ICommandSender par1, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, new String[]{"list", "view", "open"});
        }
        return null;
    }
}
