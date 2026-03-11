# Auction House

---

The **Auction House** is a global player marketplace where players can list items, bid against each other, and purchase instantly. Combined with a built-in currency system, it creates a full economy for your server.

---

## Currency System

CNPC+ has a **built-in currency** used across all market features.

- **Fully Configurable** - Name it Coins, Gold, Credits — whatever fits your world. Set a starting balance and max balance cap
- **Profile Persistent** - Currency carries across all your character slots
- **Vault Support** - Integrates with Bukkit-compatible economy plugins if Vault is installed. Falls back to built-in currency otherwise
- **Lifetime Tracking** - Tracks total earned and total spent per player

> Full scripting API support for currency operations on players.

---

## Selling

List any item with a **Starting Price** for competitive bidding. Optionally set a **Buyout Price** so players can skip the wait and buy immediately. Auctions last **24 hours** by default.

- A configurable **Listing Fee** is charged when creating an auction
- A **Sales Tax** is taken from successful sales
- A configurable **minimum listing price** prevents spam listings

---

## Bidding

Compete for items by placing bids. Each bid must meet a **minimum increment** above the current bid (default 5%, configurable by the server).

- **Snipe Protection** - Auctions extend when last-second bids come in, giving everyone a fair shot
- **Auto Refund** - If you get outbid, your currency is automatically returned

---

## Buyout

See something you want right now? Hit **Buy Now** to purchase instantly at the seller's Buyout Price. If you're already the highest bidder, you only pay the **difference** between your bid and the buyout.

---

## My Trades

One screen to manage all your auction activity:

- **Active Listings** - Items you're currently selling
- **Active Bids** - Auctions you're competing in
- **Ready to Claim** - Won items and currency waiting for pickup

You can **cancel** your own listings — items are returned as a claim. If there are active bids, a configurable **cancellation penalty** is charged (default 10%).

> Claims expire after **20 days**. Collect your items and currency before they're gone!

---

## Search & Sorting

Find items with search and sort tools. Search filters by both **item name** and **seller name** at the same time. Item listings display their **attributes and stats** so you can evaluate items before bidding.

**Sort Options:**
- Newest
- Ending Soon
- Price (Low to High)
- Price (High to Low)
- Most Bids

---

## Notifications

Get notified immediately when:
- Your item **sells**
- You get **outbid**
- You **win** an auction
- Your auction **expires** with no bids
- A **claim is ready** for pickup

If you're offline, you'll get a full **claim summary** when you log back in.

---

## Trade Slots

Every player gets a limited number of **Trade Slots** (default 8). Slots are shared across Active Listings, Active Bids, **and** Pending Claims — uncollected claims take up space. Pick up your claims to free slots!

> Server owners can grant more slots via permissions (up to 45) using `customnpcs.auction.trades.X` (where X is the number of slots), or grant **unlimited slots** with `customnpcs.auction.trades.*`.

---

## Item Blacklist

Server owners control what items can be listed on the Auction House.

**Block by:**
- **Item** - Specific registry names like `minecraft:diamond_sword`
- **Wildcard** - Patterns like `minecraft:diamond*` to block all diamond items
- **Mod** - Block all items from a specific mod
- **NBT Tag** - Block items containing certain NBT data

> **Soulbound** and **Profile-Slotbound** items are always blocked automatically. Bound items cannot be traded.

**Blacklist Commands:**
- `/kam auction blacklist add <item|mod|nbt> <value>` - Add to the blacklist
- `/kam auction blacklist remove <item|mod|nbt> <value>` - Remove from the blacklist
- `/kam auction blacklist list [item|mod|nbt]` - View blacklisted entries
- `/kam auction blacklist reload` - Reload from config
- `/kam auction blacklist check` - Check if your held item is blacklisted

---

## Auctioneer Role

A new NPC role that gives players access to the Auction House. Set the Auctioneer role on any NPC and players can interact with them to open the full Auction House — no extra setup needed.

---

## Admin Tools

### Admin Auction GUI

A management interface for server admins with three tabs:

- **Listings** - View and search all active auctions. Stop or cancel any listing
- **Create** - Create server-managed listings with custom seller names, prices, and durations
- **Claims** - Manage the global admin claims pool

### Commands

**Player Commands:**
- `/kam auction open` - Open the Auction House GUI

**Admin Commands:**
- `/kam auction list [page]` - View active auctions in chat
- `/kam auction view <player>` - View a player's auction activity

> Server owners can enable **auction logging** to track all activity — creates, bids, buyouts, sales, expirations, cancellations, and claims. Each log type can be toggled individually.

---

## Scripting API

All auction events are **cancelable**, letting scripts intercept and control behavior:

- **CreateEvent** - Before an auction is created
- **BidEvent** - Before a bid is placed
- **BuyoutEvent** - Before an instant purchase
- **CancelEvent** - Before a listing is cancelled
- **ClaimEvent** - Before a claim is collected

---
