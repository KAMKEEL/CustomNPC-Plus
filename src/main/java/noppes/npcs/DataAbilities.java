package noppes.npcs;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.ability.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.AbilitySlot;
import kamkeel.npcs.controllers.data.ability.type.AbilityGuard;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphRemovePacket;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphSpawnPacket;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.AbilityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages NPC abilities - storage, selection, execution, and cooldowns.
 * Follows the DataStats/DataAI pattern for NPC data management.
 */
public class DataAbilities {

    private final EntityNPCInterface npc;
    private final Random random = new Random();

    // ═══════════════════════════════════════════════════════════════════
    // CONFIGURATION (saved to NBT)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * List of ability slots this NPC can use (inline or reference).
     */
    private List<AbilitySlot> abilitySlots = new ArrayList<>();

    /**
     * Whether the ability system is enabled for this NPC
     */
    public boolean enabled = false;

    /**
     * Minimum cooldown ticks between abilities (global minimum)
     */
    public int minCooldown = 80;

    /**
     * Maximum cooldown ticks between abilities (global maximum)
     */
    public int maxCooldown = 200;

    // ═══════════════════════════════════════════════════════════════════
    // RUNTIME STATE (not saved)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Currently executing ability (null if none)
     */
    private transient Ability currentAbility;

    /**
     * World time when NPC cooldown ends (can select next ability)
     */
    private transient long cooldownEndTime = 0;

    /**
     * Last target used for ability execution
     */
    private transient EntityLivingBase lastTarget;

    /**
     * Recent hit timestamps for hit count condition
     */
    private transient List<Long> recentHitTimes = new ArrayList<>();

    /**
     * Locked rotation values for ACTIVE phase when movement is locked.
     * Stored when entering ACTIVE phase, applied every tick to prevent rotation changes.
     */
    private transient boolean rotationLocked = false;
    private transient float lockedYaw = 0;
    private transient float lockedYawHead = 0;
    private transient float lockedRenderYawOffset = 0;
    private transient float lockedPitch = 0;

    // ═══════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════

    public DataAbilities(EntityNPCInterface npc) {
        this.npc = npc;
    }

    // ═══════════════════════════════════════════════════════════════════
    // TICK - Called from EntityNPCInterface.onLivingUpdate()
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Main tick method - handles ability execution and cooldown management.
     * Should be called every tick from onLivingUpdate().
     */
    public void tick() {
        if (!enabled || npc.worldObj.isRemote || npc.isKilled()) {
            return;
        }

        // Tick current ability if executing
        if (currentAbility != null && currentAbility.isExecuting()) {
            tickCurrentAbility();
        }
    }

    /**
     * Tick the currently executing ability.
     */
    private void tickCurrentAbility() {
        EntityLivingBase target = lastTarget != null ? lastTarget : npc.getAttackTarget();
        AbilityPhase oldPhase = currentAbility.getPhase();

        // Tick advances time and possibly changes phase
        boolean phaseChanged = currentAbility.tick();

        // Fire tick event for scripts
        fireTickEvent(currentAbility, target);

        // Handle phase-specific logic
        switch (currentAbility.getPhase()) {
            case WINDUP:
                currentAbility.onWindUpTick(npc, target, npc.worldObj, currentAbility.getCurrentTick());
                break;

            case ACTIVE:
                if (phaseChanged && oldPhase == AbilityPhase.WINDUP) {
                    // Just entered ACTIVE phase - lock telegraph position if it was following
                    // This commits the ability to its current target position
                    TelegraphInstance telegraph = currentAbility.getTelegraphInstance();
                    if (telegraph != null) {
                        telegraph.lockPosition();
                    }

                    // Remove telegraph - it has served its purpose
                    removeTelegraph(currentAbility);

                    // Lock rotation if movement is locked during ACTIVE phase
                    // Capture current rotation values to force them every tick
                    if (currentAbility.isMovementLockedDuringActive()) {
                        captureLockedRotation();
                    }

                    // Play active sound and animation
                    playAbilitySound(currentAbility.getActiveSound());
                    playAbilityAnimation(currentAbility.getActiveAnimation());

                    // Fire execute event (cancelable)
                    AbilityEvent.ExecuteEvent executeEvent = new AbilityEvent.ExecuteEvent(
                        npc.wrappedNPC, currentAbility, target);
                    if (NpcAPI.EVENT_BUS.post(executeEvent)) {
                        // Event was cancelled - skip execution but continue to recovery
                        return;
                    }

                    // Call onExecute
                    currentAbility.onExecute(npc, target, npc.worldObj);

                    // Check if ability completed during onExecute (signalCompletion was called)
                    if (currentAbility.getPhase() == AbilityPhase.IDLE) {
                        handleAbilityCompletion(target);
                        return;
                    }
                }
                currentAbility.onActiveTick(npc, target, npc.worldObj, currentAbility.getCurrentTick());

                // Check if ability completed during onActiveTick (signalCompletion was called)
                if (currentAbility.getPhase() == AbilityPhase.IDLE) {
                    handleAbilityCompletion(target);
                    return;
                }
                break;

            case DAZED:
                // Dazed phase - NPC cannot attack, just wait for daze to end
                break;

            case IDLE:
                // Ability completed (reached via tick() phase transition from DAZED)
                handleAbilityCompletion(target);
                break;
        }

        // Apply movement control if ability is still executing
        if (currentAbility != null && currentAbility.isExecuting()) {
            applyMovementControl();
        }
    }

    /**
     * Apply movement control based on lock movement settings.
     * Called every tick during ability execution.
     */
    private void applyMovementControl() {
        if (currentAbility == null) return;

        // Check if movement should be locked for the current phase
        if (currentAbility.isMovementLockedForCurrentPhase()) {
            // Clear navigator to prevent AI pathfinding
            npc.getNavigator().clearPathEntity();

            // Zero motion if ability doesn't have its own movement
            // This allows abilities like Charge/Slam to control their own motion
            if (!currentAbility.hasAbilityMovement()) {
                npc.motionX = 0;
                npc.motionZ = 0;
            }
        }
    }

    /**
     * Handle ability completion - called when ability phase becomes IDLE.
     * This can happen from:
     * - signalCompletion() called in onExecute() or onActiveTick()
     * - tick() transitioning from DAZED to IDLE
     */
    private void handleAbilityCompletion(EntityLivingBase target) {
        if (currentAbility == null) return;

        // Call onComplete callback
        currentAbility.onComplete(npc, target);

        // Fire complete event
        AbilityEvent.CompleteEvent completeEvent = new AbilityEvent.CompleteEvent(
            npc.wrappedNPC, currentAbility, target);
        NpcAPI.EVENT_BUS.post(completeEvent);

        // Release rotation lock before cleanup
        releaseLockedRotation();

        // Clean up and roll cooldown (this sets currentAbility to null)
        onAbilityComplete();
    }

    /**
     * Called when an ability completes.
     * Calculates cooldown as: random(minCooldown, maxCooldown) + ability's cooldown offset
     */
    private void onAbilityComplete() {
        if (currentAbility != null) {
            // Stop any ability animation
            stopAbilityAnimation();

            // Release rotation lock
            releaseLockedRotation();

            // Calculate cooldown: random(min, max) + ability offset
            int abilityCooldownOffset = currentAbility.getCooldownTicks();
            rollCooldown();
            // Add ability-specific cooldown offset
            cooldownEndTime += abilityCooldownOffset;

            currentAbility = null;
            lastTarget = null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ABILITY SELECTION - Called from CombatHandler
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Try to select and start an ability for the given target.
     * Called by CombatHandler when NPC is in combat and ready for an ability.
     *
     * @param target The combat target
     * @return true if an ability was started
     */
    public boolean trySelectAndStart(EntityLivingBase target) {
        if (!canSelectAbility()) {
            return false;
        }

        Ability selected = selectAbility(target);
        if (selected == null) {
            return false;
        }

        return startAbility(selected, target);
    }

    /**
     * Check if an ability can be selected right now.
     */
    public boolean canSelectAbility() {
        if (!enabled || abilitySlots.isEmpty()) {
            return false;
        }
        if (currentAbility != null && currentAbility.isExecuting()) {
            return false;
        }
        // Check if still on cooldown
        if (npc.worldObj.getTotalWorldTime() < cooldownEndTime) {
            return false;
        }
        return true;
    }

    /**
     * Select an ability using weighted random from eligible abilities.
     */
    private Ability selectAbility(EntityLivingBase target) {
        List<Ability> eligible = new ArrayList<>();
        int totalWeight = 0;

        for (Ability ability : getAbilities()) {
            if (isAbilityEligible(ability, target)) {
                eligible.add(ability);
                totalWeight += ability.getWeight();
            }
        }

        if (eligible.isEmpty() || totalWeight <= 0) {
            return null;
        }

        // Weighted random selection
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Ability ability : eligible) {
            cumulative += ability.getWeight();
            if (roll < cumulative) {
                return ability;
            }
        }

        return eligible.get(eligible.size() - 1);
    }

    /**
     * Check if a specific ability is eligible for use.
     */
    private boolean isAbilityEligible(Ability ability, EntityLivingBase target) {
        // Check enabled
        if (!ability.isEnabled()) {
            return false;
        }

        // Check if already executing
        if (ability.isExecuting()) {
            return false;
        }

        // Check range
        if (target != null) {
            float distance = npc.getDistanceToEntity(target);
            if (distance < ability.getMinRange() || distance > ability.getMaxRange()) {
                return false;
            }
        }

        // Check conditions
        if (!ability.checkConditions(npc, target)) {
            return false;
        }

        return true;
    }

    /**
     * Start executing an ability.
     */
    private boolean startAbility(Ability ability, EntityLivingBase target) {
        // Fire start event (cancelable)
        AbilityEvent.StartEvent startEvent = new AbilityEvent.StartEvent(
            npc.wrappedNPC, ability, target);
        if (NpcAPI.EVENT_BUS.post(startEvent)) {
            // Event was cancelled - don't start the ability
            return false;
        }

        currentAbility = ability;
        lastTarget = target;
        ability.start(target);

        // Spawn telegraph and send to clients
        spawnTelegraph(ability, target);

        // Play wind up sound if configured
        playAbilitySound(ability.getWindUpSound());

        // Play wind up animation if configured
        playAbilityAnimation(ability.getWindUpAnimation());

        return true;
    }

    /**
     * Play a sound at the NPC's location.
     */
    private void playAbilitySound(String sound) {
        if (sound != null && !sound.isEmpty()) {
            npc.worldObj.playSoundAtEntity(npc, sound, 1.0f, 1.0f);
        }
    }

    /**
     * Play an animation on the NPC by ID.
     * Public so abilities can trigger animations directly if needed.
     *
     * @param animation The animation to play, or null if none
     */
    public void playAbilityAnimation(Animation animation) {
        if (animation == null) return;
        if (AnimationController.Instance == null) return;

        npc.display.animationData.setEnabled(true);
        npc.display.animationData.setAnimation(animation);
        npc.display.animationData.updateClient();
    }

    public void playAbilityAnimation(int animation) {
        if (animation < 0) return;
        if (AnimationController.Instance == null) return;
        if (AnimationController.Instance.get(animation) == null) return;

        playAbilityAnimation((Animation) AnimationController.Instance.get(animation));
    }

    /**
     * Stop any currently playing ability animation.
     */
    private void stopAbilityAnimation() {
        npc.display.animationData.setAnimation(null);
        npc.display.animationData.updateClient();
    }

    /**
     * Spawn and send telegraph for an ability.
     */
    private void spawnTelegraph(Ability ability, EntityLivingBase target) {
        TelegraphInstance telegraph = ability.createTelegraph(npc, target);
        if (telegraph != null) {
            ability.setTelegraphInstance(telegraph);
            // Send to all nearby players
            TelegraphSpawnPacket.sendToTracking(telegraph, npc);
        }
    }

    /**
     * Remove telegraph when ability ends or is interrupted.
     */
    private void removeTelegraph(Ability ability) {
        TelegraphInstance telegraph = ability.getTelegraphInstance();
        if (telegraph != null) {
            TelegraphRemovePacket.sendToTracking(telegraph.getInstanceId(), npc);
            ability.setTelegraphInstance(null);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // COOLDOWN MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if NPC is on cooldown (cannot use any ability).
     */
    public boolean isOnCooldown() {
        return npc.worldObj.getTotalWorldTime() < cooldownEndTime;
    }

    /**
     * Get remaining cooldown ticks.
     */
    public long getRemainingCooldown() {
        long remaining = cooldownEndTime - npc.worldObj.getTotalWorldTime();
        return remaining > 0 ? remaining : 0;
    }

    /**
     * Reset cooldown (allow immediate ability use).
     */
    public void resetCooldown() {
        cooldownEndTime = 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // INTERRUPTION - Called from CombatHandler on damage
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Handle damage taken - may interrupt current ability or trigger guard counter.
     *
     * @param source The damage source
     * @param amount The damage amount
     * @return true if ability was interrupted
     */
    public boolean onDamage(DamageSource source, float amount) {
        // Track hit for hit count condition
        recordHit();

        // Check if currently executing a Guard ability and notify it of damage taken
        if (currentAbility instanceof AbilityGuard) {
            AbilityGuard guard = (AbilityGuard) currentAbility;
            if (guard.isGuarding() && source.getEntity() instanceof EntityLivingBase) {
                guard.onDamageTaken(npc, (EntityLivingBase) source.getEntity(), source, amount);
            }
        }

        if (currentAbility == null || !currentAbility.isExecuting()) {
            return false;
        }

        if (currentAbility.canInterrupt(source)) {
            interruptCurrentAbility(source, amount);
            return true;
        }

        return false;
    }

    /**
     * Get the damage reduction factor if a Guard ability is currently active.
     *
     * @return The damage reduction factor (0.0 = no reduction, 1.0 = full immunity), or 0 if not guarding
     */
    public float getGuardDamageReduction() {
        if (currentAbility instanceof AbilityGuard) {
            return ((AbilityGuard) currentAbility).getDamageReductionFactor();
        }
        return 0.0f;
    }

    /**
     * Record a hit for the hit count condition.
     * Includes periodic cleanup to prevent memory leak.
     */
    private void recordHit() {
        long currentTime = npc.worldObj.getTotalWorldTime();
        recentHitTimes.add(currentTime);

        // Cleanup old entries periodically to prevent unbounded growth
        // Keep only hits from last 5 minutes (6000 ticks) max
        if (recentHitTimes.size() > 50) {
            long cutoff = currentTime - 6000;
            recentHitTimes.removeIf(time -> time < cutoff);
        }
    }

    /**
     * Get the number of hits received within the specified tick window.
     *
     * @param withinTicks The time window in ticks
     * @return Number of hits in that window
     */
    public int getRecentHitCount(int withinTicks) {
        long currentTime = npc.worldObj.getTotalWorldTime();
        long cutoff = currentTime - withinTicks;

        // Clean up old entries and count
        int count = 0;
        recentHitTimes.removeIf(time -> time < cutoff);
        for (Long time : recentHitTimes) {
            if (time >= cutoff) {
                count++;
            }
        }
        return count;
    }

    /**
     * Interrupt the currently executing ability.
     * If interrupted during WINDUP, the ability will transition to DAZED phase.
     * When DAZED completes, the ability will move to IDLE and onAbilityComplete() is called.
     */
    public void interruptCurrentAbility(DamageSource source, float damage) {
        if (currentAbility != null) {
            // Remove telegraph early
            removeTelegraph(currentAbility);

            // Stop any ability animation
            stopAbilityAnimation();

            // Release rotation lock
            releaseLockedRotation();

            // Fire interrupt event
            AbilityEvent.InterruptEvent interruptEvent = new AbilityEvent.InterruptEvent(
                npc.wrappedNPC, currentAbility, lastTarget, source, damage);
            NpcAPI.EVENT_BUS.post(interruptEvent);

            currentAbility.onInterrupt(npc, source, damage);
            currentAbility.interrupt(); // Transitions to DAZED if interrupted during WINDUP

            // Don't clear currentAbility - let it tick through DAZED phase
            // When DAZED ends and phase becomes IDLE, onAbilityComplete() will be called
        }
    }

    /**
     * Force stop the current ability (no interrupt event).
     */
    public void stopCurrentAbility() {
        if (currentAbility != null) {
            removeTelegraph(currentAbility);
            stopAbilityAnimation();
            releaseLockedRotation();
            currentAbility.interrupt();
            currentAbility = null;
            lastTarget = null;
        }
    }

    /**
     * Force start a specific ability, bypassing normal selection and cooldown.
     * If an ability is currently executing, it will be cancelled.
     *
     * @param ability The ability to start
     * @param target The target entity (can be null for self-targeted abilities)
     * @return true if the ability was started successfully
     */
    public boolean forceStartAbility(Ability ability, EntityLivingBase target) {
        if (ability == null || npc.worldObj.isRemote) {
            return false;
        }

        // Stop any currently executing ability
        stopCurrentAbility();

        // Reset the ability state
        ability.reset();

        // Start the ability directly
        return startAbility(ability, target);
    }

    /**
     * Execute an ability on this NPC by key (built-in name or custom UUID).
     * The NPC does NOT need to have this ability assigned.
     *
     * @param key The ability key (built-in name or custom UUID)
     * @param target The target entity (can be null for self-targeted abilities)
     * @return true if the ability was started successfully
     */
    public boolean executeAbility(String key, EntityLivingBase target) {
        if (key == null || key.isEmpty() || npc.worldObj.isRemote) {
            return false;
        }

        Ability resolved = AbilityController.Instance.resolveAbility(key);
        if (resolved == null) {
            return false;
        }

        return forceStartAbility(resolved, target);
    }

    // ═══════════════════════════════════════════════════════════════════
    // SCRIPT EVENTS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Fire the ability tick event for scripts.
     */
    private void fireTickEvent(Ability ability, EntityLivingBase target) {
        AbilityEvent.TickEvent event = new AbilityEvent.TickEvent(
            npc.wrappedNPC, ability, target,
            ability.getPhase().ordinal(), ability.getCurrentTick());
        NpcAPI.EVENT_BUS.post(event);
    }

    /**
     * Fire an ability hit event. Called by abilities when they hit an entity.
     * Returns null if the event was cancelled, otherwise returns the (possibly modified) event.
     *
     * @param ability     The ability doing the hit
     * @param target      The original target of the ability
     * @param hitEntity   The entity being hit
     * @param damage      The damage amount
     * @param knockback   The horizontal knockback
     * @param knockbackUp The vertical knockback
     * @return The event (with possibly modified values), or null if cancelled
     */
    public AbilityEvent.HitEvent fireHitEvent(Ability ability, EntityLivingBase target,
                                              EntityLivingBase hitEntity, float damage,
                                              float knockback, float knockbackUp) {
        AbilityEvent.HitEvent event = new AbilityEvent.HitEvent(
            npc.wrappedNPC, ability, target, hitEntity, damage, knockback, knockbackUp);
        if (NpcAPI.EVENT_BUS.post(event)) {
            return null; // Cancelled
        }
        return event;
    }

    /**
     * Get the NPC this DataAbilities belongs to.
     */
    public EntityNPCInterface getNpc() {
        return npc;
    }

    // ═══════════════════════════════════════════════════════════════════
    // RESET - Called on NPC death/respawn
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Reset all runtime state (cooldowns, current ability).
     * Called when NPC respawns or combat ends.
     * Rolls cooldown so NPC doesn't immediately use an ability.
     */
    public void reset() {
        stopCurrentAbility();

        // Roll cooldown so NPC doesn't immediately attack after reset
        rollCooldown();

        // Reset execution state on all resolved abilities
        for (Ability ability : getAbilities()) {
            ability.reset();
        }
    }

    /**
     * Roll a new cooldown using the min/max range.
     * Called after ability completes and on reset.
     */
    private void rollCooldown() {
        int baseCooldown = minCooldown;
        if (maxCooldown > minCooldown) {
            baseCooldown = minCooldown + random.nextInt(maxCooldown - minCooldown + 1);
        }
        cooldownEndTime = npc.worldObj.getTotalWorldTime() + baseCooldown;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ABILITY LIST MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get resolved abilities from all slots, filtering out broken references.
     */
    public List<Ability> getAbilities() {
        List<Ability> resolved = new ArrayList<>();
        for (AbilitySlot slot : abilitySlots) {
            Ability a = slot.getAbility();
            if (a != null) {
                resolved.add(a);
            }
        }
        return resolved;
    }

    /** Get the raw slot list (for GUI access). */
    public List<AbilitySlot> getAbilitySlots() {
        return abilitySlots;
    }

    /** Add an inline ability. */
    public void addAbility(Ability ability) {
        abilitySlots.add(AbilitySlot.inline(ability));
    }

    /** Add a reference ability by key (built-in name or custom UUID). */
    public void addAbilityReference(String key) {
        abilitySlots.add(AbilitySlot.reference(key));
    }

    public void removeAbility(int index) {
        if (index >= 0 && index < abilitySlots.size()) {
            abilitySlots.remove(index);
        }
    }

    public void removeAbility(String id) {
        abilitySlots.removeIf(slot -> {
            if (slot.isReference()) {
                return slot.getReferenceId().equals(id);
            }
            Ability a = slot.getAbility();
            return a != null && a.getId().equals(id);
        });
    }

    public Ability getAbility(String id) {
        for (AbilitySlot slot : abilitySlots) {
            Ability a = slot.getAbility();
            if (a != null && a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    /** Check if a slot at a given index is a reference. */
    public boolean isSlotReference(int index) {
        if (index < 0 || index >= abilitySlots.size()) return false;
        return abilitySlots.get(index).isReference();
    }

    /** Convert a reference slot to inline. Returns false if resolution fails. */
    public boolean convertToInline(int index) {
        if (index < 0 || index >= abilitySlots.size()) return false;
        return abilitySlots.get(index).convertToInline();
    }

    public void clearAbilities() {
        abilitySlots.clear();
    }

    public boolean isEmpty() {
        return abilitySlots.isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════
    // STATE QUERIES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if NPC is currently executing an ability.
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
     * Check if ability is controlling NPC movement (AI pathfinding should be blocked).
     * Called by AI tasks to determine if they should try to path.
     * <p>
     * Rules:
     * - WINDUP phase: Block AI if movement is locked during windup
     * - ACTIVE phase: Block AI if movement is locked during active OR hasAbilityMovement is true
     * - DAZED phase: Always block AI (NPC is stunned)
     */
    public boolean isAbilityControllingMovement() {
        if (currentAbility == null || !currentAbility.isExecuting()) {
            return false;
        }

        AbilityPhase phase = currentAbility.getPhase();

        switch (phase) {
            case WINDUP:
                // During windup, only block if movement is locked during windup
                // This allows NPC to keep chasing while winding up if not locked
                return currentAbility.isMovementLockedDuringWindup();

            case ACTIVE:
                // During active phase, block if either:
                // - movement is locked during active (stationary ability)
                // - hasAbilityMovement is true (ability is moving the NPC itself)
                return currentAbility.isMovementLockedDuringActive() || currentAbility.hasAbilityMovement();

            case DAZED:
                // During dazed phase, always block - NPC is stunned from interrupt
                return true;

            default:
                return false;
        }
    }

    /**
     * Check if NPC's look direction should be locked to target.
     * Only locks during ACTIVE phase when movement is locked during active.
     * This allows the NPC to freely track target during WINDUP and DAZED.
     */
    public boolean shouldLockLookDirection() {
        if (currentAbility == null || !currentAbility.isExecuting()) {
            return false;
        }

        // Only lock look direction during ACTIVE phase
        if (currentAbility.getPhase() != AbilityPhase.ACTIVE) {
            return false;
        }

        return currentAbility.isMovementLockedDuringActive();
    }

    /**
     * Check if NPC should skip normal attacks during ability execution.
     */
    public boolean shouldBlockAttack() {
        if (currentAbility == null || !currentAbility.isExecuting()) {
            return false;
        }
        AbilityPhase phase = currentAbility.getPhase();
        return phase == AbilityPhase.WINDUP || phase == AbilityPhase.ACTIVE;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROTATION LOCKING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Capture current rotation values to lock NPC's look direction.
     * Called when entering ACTIVE phase with movement locked during active.
     */
    private void captureLockedRotation() {
        lockedYaw = npc.rotationYaw;
        lockedYawHead = npc.rotationYawHead;
        lockedRenderYawOffset = npc.renderYawOffset;
        lockedPitch = npc.rotationPitch;
        rotationLocked = true;
    }

    /**
     * Release the rotation lock.
     * Called when leaving ACTIVE phase or ability completes.
     */
    private void releaseLockedRotation() {
        rotationLocked = false;
    }

    /**
     * Apply locked rotation values to the NPC.
     * Called AFTER super.onLivingUpdate() in EntityNPCInterface to override
     * any rotation changes made by the look helper or AI tasks.
     */
    public void applyLockedRotation() {
        if (!rotationLocked) {
            return;
        }

        // Force rotation back to locked values
        npc.rotationYaw = lockedYaw;
        npc.rotationYawHead = lockedYawHead;
        npc.renderYawOffset = lockedRenderYawOffset;
        npc.rotationPitch = lockedPitch;

        // Also set prev values to prevent interpolation jitter
        npc.prevRotationYaw = lockedYaw;
        npc.prevRotationYawHead = lockedYawHead;
        npc.prevRenderYawOffset = lockedRenderYawOffset;
        npc.prevRotationPitch = lockedPitch;
    }

    /**
     * Check if rotation is currently locked.
     */
    public boolean isRotationLocked() {
        return rotationLocked;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("AbilitiesEnabled", enabled);
        compound.setInteger("AbilityMinCooldown", minCooldown);
        compound.setInteger("AbilityMaxCooldown", maxCooldown);

        NBTTagList abilityList = new NBTTagList();
        for (AbilitySlot slot : abilitySlots) {
            abilityList.appendTag(slot.writeNBT());
        }
        compound.setTag("Abilities", abilityList);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        enabled = compound.getBoolean("AbilitiesEnabled");
        minCooldown = compound.getInteger("AbilityMinCooldown");
        maxCooldown = compound.getInteger("AbilityMaxCooldown");

        abilitySlots.clear();
        NBTTagList abilityList = compound.getTagList("Abilities", 10);
        for (int i = 0; i < abilityList.tagCount(); i++) {
            NBTTagCompound abilityNBT = abilityList.getCompoundTagAt(i);
            AbilitySlot slot = AbilitySlot.fromNBT(abilityNBT);
            if (slot != null) {
                abilitySlots.add(slot);
            }
        }
    }
}
