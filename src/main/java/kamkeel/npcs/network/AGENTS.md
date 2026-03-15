# AGENTS.md — Network Layer (`kamkeel.npcs.network`)

## Purpose
All client-server packet communication. Three FML channels, ~200+ packet types, chunked large-packet support, and revision-based sync protocol.

## Architecture Overview

### Channels
| Channel | Name | Direction | Purpose |
|---|---|---|---|
| `REQUEST_PACKET` | `CNPC+\|Req` | Client → Server | Editor/admin operations (CRUD for dialogs, quests, factions, etc.) |
| `PLAYER_PACKET` | `CNPC+\|Player` | Client → Server | Player actions (dialog select, bank, auction, ability, profile, party) |
| `DATA_PACKET` | `CNPC+\|Data` | Server → Client | GUI data, entity sync, visual effects, ability state, overlays |

### Core Classes

| Class | Purpose |
|---|---|
| `PacketHandler` | Singleton. Registers all packets, routes incoming packets, provides send API |
| `AbstractPacket` | Base class for all packets. Defines `sendData()`/`receiveData()`, channel, type enum |
| `LargeAbstractPacket` | Extends `AbstractPacket`. Chunks data into 10KB segments with UUID reassembly |
| `PacketChannel` | Wraps an FML channel + registered packet map (ordinal → packet instance) |
| `PacketClient` | Client-side convenience: `sendClient(packet)` → `PacketHandler.Instance.sendToServer()` |
| `PacketUtil` | Verification utilities: permission checks, item validation, script handling |

### Packet Enums (`enums/`)
Each channel has a corresponding enum whose ordinal is the packet ID:
- `EnumRequestPacket` — ~233 values for all editor/admin operations
- `EnumPlayerPacket` — ~45 values for player-initiated actions
- `EnumDataPacket` — ~91 values for server-to-client data
- `EnumChannelType` — `REQUEST`, `PLAYER`, `DATA`, `INFO`
- `EnumSyncType` — Data types for bulk sync: factions, dialogs, quests, recipes, effects, abilities
- `EnumSyncAction` — `RELOAD`, `UPDATE`, `REMOVE`

## Packet Lifecycle

### Defining a New Packet
1. Add enum value to the appropriate `Enum*Packet`
2. Create class extending `AbstractPacket` (or `LargeAbstractPacket` for >10KB data)
3. Implement: `getType()` → enum value, `getChannel()` → channel constant, `sendData(ByteBuf)`, `receiveData(ByteBuf, EntityPlayer)`
4. Register in `PacketHandler.register*Packets()` via `CHANNEL.registerPacket(new MyPacket())`

### Sending
```
// Client → Server
PacketClient.sendClient(new MyRequestPacket(...));
// or: PacketHandler.Instance.sendToServer(packet);

// Server → Client
PacketHandler.Instance.sendToPlayer(packet, playerMP);
PacketHandler.Instance.sendToAll(packet);
PacketHandler.Instance.sendToDimension(packet, dimId);
PacketHandler.Instance.sendTracking(packet, entity); // 60-block radius
```

### Receiving
`PacketHandler.handlePacket()` reads channel type ordinal + packet ID ordinal from buffer, looks up the registered `AbstractPacket`, and calls `receiveData(buf, player)`.

Server-side checks before dispatch:
- `ConfigMain.OpsOnly` → rejects non-ops for REQUEST channel
- `abstractPacket.getPermission()` → `CustomNpcsPermissions` check
- `abstractPacket.needsNPC()` → requires `NoppesUtilServer.getEditingNpc(player)` to be non-null

Client-side receive methods are annotated `@SideOnly(Side.CLIENT)`.

## Large Packet Protocol
`LargeAbstractPacket` handles data exceeding FML's packet size limit:
1. `generatePackets()` serializes full data via `getData()`, splits into 10KB chunks
2. Each chunk includes: UUID, total size, offset, chunk count, chunk bytes
3. Receiver reassembles in `ConcurrentHashMap<UUID, PacketStorage>`
4. On complete assembly → calls `handleCompleteData(ByteBuf, EntityPlayer)`

Used by: `SyncPacket`, `SyncEffectPacket`, `ScrollDataPacket`, `ScrollListPacket`, `GuiDataPacket`, `PartyDataPacket`, `ClonerPacket`

## Sync Protocol (`SyncController`)
Revision-based sync ensures clients have current data without redundant transfers:
1. On player login → server sends full sync for all `EnumSyncType` categories
2. Each data type has a revision counter (incremented on server-side changes)
3. On data change → server sends incremental `SyncPacket` with action (`UPDATE`/`REMOVE`/`RELOAD`)
4. Client stores synced data in `ClientCacheHandler`
5. Player can send `SyncRevisionInfoPacket` to report current revisions → server sends only deltas

### Synced Data Types
Factions, Dialogs, Dialog Categories, Quests, Quest Categories, PlayerData, Magic, Magic Cycles, Workbench/Carpentry/Anvil Recipes, Custom Effects, Custom Abilities, Chained Abilities

## GUI ↔ Packet Integration
1. Server sends `GuiOpenPacket` → client opens GUI
2. GUI sends REQUEST packet to fetch data (e.g., `DialogGetPacket`)
3. Server processes, sends `GuiDataPacket` (NBT) or `ScrollDataPacket` (list data) back
4. Client GUI implements `IGuiData.setGuiData()` or `IScrollData.setData()` to populate
5. On save, GUI sends save REQUEST packet (e.g., `DialogSavePacket`) with serialized NBT

## Packet Subdirectories (`packets/`)

| Directory | Channel | Content |
|---|---|---|
| `request/` | REQUEST | ~25 subdirs: dialog, quest, faction, script, ability, animation, clone, etc. |
| `player/` | PLAYER | Player actions: ability, profile, party, customgui, item, auction |
| `data/` | DATA | Server pushes: gui, npc, script, energy, telegraph, ability, large |

## Key Conventions
- **Never add packets without enum entries** — ordinal mismatch breaks all networking
- **Enum ordering is permanent** — never reorder or remove enum values, only append
- **`@SideOnly(Side.CLIENT)`** on `receiveData()` for DATA packets to prevent server-side class loading
- **Permission-gated**: REQUEST packets can declare `getPermission()` for fine-grained access control
- **NPC context**: REQUEST packets can declare `needsNPC() = true` to require an active NPC editing session
