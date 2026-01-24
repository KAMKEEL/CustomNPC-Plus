# Auction Item Blacklist Feature Plan

## Overview

Add the ability to prevent certain items from being listed on the Auction House via a configurable blacklist system.

---

## 1. Blacklist Types

### 1.1 Item ID Blacklist
- Block specific items by registry name (e.g., `minecraft:diamond_sword`, `customnpcs:npcSoulstoneFilled`)
- Supports wildcard patterns (e.g., `minecraft:*_sword` for all swords)

### 1.2 Mod Blacklist
- Block all items from a specific mod (e.g., `appliedenergistics2`, `thaumcraft`)
- Useful for blocking items from tech/magic mods that could disrupt economy

### 1.3 NBT Pattern Blacklist (Advanced)
- Block items with specific NBT tags (e.g., items with "Soulbound" enchant, creative-only items)
- Pattern matching on NBT paths

---

## 2. Configuration

### 2.1 Config File Location
```
config/CustomNPC+/auction_blacklist.cfg
```

### 2.2 Config Structure
```java
// In ConfigMarket.java or separate ConfigAuctionBlacklist.java
public static String[] BlacklistedItems = new String[]{};
public static String[] BlacklistedMods = new String[]{};
public static String[] BlacklistedNBTPatterns = new String[]{};
public static boolean EnableBlacklist = true;
public static boolean ShowBlacklistReason = true;
```

### 2.3 Example Configuration
```cfg
# Items that cannot be listed on the Auction House
S:BlacklistedItems <
    minecraft:bedrock
    minecraft:command_block
    minecraft:barrier
    customnpcs:npcSoulstoneFilled
    customnpcs:npcMoney*
 >

# Mods whose items are completely blocked
S:BlacklistedMods <
    projecte
    equivalentexchange3
 >

# NBT patterns that block items (advanced)
S:BlacklistedNBTPatterns <
    tag.ench[*].id=34
    tag.Unbreakable=1
 >
```

---

## 3. Implementation

### 3.1 New Files
```
controllers/data/AuctionBlacklist.java    - Blacklist checking logic
config/ConfigAuctionBlacklist.java        - Configuration handling
```

### 3.2 AuctionBlacklist.java
```java
public class AuctionBlacklist {
    private static Set<String> blacklistedItems = new HashSet<>();
    private static Set<String> blacklistedMods = new HashSet<>();
    private static List<NBTPattern> blacklistedNBT = new ArrayList<>();

    public static boolean isBlacklisted(ItemStack item) {
        if (item == null) return true;

        // Check item registry name
        String registryName = GameRegistry.findUniqueIdentifierFor(item.getItem()).toString();
        if (matchesPattern(registryName, blacklistedItems)) return true;

        // Check mod
        String modId = GameRegistry.findUniqueIdentifierFor(item.getItem()).modId;
        if (blacklistedMods.contains(modId.toLowerCase())) return true;

        // Check NBT patterns
        if (item.hasTagCompound()) {
            for (NBTPattern pattern : blacklistedNBT) {
                if (pattern.matches(item.getTagCompound())) return true;
            }
        }

        return false;
    }

    public static String getBlacklistReason(ItemStack item) {
        // Returns human-readable reason why item is blocked
    }

    public static void reload() {
        // Reload from config
    }
}
```

### 3.3 Integration Points

#### AuctionController.createListing()
```java
// Add at start of createListing method:
if (AuctionBlacklist.isBlacklisted(item)) {
    if (ConfigMarket.ShowBlacklistReason) {
        return "This item cannot be listed: " + AuctionBlacklist.getBlacklistReason(item);
    }
    return "This item cannot be listed on the Auction House.";
}
```

#### GuiAuctionSell (optional visual feedback)
- Gray out confirm button if item is blacklisted
- Show tooltip explaining why item can't be listed

---

## 4. Commands

### 4.1 Admin Commands
```
/auction blacklist add <item|mod|nbt> <pattern>
/auction blacklist remove <item|mod|nbt> <pattern>
/auction blacklist list
/auction blacklist reload
/auction blacklist check <held|target>
```

### 4.2 Examples
```
/auction blacklist add item minecraft:diamond_block
/auction blacklist add mod projecte
/auction blacklist add nbt tag.ench[*].id=34
/auction blacklist check held
```

---

## 5. Default Blacklist (Recommended)

### 5.1 Vanilla Items
- `minecraft:command_block*`
- `minecraft:barrier`
- `minecraft:bedrock`
- `minecraft:structure_block`
- `minecraft:structure_void`

### 5.2 CustomNPC+ Items
- `customnpcs:npcSoulstoneFilled` (contains NPC data)
- `customnpcs:npcWand` (admin tool)
- `customnpcs:npcMobCloner` (admin tool)
- `customnpcs:npcScripter` (admin tool)

---

## 6. Future Considerations

### 6.1 Whitelist Mode
- Invert the blacklist to only allow specific items
- Useful for strict economy servers

### 6.2 Category-Based Blocking
- Block by item category (weapons, tools, armor, etc.)
- Integration with Minecraft's creative tabs

### 6.3 Price Restrictions
- Set minimum/maximum prices for specific items
- Prevent price manipulation on valuable items

### 6.4 Per-Player Overrides
- Permission to bypass blacklist: `customnpcs.auction.blacklist.bypass`
- Admin listing capability

---

## 7. Files to Create/Modify

### New Files
```
config/ConfigAuctionBlacklist.java
controllers/data/AuctionBlacklist.java
```

### Modified Files
```
AuctionController.java          - Add blacklist check in createListing
AuctionCommand.java             - Add blacklist subcommands
en_US.lang                      - Add blacklist messages
1.11_Market_Update.md           - Document feature
```

---

## 8. Estimated Scope

| Component | Complexity | Priority |
|-----------|------------|----------|
| Basic item blacklist | Low | High |
| Mod blacklist | Low | High |
| NBT pattern matching | Medium | Medium |
| Admin commands | Low | Medium |
| GUI feedback | Low | Low |
| Whitelist mode | Low | Low |
