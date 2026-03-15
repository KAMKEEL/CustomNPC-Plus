# Phase 0D Report — Sync Missing Methods from mc1710 Shadows into core/

**Date:** 2026-03-15 (corrected)
**Agent:** Sisyphus-Junior (claude-opus-4.6)

## Summary

Full method-level diff of all 8 core files against their mc1710 shadows. Added missing methods, fixed return types, cleaned TODO/OLD comments. Both `:core:compileJava` and `:mc1710:compileJava` pass.

## Changes Per File

### 1. FactionController.java

**Missing methods added:**
- `public List<Faction> list()` — returns all factions as a list (mc1710 returns `List<IFaction>`)
- `public Faction delete(int id)` — changed from `void` to `Faction` return type, matching mc1710's `IFaction delete(int id)` logic (remove, save, set id=-1, return)

**Cleanup:** Removed all TODO/OLD comments (SyncController notes, DataInputStream notes, LogWriter notes). SyncController calls remain mc1710-only (added by shadow).

**Methods intentionally NOT added (MC-specific, stay in mc1710 shadow):**
- SyncController.syncUpdate/syncRemove calls (network layer)

### 2. MagicController.java

**No missing methods.** Core already had all methods matching mc1710.

**Cleanup:** Removed all TODO/OLD comments (8 blocks: IMagicHandler note, ItemStack defaults, SyncController calls, DataInputStream, CompressedStreamTools, LogWriter). Made `createDefaults()` protected so mc1710 can override to add ItemStack items.

### 3. TransportController.java

**Missing methods added:**
- `public TransportCategory[] categories()` — returns all categories as array
- `public void createCategory(String title)` — delegates to `saveCategory(title, -1)`
- `public TransportCategory getCategory(String title)` — finds category by title
- `public void removeCategory(String title)` — removes category by title

**Cleanup:** Removed all TODO/OLD comments (ITransportHandler note, CompressedStreamTools, NBTWrapper, LogWriter).

**Methods intentionally NOT added (MC-specific):**
- `saveLocation(int categoryId, NBTTagCompound compound, EntityNPCInterface npc)` — uses EntityNPCInterface, RoleTransporter, EnumRoleType

### 4. TagController.java

**Missing methods added:**
- `public List<Tag> list()` — returns all tags as a list (mc1710 returns `List<ITag>`)

**Return type fixed:**
- `public Tag delete(int id)` — changed from `void` to `Tag` return type, matching mc1710's `ITag delete(int id)` logic

**Cleanup:** Removed all TODO/OLD comments (ITagHandler note, DataInputStream, NBTWrapper, CompressedStreamTools, LogWriter).

**Methods intentionally NOT added (MC-specific):**
- `sendCategoryTagMap(EntityPlayerMP, HashMap<String, HashSet<UUID>>)` — uses EntityPlayerMP, GuiDataPacket

### 5. Faction.java (data)

**Missing methods added:**
- `public int playerStatus(IPlayer player)` — returns 1/0/-1 for friendly/neutral/enemy using IPlayerFactionData
- `public boolean isAggressiveToNpc(ICustomNpc npc)` — checks if faction attacks NPC's faction
- `public boolean isEnemyFaction(IFaction faction)` — IFaction-parameter overload
- `public Faction[] getEnemyFactions()` — returns array of enemy factions (core returns `Faction[]`, mc1710 shadows to `IFaction[]`)
- `public void addEnemyFaction(IFaction faction)` — IFaction-parameter overload
- `public void removeEnemyFaction(IFaction faction)` — IFaction-parameter overload
- `public void save()` — delegates to `FactionController.getInstance().saveFaction(this)`

**Imports added:** `ICustomNpc`, `IPlayer`, `IPlayerFactionData`, `IFaction`, `FactionController`, `ArrayList`

**Cleanup:** Removed TODO block listing 9 mc1710-only methods.

**Methods intentionally NOT added (MC-specific, stay in mc1710 shadow):**
- `isFriendlyToPlayer(EntityPlayer)` — uses PlayerData.get(EntityPlayer)
- `isAggressiveToPlayer(EntityPlayer)` — uses PlayerData.get(EntityPlayer)
- `isNeutralToPlayer(EntityPlayer)` — uses PlayerData.get(EntityPlayer)
- `isAggressiveToNpc(EntityNPCInterface)` — uses entity.faction.id
- `isFriendlyToPlayer(IPlayer)` — delegates to EntityPlayer overload via getMCEntity()
- `isNeutralToPlayer(IPlayer)` — delegates to EntityPlayer overload via getMCEntity()
- `isAggressiveToPlayer(IPlayer)` — delegates to EntityPlayer overload via getMCEntity()

### 6. Magic.java (data)

**No missing methods.** Core already had all MC-free methods.

**Cleanup:** Removed 4 TODO/OLD comment blocks (ItemStack field note, GameRegistry notes in readNBT/writeNBT, IMagic setItem/getItem note).

**Methods intentionally NOT added (MC-specific):**
- `public void setItem(ItemStack item)` — uses GameRegistry.findUniqueIdentifierFor
- `public ItemStack getItem()` — returns MC ItemStack

### 7. NBTTags.java

**No missing methods.** All MC-free utility methods were already present.

**Cleanup:** Removed 4 TODO/OLD comment blocks (ItemStack methods, nbtDoubleList, Script methods, getIntAt).

**Methods intentionally NOT added (MC-specific):**
- `getItemStackList/getItemStackArray` — use NoppesUtilServer.readItem
- `nbtItemStackList/nbtItemStackArray` — use NoppesUtilServer.writeItem
- `nbtDoubleList` — uses NBTTagDouble directly
- `GetScriptOld/GetScript/NBTScript` — use IScriptHandler/IScriptUnit
- `getIntAt` — uses NBTTagInt.func_150287_d()

### 8. PlayerEffect.java

**Signature fixed:**
- `performEffect(Object player)` → `performEffect(IPlayer player)` — now matches the `IPlayerEffect` interface default method signature from platform-api

**Import added:** `noppes.npcs.api.entity.IPlayer`

**Cleanup:** Removed TODO/OLD comments from getName() and performEffect().

## Verification

```
:core:compileJava        → BUILD SUCCESSFUL
:mc1710:compileJava      → BUILD SUCCESSFUL
```

## Method Surface Comparison Summary

| File | Core Methods Before | Core Methods After | mc1710 MC-Only Methods |
|------|--------------------|--------------------|----------------------|
| FactionController | 16 | 18 (+list, delete→Faction) | 0 (SyncController calls in shadow) |
| MagicController | 16 | 16 (no change) | 0 |
| TransportController | 14 | 18 (+categories, createCategory, getCategory, removeCategory(String)) | 1 (saveLocation with NBT+Entity) |
| TagController | 16 | 18 (+list, delete→Tag) | 1 (sendCategoryTagMap) |
| Faction | 20 | 27 (+save, playerStatus, isAggressiveToNpc, isEnemyFaction(IF), getEnemyFactions, addEnemyFaction(IF), removeEnemyFaction(IF)) | 7 (EntityPlayer/EntityNPCInterface methods) |
| Magic | 16 | 16 (no change) | 2 (setItem/getItem with ItemStack) |
| NBTTags | 33 | 33 (no change) | 7 (ItemStack, Script, Double, IntAt) |
| PlayerEffect | 10 | 10 (signature fix only) | 0 |

## Design Decisions

1. **`getEnemyFactions()` returns `Faction[]` in core, not `IFaction[]`.** Core's Faction doesn't implement IFaction (mc1710 shadow adds `implements IFaction`). The mc1710 shadow overrides this to return `IFaction[]`.

2. **`list()` returns `List<Faction>` / `List<Tag>` in core**, not `List<IFaction>` / `List<ITag>`. Same reason — the interface implementation is added by the mc1710 shadow.

3. **`delete()` returns concrete type** (`Faction` / `Tag`), not interface type. mc1710 shadow widens to `IFaction` / `ITag`.

4. **`playerStatus(IPlayer)` compiles in core** because `IPlayer.getData().getFactionData().getPoints(int)` is fully defined in platform-api interfaces.

5. **`performEffect(IPlayer)` now properly overrides** the interface default method instead of using `Object` parameter.
