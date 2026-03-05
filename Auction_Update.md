# 🏪 CustomNPC+ 1.11 - Market Update 🏪

---

The **Market Update** brings a complete economy system to CNPC+! Create bustling marketplaces with **Traders** that have limited stock, or let players trade with each other through the brand new **Auction House**!

---

## Currency System 💰

CNPC+ now has a **built-in currency** used across all Market features!

- **Fully Configurable** - Name it Coins, Gold, Credits, whatever fits your world. Set a starting balance and max balance cap to shape your economy
- **Profile Persistent** - Currency carries across all your character slots
- **Vault Support** - Integrates seamlessly with Bukkit-compatible economy plugins if Vault is installed. Falls back to built-in currency otherwise
- **Lifetime Tracking** - Tracks total earned and spent per player

> Full Scripting API support for currency operations on players.

---

## Auction House 🏛️

A **global player marketplace** where players can list items, bid against each other, and purchase instantly!

### Selling

List any item with a **Starting Price** for competitive bidding. Set an optional **Buyout Price** for players who want to skip the wait and buy immediately. Auctions last **24 hours** by default with a configurable **minimum listing price**.

> A configurable **Listing Fee** is charged when creating auctions, and a **Sales Tax** is taken from successful sales.

### Bidding

Compete for items by placing bids! Each bid must be at least **5%** higher than the current bid. **Snipe Protection** extends auctions when last-second bids come in, giving everyone a fair shot. If you get outbid, your currency is **automatically refunded**.

### Buyout

See something you need right now? Hit **Buy Now** to purchase instantly at the seller's Buyout Price. If you're currently the highest bidder on an item, you only pay the **difference** between your bid and the buyout.

### My Trades

One screen to manage all your auction activity:

- **Active Listings** - Items you're currently selling
- **Active Bids** - Auctions you're competing in
- **Ready to Claim** - Won items and currency waiting for pickup

Players can **cancel** their own listings — items are returned as a claim. If there are active bids on the listing, a configurable **cancellation penalty** is charged (default 10%).

> Claims expire after **20 days**. Make sure to collect your items and currency before they're gone!

### Search & Sorting

Find what you're looking for with search and sort tools. Search filters by both **item name** and **seller name** simultaneously.

**Sort Options:**
- Newest
- Ending Soon
- Price (Low to High)
- Price (High to Low)
- Most Bids

### Notifications

Get notified immediately when:
- Your item **sells**
- You get **outbid**
- You **win** an auction
- Your auction **expires** with no bids
- A **claim is ready** for pickup

Offline? You'll get a full **claim summary** when you log back in showing exactly what's waiting for you.

### Trade Slots

Every player gets a limited number of **Trade Slots** (default 8). Slots are shared across Active Listings, Active Bids, **and** Pending Claims — so uncollected claims take up space. Pick up your claims to free slots for new listings and bids!

> Server owners can grant players more slots via permissions up to a maximum of 45, or grant **unlimited slots** with the `customnpcs.auction.trades.unlimited` permission.

---

## Item Blacklist 🚫

Server owners have full control over what items can be listed on the Auction House.

**Block by:**
- **Item** - Specific registry names like `minecraft:diamond_sword`
- **Wildcard** - Patterns like `minecraft:diamond*` to block all diamond items
- **Mod** - Block all items from a specific mod
- **NBT Tag** - Block items containing certain NBT data

> **Soulbound** and **Profile-Slotbound** items are always blocked automatically, regardless of blacklist settings. Bound items cannot be traded!

**Blacklist Commands:**
- `/kam auction blacklist add <item|mod|nbt> <value>` - Add to the blacklist
- `/kam auction blacklist remove <item|mod|nbt> <value>` - Remove from the blacklist
- `/kam auction blacklist list [item|mod|nbt]` - View blacklisted entries
- `/kam auction blacklist reload` - Reload blacklist from config
- `/kam auction blacklist check` - Check if your held item is blacklisted

---

## Trader Enhancements 🛒

### Stock System

Traders now support **limited stock** with automatic restocking!

**Stock Modes:**
- **Global** - All players share the same stock pool
- **Per-Player** - Each player has their own purchase limits

**Restock Timers:**
- **MC Daily / Weekly** - Restock based on Minecraft time
- **RL Daily / Weekly** - Restock based on real-life time
- **Custom** - Set your own interval in MC ticks or real-time

Stock counts, out-of-stock status, and restock timers are all visible directly in the trade interface.

> Admins can manually reset stock or cooldowns via the Trader GUI.

### Currency Trades

Trades can now require **currency on top of items**. Combine material costs with coin costs for balanced economy progression!

---

## Auctioneer Role 🧑‍💼

New NPC role for the Auction House! Set the role on any NPC and players can access the full Auction House by interacting with them — no extra configuration needed. The role respects the global Auction Enabled setting.

---

## Admin Tools 🔨

### Admin Auction GUI

A dedicated management interface for server admins with three tabs:

- **Listings** - View and search all active auctions. Stop or cancel any listing
- **Create** - Create server-managed listings with custom seller names, prices, and durations
- **Claims** - Manage the global admin claims pool

### Auction Commands

**Player Commands:**
- `/kam auction open` - Opens the Auction House GUI

**Admin Commands:**
- `/kam auction list [page]` - View active auctions in chat
- `/kam auction view <player>` - View a player's auction activity

> Server owners can enable **auction logging** to track all auction activity — creates, bids, buyouts, sales, expirations, cancellations, and claims. Each log type can be toggled individually.

---

## Scripting API 📜

Full event support for auction interactions. All events are **cancelable**, allowing scripts to intercept and control auction behavior:

- **CreateEvent** - Before an auction is created
- **BidEvent** - Before a bid is placed
- **BuyoutEvent** - Before an instant purchase
- **CancelEvent** - Before a listing is cancelled
- **ClaimEvent** - Before a claim is collected

---
