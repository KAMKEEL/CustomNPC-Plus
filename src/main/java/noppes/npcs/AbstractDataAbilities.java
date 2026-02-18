package noppes.npcs;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.ChainedAbilityEntry;
import kamkeel.npcs.controllers.data.ability.ToggleEntry;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import java.util.ArrayList;
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
     * World time when cooldown ends
     */
    protected long cooldownEndTime = 0;

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
     * Currently active toggles. Key = ability key, Value = toggle entry with tick counter.
     */
    protected Map<String, ToggleEntry> activeToggles = new LinkedHashMap<>();

    /**
     * Toggle an ability ON or OFF. If currently on, turns off; if off, turns on.
     *
     * @param key The ability key (e.g., "npcdbc:ki_fist")
     * @return true if the toggle is now ON, false if OFF
     */
    public boolean toggleAbility(String key) {
        if (activeToggles.containsKey(key)) {
            deactivateToggle(key);
            return false;
        } else {
            return activateToggle(key);
        }
    }

    /**
     * Set a toggle to a specific state.
     *
     * @param key The ability key
     * @param on  true to activate, false to deactivate
     */
    public void setAbilityToggled(String key, boolean on) {
        if (on && !activeToggles.containsKey(key)) {
            activateToggle(key);
        } else if (!on && activeToggles.containsKey(key)) {
            deactivateToggle(key);
        }
    }

    /**
     * Check if a toggle ability is currently active.
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

    private boolean activateToggle(String key) {
        Ability ability = AbilityController.Instance != null
            ? AbilityController.Instance.resolveAbility(key) : null;
        if (ability == null || !ability.isToggleable()) return false;

        ToggleEntry entry = new ToggleEntry(ability);
        activeToggles.put(key, entry);
        ability.onToggleOn(getEntity());
        onToggleStateChanged(key, true);
        return true;
    }

    private void deactivateToggle(String key) {
        ToggleEntry entry = activeToggles.remove(key);
        if (entry != null) {
            entry.getAbility().onToggleOff(getEntity());
            onToggleStateChanged(key, false);
        }
    }

    /**
     * Tick all active toggles. Call from tick() each game tick.
     * Toggles with hasActiveToggle=true get their onToggleTick() called.
     * If onToggleTick returns false, the toggle is auto-deactivated.
     */
    protected void tickActiveToggles() {
        if (activeToggles.isEmpty()) return;

        EntityLivingBase entity = getEntity();
        List<String> toRemove = null;

        for (Map.Entry<String, ToggleEntry> mapEntry : activeToggles.entrySet()) {
            ToggleEntry entry = mapEntry.getValue();
            entry.incrementTick();

            if (entry.getAbility().hasActiveToggle()) {
                if (!entry.getAbility().onToggleTick(entity, entry.getTickCount())) {
                    if (toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(mapEntry.getKey());
                }
            }
        }

        if (toRemove != null) {
            for (String key : toRemove) {
                ToggleEntry entry = activeToggles.remove(key);
                if (entry != null) {
                    entry.getAbility().onToggleOff(entity);
                    onToggleStateChanged(key, false);
                }
            }
        }
    }

    /**
     * Hook for subclass to react to toggle state changes (sync packets, script events).
     */
    protected void onToggleStateChanged(String key, boolean active) {
    }

    /**
     * Clear all active toggles (e.g., on death/reset).
     * Calls onToggleOff for each active toggle.
     */
    protected void clearActiveToggles() {
        if (activeToggles.isEmpty()) return;
        EntityLivingBase entity = getEntity();
        for (ToggleEntry entry : activeToggles.values()) {
            entry.getAbility().onToggleOff(entity);
        }
        activeToggles.clear();
    }

    /**
     * Add a toggle entry directly without calling onToggleOn.
     * Used for client-side sync and NBT restoration.
     */
    public void setToggleEntryDirect(String key, boolean active) {
        if (active) {
            Ability ability = AbilityController.Instance != null
                ? AbilityController.Instance.resolveAbility(key) : null;
            if (ability != null && ability.isToggleable()) {
                activeToggles.put(key, new ToggleEntry(ability));
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
        onPositionLockChanged(true);
    }

    /**
     * Release position lock.
     */
    protected void releaseLockedPosition() {
        positionLocked = false;
        onPositionLockChanged(false);
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
                startChainEntry(chainTarget);
            }
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

                // Auto-complete for burst overlap mode
                if (currentAbility.isBurstEnabled()
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
            chainEntryIndex++;
            if (chainEntryIndex < currentChain.getEntries().size()) {
                ChainedAbilityEntry nextEntry = currentChain.getEntries().get(chainEntryIndex);

                // Check if target died and retarget
                if (target != null && target.isDead) {
                    target = retargetForChain();
                    if (target == null) {
                        completeChain();
                        return;
                    }
                }

                int delay = nextEntry.getDelayTicks();
                if (delay > 0) {
                    chainDelayRemaining = delay;
                    currentAbility = null;
                } else {
                    startChainEntry(target);
                }
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

        // Check if first entry has a delay
        ChainedAbilityEntry firstEntry = chain.getEntries().get(0);
        int delay = firstEntry.getDelayTicks();
        if (delay > 0) {
            chainDelayRemaining = delay;
            return true;
        }

        return startChainEntry(target);
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

    /**
     * Complete the chain execution. Rolls chain cooldown and clears all chain state.
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
