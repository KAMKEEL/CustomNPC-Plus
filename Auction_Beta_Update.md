# CNPC+ 1.11 Beta - Market Update

## Auction Features: ⚒️

- **Built-in Currency System:**
> Fully configurable economy currency used across all Market features. Profile persistent across character slots with Vault support for Bukkit-compatible plugins.

- **Auction House:**
> Global player marketplace for listing items, bidding, and instant buyouts. Auctions last 24 hours by default.

- **Auction Bidding & Snipe Protection:**
> Competitive bidding with a 5% minimum bid increment. Snipe Protection extends auctions on last-second bids. Outbid players are automatically refunded.

- **Buyout System:**
> Instant purchase at the seller's Buyout Price. Highest bidder only pays the difference between their bid and the buyout.

- **My Trades:**
> Single screen for managing Active Listings, Active Bids, and Pending Claims. Players can cancel listings with a 10% penalty if bids exist.

- **Auction Notifications:**
> Real-time notifications for sales, outbids, and wins. Offline players receive a claim summary on login.

- **Trade Slots:**
> Limited slots (default 8, max 45) shared across listings, bids, and claims. Configurable via permissions.

- **Claim Expiration:**
> Uncollected claims expire after 20 days.

- **Item Blacklist:**
> Block items by registry name, wildcard pattern, mod, or NBT tag. Soulbound and Profile-Slotbound items are always blocked automatically.

- **Listing Fee & Sales Tax:**
> Configurable listing fee charged on creation and sales tax taken from successful sales.

- **Search & Sorting:**
> Search by item name or seller. Sort by newest, ending soon, price, or most bids.

- **Trader Stock System:**
> Limited stock with Global or Per-Player modes. Restock timers for MC Daily/Weekly, RL Daily/Weekly, or Custom intervals.

- **Trader Currency Trades:**
> Trades can require currency on top of item costs.

- **Auctioneer NPC Role:**
> New role that opens the Auction House on interaction. No configuration needed.

- **Auction Commands:**
> `/kam auction open` for players. Admin commands for listing view and player activity inspection.

**Extras**
- Auction logging for server admins (tracks creates, bids, buyouts, sales, expirations, cancellations, claims).
- Lifetime currency tracking per player (total earned and spent).
- Configurable starting balance and max balance cap.
- Blacklist bypass permission for admins.
