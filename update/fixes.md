# Bug Fixes

---

Fixes to pre-existing systems included in this update.

---

## Scripting Engine

- Fixed instance scripts not loading correctly
- Fixed script contexts not initializing properly
- Fixed hooks not registering in certain scenarios
- Fixed script configuration not syncing between server and client
- Fixed Bukkit integration compatibility issues

---

## Quests & Dialogs

- Fixed quest cooldown not working for MC Custom and RL Custom timer types
- Fixed quest and dialog modification names not saving correctly
- Fixed quest and dialog saving bugs
- Fixed dialog and quest category assignment bugs
- Fixed bard music restarting or duplicating when opening dialogs

---

## NPCs

- Fixed NPC spawner yaw using camera yaw instead of proper facing direction
- Fixed NPC biome spawn settings not persisting after editing
- Fixed NPC invisibility caused by request client packet issues
- Fixed NPC track speed handling
- Fixed NPC set speed edge cases
- Fixed missing spawn data on certain NPCs
- Fixed block waypoint pathing
- Fixed job spawner behavior
- Fixed `setRace` not applying correctly
- Fixed timer not being callable from a spawning NPC

---

## Animations

- Fixed animation offsets conflicting with vanilla offsets
- Fixed animation lookup failures
- Fixed animation syncing for built-in animations
- Fixed animation resync on teleport
- Fixed wrong names being registered on animation registration
- Fixed animation controller loading twice on startup

---

## GUI

- Fixed hover labels on scroll components
- Fixed text field input handling
- Fixed close-on-ESC for outline and tag selection screens
- Fixed rendering and selection in fullscreen move systems
- Fixed crosshair alignment
- Fixed sub-GUI spam closing on rapid clicks
- Fixed GUI tab navigation
- Fixed panel redirection logic
- Fixed player data search functionality
- Fixed GL rendering issues

---

## Mounts

- Fixed mount movement jitter by following vanilla horse/boat sync pattern
- Fixed mount edge case handling for dismount scenarios

---

## Networking

- Fixed packet race conditions with language handling
- Fixed wrong packet channel and misplaced SideOnly annotations
- Fixed categorical assign packets

---

## Rendering & Textures

- Fixed texture pathing removing icons
- Fixed rendering glitches in various systems

---

## Hitbox

- Fixed hitbox scaling and collision issues

---
