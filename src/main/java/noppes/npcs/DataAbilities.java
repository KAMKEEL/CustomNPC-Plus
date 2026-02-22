package noppes.npcs;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityAction;
import kamkeel.npcs.controllers.data.ability.enums.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.data.IAbilityAction;
import kamkeel.npcs.controllers.data.ability.data.entry.AbilityToggleEntry;
import kamkeel.npcs.controllers.data.ability.type.AbilityGuard;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphRemovePacket;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphSpawnPacket;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.AbilityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Manages NPC abilities - storage, selection, execution, and cooldowns.
 * Extends AbstractDataAbilities for shared ability lifecycle logic.
 */
public class DataAbilities extends AbstractDataAbilities {

    private final EntityNPCInterface npc;
    private final Random random = new Random();

    // ═══════════════════════════════════════════════════════════════════
    // CONFIGURATION (saved to NBT)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Unified list of action slots (abilities and chained abilities).
     */
    private List<AbilityAction> actionSlots = new ArrayList<>();

    /**
     * Whether the ability system is enabled for this NPC
     */
    public boolean enabled = true;

    /**
     * Minimum cooldown ticks between abilities (global minimum)
     */
    public int minCooldown = 80;

    /**
     * Maximum cooldown ticks between abilities (global maximum)
     */
    public int maxCooldown = 200;

    // ═══════════════════════════════════════════════════════════════════
    // NPC-SPECIFIC RUNTIME STATE (not saved)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Last target used for ability execution
     */
    private transient EntityLivingBase lastTarget;

    /**
     * Recent hit timestamps for hit count condition
     */
    private transient List<Long> recentHitTimes = new ArrayList<>();

    /**
     * NPC-specific locked rotation fields (yawHead, renderYawOffset).
     * Base class handles lockedYaw and lockedPitch.
     */
    private transient float lockedYawHead = 0;
    private transient float lockedRenderYawOffset = 0;

    /**
     * Hit scan state - forces NPC to face target.
     * Set during onPostPhaseTick(), applied after super.onLivingUpdate()
     * to override AI look helper rotation.
     */
    private transient boolean hitScanActive = false;
    private transient EntityLivingBase hitScanTarget = null;

    /**
     * Bit flag for rotation control (LOCKED or TRACK) in data watcher slot 15
     */
    private static final int ROTATION_CONTROLLED_FLAG = 16;

    /**
     * Bit flag for position lock in data watcher slot 15
     */
    private static final int POSITION_LOCKED_FLAG = 32;

    // ═══════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════

    public DataAbilities(EntityNPCInterface npc) {
        this.npc = npc;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ABSTRACT METHOD IMPLEMENTATIONS
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected EntityLivingBase getEntity() {
        return npc;
    }

    @Override
    protected EntityLivingBase getTarget() {
        return lastTarget != null ? lastTarget : npc.getAttackTarget();
    }

    @Override
    protected long getWorldTime() {
        return npc.worldObj.getTotalWorldTime();
    }

    @Override
    protected void fireTickEvent(Ability ability, EntityLivingBase target) {
        AbilityEvent.TickEvent event = new AbilityEvent.TickEvent(
            npc.wrappedNPC, ability, target,
            ability.getPhase().ordinal(), ability.getCurrentTick());
        NpcAPI.EVENT_BUS.post(event);
    }

    @Override
    protected boolean fireExecuteEvent(Ability ability, EntityLivingBase target) {
        AbilityEvent.ExecuteEvent executeEvent = new AbilityEvent.ExecuteEvent(
            npc.wrappedNPC, ability, target);
        return NpcAPI.EVENT_BUS.post(executeEvent);
    }

    @Override
    protected void fireCompleteEvent(Ability ability, EntityLivingBase target) {
        AbilityEvent.CompleteEvent completeEvent = new AbilityEvent.CompleteEvent(
            npc.wrappedNPC, ability, target);
        NpcAPI.EVENT_BUS.post(completeEvent);
    }

    @Override
    protected void fireInterruptEvent(Ability ability, EntityLivingBase target,
                                      DamageSource source, float damage) {
        AbilityEvent.InterruptEvent interruptEvent = new AbilityEvent.InterruptEvent(
            npc.wrappedNPC, ability, target, source, damage);
        NpcAPI.EVENT_BUS.post(interruptEvent);
    }

    @Override
    protected boolean fireToggleEvent(Ability ability, int oldState, int newState) {
        AbilityEvent.ToggleEvent event = new AbilityEvent.ToggleEvent(
            npc.wrappedNPC, ability, oldState, newState);
        return NpcAPI.EVENT_BUS.post(event);
    }

    @Override
    protected boolean fireToggleUpdateEvent(Ability ability, int tick, int state) {
        AbilityEvent.ToggleUpdateEvent event = new AbilityEvent.ToggleUpdateEvent(
            npc.wrappedNPC, ability, tick, state);
        NpcAPI.EVENT_BUS.post(event);
        return event.isEnabled();
    }

    @Override
    protected void spawnTelegraph(Ability ability, EntityLivingBase target) {
        List<TelegraphInstance> telegraphs = ability.createTelegraphs(npc, target);
        if (!telegraphs.isEmpty()) {
            ability.setTelegraphInstances(telegraphs);
            for (TelegraphInstance telegraph : telegraphs) {
                TelegraphSpawnPacket.sendToTracking(telegraph, npc);
            }
        }
    }

    @Override
    protected void removeTelegraph(Ability ability) {
        List<TelegraphInstance> telegraphs = ability.getTelegraphInstances();
        for (TelegraphInstance telegraph : telegraphs) {
            TelegraphRemovePacket.sendToTracking(telegraph.getInstanceId(), npc);
        }
        ability.setTelegraphInstances(null);
    }

    @Override
    protected void setAnimationData(Animation animation) {
        npc.display.animationData.setEnabled(true);
        npc.display.animationData.setAnimation(animation);
        npc.display.animationData.updateClient();
    }

    @Override
    protected void clearAnimationData() {
        npc.display.animationData.setAnimation(null);
        npc.display.animationData.updateClient();
    }

    @Override
    protected void playAbilitySound(String sound) {
        if (sound != null && !sound.isEmpty()) {
            npc.worldObj.playSoundAtEntity(npc, sound, 1.0f, 1.0f);
        }
    }

    @Override
    protected void captureLockedRotation() {
        lockedYaw = npc.rotationYaw;
        lockedYawHead = npc.rotationYawHead;
        lockedRenderYawOffset = npc.renderYawOffset;
        lockedPitch = npc.rotationPitch;
        rotationLocked = true;
        npc.setBoolFlag(true, ROTATION_CONTROLLED_FLAG);
    }

    @Override
    protected void rollCooldown(Ability ability) {
        // Always roll NPC global cooldown (pacing timer)
        int baseCooldown = minCooldown;
        if (maxCooldown > minCooldown) {
            baseCooldown = minCooldown + random.nextInt(maxCooldown - minCooldown + 1);
        }
        cooldownEndTime = npc.worldObj.getTotalWorldTime() + baseCooldown + ability.getCooldownTicks();

        // Additionally set per-ability cooldown if enabled
        if (ability.isPerAbilityCooldown() && ability.getCooldownTicks() > 0) {
            long endTime = npc.worldObj.getTotalWorldTime() + ability.getCooldownTicks();
            setPerAbilityCooldown(ability.getName(), endTime, ability.getCooldownTicks());
        }
    }

    @Override
    protected void rollChainCooldown(ChainedAbility chain) {
        int baseCooldown = minCooldown;
        if (maxCooldown > minCooldown) {
            baseCooldown = minCooldown + random.nextInt(maxCooldown - minCooldown + 1);
        }
        cooldownEndTime = npc.worldObj.getTotalWorldTime() + baseCooldown + chain.getCooldownTicks();
    }

    @Override
    protected EntityLivingBase retargetForChain() {
        return npc.getAttackTarget();
    }

    @Override
    protected void onAbilityComplete() {
        currentAbility = null;
        lastTarget = null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // HOOK OVERRIDES
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void onPreExecute(Ability ability, EntityLivingBase target) {
        // Snap NPC to face target before execute so startMoving() reads correct rotation
        if (ability.isHitScanForCurrentPhase() && target != null) {
            faceTarget(target);
        }
    }

    @Override
    protected void onPostPhaseTick(Ability ability, EntityLivingBase target) {
        // Update hit scan state - actual facing is deferred to applyRotationControl()
        // which runs AFTER super.onLivingUpdate() to override AI look helper
        if (ability != null && ability.isExecuting()
            && ability.isHitScanForCurrentPhase() && target != null) {
            enableHitScan(target);
        } else if (hitScanActive) {
            releaseRotationControl();
        }

        // Apply movement control if ability is still executing
        if (ability != null && ability.isExecuting()) {
            applyMovementControl();
        }
    }

    @Override
    protected void onBurstDelayReleaseLocks() {
        if (hitScanActive) releaseRotationControl();
    }

    @Override
    protected void onPositionLockChanged(boolean locked) {
        npc.setBoolFlag(locked, POSITION_LOCKED_FLAG);
    }

    @Override
    protected void onRotationLockChanged(boolean locked) {
        if (!locked) {
            hitScanActive = false;
            hitScanTarget = null;
        }
        npc.setBoolFlag(locked, ROTATION_CONTROLLED_FLAG);
    }

    // ═══════════════════════════════════════════════════════════════════
    // TICK - Called from EntityNPCInterface.onLivingUpdate()
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Main tick method - handles ability execution and cooldown management.
     * Should be called every tick from onLivingUpdate().
     */
    public void tick() {
        if (npc.worldObj.isRemote) {
            return;
        }

        // Force-stop any running ability when NPC is killed or abilities are disabled
        if (!enabled || npc.isKilled()) {
            if (currentAbility != null) {
                removeTelegraph(currentAbility);
                stopAbilityAnimation();
                releaseRotationControl();
                releaseLockedPosition();
                currentAbility = null;
                lastTarget = null;
            } else {
                // No ability but locks could be orphaned
                if (rotationLocked || hitScanActive) releaseRotationControl();
                if (positionLocked) releaseLockedPosition();
            }
            // Clear chain and concurrent state
            currentChain = null;
            chainEntryIndex = -1;
            chainDelayRemaining = -1;
            interruptConcurrentSlots();
            return;
        }

        // Safety: release orphaned locks if no ability is actively executing
        if ((currentAbility == null || !currentAbility.isExecuting()) && chainDelayRemaining <= 0) {
            if (rotationLocked || hitScanActive) releaseRotationControl();
            if (positionLocked) releaseLockedPosition();
        }

        // Tick active toggles (independent of currentAbility)
        tickActiveToggles();

        // Tick current ability if executing, or tick chain delay between entries
        if (chainDelayRemaining > 0 || (currentAbility != null && currentAbility.isExecuting())) {
            tickCurrentAbility();
        }
    }

    /**
     * Apply movement control based on lock movement settings.
     * Called every tick during ability execution via onPostPhaseTick().
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

    // ═══════════════════════════════════════════════════════════════════
    // ABILITY SELECTION - Called from CombatHandler
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Try to select and start an ability or chained ability for the given target.
     * Called by CombatHandler when NPC is in combat and ready for an ability.
     *
     * @param target The combat target
     * @return true if an ability was started
     */
    public boolean trySelectAndStart(EntityLivingBase target) {
        if (!canSelectAbility()) {
            return false;
        }

        // Build eligible pool from unified action slots
        List<IAbilityAction> eligible = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int totalWeight = 0;

        for (AbilityAction slot : actionSlots) {
            IAbilityAction action = slot.getAction();
            if (action != null && isActionEligible(slot, action, target)) {
                eligible.add(action);
                weights.add(action.getWeight());
                totalWeight += action.getWeight();
            }
        }

        if (eligible.isEmpty() || totalWeight <= 0) {
            return false;
        }

        // Weighted random selection
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        IAbilityAction selected = null;
        for (int i = 0; i < eligible.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) {
                selected = eligible.get(i);
                break;
            }
        }
        if (selected == null) {
            selected = eligible.get(eligible.size() - 1);
        }

        // Dispatch
        lastTarget = target;
        if (selected.isChain()) {
            return startChain((ChainedAbility) selected, target);
        } else {
            return startAbility((Ability) selected, target);
        }
    }

    /**
     * Check if an ability can be selected right now.
     */
    public boolean canSelectAbility() {
        if (!enabled || actionSlots.isEmpty()) {
            return false;
        }
        if (currentAbility != null && currentAbility.isExecuting()) {
            return false;
        }
        if (isExecutingChain()) {
            return false;
        }
        // Check if still on cooldown
        if (npc.worldObj.getTotalWorldTime() < cooldownEndTime) {
            return false;
        }
        return true;
    }

    /**
     * Check if an action (ability or chain) is eligible for use.
     */
    private boolean isActionEligible(AbilityAction slot, IAbilityAction action, EntityLivingBase target) {
        if (!action.getAllowedBy().allowsNpc()) {
            return false;
        }
        if (!slot.isSlotEnabled()) {
            return false;
        }

        // Ability-specific: check if already executing
        if (!action.isChain() && ((Ability) action).isExecuting()) {
            return false;
        }

        // Chain-specific: must have entries
        if (action.isChain() && ((ChainedAbility) action).getEntries().isEmpty()) {
            return false;
        }

        // Range check
        if (target != null) {
            float distance = npc.getDistanceToEntity(target);
            if (distance < action.getMinRange() || distance > action.getMaxRange()) {
                return false;
            }
        }

        // Conditions
        if (!action.checkConditions(npc, target)) {
            return false;
        }

        // Per-ability cooldown filter: ability may have its own independent cooldown
        if (!action.isChain()) {
            Ability ab = (Ability) action;
            if (ab.isPerAbilityCooldown() && isOnPerAbilityCooldown(ab.getName())) {
                return false;
            }
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

        // Fire extender start hook (e.g., resource cost checks)
        if (!AbilityController.Instance.fireOnAbilityStart(ability, npc, target)) {
            return false;
        }

        currentAbility = ability;
        lastTarget = target;
        ability.start(target);

        if (ability.getPhase() == AbilityPhase.ACTIVE) {
            // Windup was 0 — capture locks for immediate active phase, then execute
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

        if (currentAbility == null || !currentAbility.isExecuting()) {
            return false;
        }

        net.minecraft.entity.Entity sourceEntity = source.getEntity();
        EntityLivingBase attacker = sourceEntity instanceof EntityLivingBase ? (EntityLivingBase) sourceEntity : null;
        currentAbility.onDamageTaken(npc, attacker, source, amount);

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
     * Force stop the current ability (no interrupt event).
     */
    public void stopCurrentAbility() {
        if (currentAbility != null) {
            removeTelegraph(currentAbility);
            stopAbilityAnimation();
            releaseRotationControl();
            releaseLockedPosition();
            currentAbility.interrupt();
            currentAbility = null;
            lastTarget = null;
        }
        // Also clear chain state
        currentChain = null;
        chainEntryIndex = -1;
        chainDelayRemaining = -1;
    }

    /**
     * Force start a specific ability, bypassing normal selection and cooldown.
     * If an ability is currently executing, it will be cancelled.
     *
     * @param ability The ability to start
     * @param target  The target entity (can be null for self-targeted abilities)
     * @return true if the ability was started successfully
     */
    public boolean forceStartAbility(Ability ability, EntityLivingBase target) {
        if (ability == null || npc.worldObj.isRemote) {
            return false;
        }

        // Check UserType allows NPCs
        if (!ability.getAllowedBy().allowsNpc()) {
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
     * @param key    The ability key (built-in name or custom UUID)
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
        clearActiveToggles();
        interruptConcurrentSlots();
        resetAllPerAbilityCooldowns();

        // Roll cooldown so NPC doesn't immediately attack after reset
        rollCooldownOnReset();

        // Reset execution state on all resolved abilities
        for (Ability ability : getAbilities()) {
            ability.reset();
        }
    }

    /**
     * Roll a new cooldown using the min/max range (without ability offset).
     * Called on reset when no ability is involved.
     */
    private void rollCooldownOnReset() {
        int baseCooldown = minCooldown;
        if (maxCooldown > minCooldown) {
            baseCooldown = minCooldown + random.nextInt(maxCooldown - minCooldown + 1);
        }
        cooldownEndTime = npc.worldObj.getTotalWorldTime() + baseCooldown;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ACTION SLOT MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get the unified action slot list.
     */
    public List<AbilityAction> getAbilityActions() {
        return actionSlots;
    }

    /**
     * Get resolved abilities from all ability slots, filtering out broken references and chains.
     */
    public List<Ability> getAbilities() {
        List<Ability> resolved = new ArrayList<>();
        for (AbilityAction slot : actionSlots) {
            Ability a = slot.getAbility();
            if (a != null) {
                resolved.add(a);
            }
        }
        return resolved;
    }

    /**
     * Add an inline ability.
     */
    public void addAbility(Ability ability) {
        actionSlots.add(AbilityAction.inline(ability));
    }

    /**
     * Add a reference ability by key (built-in name or custom ability name).
     */
    public void addAbilityReference(String key) {
        actionSlots.add(AbilityAction.abilityReference(key));
    }

    /**
     * Add a chained ability reference by name.
     */
    public void addChainReference(String name) {
        if (name != null && !name.isEmpty()) {
            for (AbilityAction slot : actionSlots) {
                if (slot.isChainReference() && name.equals(slot.getReferenceId())) {
                    return; // Prevent duplicates
                }
            }
            actionSlots.add(AbilityAction.chainReference(name));
        }
    }

    /**
     * Remove an action slot by index.
     */
    public void removeAction(int index) {
        if (index >= 0 && index < actionSlots.size()) {
            actionSlots.remove(index);
        }
    }

    public void removeAbility(int index) {
        removeAction(index);
    }

    public void removeAbility(String id) {
        actionSlots.removeIf(slot -> {
            if (slot.isAbilityReference()) {
                return slot.getReferenceId().equals(id);
            }
            if (slot.isChainReference()) {
                return false; // Don't remove chain references via removeAbility
            }
            Ability a = slot.getAbility();
            return a != null && a.getId().equals(id);
        });
    }

    public Ability getAbility(String id) {
        for (AbilityAction slot : actionSlots) {
            Ability a = slot.getAbility();
            if (a != null && a.getId().equals(id)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Check if a slot at a given index is a reference.
     */
    public boolean isSlotReference(int index) {
        if (index < 0 || index >= actionSlots.size()) return false;
        return actionSlots.get(index).isReference();
    }

    /**
     * Convert a reference slot to inline. Returns false if resolution fails or is a chain.
     */
    public boolean convertToInline(int index) {
        if (index < 0 || index >= actionSlots.size()) return false;
        return actionSlots.get(index).convertToInline();
    }

    public void clearAbilities() {
        actionSlots.clear();
    }

    public boolean isEmpty() {
        return actionSlots.isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════
    // STATE QUERIES (NPC-specific)
    // ═══════════════════════════════════════════════════════════════════

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
    // ROTATION CONTROL (NPC-specific: hit scan + 4-field rotation)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Enable hit scan tracking for the given target.
     */
    private void enableHitScan(EntityLivingBase target) {
        if (!hitScanActive) {
            hitScanActive = true;
            npc.setBoolFlag(true, ROTATION_CONTROLLED_FLAG);
        }
        hitScanTarget = target;
    }

    /**
     * Snap the NPC to face the target instantly.
     */
    private void faceTarget(EntityLivingBase target) {
        double dx = target.posX - npc.posX;
        double dz = target.posZ - npc.posZ;
        double dy = (target.posY + target.getEyeHeight() * 0.5) - (npc.posY + npc.getEyeHeight());
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Math.atan2(-dx, dz) * 180.0 / Math.PI);
        float pitch = (float) (-(Math.atan2(dy, distXZ)) * 180.0 / Math.PI);

        npc.rotationYaw = yaw;
        npc.rotationYawHead = yaw;
        npc.renderYawOffset = yaw;
        npc.rotationPitch = pitch;
        npc.prevRotationYaw = yaw;
        npc.prevRotationYawHead = yaw;
        npc.prevRenderYawOffset = yaw;
        npc.prevRotationPitch = pitch;
    }

    /**
     * Apply rotation control after super.onLivingUpdate() and super.onUpdate().
     * Handles both LOCKED (freeze at captured values) and TRACK (face target) modes.
     * <p>
     * Server: computes the correct rotation (locked values or target facing).
     * Client: trusts the server-synced rotation values and prevents body smoothing override.
     */
    public void applyRotationControl() {
        if (npc.worldObj.isRemote) {
            // Client: check single flag for any rotation control
            if (!npc.getBoolFlag(ROTATION_CONTROLLED_FLAG)) {
                rotationLocked = false;
                return;
            }
            // Trust server rotation, prevent body smoothing from overriding
            npc.prevRotationYaw = npc.rotationYaw;
            npc.prevRotationYawHead = npc.rotationYawHead;
            npc.renderYawOffset = npc.rotationYawHead;
            npc.prevRenderYawOffset = npc.rotationYawHead;
            npc.prevRotationPitch = npc.rotationPitch;
            return;
        }

        // Server: apply the appropriate rotation
        if (hitScanActive && hitScanTarget != null && !hitScanTarget.isDead) {
            faceTarget(hitScanTarget);
        } else if (rotationLocked) {
            npc.rotationYaw = lockedYaw;
            npc.rotationYawHead = lockedYawHead;
            npc.renderYawOffset = lockedRenderYawOffset;
            npc.rotationPitch = lockedPitch;
            npc.prevRotationYaw = lockedYaw;
            npc.prevRotationYawHead = lockedYawHead;
            npc.prevRenderYawOffset = lockedRenderYawOffset;
            npc.prevRotationPitch = lockedPitch;
        }
    }

    /**
     * Check if rotation is currently controlled (locked or tracking target).
     * Used by AI tasks to determine if they should try to change NPC look direction.
     */
    public boolean isRotationLocked() {
        return rotationLocked || hitScanActive;
    }

    // ═══════════════════════════════════════════════════════════════════
    // POSITION LOCKING (NPC-specific: data watcher + client-side logic)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Apply locked position values to the NPC.
     * Called AFTER super.onLivingUpdate() in EntityNPCInterface to override
     * any position changes from AI pathfinding, physics, or entity collisions.
     * Runs on both client and server.
     */
    public void applyLockedPosition() {
        if (npc.worldObj.isRemote) {
            // Client-side: check flag via data watcher
            boolean flagActive = npc.getBoolFlag(POSITION_LOCKED_FLAG);
            if (!flagActive) {
                positionLocked = false;
                return;
            }
            // First tick the flag is active: capture current position
            if (!positionLocked) {
                lockedPosX = npc.posX;
                lockedPosY = npc.posY;
                lockedPosZ = npc.posZ;
                positionLocked = true;
            }
            // Snap position back and set prev to prevent interpolation jitter
            npc.setPosition(lockedPosX, lockedPosY, lockedPosZ);
            npc.prevPosX = lockedPosX;
            npc.prevPosY = lockedPosY;
            npc.prevPosZ = lockedPosZ;
            npc.motionX = 0;
            npc.motionY = 0;
            npc.motionZ = 0;
            return;
        }

        // Server-side: use stored locked values
        if (!positionLocked) {
            return;
        }

        npc.setPosition(lockedPosX, lockedPosY, lockedPosZ);
        npc.prevPosX = lockedPosX;
        npc.prevPosY = lockedPosY;
        npc.prevPosZ = lockedPosZ;
        npc.motionX = 0;
        npc.motionY = 0;
        npc.motionZ = 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("AbilitiesEnabled", enabled);
        compound.setInteger("AbilityMinCooldown", minCooldown);
        compound.setInteger("AbilityMaxCooldown", maxCooldown);

        NBTTagList actionList = new NBTTagList();
        for (AbilityAction slot : actionSlots) {
            actionList.appendTag(slot.writeNBT());
        }
        compound.setTag("AbilityActions", actionList);

        // Active toggles (compound format with state)
        NBTTagList toggleList = new NBTTagList();
        for (Map.Entry<String, AbilityToggleEntry> entry : activeToggles.entrySet()) {
            NBTTagCompound toggleNbt = new NBTTagCompound();
            toggleNbt.setString("Key", entry.getKey());
            toggleNbt.setInteger("State", entry.getValue().getState());
            toggleList.appendTag(toggleNbt);
        }
        compound.setTag("ActiveToggles", toggleList);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        enabled = compound.getBoolean("AbilitiesEnabled");
        minCooldown = compound.getInteger("AbilityMinCooldown");
        maxCooldown = compound.getInteger("AbilityMaxCooldown");

        actionSlots.clear();

        if (compound.hasKey("AbilityActions")) {
            // New unified format
            NBTTagList actionList = compound.getTagList("AbilityActions", 10);
            for (int i = 0; i < actionList.tagCount(); i++) {
                AbilityAction slot = AbilityAction.fromNBT(actionList.getCompoundTagAt(i));
                if (slot != null) {
                    actionSlots.add(slot);
                }
            }
        } else {
            // Legacy migration: read old separate lists
            if (compound.hasKey("Abilities")) {
                NBTTagList abilityList = compound.getTagList("Abilities", 10);
                for (int i = 0; i < abilityList.tagCount(); i++) {
                    AbilityAction slot = AbilityAction.fromNBT(abilityList.getCompoundTagAt(i));
                    if (slot != null) {
                        actionSlots.add(slot);
                    }
                }
            }
            if (compound.hasKey("ChainedAbilities")) {
                NBTTagList chainList = compound.getTagList("ChainedAbilities", 8);
                for (int i = 0; i < chainList.tagCount(); i++) {
                    String ref = chainList.getStringTagAt(i);
                    if (ref != null && !ref.isEmpty()) {
                        actionSlots.add(AbilityAction.chainReference(ref));
                    }
                }
            }
        }

        // Active toggles - restore state directly (no onToggleOn callback during load)
        activeToggles.clear();
        if (compound.hasKey("ActiveToggles")) {
            NBTTagList toggleNbt = compound.getTagList("ActiveToggles", 10); // 10 = TAG_COMPOUND
            for (int i = 0; i < toggleNbt.tagCount(); i++) {
                NBTTagCompound entry = toggleNbt.getCompoundTagAt(i);
                String key = entry.getString("Key");
                int state = entry.hasKey("State") ? entry.getInteger("State") : 1;
                setToggleEntryDirect(key, state);
            }
        }
    }
}
