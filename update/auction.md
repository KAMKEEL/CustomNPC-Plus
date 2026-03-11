# Auction House

---

The **Auction House** is a global player marketplace where players can list items, bid against each other, and purchase instantly. Combined with a built-in currency system, it creates a full economy for your server.

---

## Currency System

CNPC+ has a **built-in currency** — fully configurable name, starting balance, and max cap. Profile-persistent across character slots. Integrates with Vault-compatible economy plugins. Tracks lifetime earned/spent per player. Full scripting API support.

---

## Selling

List items with a **Starting Price** for bidding and an optional **Buyout Price** for instant purchase. Auctions last **24 hours** by default. Configurable listing fee, sales tax, and minimum listing price.

---

## Bidding

Bids must meet a **minimum increment** above the current bid (default 5%, configurable). **Snipe Protection** extends auctions on last-second bids. Outbid currency is automatically refunded.

---

## Buyout

Instant purchase at the seller's Buyout Price. If you're the highest bidder, you only pay the **difference** between your bid and the buyout.

---

## My Trades

Manage all auction activity in one screen: Active Listings, Active Bids, and Ready to Claim. Cancel your own listings (items returned as claims, configurable cancellation penalty if bids exist, default 10%).

> Claims expire after **20 days**.

---

## Search & Sorting

Search by **item name** and **seller name** simultaneously. Listings show item attributes and stats. Sort by: Newest, Ending Soon, Price (Low/High), Most Bids.

---

## Notifications

Instant notifications for: item sold, outbid, auction won, auction expired, claim ready. Offline players receive a **claim summary** on login.

---

## Trade Slots

Players get limited **Trade Slots** (default 8) shared across listings, bids, and pending claims. Expandable via permissions (`customnpcs.auction.trades.X`, up to 45, or `.*` for unlimited).

---

## Item Blacklist

Block items by: specific registry name, wildcard patterns, entire mods, or NBT tags. **Soulbound** and **Profile-Slotbound** items are always blocked.

**Commands:** `/kam auction blacklist add|remove|list|reload|check`

---

## Auctioneer Role

Set the Auctioneer role on any NPC — players interact to open the full Auction House.

---

## Admin Tools

**Admin GUI** with three tabs: Listings (view/search/stop/cancel), Create (server listings with custom seller/prices/durations), Claims (global admin pool).

**Commands:** `/kam auction open` (player), `/kam auction list [page]` and `/kam auction view <player>` (admin). Configurable per-type auction logging.

---

## Scripting API

All auction events are **cancelable**: CreateEvent, BidEvent, BuyoutEvent, CancelEvent, ClaimEvent.

---
