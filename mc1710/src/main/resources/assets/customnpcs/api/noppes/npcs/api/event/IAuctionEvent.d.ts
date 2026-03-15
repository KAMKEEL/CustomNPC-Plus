/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.event
 */

/**
 * Events related to auction actions.
 * These events are fired when players interact with the auction system.
  * @javaFqn noppes.npcs.api.event.IAuctionEvent
*/
export interface IAuctionEvent extends import('./IPlayerEvent').IPlayerEvent {
}

export namespace IAuctionEvent {
    /**
     * Fired before a player creates a new auction listing.
     * Cancel to prevent the listing from being created.
     * The listing fee has NOT yet been deducted when this fires.
     *
     * @hookName auctionCreate
          * @javaFqn noppes.npcs.api.event.IAuctionEvent.CreateEvent
*/
    export interface CreateEvent extends IAuctionEvent {
        getItem(): import('../item/IItemStack').IItemStack;
        getStartingPrice(): import('./long').long;
        getBuyoutPrice(): import('./long').long;
        readonly item: import('../item/IItemStack').IItemStack;
        readonly startingPrice: import('./long').long;
        readonly buyoutPrice: import('./long').long;
    }
    /**
     * Fired before a player places a bid on an auction.
     * Cancel to prevent the bid from being placed.
     * Currency has NOT yet been deducted when this fires.
     *
     * @hookName auctionBid
          * @javaFqn noppes.npcs.api.event.IAuctionEvent.BidEvent
*/
    export interface BidEvent extends IAuctionEvent {
        getListing(): import('../handler/data/IAuctionListing').IAuctionListing;
        getBidAmount(): import('./long').long;
        readonly listing: import('../handler/data/IAuctionListing').IAuctionListing;
        readonly bidAmount: import('./long').long;
    }
    /**
     * Fired before a player buys out an auction.
     * Cancel to prevent the buyout.
     * Currency has NOT yet been deducted when this fires.
     *
     * @hookName auctionBuyout
          * @javaFqn noppes.npcs.api.event.IAuctionEvent.BuyoutEvent
*/
    export interface BuyoutEvent extends IAuctionEvent {
        getListing(): import('../handler/data/IAuctionListing').IAuctionListing;
        readonly listing: import('../handler/data/IAuctionListing').IAuctionListing;
    }
    /**
     * Fired before a player cancels an auction listing.
     * Cancel to prevent the cancellation.
     *
     * @hookName auctionCancel
          * @javaFqn noppes.npcs.api.event.IAuctionEvent.CancelEvent
*/
    export interface CancelEvent extends IAuctionEvent {
        getListing(): import('../handler/data/IAuctionListing').IAuctionListing;
        isAdmin(): import('./boolean').boolean;
        readonly listing: import('../handler/data/IAuctionListing').IAuctionListing;
        readonly admin: import('./boolean').boolean;
    }
    /**
     * Fired before a player claims an item or currency from an auction.
     * Cancel to prevent the claim.
     *
     * @hookName auctionClaim
          * @javaFqn noppes.npcs.api.event.IAuctionEvent.ClaimEvent
*/
    export interface ClaimEvent extends IAuctionEvent {
        getClaim(): import('../handler/data/IAuctionClaim').IAuctionClaim;
        readonly claim: import('../handler/data/IAuctionClaim').IAuctionClaim;
    }
}
