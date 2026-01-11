package noppes.npcs;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphInstance;
import kamkeel.npcs.network.packets.data.ability.TelegraphRemovePacket;
import kamkeel.npcs.network.packets.data.ability.TelegraphSpawnPacket;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /** List of abilities this NPC can use */
    private List<Ability> abilities = new ArrayList<>();

    /** Whether the ability system is enabled for this NPC */
    public boolean enabled = false;

    /** Minimum ticks between ability selections (global cooldown) */
    public int globalCooldown = 20;

    // ═══════════════════════════════════════════════════════════════════
    // RUNTIME STATE (not saved)
    // ═══════════════════════════════════════════════════════════════════

    /** Currently executing ability (null if none) */
    private transient Ability currentAbility;

    /** Per-ability cooldown timers (ability ID -> world time when cooldown ends) */
    private transient Map<String, Long> cooldowns = new HashMap<>();

    /** Ticks until next ability can be selected (global cooldown timer) */
    private transient int globalCooldownTimer = 0;

    /** Last target used for ability execution */
    private transient EntityLivingBase lastTarget;

    /** Recent hit timestamps for hit count condition */
    private transient List<Long> recentHitTimes = new ArrayList<>();

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

        // Tick global cooldown
        if (globalCooldownTimer > 0) {
            globalCooldownTimer--;
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

                    // Play active sound and animation
                    playAbilitySound(currentAbility.getActiveSound());
                    playAbilityAnimation(currentAbility.getActiveAnimationId());

                    // Fire execute event (cancelable)
                    AbilityEvent.ExecuteEvent executeEvent = new AbilityEvent.ExecuteEvent(
                        npc.wrappedNPC, currentAbility, target);
                    if (NpcAPI.EVENT_BUS.post(executeEvent)) {
                        // Event was cancelled - skip execution but continue to recovery
                        return;
                    }

                    // Call onExecute
                    currentAbility.onExecute(npc, target, npc.worldObj);
                }
                currentAbility.onActiveTick(npc, target, npc.worldObj, currentAbility.getCurrentTick());
                break;

            case RECOVERY:
                // Recovery phase - just wait
                break;

            case IDLE:
                // Ability completed
                currentAbility.onComplete(npc, target);

                // Fire complete event
                AbilityEvent.CompleteEvent completeEvent = new AbilityEvent.CompleteEvent(
                    npc.wrappedNPC, currentAbility, target);
                NpcAPI.EVENT_BUS.post(completeEvent);

                onAbilityComplete();
                break;
        }

        // Clear AI pathfinding if ability is controlling movement
        // But DON'T zero motion - let the ability set it if needed
        if (currentAbility != null && currentAbility.isExecuting()) {
            if (isAbilityControllingMovement()) {
                // Clear navigator so AI doesn't fight with ability movement
                npc.getNavigator().clearPathEntity();

                // Only zero motion if lockMovement is true AND ability doesn't have its own movement
                // This allows stationary abilities to freeze the NPC, while movement abilities can set their own motion
                if (currentAbility.isLockMovement() && !currentAbility.hasAbilityMovement()) {
                    npc.motionX = 0;
                    npc.motionZ = 0;
                }
            }
        }
    }

    /**
     * Called when an ability completes.
     */
    private void onAbilityComplete() {
        if (currentAbility != null) {
            // Stop any ability animation
            stopAbilityAnimation();

            // Start per-ability cooldown
            startCooldown(currentAbility);
            currentAbility = null;
            lastTarget = null;

            // Start global cooldown
            globalCooldownTimer = globalCooldown;
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
        if (!enabled || abilities.isEmpty()) {
            return false;
        }
        if (currentAbility != null && currentAbility.isExecuting()) {
            return false;
        }
        if (globalCooldownTimer > 0) {
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

        for (Ability ability : abilities) {
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

        // Check cooldown
        if (isOnCooldown(ability)) {
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
        playAbilityAnimation(ability.getWindUpAnimationId());

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
     */
    private void playAbilityAnimation(int animationId) {
        if (animationId < 0) return;
        if (AnimationController.Instance == null) return;

        Animation animation = AnimationController.Instance.animations.get(animationId);
        if (animation != null) {
            npc.display.animationData.setEnabled(true);
            npc.display.animationData.setAnimation(animation);
            npc.display.animationData.updateClient();
        }
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
     * Check if an ability is on cooldown.
     */
    public boolean isOnCooldown(Ability ability) {
        Long endTime = cooldowns.get(ability.getId());
        if (endTime == null) {
            return false;
        }
        return npc.worldObj.getTotalWorldTime() < endTime;
    }

    /**
     * Start cooldown for an ability.
     */
    private void startCooldown(Ability ability) {
        long endTime = npc.worldObj.getTotalWorldTime() + ability.getCooldownTicks();
        cooldowns.put(ability.getId(), endTime);
    }

    /**
     * Reset cooldown for a specific ability.
     */
    public void resetCooldown(String abilityId) {
        cooldowns.remove(abilityId);
    }

    /**
     * Reset all cooldowns.
     */
    public void resetAllCooldowns() {
        cooldowns.clear();
        globalCooldownTimer = 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // INTERRUPTION - Called from CombatHandler on damage
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Handle damage taken - may interrupt current ability.
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

        if (currentAbility.canInterrupt(source)) {
            interruptCurrentAbility(source, amount);
            return true;
        }

        return false;
    }

    /**
     * Record a hit for the hit count condition.
     */
    private void recordHit() {
        recentHitTimes.add(npc.worldObj.getTotalWorldTime());
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
     */
    public void interruptCurrentAbility(DamageSource source, float damage) {
        if (currentAbility != null) {
            // Remove telegraph early
            removeTelegraph(currentAbility);

            // Stop any ability animation
            stopAbilityAnimation();

            // Fire interrupt event
            AbilityEvent.InterruptEvent interruptEvent = new AbilityEvent.InterruptEvent(
                npc.wrappedNPC, currentAbility, lastTarget, source, damage);
            NpcAPI.EVENT_BUS.post(interruptEvent);

            currentAbility.onInterrupt(npc, source, damage);
            currentAbility.interrupt();
            currentAbility = null;
            lastTarget = null;
        }
    }

    /**
     * Force stop the current ability (no interrupt event).
     */
    public void stopCurrentAbility() {
        if (currentAbility != null) {
            removeTelegraph(currentAbility);
            stopAbilityAnimation();
            currentAbility.interrupt();
            currentAbility = null;
            lastTarget = null;
        }
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
     * @param ability The ability doing the hit
     * @param target The original target of the ability
     * @param hitEntity The entity being hit
     * @param damage The damage amount
     * @param knockback The horizontal knockback
     * @param knockbackUp The vertical knockback (deprecated, ignored)
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
     */
    public void reset() {
        stopCurrentAbility();
        resetAllCooldowns();

        // Reset execution state on all abilities
        for (Ability ability : abilities) {
            ability.reset();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ABILITY LIST MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    public List<Ability> getAbilities() {
        return abilities;
    }

    public void addAbility(Ability ability) {
        abilities.add(ability);
    }

    public void removeAbility(int index) {
        if (index >= 0 && index < abilities.size()) {
            abilities.remove(index);
        }
    }

    public void removeAbility(String id) {
        abilities.removeIf(a -> a.getId().equals(id));
    }

    public Ability getAbility(String id) {
        for (Ability ability : abilities) {
            if (ability.getId().equals(id)) {
                return ability;
            }
        }
        return null;
    }

    public void clearAbilities() {
        abilities.clear();
    }

    public boolean isEmpty() {
        return abilities.isEmpty();
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
     *
     * Rules:
     * - WINDUP phase: Block AI if lockMovement is true
     * - ACTIVE phase: Block AI if lockMovement OR hasAbilityMovement is true
     * - RECOVERY phase: Block AI if lockMovement is true
     */
    public boolean isAbilityControllingMovement() {
        if (currentAbility == null || !currentAbility.isExecuting()) {
            return false;
        }

        AbilityPhase phase = currentAbility.getPhase();

        switch (phase) {
            case WINDUP:
                // During windup, only block if lockMovement is set
                // This allows NPC to keep chasing while winding up if lockMovement=false
                return currentAbility.isLockMovement();

            case ACTIVE:
                // During active phase, block if either:
                // - lockMovement is true (stationary ability)
                // - hasAbilityMovement is true (ability is moving the NPC itself)
                return currentAbility.isLockMovement() || currentAbility.hasAbilityMovement();

            case RECOVERY:
                // During recovery, block if lockMovement is set
                return currentAbility.isLockMovement();

            default:
                return false;
        }
    }

    /**
     * Check if NPC movement should be blocked due to ability execution.
     * @deprecated Use isAbilityControllingMovement() instead
     */
    @Deprecated
    public boolean isMovementBlocked() {
        return isAbilityControllingMovement();
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
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("AbilitiesEnabled", enabled);
        compound.setInteger("AbilityGlobalCooldown", globalCooldown);

        NBTTagList abilityList = new NBTTagList();
        for (Ability ability : abilities) {
            abilityList.appendTag(ability.writeNBT());
        }
        compound.setTag("Abilities", abilityList);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound) {
        enabled = compound.getBoolean("AbilitiesEnabled");
        globalCooldown = compound.hasKey("AbilityGlobalCooldown") ?
            compound.getInteger("AbilityGlobalCooldown") : 20;

        abilities.clear();
        NBTTagList abilityList = compound.getTagList("Abilities", 10);
        for (int i = 0; i < abilityList.tagCount(); i++) {
            NBTTagCompound abilityNBT = abilityList.getCompoundTagAt(i);
            Ability ability = AbilityController.Instance.fromNBT(abilityNBT);
            if (ability != null) {
                abilities.add(ability);
            }
        }
    }
}
