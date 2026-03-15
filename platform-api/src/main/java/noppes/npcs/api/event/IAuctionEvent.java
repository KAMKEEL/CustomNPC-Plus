package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.handler.data.IAuctionClaim;
import noppes.npcs.api.handler.data.IAuctionListing;
import noppes.npcs.api.item.IItemStack;

/**
 * Events related to auction actions.
 * These events are fired when players interact with the auction system.
 */
public interface IAuctionEvent extends IPlayerEvent {

    /**
     * Fired before a player creates a new auction listing.
     * Cancel to prevent the listing from being created.
     * The listing fee has NOT yet been deducted when this fires.
     *
     * @hookName auctionCreate
     */
    @Cancelable
    interface CreateEvent extends IAuctionEvent {
        IItemStack getItem();

        long getStartingPrice();

        long getBuyoutPrice();
    }

    /**
     * Fired before a player places a bid on an auction.
     * Cancel to prevent the bid from being placed.
     * Currency has NOT yet been deducted when this fires.
     *
     * @hookName auctionBid
     */
    @Cancelable
    interface BidEvent extends IAuctionEvent {
        IAuctionListing getListing();

        long getBidAmount();
    }

    /**
     * Fired before a player buys out an auction.
     * Cancel to prevent the buyout.
     * Currency has NOT yet been deducted when this fires.
     *
     * @hookName auctionBuyout
     */
    @Cancelable
    interface BuyoutEvent extends IAuctionEvent {
        IAuctionListing getListing();
    }

    /**
     * Fired before a player cancels an auction listing.
     * Cancel to prevent the cancellation.
     *
     * @hookName auctionCancel
     */
    @Cancelable
    interface CancelEvent extends IAuctionEvent {
        IAuctionListing getListing();

        boolean isAdmin();
    }

    /**
     * Fired before a player claims an item or currency from an auction.
     * Cancel to prevent the claim.
     *
     * @hookName auctionClaim
     */
    @Cancelable
    interface ClaimEvent extends IAuctionEvent {
        IAuctionClaim getClaim();
    }
}
