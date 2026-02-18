package noppes.npcs.scripted.event.player;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.IAuctionEvent;
import noppes.npcs.api.handler.data.IAuctionClaim;
import noppes.npcs.api.handler.data.IAuctionListing;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;

public class AuctionEvent extends PlayerEvent implements IAuctionEvent {

    public AuctionEvent(IPlayer player) {
        super(player);
    }

    @Override
    public String getHookName() {
        return "auctionEvent";
    }

    @Cancelable
    public static class CreateEvent extends AuctionEvent implements IAuctionEvent.CreateEvent {
        public final IItemStack item;
        public final long startingPrice;
        public final long buyoutPrice;

        public CreateEvent(IPlayer player, IItemStack item, long startingPrice, long buyoutPrice) {
            super(player);
            this.item = item;
            this.startingPrice = startingPrice;
            this.buyoutPrice = buyoutPrice;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.AUCTION_CREATE.function;
        }

        @Override
        public IItemStack getItem() {
            return item;
        }

        @Override
        public long getStartingPrice() {
            return startingPrice;
        }

        @Override
        public long getBuyoutPrice() {
            return buyoutPrice;
        }
    }

    @Cancelable
    public static class BidEvent extends AuctionEvent implements IAuctionEvent.BidEvent {
        public final IAuctionListing listing;
        public final long bidAmount;

        public BidEvent(IPlayer player, IAuctionListing listing, long bidAmount) {
            super(player);
            this.listing = listing;
            this.bidAmount = bidAmount;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.AUCTION_BID.function;
        }

        @Override
        public IAuctionListing getListing() {
            return listing;
        }

        @Override
        public long getBidAmount() {
            return bidAmount;
        }
    }

    @Cancelable
    public static class BuyoutEvent extends AuctionEvent implements IAuctionEvent.BuyoutEvent {
        public final IAuctionListing listing;

        public BuyoutEvent(IPlayer player, IAuctionListing listing) {
            super(player);
            this.listing = listing;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.AUCTION_BUYOUT.function;
        }

        @Override
        public IAuctionListing getListing() {
            return listing;
        }
    }

    @Cancelable
    public static class CancelEvent extends AuctionEvent implements IAuctionEvent.CancelEvent {
        public final IAuctionListing listing;
        public final boolean admin;

        public CancelEvent(IPlayer player, IAuctionListing listing, boolean isAdmin) {
            super(player);
            this.listing = listing;
            this.admin = isAdmin;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.AUCTION_CANCEL.function;
        }

        @Override
        public IAuctionListing getListing() {
            return listing;
        }

        @Override
        public boolean isAdmin() {
            return admin;
        }
    }

    @Cancelable
    public static class ClaimEvent extends AuctionEvent implements IAuctionEvent.ClaimEvent {
        public final IAuctionClaim claim;

        public ClaimEvent(IPlayer player, IAuctionClaim claim) {
            super(player);
            this.claim = claim;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.AUCTION_CLAIM.function;
        }

        @Override
        public IAuctionClaim getClaim() {
            return claim;
        }
    }
}
