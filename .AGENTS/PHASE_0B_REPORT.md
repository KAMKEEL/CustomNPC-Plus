# Phase 0B Report — Move Scripting API Interfaces to platform-api

**Date:** 2026-03-15
**Agent:** Sisyphus-Junior (claude-opus-4.6)

## What Was Done

All 232 scripting API interfaces from `mc1710/src/api/java/noppes/npcs/api/` are now present in `platform-api/src/main/java/noppes/npcs/api/` as MC-free versions. Platform-api compiles as a pure Java library with zero Minecraft/Forge dependencies.

### Key changes:
1. **168 MC-free files** — Already present in platform-api (copied in a prior operation). Verified identical to API submodule versions.
2. **26 entity interfaces** — Stripped generic type parameters bound to MC Entity classes (`<T extends Entity>` → raw types), replaced `T getMCEntity()` → `Object getMCEntity()`, removed MC imports.
3. **37 non-entity MC-contaminated files** — Replaced all MC types with `Object` returns/params (e.g., `ItemStack` → `Object`, `NBTTagCompound` → `Object`, `BlockPos` → `Object`, `DamageSource` → `Object`, etc.)
4. **Created `@Cancelable` annotation** — Platform-api replacement for `cpw.mods.fml.common.eventhandler.Cancelable` used by 14 event interfaces.
5. **AbstractNpcAPI** — Rewrote to remove all 12 MC/FML imports; `IsAvailable()` uses reflection instead of `Loader.isModLoaded()`.
6. **ISlot** — Used `INbt` instead of `Object` for NBT-typed methods (matching core's implementation).
7. **ILine.formatTarget()** and **IPlayerEffect.performEffect()** — Made default methods to avoid breaking core implementations that don't override them.

## Files Changed (63 modified, 1 created)

### Created
- `platform-api/src/main/java/noppes/npcs/api/Cancelable.java` — Platform annotation replacing FML's `@Cancelable`

### Entity Interfaces — Generics Stripped (26 files)
**Hierarchy (5):**
- `entity/IEntity.java` — `<T extends Entity>` → raw, `T getMCEntity()` → `Object`, `IEntity<?>[]` → `IEntity[]`
- `entity/IEntityLivingBase.java` — `<T extends EntityLivingBase>` → raw, `T getMCEntity()` → `Object`
- `entity/IEntityLiving.java` — `<T extends EntityLiving>` → raw, `T getMCEntity()` → `Object`
- `entity/ICustomNpc.java` — `<T extends EntityCreature>` → raw
- `entity/IPlayer.java` — `<T extends EntityPlayerMP>` → raw, `mountEntity(Entity)` → `mountEntity(Object)`

**Other entity interfaces (21):**
- `entity/IAnimal.java`, `IArrow.java`, `IEnergyAbility.java`, `IEnergyBarrier.java`, `IEnergyBeam.java`, `IEnergyDisc.java`, `IEnergyDome.java`, `IEnergyExplosion.java`, `IEnergyLaser.java`, `IEnergyOrb.java`, `IEnergyPanel.java`, `IEnergyProjectile.java`, `IEnergySlicer.java`, `IEnergySweeper.java`, `IEnergyZone.java`, `IEntityItem.java`, `IFishHook.java`, `IMonster.java`, `IPixelmon.java`, `IThrowable.java`, `IVillager.java` — All stripped of `<T extends MC_Entity>` and MC imports

### MC Type Replacements (37 files)
**Root package (8):**
- `AbstractNpcAPI.java` — Full rewrite: 12 MC imports removed, MC-typed params → Object, `IsAvailable()` uses reflection
- `IBlock.java` — `Block getMCBlock()` → `Object`, `TileEntity getMCTileEntity()` → `Object`
- `IContainer.java` — `IInventory getMCInventory()` → `Object`, `Container getMCContainer()` → `Object`
- `IDamageSource.java` — `DamageSource getMCDamageSource()` → `Object`
- `INbt.java` — `NBTTagCompound getMCNBT()` → `Object`
- `IPos.java` — `BlockPos getMCPos()` → `Object`
- `ITileEntity.java` — `TileEntity getMCTileEntity()` → `Object`
- `IWorld.java` — `WorldServer getMCWorld()` → `Object`

**Events (15):**
- `event/IAbilityEvent.java`, `IAnimationEvent.java`, `IAuctionEvent.java`, `IBlockEvent.java`, `ICustomGuiEvent.java`, `ICustomNPCsEvent.java`, `IDialogEvent.java`, `IFactionEvent.java`, `IItemEvent.java`, `INpcEvent.java`, `IPartyEvent.java`, `IPlayerEvent.java`, `IQuestEvent.java`, `IRecipeEvent.java` — `cpw.mods.fml.common.eventhandler.Cancelable` → `noppes.npcs.api.Cancelable`
- `event/IForgeEvent.java` — `Event getEvent()` → `Object getEvent()`

**GUI (3):**
- `gui/ICustomGui.java` — `NBTTagCompound` → `Object`
- `gui/ICustomGuiComponent.java` — `NBTTagCompound` → `Object`
- `gui/IItemSlot.java` — `Slot getMCSlot()` → `Object getMCSlot()`

**Handler/data (5):**
- `handler/IRecipeHandler.java` — `ItemStack` → `Object`
- `handler/data/IRecipe.java` — `ItemStack` → `Object`
- `handler/data/IAnvilRecipe.java` — `ItemStack` → `Object`
- `handler/data/ISlot.java` — `NBTTagCompound` → `INbt` (matching core implementation)
- `handler/data/IProfile.java` — `NBTTagCompound` → `Object`

**Item (1):**
- `item/IItemStack.java` — `ItemStack getMCItemStack()` → `Object`, `NBTTagCompound getMCNbt()` → `Object`, `setMCNbt(NBTTagCompound)` → `setMCNbt(Object)`

**Others (5):**
- `jobs/IJobSpawner.java` — Stripped `IEntityLivingBase<?>` → `IEntityLivingBase`, `IPlayer<EntityPlayerMP>` → `IPlayer`
- `overlay/ICustomOverlay.java` — `NBTTagCompound` → `Object`
- `overlay/ICustomOverlayComponent.java` — `NBTTagCompound` → `Object`
- `roles/IRole.java` — `INpc getNpc()` → `Object getNpc()`
- `roles/IRoleTransporter.java` — `IPlayer<EntityPlayerMP>` → `IPlayer`

### Core Compatibility (2 files — default methods added)
- `handler/data/ILine.java` — `formatTarget(IEntityLivingBase)` made default (returns `this`)
- `handler/data/IPlayerEffect.java` — `performEffect(IPlayer)` made default (no-op)

### Package Info (1 file)
- `package-info.java` — Removed `@API` annotation (FML-specific)

## Verification

| Check | Result |
|---|---|
| `./gradlew :platform-api:compileJava` | **PASS** |
| `./gradlew :core:compileJava` | **PASS** |
| `./gradlew :mc1710:build` | **PASS** |
| Zero MC imports in platform-api | **PASS** (0 files) |
| All 232 files retained in mc1710/src/api/java | **PASS** |
| platform-api file count | 233 (232 API + 1 Cancelable annotation) |
| Package structure preserved | **PASS** — exact `noppes.npcs.api.*` structure |

## Discoveries / Notes

1. **Prior full copy**: A previous operation had already copied all 232 API files to platform-api (including MC-contaminated ones). Phase 0B's actual work was making ALL files MC-free, not just copying the MC-free subset.

2. **Scope expansion**: The task described moving ~100 MC-free files. In practice, all 232 files were already present, and 63 of them needed MC imports stripped. This was necessary because MC-free files depend on MC-contaminated interfaces (e.g., many files import `IItemStack` which had MC imports). Removing them would break compilation.

3. **Entity generics stripping** applied to all 26 entity interfaces (not just the 5 hierarchy ones), because all entity interfaces used `<T extends MC_Entity>` generics.

4. **ISlot used INbt instead of Object**: The core `Slot` class implements `ISlot` using `INbt` types. Using `Object` (as a blanket MC-type replacement) broke core. Changed to `INbt` for NBT-typed methods.

5. **Default methods for new API methods**: Two methods (`ILine.formatTarget`, `IPlayerEffect.performEffect`) existed in the API submodule but not in the older platform-api versions. Core implementations don't override them. Made them `default` to maintain backward compatibility.

6. **AbstractNpcAPI.IsAvailable()**: Changed from `Loader.isModLoaded("customnpcs")` to reflection-based `Class.forName()` check, making it MC-free.

7. **Split-package shadow works correctly**: mc1710 still compiles because its API submodule has the full MC-typed versions that shadow the platform-api versions at compile time. The mc1710 build sees its own generic/MC-typed interfaces, not the stripped ones.

8. **Phase 0C scope reduced**: Since all MC-contaminated files were handled in this phase (stripped to MC-free), Phase 0C's remaining work is limited to any NEW interfaces that may need to be created, rather than stripping existing ones.
