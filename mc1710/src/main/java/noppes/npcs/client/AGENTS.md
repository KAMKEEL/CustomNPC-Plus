# AGENTS.md — Client Layer (`noppes.npcs.client` + `kamkeel.npcs.client`)

## Purpose
Client-side rendering, event handling, caching, and utilities. Everything here is `@SideOnly(Side.CLIENT)`.

## Entry Point
`ClientProxy` (extends `CommonProxy`) — registered via `@Mod`. Handles:
- Entity renderer registration (`RenderingRegistry.registerEntityRenderingHandler`)
- Key bindings (`ClientRegistry.registerKeyBinding`)
- GUI opening (`openGui(npc, EnumGuiType, x, y, z)` → maps type to GUI class)
- Client commands, resource listeners, tick handlers

## Rendering Architecture

### NPC Entity Renderers (`noppes.npcs.client.renderer/`)

| Renderer | Entity | Model |
|---|---|---|
| `RenderCustomNpc` | `EntityCustomNpc` | Delegates to entity-specific renderers or `ModelMPM` |
| `RenderNPCInterface` | Base for all NPC renderers | Handles texture binding, overlays, scaling, Gecko hook |
| `RenderNPCHumanMale` | `EntityNPCGolem` | `ModelBiped` variant |
| `RenderNPCPony` | `EntityNpcPony` | Pony model |
| `RenderNpcDragon` | `EntityNpcDragon` | Dragon model |
| `RenderNpcSlime` | `EntityNpcSlime` | Slime model |
| `RenderNpcCrystal` | `EntityNpcCrystal` | Crystal model |
| `RenderCNPCPlayer` | Player entities | Skin overlay support |
| `RenderCNPCHand` | First-person hand | Hand rendering |
| `RenderProjectile` | `EntityProjectile` | NPC projectiles |

### Rendering Pipeline
1. **Entity spawn** → Forge dispatches to registered renderer
2. **Renderer selection** → `RenderCustomNpc` checks for addon models (Gecko/DBC), delegates or uses `ModelMPM`
3. **Texture binding** → `RenderNPCInterface.getEntityTexture()`: skin type 0=custom, 1=player profile, 2/3=URL, fallback to Steve/Alex
4. **Model rendering** → `ModelMPM` with modular parts (body, limbs, overlays)
5. **Post-render** → Skin overlays via GL blending, glow effects, `MarkRenderer` for quest markers
6. **Animation** → `AnimationHelper` computes UV offsets for animated textures

### Addon Integration
- **Gecko Addon**: `GeckoAddonClient.Instance.geckoRenderModel()` — bypasses standard rendering for Blockbench models
- **DBC Addon**: `DBCClient` — Dragon Block C model/animation support
- Addons are checked via reflection/instance nullability, no hard dependency

### Ability Entity Renderers (`kamkeel.npcs.client.renderer/`)

| Renderer | Entity |
|---|---|
| `RenderEnergyOrb` | `EntityAbilityOrb` |
| `RenderEnergyDisc` | `EntityAbilityDisc` |
| `RenderEnergyBeam` | `EntityAbilityBeam` |
| `RenderEnergyLaser` | `EntityAbilityLaser` |
| `RenderEnergyDome` | `EntityEnergyDome` |
| `RenderEnergyPanel` | `EntityEnergyPanel` |
| `RenderEnergySlicer` | `EntityEnergySlicer` |
| `RenderEnergyExplosion` | `EntityEnergyExplosion` |
| `RenderSweeper` | `EntityEnergySweeper` |
| `RenderZone` | `EntityAbilityZone` |
| `TelegraphRenderer` | Telegraph preview overlays |
| `EnergyChargePreviewRenderer` | Charge-up preview effects |

All extend `RenderEnergy` base class or `Render` directly. `lightning/` subdirectory handles lightning VFX.

### Block Renderers (`noppes.npcs.client.renderer.blocks/`)
~22 block renderers extending `BlockRendererInterface` (which extends `TileEntitySpecialRenderer`). Each handles a specific decorative block type (banners, chairs, signs, etc.).

### Model System (`noppes.npcs.client.model/`)
- `ModelMPM` — Main humanoid model with configurable parts
- `ModelNPCMale` — Male NPC variant
- `ModelPonyArmor` / `ModelSkirtArmor` — Armor model variants
- `model/part/` — Modular body parts: tails (7 types), horns (4 types), legs (4 types), wings, ears, hair, beard, claws, fins, snout, skirt, breasts
- `model/util/` — Rendering helpers: `ModelScaleRenderer`, `ModelPlaneRenderer`, `Model2DRenderer`, `ModelPartInterface`

## Other Client Components

| File | Purpose |
|---|---|
| `ClientProxy` | Entry point: renderer registration, GUI dispatch, client init |
| `NoppesUtil` | GUI data routing: `setGuiData()`, `setScrollData()`, `requestOpenGUI()` |
| `ClientEventHandler` | Forge client event hooks (render, tick, input) |
| `ClientTickHandler` | Per-tick client logic |
| `ClientCacheHandler` | Client-side data cache for synced controller data |
| `ClientAbilityState` | Tracks player ability state on client |
| `KeyPressHandler` | Hotkey processing |
| `RenderChatMessages` | Custom chat bubble rendering |
| `EntityUtil` | Client entity utilities |
| `AssetsBrowser` | Asset file browsing |
| `ImageDownloadAlt` | Async skin/image downloading |
| `VersionChecker` | Mod version checking |
| `TranslateUtil` | i18n helper |
| `fx/` | Particle effects |
| `key/` | Key binding definitions |
| `controllers/` | Client-side controller instances |

## Key Conventions
- **All classes** must be `@SideOnly(Side.CLIENT)` or guarded — server will crash if client classes load server-side.
- **Texture caching**: `ClientCacheHandler` caches downloaded skins and synced data to avoid re-fetching.
- **Addon checks**: Always null-check addon instances before calling addon rendering. No hard imports.
- **GUI dispatch**: `ClientProxy.openGui()` is the sole entry point for opening GUIs from packets.
