# Ability System Review - CustomNPC+

A comprehensive technical review of the Ability System identifying bugs, logic issues, incomplete features, and inconsistencies.

---

## Critical Issues

### 1. Cooldown Uses Mixed Time Systems
**Status:** FIXED
**Files:**
- `Ability.java` - Removed cooldown tracking (cooldownEndTime, startCooldown(), isOnCooldown(), canUse())
- `DataAbilities.java` - Now sole owner of cooldown management using world time

**Solution:** Removed all cooldown tracking from `Ability.java`. Cooldowns are now managed exclusively by `DataAbilities` using `world.getTotalWorldTime()` for consistency.

---

### 2. AbilityHeal - Heal Over Time Completely Broken
**Status:** FIXED
**File:** `AbilityHeal.java`

**Solution:** Refactored to separate ally-finding from healing. The `findAlliesInRadius()` method is now called regardless of `instantHeal` flag, populating the list for HoT to use. Added heal particles during HoT ticks.

---

### 3. AbilityGuard - Counter Integration Missing
**Status:** FIXED
**Files:**
- `DataAbilities.java:onDamage()` - Added integration to call `AbilityGuard.onDamageTaken()`
- `DataAbilities.java:getGuardDamageReduction()` - Added helper method for damage reduction

**Solution:** `DataAbilities.onDamage()` now checks if current ability is an `AbilityGuard` and calls `onDamageTaken()` when the NPC is hit while guarding.

---

## High Priority Issues

### 4. knockbackUp Stored But Never Applied
**Status:** FIXED
**File:** `Ability.java`

**Solution:** Updated `applyNpcKnockback()` to accept a `knockbackUp` parameter and use it instead of hardcoded `0.1D`. Both `applyAbilityDamage()` and `applyAbilityDamageWithDirection()` now pass the knockbackUp value from the HitEvent.

---

### 5. Targeting Modes Not Implemented
**Status:** FIXED (Removed)
**File:** `TargetingMode.java`

**Solution:** Removed the unused targeting modes (`RANDOM_ENEMY`, `LOWEST_HP`, `HIGHEST_THREAT`) from the enum. Only the implemented modes remain: `AGGRO_TARGET`, `SELF`, `AOE_SELF`, `AOE_TARGET`.

---

### 6. Damage Source Inconsistency
**Status:** FIXED
**File:** `Ability.java`

**Solution:** Standardized both `applyAbilityDamage()` and `applyAbilityDamageWithDirection()` to use `NpcDamageSource("mob", npc)` for consistent damage source handling.

---

## Incomplete Features

### 7. AbilityProjectile - No Actual Projectile
**Status:** CONFIRMED - INCOMPLETE
**File:** `AbilityProjectile.java:88`

```java
// TODO: Use custom EntityAbilityProjectile for actual tracking
applyAbilityDamageWithDirection(npc, target, damage, knockback, dx, dz);
```

**Current behavior:**
- `speed`, `homing`, `homingStrength` fields exist but are unused
- No projectile entity is spawned
- Damage is instant, just with particle cosmetics

---

### 8. AbilityOrb - No Visual Orb Rendering
**Status:** CONFIRMED - INCOMPLETE
**File:** `AbilityOrb.java:117-179`

**Problem:** The orb tracks position (`orbX`, `orbY`, `orbZ`) with runtime getters but nothing renders it client-side. Players only see particle effects on hit, not the flying orb itself.

---

### 9. AbilityBeam - No Visual Beam Rendering
**Status:** CONFIRMED - INCOMPLETE
**File:** `AbilityBeam.java:213-220`

**Problem:** Only spawns flame particles along the path. No actual beam entity or proper beam visual. The `lockOnTarget` telegraph during active phase isn't visually communicated.

---

## Medium Priority Issues

### 10. Hazard/Trap Telegraph Offset Mismatch
**Status:** CONFIRMED
**Files:**
- `AbilityHazard.java:119-147, 201-248`
- `AbilityTrap.java:109-138, 166-205`

**Problem:** Telegraph is created at target position, but actual effect applies random offset:

```java
// createTelegraph() - No offset applied, shows at target position

// onExecute() - Random offset applied AFTER telegraph locks
double[] pos = calculateOffsetPosition(telegraph.getX(), telegraph.getY(), telegraph.getZ());
zoneX = pos[0];  // Actual effect position differs from telegraph
```

**Impact:** Players see telegraph at one location, damage happens elsewhere. Misleading visual feedback.

**Fix:** Either apply offset during telegraph creation, or update telegraph position in `onExecute()`.

---

### 11. AbilityCharge Telegraph Direction Timing
**Status:** CONFIRMED - MINOR
**File:** `AbilityCharge.java:91, 254-286`

**Problem:** Telegraph created at tick 0, direction locked at tick 1:

```java
// Telegraph created during setup (tick 0) with current target direction
// Line 91: Direction locked at tick 1
if (tick == 1) {
    lockChargeDirection(npc, target);
}
```

**Impact:** If target moves between tick 0-1, telegraph direction won't match actual charge.

---

### 12. AbilitySlam - activeTicks May End Before Landing
**Status:** CONFIRMED - MINOR
**File:** `AbilitySlam.java:52-53, 230-250`

**Problem:** `activeTicks = 60` is fixed, but if NPC takes longer to land, ability transitions to RECOVERY without `onLanding()` being called.

**Secondary issue:** If NPC lands early (tick 15), active phase continues until tick 60 doing nothing - wasted ticks.

---

### 13. FOLLOW Mode Telegraph Not Updated
**Status:** CONFIRMED
**Files:** `AbilityHazard.java:139-146`, `AbilityVortex.java`

**Problem:** For `FOLLOW_TARGET` mode, telegraph locks position on execute, but the actual hazard zone follows the target. Visual mismatch between warning and danger.

---

## Redundancies

### 14. Duplicate findGroundLevel() Methods
**Status:** FIXED
**Files:**
- `Ability.java` - Made `findGroundLevel()` public static
- `TelegraphInstance.java` - Now uses `Ability.findGroundLevel()`

---

### 15. Duplicate calculateOffsetPosition() Methods
**Status:** FIXED
**Files:**
- `Ability.java` - Added shared static `calculateOffsetPosition()` method
- `AbilityTrap.java` - Removed duplicate, uses `Ability.calculateOffsetPosition()`
- `AbilityHazard.java` - Removed duplicate, uses `Ability.calculateOffsetPosition()`

---

### 16. Unused applyKnockback in AbilityOrb
**Status:** FIXED
**File:** `AbilityOrb.java`

Removed the unused `applyKnockback()` method.

---

### 17. Duplicate Reset/Interrupt Cleanup
**Status:** NOT FIXED (Low Priority)
**Multiple ability files**

Most abilities override both `reset()` and `onInterrupt()` with duplicate cleanup code. Pattern should have `onInterrupt()` call `reset()` or use shared cleanup method.

---

## Minor Issues & Inconsistencies

### 18. Field Naming Confusion
- `AbilityHazard`: `damagePerSecond` with `damageInterval` - name suggests per-second but applied every N ticks
- `AbilityCharge`: `hitWidth` stored as `hitRadius` in older NBT (backward compat exists)

### 19. AbilityVortex Entity Lookup Performance
**File:** `AbilityVortex.java:201-212`

Iterates through ALL loaded entities every tick to find pulled entities by UUID. Should cache entity references or use entity IDs.

### 20. Telegraph Yaw Rotation
**File:** `TelegraphRenderer.java:95`

Uses `GL11.glRotatef(-instance.getYaw(), 0, 1, 0)` - negative yaw. Should verify this matches Minecraft's yaw conventions.

### 21. Missing Null Checks
Various locations where `target` could be null but is used without defensive checks:
- `AbilityProjectile.java:73`
- `AbilityOrb.java:138-171`

---

## Missing Features (Enhancement Requests)

These are not bugs but notable missing functionality:

1. **No Ability Chaining/Combo System** - No way to trigger abilities from other abilities
2. **No Cooldown Reduction on Hit** - Can't reduce cooldowns based on successful hits
3. **No Multi-Target Hit Limit** - AOE abilities hit unlimited targets (unlike Vortex's `maxTargets`)
4. **No Damage Type Configuration** - All abilities use mob/melee damage, no magic/fire/etc options
5. **No Target Validation During Execution** - If target dies/despawns mid-ability, behavior varies

---

## Priority Summary

| Priority | Issue | Status |
|----------|-------|--------|
| **CRITICAL** | Cooldown time system mismatch | FIXED |
| **CRITICAL** | AbilityHeal HoT broken | FIXED |
| **CRITICAL** | AbilityGuard counter not integrated | FIXED |
| **HIGH** | knockbackUp ignored | FIXED |
| **HIGH** | Targeting modes unimplemented | FIXED (Removed) |
| **HIGH** | Damage source inconsistency | FIXED |
| **MEDIUM** | Projectile/Orb/Beam no visuals | Not Fixed |
| **MEDIUM** | Hazard/Trap telegraph offset | Not Fixed |
| **LOW** | Charge telegraph timing | Not Fixed |
| **LOW** | Slam activeTicks timing | Not Fixed |
| **LOW** | Code redundancies | FIXED |

---

## Remaining Issues

The following issues were not addressed in this fix round:

1. **Projectile/Orb/Beam Visual Rendering** - These abilities still only use particles, no actual entity rendering
2. **Hazard/Trap Telegraph Offset Mismatch** - Telegraph shows at target, effect spawns with offset
3. **Charge Telegraph Direction Timing** - Minor visual mismatch between tick 0-1
4. **Slam activeTicks Timing** - Fixed at 60 ticks regardless of landing time

---

## Changes Made

### Ability.java
- Removed `cooldownEndTime` transient field
- Removed `startCooldown()`, `isOnCooldown()`, `canUse()` methods (cooldown managed by DataAbilities)
- Updated `tick()` to not call startCooldown (DataAbilities handles it)
- Updated `reset()` to not reset cooldownEndTime
- Made `findGroundLevel()` public static for shared use
- Added static `calculateOffsetPosition()` utility method
- Fixed `applyNpcKnockback()` to accept and use knockbackUp parameter
- Standardized damage source to `NpcDamageSource("mob", npc)` in both damage methods
- Removed deprecated getters/setters (telegraphColor, castSound, animationId)

### IAbility.java (API)
- Removed `isOnCooldown()` method from interface

### DataAbilities.java
- Added AbilityGuard import
- Added guard counter integration in `onDamage()`
- Added `getGuardDamageReduction()` helper method
- Removed deprecated `isMovementBlocked()` method
- Updated knockbackUp javadoc (no longer deprecated)

### CombatHandler.java
- Renamed `isMovementBlocked()` to `isAbilityControllingMovement()`

### TargetingMode.java
- Removed unused modes: `RANDOM_ENEMY`, `LOWEST_HP`, `HIGHEST_THREAT`

### AbilityHeal.java
- Fixed HoT by separating `findAlliesInRadius()` from healing logic
- Allies are now found in `onExecute()` regardless of instantHeal flag
- Added heal particles during HoT ticks

### AbilityHazard.java
- Removed duplicate `calculateOffsetPosition()`, uses `Ability.calculateOffsetPosition()`
- Removed deprecated `getDamagePerTick()`/`setDamagePerTick()` methods

### AbilityTrap.java
- Removed duplicate `calculateOffsetPosition()`, uses `Ability.calculateOffsetPosition()`

### AbilityOrb.java
- Removed unused `applyKnockback()` method

### TelegraphInstance.java
- Removed duplicate `findGroundLevel()`, uses `Ability.findGroundLevel()`
