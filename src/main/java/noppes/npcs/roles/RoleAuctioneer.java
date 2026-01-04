package noppes.npcs.roles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.config.ConfigMarket;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.AuctionController;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * NPC role for Auctioneer - provides access to the global auction house.
 */
public class RoleAuctioneer extends RoleInterface {

    // Configuration
    public boolean showActiveListings = true;      // Show active listings tab
    public boolean showMyListings = true;          // Show player's listings tab
    public boolean showMyBids = true;              // Show player's bids tab
    public boolean showClaims = true;              // Show claimable items tab
    public boolean allowCreatingListings = true;   // Allow creating new listings
    public boolean allowBidding = true;            // Allow bidding on items
    public boolean allowBuyout = true;             // Allow buyout purchases

    // Custom greeting messages
    public String welcomeMessage = "";
    public String noAuctionMessage = "";

    public RoleAuctioneer(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("ShowActiveListings", showActiveListings);
        compound.setBoolean("ShowMyListings", showMyListings);
        compound.setBoolean("ShowMyBids", showMyBids);
        compound.setBoolean("ShowClaims", showClaims);
        compound.setBoolean("AllowCreatingListings", allowCreatingListings);
        compound.setBoolean("AllowBidding", allowBidding);
        compound.setBoolean("AllowBuyout", allowBuyout);
        compound.setString("WelcomeMessage", welcomeMessage);
        compound.setString("NoAuctionMessage", noAuctionMessage);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        showActiveListings = compound.hasKey("ShowActiveListings") ?
            compound.getBoolean("ShowActiveListings") : true;
        showMyListings = compound.hasKey("ShowMyListings") ?
            compound.getBoolean("ShowMyListings") : true;
        showMyBids = compound.hasKey("ShowMyBids") ?
            compound.getBoolean("ShowMyBids") : true;
        showClaims = compound.hasKey("ShowClaims") ?
            compound.getBoolean("ShowClaims") : true;
        allowCreatingListings = compound.hasKey("AllowCreatingListings") ?
            compound.getBoolean("AllowCreatingListings") : true;
        allowBidding = compound.hasKey("AllowBidding") ?
            compound.getBoolean("AllowBidding") : true;
        allowBuyout = compound.hasKey("AllowBuyout") ?
            compound.getBoolean("AllowBuyout") : true;
        welcomeMessage = compound.getString("WelcomeMessage");
        noAuctionMessage = compound.getString("NoAuctionMessage");
    }

    @Override
    public void interact(EntityPlayer player) {
        // Check if auction system is enabled
        if (!ConfigMarket.AuctionEnabled) {
            if (!noAuctionMessage.isEmpty()) {
                npc.say(player, new Line(noAuctionMessage));
            } else {
                npc.say(player, new Line("The auction house is currently closed."));
            }
            return;
        }

        // Process expired auctions before opening GUI
        if (AuctionController.Instance != null) {
            AuctionController.Instance.processExpiredAuctions();
        }

        // Say welcome message
        if (!welcomeMessage.isEmpty()) {
            npc.say(player, new Line(welcomeMessage));
        } else {
            npc.say(player, npc.advanced.getInteractLine());
        }

        // Open auction GUI
        NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerAuction, npc);
    }

    /**
     * Check if player has claims waiting
     */
    public boolean hasClaimsWaiting(EntityPlayer player) {
        if (AuctionController.Instance == null) {
            return false;
        }
        return !AuctionController.Instance.getClaimableListings(player.getUniqueID()).isEmpty();
    }

    /**
     * Get number of active listings in the auction house
     */
    public int getActiveListingCount() {
        if (AuctionController.Instance == null) {
            return 0;
        }
        return AuctionController.Instance.getActiveListingCount();
    }

    /**
     * Get player's current listing count
     */
    public int getPlayerListingCount(EntityPlayer player) {
        if (AuctionController.Instance == null) {
            return 0;
        }
        return AuctionController.Instance.getActiveListingsCount(player.getUniqueID());
    }

    /**
     * Get player's maximum allowed listings
     */
    public int getPlayerMaxListings(EntityPlayer player) {
        if (AuctionController.Instance == null) {
            return ConfigMarket.DefaultMaxListings;
        }
        return AuctionController.Instance.getMaxListings(player);
    }
}
