# CustomNPC+ 1.11 Market Update - Implementation Status

## Overview

This document tracks the implementation status of the CustomNPC+ 1.11 Market Update, which includes:

1. **Auction House** - Global auction system with bidding, buyout, and claims
2. **Currency System** - Built-in currency with VaultAPI integration for Bukkit hybrid servers
3. **Trader Enhancements** - Stock/inventory system with reset timers

---

## Implementation Status Summary

| Phase | Feature | Status |
|-------|---------|--------|
| Phase 1 | Currency System Foundation | **NOT STARTED** |
| Phase 2 | Trader Enhancements | **COMPLETED** |
| Phase 3 | Item Attributes & Restrictions | **COMPLETED** |
| Phase 4 | Auction House Core | **COMPLETED** |
| Phase 5 | Network Packets & Permissions | **COMPLETED** |
| Phase 6 | Auction GUI & Notifications | **COMPLETED** |
| Phase 7 | Admin Management | **NOT STARTED** |

---

## Completed Features

### Phase 2: Trader Enhancements (COMPLETED)
- `EnumStockReset.java` - Stock reset timing options
- `TraderStock.java` - Stock tracking with per-player and per-server modes
- Modified `RoleTrader.java` with stock and currency support
- Modified `ContainerNPCTrader.java` for stock/currency transactions
- Stock persistence via NBT

### Phase 3: Item Attributes & Restrictions (COMPLETED)
- `ItemTradeAttribute.java` - Untradeable, ProfileSlot Bound, Soulbound checks
- `AuctionBlacklist.java` - Item/mod/NBT blacklist system
- Integration with AuctionListing validation

### Phase 4: Auction House Core (COMPLETED)
- `EnumAuctionDuration.java` - Configurable durations with lang keys
- `EnumAuctionStatus.java` - Auction state management
- `EnumAuctionSort.java` - Sort options with lang keys
- `AuctionListing.java` - Complete listing data structure with bidding logic
- `AuctionFilter.java` - Search/filter criteria (categories removed)
- `AuctionController.java` - Consolidated controller with:
  - Listing creation/management
  - Bidding and buyout logic
  - Claim system for items and currency refunds
  - Inline logging (via LogWriter)
  - Inline player notifications (via chat messages)
  - Pending notification queue for offline players
  - Snipe protection
  - Sales tax and listing fees
- `AuctionClaim.java` - Claim data for items/money
- `RoleAuctioneer.java` - Auctioneer NPC role
- `ContainerAuction.java` - Auction container
- `ConfigMarket.java` - All market configuration options

### Phase 5: Network Packets & Permissions (COMPLETED)
- `AuctionActionPacket.java` - Client-to-server auction actions
- `AuctionDataPacket.java` - Server-to-client data sync
- Packet registration in `PacketHandler.java`
- Permission nodes added to `CustomNpcsPermissions.java`
- API methods added to `ICustomNpc.java`

### Phase 6: Auction GUI & Notifications (COMPLETED)
- `GuiAuction.java` - Main auction browser with tabs
- `SubGuiAuctionCreate.java` - Create listing sub-GUI
- `SubGuiAuctionBid.java` - Place bid sub-GUI
- `SubGuiAuctionDetails.java` - View listing details sub-GUI
- All GUI strings use lang keys via `StatCollector.translateToLocal()`
- Localization keys added to `en_US.lang`

---

## Removed/Changed Features

### Removed: Category System
Originally planned to have item categories (Weapons, Armor, Tools, etc.) for filtering. This was **removed** to simplify the initial implementation:
- Deleted `EnumAuctionCategory.java`
- Removed category field from `AuctionListing.java`
- Removed category filtering from `AuctionFilter.java`
- Removed category buttons from `GuiAuction.java`

**Rationale:** Categories can be added in a future update. Current implementation uses search-based filtering.

### Consolidated: Controller Architecture
Originally planned separate controllers:
- ~~`AuctionLogger.java`~~ - **CONSOLIDATED** into `AuctionController.logEvent()`
- ~~`AuctionNotificationController.java`~~ - **CONSOLIDATED** into `AuctionController.notifyPlayer()`

**Rationale:** Reduces complexity, all auction logic in one place.

### Changed: Localization Approach
- All client GUI strings now use `StatCollector.translateToLocal()` with lang keys
- `EnumAuctionSort` uses lang keys for display names
- `EnumAuctionDuration` uses lang keys for display names

---

## Not Yet Implemented

### Phase 1: Currency System (NOT STARTED)
Requires implementation:
- [ ] `VaultHelper.java` - VaultAPI integration via reflection
- [ ] `CurrencyController.java` - Currency management
- [ ] `PlayerCurrencyData.java` - Per-player currency (shared across profile slots)
- [ ] Vault dependency in build files
- [ ] Currency configuration GUI

**Note:** Currently auctions use `long` for prices but the actual currency deduction/deposit is not connected to any economy system.

### Phase 7: Admin Management (NOT STARTED)
Requires implementation:
- [ ] `GuiAuctionAdmin.java` - Admin auction management GUI
- [ ] `GuiAuctionBlacklist.java` - Blacklist management GUI
- [ ] Admin methods in `AuctionController.java` (adminCancelListing, adminEditListing, etc.)
- [ ] Audit logging for admin actions
- [ ] `/cnpc auction admin` command

### Additional Missing Features
- [ ] Auctioneer NPC setup GUI (`GuiNpcAuctioneerSetup.java`)
- [ ] Trader stock reset timer banner display
- [ ] Comprehensive testing
- [ ] Player balance display (requires Currency System)

---

## Configuration Reference

All settings are in `ConfigMarket.java`:

```java
// Currency
public static String CurrencyName = "Gold";

// Auction Durations (in hours)
public static int AuctionDurationShort = 12;
public static int AuctionDurationMedium = 24;
public static int AuctionDurationLong = 48;
public static int AuctionDurationVeryLong = 72;

// Auction Fees (per duration)
public static long AuctionFeeShort = 10;
public static long AuctionFeeMedium = 25;
public static long AuctionFeeLong = 50;
public static long AuctionFeeVeryLong = 100;

// Listing Configuration
public static int DefaultMaxListings = 5;
public static double ListingFeePercent = 0.0;
public static double SalesTaxPercent = 0.05;
public static double MinBidIncrementPercent = 0.05;
public static int ClaimExpirationDays = 30;
public static boolean RequireAuctioneerNPC = true;

// Snipe Protection
public static int SnipeProtectionMinutes = 5;
public static int SnipeProtectionThreshold = 5;
```

---

## File Inventory

### New Files Created

| File | Purpose |
|------|---------|
| `constants/EnumAuctionDuration.java` | Auction duration options |
| `constants/EnumAuctionSort.java` | Sort options with lang keys |
| `constants/EnumAuctionStatus.java` | Auction state enum |
| `constants/EnumStockReset.java` | Trader stock reset types |
| `controllers/AuctionController.java` | Main auction controller |
| `controllers/AuctionBlacklist.java` | Item blacklist system |
| `controllers/data/AuctionListing.java` | Auction listing data |
| `controllers/data/AuctionFilter.java` | Search/filter criteria |
| `controllers/data/AuctionClaim.java` | Claim data structure |
| `controllers/data/TraderStock.java` | Trader stock system |
| `controllers/data/attribute/ItemTradeAttribute.java` | Trade restriction attributes |
| `roles/RoleAuctioneer.java` | Auctioneer NPC role |
| `containers/ContainerAuction.java` | Auction GUI container |
| `client/gui/player/GuiAuction.java` | Main auction GUI |
| `client/gui/player/SubGuiAuctionCreate.java` | Create listing GUI |
| `client/gui/player/SubGuiAuctionBid.java` | Place bid GUI |
| `client/gui/player/SubGuiAuctionDetails.java` | Listing details GUI |
| `config/ConfigMarket.java` | Market configuration |
| `network/packets/player/AuctionActionPacket.java` | Auction action packet |
| `network/packets/data/AuctionDataPacket.java` | Auction data sync packet |

### Files Deleted
| File | Reason |
|------|--------|
| ~~`constants/EnumAuctionCategory.java`~~ | Categories removed |
| ~~`controllers/AuctionLogger.java`~~ | Consolidated into AuctionController |
| ~~`controllers/AuctionNotificationController.java`~~ | Consolidated into AuctionController |

### Files Modified

| File | Changes |
|------|---------|
| `CustomNpcs.java` | Initialize AuctionController, AuctionBlacklist |
| `ServerTickHandler.java` | Call AuctionController.onPlayerLogin() |
| `constants/EnumRoleType.java` | Added Auctioneer |
| `constants/EnumGuiType.java` | Added auction GUI types |
| `DataAdvanced.java` | Handle Auctioneer role creation |
| `roles/RoleTrader.java` | Added stock system |
| `containers/ContainerNPCTrader.java` | Stock handling |
| `CustomNpcsPermissions.java` | Auction permission nodes |
| `api/ICustomNpc.java` | Auction API methods |
| `PacketHandler.java` | Register auction packets |
| `en_US.lang` | All auction lang keys |

---

## Localization Keys Added

```properties
# Main GUI
auction.title=Auction House
auction.browse=Browse
auction.myListings=My Listings
auction.myBids=My Bids
auction.claims=Claims
auction.sell=Sell
auction.search=Search
auction.refresh=Refresh
auction.viewDetails=View Details
auction.placeBid=Place Bid
auction.buyout=Buyout
auction.cancel=Cancel
auction.prevPage=< Prev
auction.nextPage=Next >
auction.noResults=No results
auction.page=Page %d of %d
auction.balance=Balance: %s
auction.listings=%d listings

# Tab Titles
auction.browseTitle=Browse Auctions
auction.myListingsTitle=My Listings
auction.myBidsTitle=My Active Bids
auction.claimsTitle=Claim Items
auction.sellTitle=Create Listing

# Listing Details
auction.item=Item:
auction.seller=Seller:
auction.startingPrice=Starting Price:
auction.buyoutPrice=Buyout Price:
auction.currentPrice=Current Price:
auction.currentBid=Current Bid:
auction.minimumBid=Minimum Bid:
auction.highBidder=High Bidder:
auction.totalBids=Total Bids:
auction.quantity=Quantity:
auction.timeLeft=Time Left:
auction.yourBid=Your Bid:
auction.yourBalance=Your Balance:
auction.ended=Ended
auction.noBidsYet=No bids yet

# Create Listing
auction.createListing=Create Listing
auction.placeItemBelow=Place item in slot below:
auction.noItemSelected=(No item selected)
auction.noBuyoutHint=(0 = no buyout)
auction.duration=Duration:
auction.listingFee=Listing Fee:
auction.create=Create

# Bid GUI
auction.minBid=Min Bid
auction.bidPlus10=+10%
auction.bidPlus25=+25%
auction.insufficientFunds=Insufficient funds!
auction.auctionEnded=Auction has ended!
auction.auctionDetails=Auction Details

# Sort Options
auction.sort.newest=Newest First
auction.sort.oldest=Oldest First
auction.sort.priceHigh=Most Expensive
auction.sort.priceLow=Least Expensive
auction.sort.endingSoon=Ending Soon
auction.sort.mostTime=Most Time Left
auction.sort.nameAZ=Name: A to Z
auction.sort.nameZA=Name: Z to A
auction.sort.mostBids=Most Bids
auction.sort.leastBids=Least Bids

# Duration Options
auction.duration.short=Short (12h)
auction.duration.medium=Medium (24h)
auction.duration.long=Long (48h)
auction.duration.veryLong=Very Long (72h)
```

---

## Design Decisions (Confirmed)

1. **Outbid Refunds** → Go to claims (not direct to balance)
2. **Currency** → Shared across all profile slots per player (NOT slot-bound)
3. **Permissions** → Uses existing CustomNPC+ permission pattern (default: 5 slots)
4. **Categories** → Removed for initial implementation (can add later)
5. **Controller Architecture** → Single consolidated AuctionController
6. **Localization** → All client strings use StatCollector with lang keys
7. **StatCollector Usage** → Safe - only called from client-side GUI code
8. **Claim Expiration** → 30 days default (configurable)
9. **Snipe Protection** → Extend TO 5 minutes when bid placed near end
10. **Logging** → Inline in AuctionController using LogWriter

---

## Next Steps

1. **Currency System** - Implement VaultHelper and CurrencyController to connect actual economy
2. **Admin Management** - Implement admin GUI and commands
3. **Testing** - Comprehensive testing of all auction flows
4. **Auctioneer Setup GUI** - Create NPC configuration interface
5. **Trader Banner** - Add stock reset timer display above trader GUI
