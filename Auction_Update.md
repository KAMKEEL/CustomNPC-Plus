# 🏪 CustomNPC+ 1.11 - Market Update 🏪

---

The **Market Update** brings a complete economy system to CNPC+! Create bustling marketplaces with **Traders** that have limited stock, or let players trade with each other through the brand new **Auction House**!

---

## Currency System 💰

CNPC+ now has a **built-in currency** used across all Market features!

- **Fully Configurable** - Name it Coins, Gold, Credits, whatever fits your world
- **Profile Persistent** - Currency carries across all your character slots
- **Vault Support** - CNPC+ integrates seamlessly with Bukkit-compatible economy plugins if Vault is installed

> Full Scripting API support for currency operations on players.

---

## Auction House 🏛️

A **global player marketplace** where players can list items, bid against each other, and purchase instantly!

### Selling

List any item with a **Starting Price** for competitive bidding. Set an optional **Buyout Price** for players who want to skip the wait and buy immediately.

> A configurable **Listing Fee** is charged when creating auctions, and a **Sales Tax** is taken from successful sales.

### Bidding

Compete for items by placing bids! **Snipe Protection** extends auctions when last-second bids come in, giving everyone a fair shot. If you get outbid, your currency is **automatically refunded**.

### Buyout

See something you need right now? Hit **Buy Now** to purchase instantly at the seller's Buyout Price. If you're currently the highest bidder on an item, you only pay the **difference** between your bid and the buyout.

### My Trades

One screen to manage all your auction activity:

- **Active Listings** - Items you're currently selling
- **Active Bids** - Auctions you're competing in
- **Ready to Claim** - Won items and currency waiting for pickup

> Players can **cancel** their own listings. Items are returned as a claim.

### Notifications

Get notified immediately when your item sells, when you're outbid, or when you win an auction. Offline? You'll get a full **claim summary** when you log back in showing exactly what's waiting for you.

### Trade Slots

Each player has a limited number of **Trade Slots** (default 8). Trade Slots are shared between Active Listings, Active Bids, and Pending Claims.

> Server owners can grant players more slots via permissions up to a maximum of 45.

---

## Item Blacklist 🚫

Server owners have full control over what items can be listed on the Auction House.

**Block by:**
- **Item** - Specific registry names like `minecraft:diamond_sword`
- **Wildcard** - Patterns like `minecraft:diamond*` to block all diamond items
- **Mod** - Block all items from a specific mod
- **NBT Tag** - Block items containing certain NBT data

> **Soulbound** and **Profile-Slotbound** items are always blocked automatically. Bound items cannot be traded!

**Blacklist Commands:**
- `/kam auction blacklist add <item|mod|nbt> <value>` - Add entry
- `/kam auction blacklist remove <item|mod|nbt> <value>` - Remove entry
- `/kam auction blacklist list` - View all blacklisted entries
- `/kam auction blacklist check` - Check if your held item is blacklisted
- `/kam auction blacklist reload` - Reload from config

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

**Visual Feedback:**
- Stock count displayed next to each trade
- **Out of Stock** overlay when unavailable
- Live **countdown timer** until next restock

> Admins can manually reset stock or cooldowns via the Trader GUI.


### Currency Trades

Trades can now require **currency on top of items**. Combine material costs with coin costs for balanced economy progression!

---

## Auctioneer Role 🧑‍💼

New NPC role for the Auction House! Set the role on any NPC and players can access the full Auction House by interacting with them.
> No configuration needed

### Auction Command

Players can also access the Auction House via command:

- `/kam auction open` - Opens the Auction House GUI

**Admin Commands:**
- `/kam auction list [page]` - View active auctions in chat
- `/kam auction view <player>` - View a player's auction activity

---

