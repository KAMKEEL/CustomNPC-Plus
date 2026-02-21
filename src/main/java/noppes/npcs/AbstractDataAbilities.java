package noppes.npcs;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.ChainedAbilityEntry;
import kamkeel.npcs.controllers.data.ability.ConcurrentSlot;
import kamkeel.npcs.controllers.data.ability.ToggleEntry;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shared base class for NPC and Player ability data management.
 * Contains the common ability lifecycle logic: tick, phase transitions,
 * animation/telegraph/sound management, cooldown state, and position/rotation locking.
 * <p>
 * Subclasses must implement abstract methods for entity-specific behavior:
 * event firing, rotation/position application, telegraph packets, and cooldown rolling.
 *
 * @see DataAbilities - NPC implementation
 * @see noppes.npcs.controllers.data.PlayerAbilityData - Player implementation
 */
public abstract class AbstractDataAbilities {

    // ═══════════════════════════════════════════════════════════════════
    // SHARED RUNTIME STATE (not saved to NBT)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Currently executing ability (null if none)
     */
    protected Ability currentAbility;

    /**
     * World time when global cooldown ends
     */
    protected long cooldownEndTime = 0;

    /**
     * Duration of the current global cooldown in ticks (for progress calculation)
     */
    protected int globalCooldownDuration = 0;

    /**
     * Per-ability cooldown end times. Key = ability key, Value = world time when cooldown ends.
     */
    protected HashMap<String, Long> perAbilityCooldownEndTimes = new HashMap<>();

    /**
     * Per-ability cooldown durations in ticks (for progress calculation).
     */
    protected HashMap<String, Integer> perAbilityCooldownDurations = new HashMap<>();

    /**
     * Rotation lock state
     */
    protected boolean rotationLocked = false;
    protected float lockedYaw = 0;
    protected float lockedPitch = 0;

    /**
     * Position lock state
     */
    protected boolean positionLocked = false;
    protected boolean wasFlyingAtLock = false;
    protected double lockedPosX = 0;
    protected double lockedPosY = 0;
    protected double lockedPosZ = 0;

    /**
     * Chained ability execution state
     */
    protected ChainedAbility currentChain;
    protected int chainEntryIndex = -1;
    protected int chainDelayRemaining = -1;

    /**
     * Set when interrupt already rolled cooldown (prevents double cooldown on NPC dazed completion).
     */
    protected boolean interruptCooldownRolled = false;

    /**
     * Concurrent ability execution slots. Ticked independently alongside the primary ability.
     * Concurrent abilities skip animations, sounds, and movement/rotation locks.
     */
    protected List<ConcurrentSlot> concurrentSlots = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════════
    // ABSTRACT METHODS - Subclasses must implement
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get the entity this ability data belongs to (NPC or Player).
     */
    protected abstract EntityLivingBase getEntity();

    /**
     * Get the current target for ability execution.
     */
    protected abstract EntityLivingBase getTarget();

    /**
     * Get the current world time.
     */
    protected abstract long getWorldTime();

    // -- Event Firing --

    /**
     * Fire tick event. Implementation differs between NPC (NpcAPI) and Player (ScriptController).
     */
    protected abstract void fireTickEvent(Ability ability, EntityLivingBase target);

    /**
     * Fire execute event. Returns true if cancelled.
     */
    protected abstract boolean fireExecuteEvent(Ability ability, EntityLivingBase target);

    /**
     * Fire complete event.
     */
    protected abstract void fireCompleteEvent(Ability ability, EntityLivingBase target);

    /**
     * Fire interrupt event.
     */
    protected abstract void fireInterruptEvent(Ability ability, EntityLivingBase target,
                                               DamageSource source, float damage);

    // -- Telegraph --

    /**
     * Spawn telegraphs for an ability.
     */
    protected abstract void spawnTelegraph(Ability ability, EntityLivingBase target);

    /**
     * Remove telegraphs for an ability.
     */
    protected abstract void removeTelegraph(Ability ability);

    // -- Animation/Sound Data Access --

    /**
     * Set animation data on the entity.
     */
    protected abstract void setAnimationData(Animation animation);

    /**
     * Clear animation data on the entity.
     */
    protected abstract void clearAnimationData();

    /**
     * Play a sound at the entity's location.
     */
    protected abstract void playAbilitySound(String sound);

    // -- Rotation (subclass handles extra fields and application) --

    /**
     * Capture rotation values for locking. NPC captures 4 fields, Player captures 2.
     */
    protected abstract void captureLockedRotation();

    // -- Completion --

    /**
     * Roll cooldown after ability completes.
     * NPC: random(min, max) + ability offset. Player: ability's base cooldownTicks.
     */
    protected abstract void rollCooldown(Ability ability);

    /**
     * Additional completion logic (NPC clears lastTarget, Player clears key/target and syncs).
     */
    protected abstract void onAbilityComplete();

    /**
     * Roll cooldown after a chained ability completes.
     * NPC: random(min, max) + chain.cooldownTicks. Player: chain.cooldownTicks only.
     */
    protected abstract void rollChainCooldown(ChainedAbility chain);


    // -- Hooks (optional overrides) --

    /**
     * Called before onExecute in ACTIVE phase. NPC uses this for faceTarget/hitScan.
     */
    protected void onPreExecute(Ability ability, EntityLivingBase target) {
    }

    /**
     * Called after the phase switch. NPC uses this for hit scan updates and movement control.
     */
    protected void onPostPhaseTick(Ability ability, EntityLivingBase target) {
    }

    /**
     * Additional lock releases in BURST_DELAY. NPC releases hitScan.
     */
    protected void onBurstDelayReleaseLocks() {
    }

    /**
     * Hook for retargeting during chain execution when target dies. NPC overrides, Player returns null.
     */
    protected EntityLivingBase retargetForChain() {
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // SHARED ANIMATION METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Play an animation on the entity.
     */
    public void playAbilityAnimation(Animation animation) {
        if (animation == null) return;
        if (AnimationController.Instance == null) return;
        setAnimationData(animation);
    }

    public void playAbilityAnimation(int animation) {
        if (animation < 0) return;
        if (AnimationController.Instance == null) return;
        if (AnimationController.Instance.get(animation) == null) return;
        playAbilityAnimation((Animation) AnimationController.Instance.get(animation));
    }

    public void playAbilityAnimation(String animation) {
        if (animation.isEmpty()) return;
        if (AnimationController.Instance == null) return;
        if (AnimationController.Instance.get(animation, true) == null) return;
        playAbilityAnimation((Animation) AnimationController.Instance.get(animation, true));
    }

    /**
     * Stop any currently playing ability animation.
     */
    protected void stopAbilityAnimation() {
        clearAnimationData();
    }

    // ═══════════════════════════════════════════════════════════════════
    // STATE QUERIES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if an ability is currently executing.
     */
    public boolean isExecutingAbility() {
        return currentAbility != null && currentAbility.isExecuting();
    }

    /**
     * Get the currently executing ability.
     */
    public Ability getCurrentAbility() {
        return currentAbility;
    }

    /**
     * Check if a chained ability is currently executing.
     */
    public boolean isExecutingChain() {
        return currentChain != null;
    }

    /**
     * Get the currently executing chained ability.
     */
    public ChainedAbility getCurrentChain() {
        return currentChain;
    }

    // ═══════════════════════════════════════════════════════════════════
    // TOGGLE STATE (active toggles, independent of currentAbility)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Currently active toggles. Key = ability key, Value = toggle entry with state and tick counter.
     */
    protected Map<String, ToggleEntry> activeToggles = new LinkedHashMap<>();

    /**
     * Cycle a toggle ability to its next state.
     * Off(0) -> State 1 -> State 2 -> ... -> State N -> Off(0)
     *
     * @param key The ability key (e.g., "npcdbc:ki_fist")
     * @return The new state (0 = off, 1+ = active state number)
     */
    public int toggleAbility(String key) {
        ToggleEntry existing = activeToggles.get(key);
        if (existing != null) {
            int currentState = existing.getState();
            int maxStates = existing.getAbility().getToggleStates();
            if (currentState < maxStates) {
                return cycleToggleState(key, existing, currentState, currentState + 1);
            } else {
                deactivateToggle(key);
                return 0;
            }
        } else {
            return activateToggle(key, 1) ? 1 : 0;
        }
    }

    /**
     * Get the current toggle state for an ability.
     * @return 0 if not active, 1+ for active state
     */
    public int getToggleState(String key) {
        ToggleEntry entry = activeToggles.get(key);
        return entry != null ? entry.getState() : 0;
    }

    /**
     * Set a toggle to a specific state. 0 = deactivate, 1+ = specific state.
     */
    public void setToggleState(String key, int state) {
        if (state <= 0) {
            if (activeToggles.containsKey(key)) {
                deactivateToggle(key);
            }
            return;
        }
        ToggleEntry existing = activeToggles.get(key);
        if (existing != null) {
            int currentState = existing.getState();
            if (currentState != state) {
                cycleToggleState(key, existing, currentState, state);
            }
        } else {
            activateToggle(key, state);
        }
    }

    /**
     * Check if a toggle ability is currently active (any state > 0).
     */
    public boolean isAbilityToggled(String key) {
        return activeToggles.containsKey(key);
    }

    /**
     * Get all active toggle keys.
     */
    public Set<String> getActiveToggleKeys() {
        return new LinkedHashSet<>(activeToggles.keySet());
    }

    private boolean activateToggle(String key, int state) {
        Ability ability = AbilityController.Instance != null
            ? AbilityController.Instance.resolveAbility(key) : null;
        if (ability == null || !ability.isToggleable()) return false;
        if (state < 1 || state > ability.getToggleStates()) state = 1;

        if (fireToggleEvent(ability, 0, state)) return false;

        ToggleEntry entry = new ToggleEntry(ability, state);
        activeToggles.put(key, entry);
        ability.onToggleOn(getEntity());
        ability.onToggleStateChanged(getEntity(), 0, state);
        onToggleStateChanged(key, true, state);
        return true;
    }

    private int cycleToggleState(String key, ToggleEntry entry, int oldState, int newState) {
        Ability ability = entry.getAbility();
        if (newState < 1 || newState > ability.getToggleStates()) return oldState;

        if (fireToggleEvent(ability, oldState, newState)) return oldState;

        entry.setState(newState);
        ability.onToggleStateChanged(getEntity(), oldState, newState);
        onToggleStateChanged(key, true, newState);
        return newState;
    }

    private void deactivateToggle(String key) {
        ToggleEntry entry = activeToggles.get(key);
        if (entry == null) return;
        int oldState = entry.getState();

        if (fireToggleEvent(entry.getAbility(), oldState, 0)) return;

        activeToggles.remove(key);
        entry.getAbility().onToggleOff(getEntity());
        entry.getAbility().onToggleStateChanged(getEntity(), oldState, 0);
        onToggleStateChanged(key, false, 0);
    }

    /**
     * Tick all active toggles. Call from tick() each game tick.
     * Toggles with hasActiveToggle=true get their onToggleTick() called.
     * If onToggleTick returns false, the toggle is auto-deactivated.
     * Every 10 ticks, fires a toggle update event for ALL active toggles.
     */
    protected void tickActiveToggles() {
        if (activeToggles.isEmpty()) return;

        EntityLivingBase entity = getEntity();
        List<String> toRemove = null;

        for (Map.Entry<String, ToggleEntry> mapEntry : activeToggles.entrySet()) {
            ToggleEntry entry = mapEntry.getValue();
            entry.incrementTick();

            if (entry.getAbility().hasActiveToggle()) {
                if (!entry.getAbility().onToggleTick(entity, entry.getTickCount(), entry.getState())) {
                    if (toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(mapEntry.getKey());
                    continue;
                }
            }

            if (entry.getTickCount() % 10 == 0) {
                boolean enabled = fireToggleUpdateEvent(entry.getAbility(), entry.getTickCount(), entry.getState());
                if (!enabled) {
                    if (toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(mapEntry.getKey());
                }
            }
        }

        if (toRemove != null) {
            for (String key : toRemove) {
                ToggleEntry entry = activeToggles.remove(key);
                if (entry != null) {
                    int oldState = entry.getState();
                    entry.getAbility().onToggleOff(entity);
                    entry.getAbility().onToggleStateChanged(entity, oldState, 0);
                    onToggleStateChanged(key, false, 0);
                }
            }
        }
    }

    /**
     * Hook for subclass to react to toggle state changes (sync packets, script events).
     */
    protected void onToggleStateChanged(String key, boolean active, int state) {
    }

    /**
     * Fire a toggle event before state change.
     * @param oldState Previous state (0 = off)
     * @param newState Target state (0 = off)
     * @return true if the event was canceled
     */
    protected boolean fireToggleEvent(Ability ability, int oldState, int newState) {
        return false;
    }

    /**
     * Fire a toggle update event every 10 ticks.
     * @return true if the toggle should remain active, false to force-deactivate
     */
    protected boolean fireToggleUpdateEvent(Ability ability, int tick, int state) {
        return true;
    }

    /**
     * Clear all active toggles (e.g., on death/reset).
     * Calls onToggleOff and onToggleStateChanged for each active toggle.
     */
    protected void clearActiveToggles() {
        if (activeToggles.isEmpty()) return;
        EntityLivingBase entity = getEntity();
        for (Map.Entry<String, ToggleEntry> mapEntry : activeToggles.entrySet()) {
            ToggleEntry entry = mapEntry.getValue();
            entry.getAbility().onToggleOff(entity);
            entry.getAbility().onToggleStateChanged(entity, entry.getState(), 0);
        }
        activeToggles.clear();
    }

    /**
     * Add a toggle entry directly without calling onToggleOn.
     * Used for client-side sync and NBT restoration.
     * @param key   The ability key
     * @param state The toggle state (0 = remove, 1+ = set at state)
     */
    public void setToggleEntryDirect(String key, int state) {
        if (state > 0) {
            Ability ability = AbilityController.Instance != null
                ? AbilityController.Instance.resolveAbility(key) : null;
            if (ability != null && ability.isToggleable()) {
                activeToggles.put(key, new ToggleEntry(ability, state));
            }
        } else {
            activeToggles.remove(key);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // COOLDOWN MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if on cooldown.
     */
    public boolean isOnCooldown() {
        return getWorldTime() < cooldownEndTime;
    }

    /**
     * Get remaining cooldown ticks.
     */
    public long getRemainingCooldown() {
        long remaining = cooldownEndTime - getWorldTime();
        return remaining > 0 ? remaining : 0;
    }

    /**
     * Reset cooldown (allow immediate ability use).
     */
    public void resetCooldown() {
        cooldownEndTime = 0;
    }

    /**
     * Set the cooldown end time directly.
     */
    public void setCooldownEndTime(long endTime) {
        cooldownEndTime = endTime;
    }

    /**
     * Get the global cooldown end time.
     */
    public long getCooldownEndTime() {
        return cooldownEndTime;
    }

    /**
     * Get the global cooldown duration value.
     */
    public int getGlobalCooldownDurationValue() {
        return globalCooldownDuration;
    }

    /**
     * Get the per-ability cooldown end times map.
     */
    public HashMap<String, Long> getPerAbilityCooldownEndTimes() {
        return perAbilityCooldownEndTimes;
    }

    /**
     * Get the per-ability cooldown durations map.
     */
    public HashMap<String, Integer> getPerAbilityCooldownDurations() {
        return perAbilityCooldownDurations;
    }

    /**
     * Apply cooldown sync data from server. Called on client side.
     */
    public void applyCooldownSync(long globalEndTime, int globalDuration,
                                  HashMap<String, Long> perEndTimes,
                                  HashMap<String, Integer> perDurations) {
        this.cooldownEndTime = globalEndTime;
        this.globalCooldownDuration = globalDuration;
        this.perAbilityCooldownEndTimes = perEndTimes;
        this.perAbilityCooldownDurations = perDurations;
    }

    // ═══════════════════════════════════════════════════════════════════
    // PER-ABILITY COOLDOWN MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if a specific ability is on its per-ability cooldown.
     */
    public boolean isOnPerAbilityCooldown(String key) {
        Long endTime = perAbilityCooldownEndTimes.get(key);
        return endTime != null && getWorldTime() < endTime;
    }

    /**
     * Set a per-ability cooldown.
     */
    public void setPerAbilityCooldown(String key, long endTime, int duration) {
        perAbilityCooldownEndTimes.put(key, endTime);
        perAbilityCooldownDurations.put(key, duration);
    }

    /**
     * Reset a specific per-ability cooldown.
     */
    public void resetPerAbilityCooldown(String key) {
        perAbilityCooldownEndTimes.remove(key);
        perAbilityCooldownDurations.remove(key);
    }

    /**
     * Reset all per-ability cooldowns.
     */
    public void resetAllPerAbilityCooldowns() {
        perAbilityCooldownEndTimes.clear();
        perAbilityCooldownDurations.clear();
    }

    /**
     * Get global cooldown progress for HUD rendering.
     * @return 1.0 when cooldown just started (fully covered), 0.0 when done
     */
    public float getGlobalCooldownProgress() {
        if (globalCooldownDuration <= 0) return 0f;
        long remaining = cooldownEndTime - getWorldTime();
        if (remaining <= 0) return 0f;
        return Math.min(1f, (float) remaining / globalCooldownDuration);
    }

    /**
     * Get per-ability cooldown progress for HUD rendering.
     * @return 1.0 when cooldown just started (fully covered), 0.0 when done
     */
    public float getPerAbilityCooldownProgress(String key) {
        Long endTime = perAbilityCooldownEndTimes.get(key);
        Integer duration = perAbilityCooldownDurations.get(key);
        if (endTime == null || duration == null || duration <= 0) return 0f;
        long remaining = endTime - getWorldTime();
        if (remaining <= 0) return 0f;
        return Math.min(1f, (float) remaining / duration);
    }

    // ═══════════════════════════════════════════════════════════════════
    // POSITION LOCKING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Capture current position to lock entity in place.
     */
    protected void captureLockedPosition() {
        EntityLivingBase entity = getEntity();
        lockedPosX = entity.posX;
        lockedPosY = entity.posY;
        lockedPosZ = entity.posZ;
        positionLocked = true;

        // Record flight state at lock time so airborne players stay at their height
        // even if their flight system deactivates during input suppression
        wasFlyingAtLock = entity instanceof net.minecraft.entity.player.EntityPlayer
            && AbilityController.Instance != null
            && AbilityController.Instance.isPlayerFlying((net.minecraft.entity.player.EntityPlayer) entity);

        onPositionLockChanged(true);
    }

    /**
     * Release position lock.
     */
    protected void releaseLockedPosition() {
        positionLocked = false;
        wasFlyingAtLock = false;
        onPositionLockChanged(false);
    }

    /**
     * Whether the entity was flying when the position lock was captured.
     */
    public boolean wasFlyingAtLock() {
        return wasFlyingAtLock;
    }

    /**
     * Hook for subclass to react to position lock state changes (e.g., NPC sets data watcher flag).
     */
    protected void onPositionLockChanged(boolean locked) {
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROTATION LOCKING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Release rotation control.
     */
    protected void releaseRotationControl() {
        rotationLocked = false;
        onRotationLockChanged(false);
    }

    /**
     * Hook for subclass to react to rotation lock state changes (e.g., NPC sets flag, clears hitScan).
     */
    protected void onRotationLockChanged(boolean locked) {
    }

    // ═══════════════════════════════════════════════════════════════════
    // CORE TICK - Phase transition logic shared between NPC and Player
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Tick the currently executing ability. Handles all phase transitions,
     * telegraph/animation/sound lifecycle, and burst management.
     * <p>
     * This is the core shared logic between DataAbilities and PlayerAbilityData.
     * Subclass-specific behavior is injected via abstract methods and hooks.
     */
    protected void tickCurrentAbility() {
        // Handle chain delay between entries
        if (chainDelayRemaining > 0) {
            chainDelayRemaining--;
            if (chainDelayRemaining <= 0) {
                EntityLivingBase chainTarget = getTarget();
                // Check if target is dead and retarget
                if (currentChain != null && chainTarget != null && chainTarget.isDead) {
                    chainTarget = retargetForChain();
                    if (chainTarget == null) {
                        completeChain();
                        return;
                    }
                }
                if (startChainEntry(chainTarget)) {
                    launchConsecutiveConcurrentEntries(chainTarget);
                }
            }
            // Tick concurrent slots even during chain delay
            tickConcurrentSlots();
            return;
        }

        EntityLivingBase entity = getEntity();
        EntityLivingBase target = getTarget();
        AbilityPhase oldPhase = currentAbility.getPhase();

        // Tick advances time and possibly changes phase
        boolean phaseChanged = currentAbility.tick();

        // Fire tick event
        fireTickEvent(currentAbility, target);

        // Fire extender tick hook (e.g., per-tick resource drain)
        if (!AbilityController.Instance.fireOnAbilityTick(currentAbility, entity, target,
            currentAbility.getPhase(), currentAbility.getCurrentTick())) {
            interruptCurrentAbility(null, 0);
            return;
        }

        // Handle phase-specific logic
        switch (currentAbility.getPhase()) {
            case WINDUP:
                if (phaseChanged && oldPhase == AbilityPhase.BURST_DELAY) {
                    // Burst replay: re-enter windup - set up locks, telegraph, sound, animation
                    if (currentAbility.isRotationLockedDuringWindup()) {
                        captureLockedRotation();
                    }
                    if (currentAbility.isMovementLockedDuringWindup() && !currentAbility.hasAbilityMovement()) {
                        captureLockedPosition();
                    }
                    spawnTelegraph(currentAbility, target);
                    playAbilitySound(currentAbility.getWindUpSound());
                    playAbilityAnimation(currentAbility.getWindUpAnimation());
                }
                currentAbility.onWindUpTick(entity, target, currentAbility.getCurrentTick());
                break;

            case ACTIVE:
                if (phaseChanged && (oldPhase == AbilityPhase.WINDUP || oldPhase == AbilityPhase.BURST_DELAY)) {
                    // Just entered ACTIVE phase - lock telegraph positions
                    for (TelegraphInstance telegraph : currentAbility.getTelegraphInstances()) {
                        telegraph.lockPosition();
                    }

                    // Remove telegraph unless the ability keeps it during active phase
                    if (!currentAbility.keepTelegraphDuringActive()) {
                        removeTelegraph(currentAbility);
                    }

                    // Handle rotation control transition from WINDUP to ACTIVE
                    if (currentAbility.isRotationLockedDuringActive()) {
                        if (!rotationLocked) {
                            captureLockedRotation();
                        }
                    } else if (rotationLocked) {
                        releaseRotationControl();
                    }

                    // Handle position lock transition from WINDUP to ACTIVE
                    if (currentAbility.isMovementLockedDuringActive() && !currentAbility.hasAbilityMovement()) {
                        if (!positionLocked) {
                            captureLockedPosition();
                        }
                    } else if (positionLocked) {
                        releaseLockedPosition();
                    }

                    // Play active sound and animation
                    if (!currentAbility.keepTelegraphDuringActive()) {
                        playAbilitySound(currentAbility.getActiveSound());
                    }
                    playAbilityAnimation(currentAbility.getActiveAnimation());

                    // Pre-execute hook (NPC: snap to face target for hit scan)
                    onPreExecute(currentAbility, target);

                    // Fire execute event (cancelable)
                    if (fireExecuteEvent(currentAbility, target)) {
                        // Script cancelled — abort cleanly instead of leaving stuck in ACTIVE
                        currentAbility.interrupt();
                        handleAbilityCompletion(target);
                        return;
                    }

                    // Call onExecute
                    currentAbility.onExecute(entity, target);

                    // Check if ability completed during onExecute
                    if (currentAbility.getPhase() == AbilityPhase.IDLE) {
                        handleAbilityCompletion(target);
                        return;
                    }
                }

                currentAbility.onActiveTick(entity, target, currentAbility.getCurrentTick());

                // Check if ability completed during onActiveTick
                if (currentAbility.getPhase() == AbilityPhase.IDLE) {
                    handleAbilityCompletion(target);
                    return;
                }

                // Auto-complete for burst overlap mode only.
                // Non-overlap waits for all entities to die naturally (via allDead check in onActiveTick).
                if (currentAbility.isBurstEnabled()
                    && currentAbility.isBurstOverlap()
                    && currentAbility.getBurstIndex() < currentAbility.getBurstAmount()
                    && currentAbility.getPhase() == AbilityPhase.ACTIVE
                    && currentAbility.isReadyForBurstCompletion(currentAbility.getCurrentTick())) {
                    currentAbility.signalCompletion();
                }

                // Check if ability entered burst delay
                if (currentAbility.getPhase() == AbilityPhase.BURST_DELAY) {
                    if (rotationLocked) releaseRotationControl();
                    if (positionLocked) releaseLockedPosition();
                    onBurstDelayReleaseLocks();
                }
                break;

            case BURST_DELAY:
                // Free movement and rotation during burst delay
                if (rotationLocked) releaseRotationControl();
                if (positionLocked) releaseLockedPosition();
                break;

            case DAZED:
                // Dazed phase - just wait for phase transition
                break;

            case IDLE:
                // Ability completed (reached via tick() from DAZED)
                handleAbilityCompletion(target);
                break;
        }

        // Post-phase hook (NPC: hit scan + movement control)
        onPostPhaseTick(currentAbility, target);

        // Tick concurrent slots independently
        tickConcurrentSlots();
    }

    // ═══════════════════════════════════════════════════════════════════
    // ABILITY COMPLETION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Handle ability completion. Called when ability phase becomes IDLE.
     * If a chain is active, advances to the next entry instead of completing.
     */
    protected void handleAbilityCompletion(EntityLivingBase target) {
        if (currentAbility == null) return;

        // Remove any remaining telegraphs
        removeTelegraph(currentAbility);

        // Fire extender complete hook
        AbilityController.Instance.fireOnAbilityComplete(currentAbility, getEntity(), target, false);

        // Call onComplete callback
        currentAbility.onComplete(getEntity(), target);

        // Fire complete event
        fireCompleteEvent(currentAbility, target);

        // Release locks
        releaseRotationControl();
        releaseLockedPosition();
        stopAbilityAnimation();

        // Chain mode: advance to next entry instead of completing
        if (currentChain != null) {
            // AFTER semantics: delay from the CURRENT (just-completed) entry
            ChainedAbilityEntry completedEntry = currentChain.getEntries().get(chainEntryIndex);
            int delay = completedEntry.getDelayTicks();

            chainEntryIndex++;

            // Skip any consecutive concurrent entries — launch them to concurrent slots
            launchConsecutiveConcurrentEntries(target);

            if (chainEntryIndex < currentChain.getEntries().size()) {
                // Check if target died and retarget
                if (target != null && target.isDead) {
                    target = retargetForChain();
                    if (target == null) {
                        completeChain();
                        return;
                    }
                }

                // Enforce minimum 1-tick delay between chain entries
                chainDelayRemaining = Math.max(1, delay);
                currentAbility = null;
                return;
            }
            // All entries complete
            completeChain();
            return;
        }

        // Normal (non-chain) completion
        if (interruptCooldownRolled) {
            // Cooldown was already rolled during interrupt (e.g., chain interrupted, NPC ticked through DAZED)
            interruptCooldownRolled = false;
        } else {
            rollCooldown(currentAbility);
        }
        onAbilityComplete();
    }

    // ═══════════════════════════════════════════════════════════════════
    // EXECUTE IMMEDIATE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Execute an ability immediately (no windup). Called when windUpTicks is 0.
     */
    protected void executeImmediate(Ability ability, EntityLivingBase target) {
        // Play active sound and animation
        playAbilitySound(ability.getActiveSound());
        playAbilityAnimation(ability.getActiveAnimation());

        // Fire execute event (cancelable)
        if (fireExecuteEvent(ability, target)) {
            // Script cancelled — abort cleanly instead of leaving stuck in ACTIVE
            ability.interrupt();
            handleAbilityCompletion(target);
            return;
        }

        // Call onExecute
        ability.onExecute(getEntity(), target);

        // Check if ability completed during onExecute
        if (ability.getPhase() == AbilityPhase.IDLE) {
            handleAbilityCompletion(target);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // INTERRUPTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Interrupt the currently executing ability.
     * Shared interrupt logic: remove telegraph, play dazed animation,
     * release locks, fire events, call ability.interrupt().
     */
    public void interruptCurrentAbility(DamageSource source, float damage) {
        if (currentAbility != null && currentAbility.isExecuting()) {
            // Remove telegraph
            removeTelegraph(currentAbility);

            // Fire extender complete hook (interrupted)
            AbilityController.Instance.fireOnAbilityComplete(currentAbility, getEntity(), getTarget(), true);

            // Fire interrupt event
            fireInterruptEvent(currentAbility, getTarget(), source, damage);

            // Call ability callbacks
            currentAbility.onInterrupt(getEntity(), source, damage);
            currentAbility.interrupt();

            // Play dazed animation
            stopAbilityAnimation();
            playAbilityAnimation(currentAbility.getDazedAnimation());

            // Release locks
            releaseRotationControl();
            releaseLockedPosition();

            // If chain was active, roll chain cooldown and clear chain state
            if (currentChain != null) {
                rollChainCooldown(currentChain);
                interruptCooldownRolled = true;
                currentChain = null;
                chainEntryIndex = -1;
                chainDelayRemaining = -1;
            }

            // Interrupt all concurrent slots
            interruptConcurrentSlots();

            // Let subclass handle cleanup
            onInterruptComplete();
        }
    }

    /**
     * Hook called after interrupt completes. Subclass handles clearing state and syncing.
     * NPC: keeps currentAbility (ticks through DAZED). Player: clears immediately.
     */
    protected void onInterruptComplete() {
    }

    /**
     * Cancel the currently executing ability (voluntary player action).
     * Unlike interrupt, this always goes directly to IDLE (never DAZED),
     * immediately rolls cooldown, and clears state.
     * Also handles cancellation during chain delay (between chain entries).
     */
    public void cancelCurrentAbility() {
        boolean hasExecutingAbility = currentAbility != null && currentAbility.isExecuting();
        boolean isInChainDelay = currentChain != null && chainDelayRemaining > 0;

        if (!hasExecutingAbility && !isInChainDelay) return;

        if (hasExecutingAbility) {
            AbilityPhase phase = currentAbility.getPhase();
            // Cannot cancel during DAZED (already interrupted)
            if (phase == AbilityPhase.DAZED) return;
            // Only allow cancel during active phases
            if (phase != AbilityPhase.WINDUP && phase != AbilityPhase.ACTIVE
                && phase != AbilityPhase.BURST_DELAY) return;

            // Remove telegraph
            removeTelegraph(currentAbility);

            // Fire extender complete hook (cancelled = interrupted)
            AbilityController.Instance.fireOnAbilityComplete(currentAbility, getEntity(), getTarget(), true);

            // Fire interrupt event (source=null, damage=0 for voluntary cancel)
            fireInterruptEvent(currentAbility, getTarget(), null, 0);

            // Cancel the ability (cleanup + go directly to IDLE, no DAZED)
            currentAbility.onInterrupt(getEntity(), null, 0);
            currentAbility.cancel();

            // Release locks and stop animation
            stopAbilityAnimation();
            releaseRotationControl();
            releaseLockedPosition();
        }

        // Roll cooldown
        if (currentChain != null) {
            rollChainCooldown(currentChain);
            currentChain = null;
            chainEntryIndex = -1;
            chainDelayRemaining = -1;
        } else if (currentAbility != null) {
            rollCooldown(currentAbility);
        }

        // Interrupt all concurrent slots
        interruptConcurrentSlots();

        // Clear state via the normal completion path
        onAbilityComplete();
    }

    // ═══════════════════════════════════════════════════════════════════
    // CHAINED ABILITY EXECUTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Start executing a chained ability. Sets up chain state and starts the first entry.
     *
     * @param chain  The chained ability to execute (should be a deep copy)
     * @param target The initial target
     * @return true if the chain was started
     */
    protected boolean startChain(ChainedAbility chain, EntityLivingBase target) {
        if (chain == null || chain.getEntries().isEmpty()) return false;

        currentChain = chain;
        chainEntryIndex = 0;
        chainDelayRemaining = -1;

        // AFTER semantics: no delay before first entry (delay applies after each entry completes)
        boolean started = startChainEntry(target);
        if (started) {
            launchConsecutiveConcurrentEntries(target);
        }
        return started;
    }

    /**
     * Start the current chain entry's ability.
     */
    protected boolean startChainEntry(EntityLivingBase target) {
        if (currentChain == null || chainEntryIndex < 0 || chainEntryIndex >= currentChain.getEntries().size()) {
            completeChain();
            return false;
        }

        ChainedAbilityEntry entry = currentChain.getEntries().get(chainEntryIndex);
        Ability ability = entry.resolve();
        if (ability == null) {
            // Broken reference - complete chain
            completeChain();
            return false;
        }

        // Validate the resolved ability's UserType matches the chain's allowed context.
        // The chain's own UserType was already checked at selection time, but individual
        // abilities may have stricter restrictions (e.g., PLAYER_ONLY in an NPC chain).
        if (!ability.getAllowedBy().allowsNpc() && getEntity() instanceof noppes.npcs.entity.EntityNPCInterface) {
            // Skip this entry - ability doesn't allow NPCs
            completeChain();
            return false;
        }
        if (!ability.getAllowedBy().allowsPlayer() && getEntity() instanceof net.minecraft.entity.player.EntityPlayer) {
            // Skip this entry - ability doesn't allow players
            completeChain();
            return false;
        }

        // If windUpAll=false, only the first ability windups; subsequent abilities skip windup
        if (!currentChain.isWindUpAll() && chainEntryIndex > 0) {
            ability.setWindUpTicks(0);
        }

        // Start the ability (skip its own conditions - chain conditions were already checked)
        currentAbility = ability;
        ability.start(target);

        if (ability.getPhase() == AbilityPhase.ACTIVE) {
            // Windup was 0 - capture locks for immediate active phase, then execute
            if (ability.isRotationLockedDuringActive()) {
                captureLockedRotation();
            }
            if (ability.isMovementLockedDuringActive() && !ability.hasAbilityMovement()) {
                captureLockedPosition();
            }
            executeImmediate(ability, target);
        } else {
            // Normal windup flow
            if (ability.isRotationLockedDuringWindup()) {
                captureLockedRotation();
            }
            if (ability.isMovementLockedDuringWindup() && !ability.hasAbilityMovement()) {
                captureLockedPosition();
            }
            spawnTelegraph(ability, target);
            playAbilitySound(ability.getWindUpSound());
            playAbilityAnimation(ability.getWindUpAnimation());
        }

        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONCURRENT SLOT MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Tick all active concurrent slots. Remove completed ones.
     * Called from tickCurrentAbility() and during chain delay countdown.
     */
    protected void tickConcurrentSlots() {
        if (concurrentSlots.isEmpty()) return;

        EntityLivingBase entity = getEntity();
        EntityLivingBase target = getTarget();

        Iterator<ConcurrentSlot> it = concurrentSlots.iterator();
        while (it.hasNext()) {
            ConcurrentSlot slot = it.next();
            slot.tick(entity, target);
            if (slot.isCompleted()) {
                it.remove();
            }
        }
    }

    /**
     * After starting a primary chain entry, scan forward and launch any consecutive
     * concurrent entries (with delay=0) to concurrent slots immediately.
     * This enables simultaneous execution of Effect abilities alongside the primary.
     */
    protected void launchConsecutiveConcurrentEntries(EntityLivingBase target) {
        if (currentChain == null) return;

        List<ChainedAbilityEntry> entries = currentChain.getEntries();
        while (chainEntryIndex + 1 < entries.size()) {
            ChainedAbilityEntry nextEntry = entries.get(chainEntryIndex + 1);
            Ability resolved = nextEntry.resolve();
            if (resolved == null || !resolved.isConcurrentCapable()
                || !nextEntry.isConcurrentEnabled()) break;

            // Only auto-launch concurrent entries with 0 delay
            if (nextEntry.getDelayTicks() > 0) break;

            // Advance past this entry in the chain (it runs concurrently, not sequentially)
            chainEntryIndex++;

            // Deep copy the ability so it has its own state
            Ability concurrentCopy = AbilityController.Instance.fromNBT(resolved.writeNBT());
            if (concurrentCopy == null) continue;

            ConcurrentSlot slot = new ConcurrentSlot(concurrentCopy);
            concurrentSlots.add(slot);
            slot.start(getEntity(), target);
        }
    }

    /**
     * Interrupt all active concurrent slots. Called on ability interrupt or combat reset.
     */
    protected void interruptConcurrentSlots() {
        for (ConcurrentSlot slot : concurrentSlots) {
            slot.interrupt();
        }
        concurrentSlots.clear();
    }

    /**
     * Complete the chain execution. Rolls chain cooldown and clears all chain state.
     * NOTE: Does NOT clear concurrent slots — they outlive the chain and tick independently.
     */
    protected void completeChain() {
        if (currentChain != null) {
            rollChainCooldown(currentChain);
        }
        currentChain = null;
        chainEntryIndex = -1;
        chainDelayRemaining = -1;
        currentAbility = null;
        onAbilityComplete();
    }
}
