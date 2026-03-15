# AGENTS.md — GUI Subsystem (`noppes.npcs.client.gui`)

## Purpose
All NPC editor, player interaction, and admin GUIs. ~80+ files, entirely `@SideOnly(Side.CLIENT)`.

## Class Hierarchy

| Base Class | Extends | Use Case |
|---|---|---|
| `GuiNPCInterface` | `GuiScreen` | Non-inventory GUIs (dialogs, quests, factions, AI, etc.) |
| `GuiNPCInterface2` | `GuiNPCInterface` | Tabbed NPC editor GUIs with top menu bar (`GuiNpcMenu`) |
| `GuiContainerNPCInterface` | `GuiContainer` | Inventory GUIs (trader, bank, carpentry, inv editor) |
| `GuiContainerNPCInterface2` | `GuiContainerNPCInterface` | Tabbed container GUIs with top menu |
| `SubGuiInterface` | `GuiNPCInterface` | Modal sub-GUIs (popups). Calls `ISubGuiListener.subGuiClosed()` on parent |
| `GuiModelInterface` / `2` | `GuiNPCInterface` / `2` | Model editor variants with 3D NPC preview |

## Widget System (`util/` package)
All widgets are integer-ID based, stored in `HashMap<Integer, Widget>` on the parent GUI.

| Widget | Purpose |
|---|---|
| `GuiNpcButton` | Standard button with hover text, icon support, right-click cycling |
| `GuiNpcTextField` | Text input with int/double/float validation, paste filtering |
| `GuiNpcLabel` | Static text label |
| `GuiCustomScroll` | Scrollable list. Variants: `GuiCustomScrollCloner`, `GuiCustomScrollIcons`, `GuiCustomScrollTagged` |
| `GuiNpcSlider` | Slider control |
| `GuiScrollWindow` | Scrollable content panel |
| `GuiDiagram` | Visual diagram widget |
| `GuiMenuTopButton` / `GuiMenuSideButton` | Tab navigation buttons |
| `GuiNpcMenu` | Top menu bar for `GuiNPCInterface2` — handles tab switching |
| `GuiScriptTextArea` / `1` | Code editor with syntax highlighting, autocomplete, error detection |
| `GuiTexturedButton` / `GuiToggleButton` | Specialized button variants |

## Callback Interfaces
GUIs implement these to receive widget events:
- `IButtonListener` — button clicks
- `ICustomScrollListener` — scroll list selection/double-click
- `ISubGuiListener` — sub-GUI closed notification
- `ITextfieldListener` / `ITextChangeListener` — text field changes
- `ISliderListener` — slider value changes
- `ITopButtonListener` — top menu tab changes
- `IGuiData` — receives `NBTTagCompound` from server via `GuiDataPacket`
- `IScrollData` — receives scroll list data from server via `ScrollDataPacket`/`ScrollListPacket`

## GUI Opening Flow
1. Player action (right-click NPC, use item, command) triggers server-side logic
2. Server sends `GuiOpenPacket` (DATA channel) with `EnumGuiType` + coordinates
3. Client `receiveData()` calls `ClientProxy.openGui(npc, guiType, x, y, z)`
4. `ClientProxy` maps `EnumGuiType` to GUI class, calls `Minecraft.displayGuiScreen()`
5. GUI `initGui()` populates widgets, may send REQUEST packets to fetch data
6. Server responds with `GuiDataPacket`/`ScrollDataPacket` → GUI's `setGuiData()`/`setData()`

## Directory Layout

| Directory | Content |
|---|---|
| `util/` | Base classes, widgets, callback interfaces, script editor components |
| `mainmenu/` | NPC editor tabs: Display, Stats, AI, Advanced, Inventory |
| `advanced/` | Ability config, faction setup, dialog options, marks, sounds (~29 files) |
| `roles/` | Role/job editors: Trader, Bank, Guard, Healer, Follower, etc. |
| `player/` | Player-facing GUIs: Trader, Dialog, Quest, Auction, Mailbox, Bank |
| `global/` | Global editor GUIs (factions, quests, dialogs, recipes, transports) |
| `model/` | NPC model editor |
| `script/` | Script editor GUIs |
| `select/` | Selection dialogs |
| `hud/` | HUD configuration GUIs |
| `custom/` | Scripted custom GUI rendering |
| `customoverlay/` | Script overlay rendering |
| `builder/` | GUI builder tools |
| `item/` | Item-related GUIs |
| `questtypes/` | Quest type-specific editors |

## Key Patterns
- **Sub-GUI stacking**: Parent GUI calls `setSubGui(subGui)` — sub-GUI renders on top. On close, `SubGuiInterface.close()` calls `parent.subGuiClosed(this)`.
- **Server data flow**: GUIs never read server state directly. They send REQUEST packets and receive data via `IGuiData.setGuiData()` or `IScrollData.setData()`.
- **Save pattern**: `GuiNPCInterface2.save()` is abstract — each tab serializes to NBT and sends a save packet.
- **Viewport panning**: `GuiNPCInterface` supports pannable GUIs via `isPannableGUI` flag.
- **Ability GUIs**: Located separately in `kamkeel.npcs.controllers.data.ability.gui.GuiAbilityInterface` — extends `GuiNPCInterface2` with 3D preview rendering.
