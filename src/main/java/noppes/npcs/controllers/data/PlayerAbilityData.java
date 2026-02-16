package noppes.npcs.controllers.data;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.ability.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.UserType;
import kamkeel.npcs.controllers.data.ability.type.AbilityGuard;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphRemovePacket;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphSpawnPacket;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import noppes.npcs.LogWriter;
import noppes.npcs.api.ability.IPlayerAbilityData;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import kamkeel.npcs.network.packets.data.ability.PlayerAbilityStatePacket;
import kamkeel.npcs.network.packets.data.ability.PlayerAbilitySyncPacket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages player ability data - unlocked abilities, selection, cooldowns, and execution.
 * Players reference abilities by key (built-in keys or preset names), never customized copies.
 * <p>
 * Key differences from NPC DataAbilities:
 * - Universal cooldown (shared across all abilities)
 * - Player manually selects ability (no weighted random)
 * - Conditions requiring a target are skipped
 * - Uses ability's base cooldownTicks only
 */
public class PlayerAbilityData implements IPlayerAbilityData {

    private final PlayerData playerData;

    // ═══════════════════════════════════════════════════════════════════
    // CONFIGURATION (saved to NBT)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * List of unlocked ability keys (preset names or built-in keys).
     * Order matters - determines selection index.
     */
    private List<String> unlockedAbilities = new ArrayList<>();

    /**
     * Currently selected ability index in the unlocked list.
     */
    private int selectedIndex = 0;

    /**
     * Tracks whether the current animation was started by an ability.
     * Persisted to NBT so orphaned ability animations can be detected and
     * cleared on login (ability state is transient, but animation state is not).
     */
    private boolean playingAbilityAnimation = false;

    // ═══════════════════════════════════════════════════════════════════
    // RUNTIME STATE (not saved)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Universal cooldown end time (shared across all abilities).
     */
    private transient long cooldownEndTime = 0;

    /**
     * Currently executing ability instance (null if none).
     */
    private transient Ability currentAbility;

    /**
     * Current ability's key (for cooldown tracking after completion).
     */
    private transient String currentAbilityKey;

    /**
     * Target for the current ability (can be null for self-targeting).
     */
    private transient EntityLivingBase currentTarget;

    /**
     * Rotation lock state - freezes player yaw/pitch during ability phases.
     */
    private transient boolean rotationLocked = false;
    private transient float lockedYaw = 0;
    private transient float lockedPitch = 0;

    /**
     * Position lock state - freezes player position during ability phases.
     */
    private transient boolean positionLocked = false;
    private transient double lockedPosX = 0;
    private transient double lockedPosY = 0;
    private transient double lockedPosZ = 0;

    /**
     * Last synced state flags byte for change detection (avoids spamming packets).
     */
    private transient byte lastSyncedFlags = 0;

    // ═══════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════

    public PlayerAbilityData(PlayerData playerData) {
        this.playerData = playerData;
    }

    // ═══════════════════════════════════════════════════════════════════
    // TICK - Called from player tick handler
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Main tick method - handles ability execution.
     * Should be called every server tick.
     */
    public void tick(EntityPlayer player) {
        if (player.worldObj.isRemote || player.isDead) {
            return;
        }

        if (currentAbility != null && currentAbility.isExecuting()) {
            tickCurrentAbility(player);

            // Apply rotation and position locks after ability tick
            applyRotationControl(player);
            applyPositionLock(player);

            // Sync lock state to client (only sends when flags change)
            syncAbilityStateIfNeeded(player);
        } else {
            // Safety: release orphaned locks if no ability is executing
            if (rotationLocked || positionLocked) {
                releaseRotationControl();
                releaseLockedPosition();
                syncAbilityStateClear(player);
            }
        }
    }

    /**
     * Tick the currently executing ability.
     */
    private void tickCurrentAbility(EntityPlayer player) {
        AbilityPhase oldPhase = currentAbility.getPhase();

        // Tick advances time and possibly changes phase
        boolean phaseChanged = currentAbility.tick();

        // Fire extender tick hook (e.g., per-tick resource drain)
        if (!AbilityController.Instance.fireOnAbilityTick(currentAbility, player, currentTarget,
                currentAbility.getPhase(), currentAbility.getCurrentTick())) {
            interruptCurrentAbility();
            return;
        }

        switch (currentAbility.getPhase()) {
            case WINDUP:
                if (phaseChanged && oldPhase == AbilityPhase.BURST_DELAY) {
                    // Burst replay: re-enter windup - set up locks
                    if (currentAbility.isRotationLockedDuringWindup()) {
                        captureLockedRotation(player);
                    }
                    if (currentAbility.isMovementLockedDuringWindup() && !currentAbility.hasAbilityMovement()) {
                        captureLockedPosition(player);
                    }
                    spawnTelegraph(currentAbility, player, null);
                    playAbilitySound(player, currentAbility.getWindUpSound());
                    playAbilityAnimation(currentAbility.getWindUpAnimation());
                }
                currentAbility.onWindUpTick(player, currentTarget, currentAbility.getCurrentTick());
                break;

            case ACTIVE:
                if (phaseChanged && (oldPhase == AbilityPhase.WINDUP || oldPhase == AbilityPhase.BURST_DELAY)) {
                    // Just entered ACTIVE phase - lock all telegraph positions
                    for (TelegraphInstance telegraph : currentAbility.getTelegraphInstances()) {
                        telegraph.lockPosition();
                    }
                    // Remove telegraph unless the ability keeps it during active phase
                    if (!currentAbility.keepTelegraphDuringActive()) {
                        removeTelegraph(currentAbility, player);
                    }

                    // Handle rotation control transition from WINDUP to ACTIVE
                    if (currentAbility.isRotationLockedDuringActive()) {
                        if (!rotationLocked) {
                            captureLockedRotation(player);
                        }
                    } else if (rotationLocked) {
                        releaseRotationControl();
                    }

                    // Handle position lock transition from WINDUP to ACTIVE
                    if (currentAbility.isMovementLockedDuringActive() && !currentAbility.hasAbilityMovement()) {
                        if (!positionLocked) {
                            captureLockedPosition(player);
                        }
                    } else if (positionLocked) {
                        releaseLockedPosition();
                    }

                    // Play active sound and animation
                    playAbilitySound(player, currentAbility.getActiveSound());
                    playAbilityAnimation(currentAbility.getActiveAnimation());

                    // Call onExecute
                    currentAbility.onExecute(player, currentTarget);

                    if (currentAbility.getPhase() == AbilityPhase.IDLE) {
                        handleAbilityCompletion(player);
                        return;
                    }
                }
                currentAbility.onActiveTick(player, currentTarget, currentAbility.getCurrentTick());

                if (currentAbility.getPhase() == AbilityPhase.IDLE) {
                    handleAbilityCompletion(player);
                    return;
                }

                // Auto-complete for burst overlap mode (entities fly independently)
                if (currentAbility.isBurstEnabled()
                    && currentAbility.getBurstIndex() < currentAbility.getBurstAmount()
                    && currentAbility.getPhase() == AbilityPhase.ACTIVE
                    && currentAbility.isReadyForBurstCompletion(currentAbility.getCurrentTick())) {
                    currentAbility.signalCompletion();
                }

                // Release locks during burst delay
                if (currentAbility.getPhase() == AbilityPhase.BURST_DELAY) {
                    if (rotationLocked) releaseRotationControl();
                    if (positionLocked) releaseLockedPosition();
                }
                break;

            case BURST_DELAY:
                // Free movement and rotation during burst delay
                if (rotationLocked) releaseRotationControl();
                if (positionLocked) releaseLockedPosition();
                break;

            case DAZED:
                // Player is dazed - just wait
                break;

            case IDLE:
                handleAbilityCompletion(player);
                break;
        }
    }

    /**
     * Handle ability completion.
     */
    private void handleAbilityCompletion(EntityPlayer player) {
        if (currentAbility == null) return;

        // Fire extender complete hook
        AbilityController.Instance.fireOnAbilityComplete(currentAbility, player, currentTarget, false);

        currentAbility.onComplete(player, currentTarget);

        // Release all locks
        releaseRotationControl();
        releaseLockedPosition();

        // Apply universal cooldown (using base cooldownTicks only, not min/max random)
        if (!currentAbility.isIgnoreCooldown()) {
            cooldownEndTime = player.worldObj.getTotalWorldTime() + currentAbility.getCooldownTicks();
        }

        // Stop animation
        stopAbilityAnimation();

        currentAbility = null;
        currentAbilityKey = null;
        currentTarget = null;

        // Send cleared state to client
        syncAbilityStateClear(player);
    }

    // ═══════════════════════════════════════════════════════════════════
    // ABILITY ACTIVATION - Called on Special Key press
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Activate the currently selected ability.
     * Called when the player presses the Special Key.
     *
     * @param player The player
     * @return true if the ability was started
     */
    public boolean activateAbility(EntityPlayer player) {
        if (player.worldObj.isRemote) return false;
        if (unlockedAbilities.isEmpty()) return false;
        if (selectedIndex < 0 || selectedIndex >= unlockedAbilities.size()) return false;

        String key = unlockedAbilities.get(selectedIndex);
        return activateAbility(player, key);
    }

    /**
     * Activate a specific ability by key.
     *
     * @param player The player
     * @param key    The ability key (built-in or preset name)
     * @return true if the ability was started
     */
    public boolean activateAbility(EntityPlayer player, String key) {
        if (player.worldObj.isRemote || key == null || key.isEmpty()) return false;

        // Can't activate if already executing
        if (currentAbility != null && currentAbility.isExecuting()) return false;

        // Resolve ability from controller
        if (AbilityController.Instance == null) return false;
        Ability ability = AbilityController.Instance.resolveAbility(key);
        if (ability == null) return false;

        // Check user type allows player
        if (!ability.getAllowedBy().allowsPlayer()) return false;

        // Check universal cooldown
        if (!ability.isIgnoreCooldown() && isOnCooldown(player)) return false;

        // Check conditions (skip target-requiring ones)
        if (!ability.checkConditionsForPlayer(player)) return false;

        // Fire extender start hook (e.g., resource cost checks)
        if (!AbilityController.Instance.fireOnAbilityStart(ability, player, null)) {
            return false;
        }

        // Start the ability
        currentAbility = ability;
        currentAbilityKey = key;
        currentTarget = null; // Players don't have auto-targets
        ability.start(null);

        if (ability.getPhase() == AbilityPhase.ACTIVE) {
            // Windup was 0 — skip telegraph/windup and go straight to active
            if (ability.isRotationLockedDuringActive()) {
                captureLockedRotation(player);
            }
            if (ability.isMovementLockedDuringActive() && !ability.hasAbilityMovement()) {
                captureLockedPosition(player);
            }
            executeImmediate(ability, player);
        } else {
            // Normal windup flow
            if (ability.isRotationLockedDuringWindup()) {
                captureLockedRotation(player);
            }
            if (ability.isMovementLockedDuringWindup() && !ability.hasAbilityMovement()) {
                captureLockedPosition(player);
            }
            spawnTelegraph(ability, player, null);
            playAbilitySound(player, ability.getWindUpSound());
            playAbilityAnimation(ability.getWindUpAnimation());
        }

        return true;
    }

    /**
     * Execute an ability immediately (no windup).
     * Called when windUpTicks is 0.
     */
    private void executeImmediate(Ability ability, EntityPlayer player) {
        // Play active sound and animation
        playAbilitySound(player, ability.getActiveSound());
        playAbilityAnimation(ability.getActiveAnimation());

        // Call onExecute
        ability.onExecute(player, currentTarget);

        // Check if ability completed during onExecute
        if (ability.getPhase() == AbilityPhase.IDLE) {
            handleAbilityCompletion(player);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CLIENT SYNC
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Sync ability data to the client.
     * Called when abilities change (unlock/lock/select) and on login.
     */
    public void syncToClient() {
        EntityPlayer player = playerData.player;
        if (player instanceof EntityPlayerMP) {
            PlayerAbilitySyncPacket.sendToPlayer((EntityPlayerMP) player);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SELECTION
    // ═══════════════════════════════════════════════════════════════════

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < unlockedAbilities.size()) {
            this.selectedIndex = index;
            syncToClient();
        }
    }

    public void selectNext() {
        if (unlockedAbilities.isEmpty()) return;
        selectedIndex = (selectedIndex + 1) % unlockedAbilities.size();
        syncToClient();
    }

    public void selectPrevious() {
        if (unlockedAbilities.isEmpty()) return;
        selectedIndex = (selectedIndex - 1 + unlockedAbilities.size()) % unlockedAbilities.size();
        syncToClient();
    }

    public String getSelectedAbilityKey() {
        if (unlockedAbilities.isEmpty() || selectedIndex < 0 || selectedIndex >= unlockedAbilities.size()) {
            return null;
        }
        return unlockedAbilities.get(selectedIndex);
    }

    // ═══════════════════════════════════════════════════════════════════
    // UNLOCKED ABILITIES
    // ═══════════════════════════════════════════════════════════════════

    public List<String> getUnlockedAbilityList() {
        return unlockedAbilities;
    }

    @Override
    public String[] getUnlockedAbilities() {
        return unlockedAbilities.toArray(new String[0]);
    }

    public void setUnlockedAbilities(List<String> abilities) {
        this.unlockedAbilities = abilities != null ? new ArrayList<>(abilities) : new ArrayList<>();
        // Clamp selected index
        if (selectedIndex >= unlockedAbilities.size()) {
            selectedIndex = Math.max(0, unlockedAbilities.size() - 1);
        }
        syncToClient();
    }

    public void unlockAbility(String key) {
        if (key != null && !key.isEmpty() && !unlockedAbilities.contains(key)) {
            unlockedAbilities.add(key);
            syncToClient();
        }
    }

    public void lockAbility(String key) {
        unlockedAbilities.remove(key);
        if (selectedIndex >= unlockedAbilities.size()) {
            selectedIndex = Math.max(0, unlockedAbilities.size() - 1);
        }
        syncToClient();
    }

    public boolean hasUnlockedAbility(String key) {
        return unlockedAbilities.contains(key);
    }

    // ═══════════════════════════════════════════════════════════════════
    // COOLDOWNS
    // ═══════════════════════════════════════════════════════════════════

    public boolean isOnCooldown(EntityPlayer player) {
        return player.worldObj.getTotalWorldTime() < cooldownEndTime;
    }

    /**
     * Check if a specific ability key is on cooldown.
     * Currently uses universal cooldown (same for all abilities).
     */
    public boolean isOnCooldown(String key, EntityPlayer player) {
        return isOnCooldown(player);
    }

    public long getRemainingCooldown(EntityPlayer player) {
        long remaining = cooldownEndTime - player.worldObj.getTotalWorldTime();
        return remaining > 0 ? remaining : 0;
    }

    /**
     * Reset cooldown for a specific ability key.
     * Currently resets the universal cooldown.
     */
    public void resetCooldown(String key) {
        cooldownEndTime = 0;
    }

    /**
     * Reset all cooldowns.
     */
    public void resetAllCooldowns() {
        cooldownEndTime = 0;
    }

    @Override
    public boolean activateAbility() {
        return activateAbility(playerData.player);
    }

    @Override
    public boolean activateAbility(String key) {
        return activateAbility(playerData.player, key);
    }

    // ═══════════════════════════════════════════════════════════════════
    // STATE QUERIES
    // ═══════════════════════════════════════════════════════════════════

    public boolean isExecutingAbility() {
        return currentAbility != null && currentAbility.isExecuting();
    }

    public Ability getCurrentAbility() {
        return currentAbility;
    }

    public void interruptCurrentAbility() {
        if (currentAbility != null && currentAbility.isExecuting()) {
            // Fire extender complete hook (interrupted)
            AbilityController.Instance.fireOnAbilityComplete(currentAbility, playerData.player, currentTarget, true);

            currentAbility.interrupt();
            stopAbilityAnimation();
            releaseRotationControl();
            releaseLockedPosition();
            currentAbility = null;
            currentAbilityKey = null;
            currentTarget = null;
            syncAbilityStateClear(playerData.player);
        }
    }

    /**
     * Handle damage taken while an ability is executing.
     * Called from LivingHurtEvent BEFORE damage is applied to health.
     * Triggers Guard counter detection and handles ability interruption.
     *
     * @param source The damage source
     * @param amount The damage amount (post-armor)
     * @return The modified damage amount (reduced by guard, or 0 if counter absorbs)
     */
    public float onDamage(DamageSource source, float amount) {
        if (currentAbility == null || !currentAbility.isExecuting()) {
            return amount;
        }

        net.minecraft.entity.Entity sourceEntity = source.getEntity();
        EntityLivingBase attacker = sourceEntity instanceof EntityLivingBase ? (EntityLivingBase) sourceEntity : null;

        // Track counter state before calling onDamageTaken
        boolean wasCounterTriggered = false;
        if (currentAbility instanceof AbilityGuard) {
            wasCounterTriggered = ((AbilityGuard) currentAbility).isCounterTriggered();
        }

        currentAbility.onDamageTaken(playerData.player, attacker, source, amount);

        // Guard: handle damage reduction and counter absorption
        if (currentAbility instanceof AbilityGuard) {
            AbilityGuard guard = (AbilityGuard) currentAbility;
            if (guard.isGuarding()) {
                // Counter just triggered — absorb full damage
                if (!wasCounterTriggered && guard.isCounterTriggered()) {
                    return 0;
                }
                // Normal guard — apply flat damage reduction
                float reduction = guard.getDamageReductionFactor();
                return Math.max(0, amount - reduction);
            }
        }

        // Check for ability interruption
        if (currentAbility != null && currentAbility.canInterrupt(source)) {
            interruptCurrentAbility();
        }

        return amount;
    }

    @Override
    public boolean isOnCooldown() {
        EntityPlayer player = playerData.player;
        if (player == null) return false;
        return isOnCooldown(player);
    }

    @Override
    public boolean isOnCooldown(String key) {
        EntityPlayer player = playerData.player;
        if (player == null) return false;
        return isOnCooldown(key, player);
    }

    @Override
    public void resetCooldown() {
        cooldownEndTime = 0;
    }

    /**
     * Check if the player's movement should be locked due to an executing ability.
     * Used by the tick handler to restrict player movement during ability phases.
     */
    public boolean isMovementLocked() {
        return currentAbility != null && currentAbility.isExecuting()
            && currentAbility.isMovementLockedForCurrentPhase();
    }

    // ═══════════════════════════════════════════════════════════════════
    // ROTATION CONTROL
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Capture current rotation values to lock player's look direction.
     */
    private void captureLockedRotation(EntityPlayer player) {
        lockedYaw = player.rotationYaw;
        lockedPitch = player.rotationPitch;
        rotationLocked = true;
    }

    /**
     * Release rotation lock.
     */
    private void releaseRotationControl() {
        rotationLocked = false;
    }

    /**
     * Apply rotation control on the server side.
     * Enforces locked rotation values on the player entity.
     */
    private void applyRotationControl(EntityPlayer player) {
        if (!rotationLocked || currentAbility == null) return;

        if (currentAbility.isRotationLockedForCurrentPhase()) {
            player.rotationYaw = lockedYaw;
            player.rotationPitch = lockedPitch;
            player.prevRotationYaw = lockedYaw;
            player.prevRotationPitch = lockedPitch;
            if (player instanceof EntityPlayerMP) {
                player.rotationYawHead = lockedYaw;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // POSITION LOCKING
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Capture current position to lock player in place.
     */
    private void captureLockedPosition(EntityPlayer player) {
        lockedPosX = player.posX;
        lockedPosY = player.posY;
        lockedPosZ = player.posZ;
        positionLocked = true;
    }

    /**
     * Release position lock.
     */
    private void releaseLockedPosition() {
        positionLocked = false;
    }

    /**
     * Apply position lock on the server side.
     * Snaps player back to locked position and zeroes motion.
     */
    private void applyPositionLock(EntityPlayer player) {
        if (!positionLocked) return;

        player.setPosition(lockedPosX, lockedPosY, lockedPosZ);
        player.prevPosX = lockedPosX;
        player.prevPosY = lockedPosY;
        player.prevPosZ = lockedPosZ;
        player.motionX = 0;
        player.motionY = 0; // Full vertical freeze for position lock
        player.motionZ = 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ABILITY STATE SYNC
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Build the current ability state flags byte.
     */
    private byte getAbilityStateFlags() {
        byte flags = 0;
        if (currentAbility == null || !currentAbility.isExecuting()) return flags;

        if (currentAbility.isMovementLockedForCurrentPhase()) {
            flags |= PlayerAbilityStatePacket.FLAG_MOVEMENT_LOCKED;
        }
        if (rotationLocked && currentAbility.isRotationLockedForCurrentPhase()) {
            flags |= PlayerAbilityStatePacket.FLAG_ROTATION_LOCKED;
        }
        if (currentAbility.hasAbilityMovement() && currentAbility.getPhase() == AbilityPhase.ACTIVE) {
            flags |= PlayerAbilityStatePacket.FLAG_HAS_ABILITY_MOVEMENT;
        }
        if (positionLocked) {
            flags |= PlayerAbilityStatePacket.FLAG_POSITION_LOCKED;
        }
        return flags;
    }

    /**
     * Send ability state to client if flags changed since last sync.
     */
    private void syncAbilityStateIfNeeded(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;
        byte flags = getAbilityStateFlags();
        if (flags != lastSyncedFlags) {
            lastSyncedFlags = flags;
            PlayerAbilityStatePacket.sendToPlayer((EntityPlayerMP) player, flags, lockedYaw, lockedPitch);
        }
    }

    /**
     * Send cleared (all-zero) state to client. Called on ability completion/interrupt.
     */
    private void syncAbilityStateClear(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;
        if (lastSyncedFlags != 0) {
            lastSyncedFlags = 0;
            PlayerAbilityStatePacket.sendToPlayer((EntityPlayerMP) player, (byte) 0, 0, 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TELEGRAPH
    // ═══════════════════════════════════════════════════════════════════

    private void spawnTelegraph(Ability ability, EntityPlayer player, EntityLivingBase target) {
        List<TelegraphInstance> telegraphs = ability.createTelegraphs(player, target);
        if (!telegraphs.isEmpty()) {
            ability.setTelegraphInstances(telegraphs);
            if (player instanceof EntityPlayerMP) {
                for (TelegraphInstance telegraph : telegraphs) {
                    TelegraphSpawnPacket.sendToTracking(telegraph, player);
                }
            }
        }
    }

    private void removeTelegraph(Ability ability, EntityPlayer player) {
        List<TelegraphInstance> telegraphs = ability.getTelegraphInstances();
        for (TelegraphInstance telegraph : telegraphs) {
            if (player instanceof EntityPlayerMP) {
                TelegraphRemovePacket.sendToTracking(telegraph.getInstanceId(), player);
            }
        }
        ability.setTelegraphInstances(null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════════════════════════

    public void playAbilityAnimation(Animation animation) {
        if (animation == null || AnimationController.Instance == null) return;
        playerData.animationData.setEnabled(true);
        playerData.animationData.setAnimation(animation);
        playerData.animationData.updateClient();
        playingAbilityAnimation = true;
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

    private void stopAbilityAnimation() {
        playerData.animationData.setAnimation(null);
        playerData.animationData.updateClient();
        playingAbilityAnimation = false;
    }

    /**
     * Returns true if an ability-driven animation is currently playing.
     * Used by PlayerData.onLogin() to detect orphaned ability animations.
     */
    public boolean isPlayingAbilityAnimation() {
        return playingAbilityAnimation;
    }

    /**
     * Clears an orphaned ability animation (e.g. after relog when the ability is gone).
     * Stops the animation and resets the tracking flag.
     */
    public void clearOrphanedAbilityAnimation() {
        stopAbilityAnimation();
    }

    private void playAbilitySound(EntityPlayer player, String sound) {
        if (sound != null && !sound.isEmpty()) {
            player.worldObj.playSoundAtEntity(player, sound, 1.0f, 1.0f);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (String key : unlockedAbilities) {
            list.appendTag(new NBTTagString(key));
        }
        compound.setTag("PlayerAbilities", list);
        compound.setInteger("PlayerAbilitySelected", selectedIndex);
        compound.setBoolean("AbilityAnimating", playingAbilityAnimation);
    }

    public void readFromNBT(NBTTagCompound compound) {
        unlockedAbilities.clear();
        if (compound.hasKey("PlayerAbilities")) {
            NBTTagList list = compound.getTagList("PlayerAbilities", 8); // 8 = string tag
            for (int i = 0; i < list.tagCount(); i++) {
                String key = list.getStringTagAt(i);
                if (key != null && !key.isEmpty()) {
                    unlockedAbilities.add(key);
                }
            }
        }
        selectedIndex = compound.getInteger("PlayerAbilitySelected");

        // Prune abilities that no longer exist (deleted custom abilities or removed built-ins)
        validateUnlockedAbilities();

        if (selectedIndex >= unlockedAbilities.size()) {
            selectedIndex = Math.max(0, unlockedAbilities.size() - 1);
        }
        playingAbilityAnimation = compound.getBoolean("AbilityAnimating");
    }

    /**
     * Remove any unlocked ability keys that can no longer be resolved.
     * Called during load to clean up references to deleted abilities.
     */
    private void validateUnlockedAbilities() {
        if (AbilityController.Instance == null) return;

        Iterator<String> it = unlockedAbilities.iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (!AbilityController.Instance.canResolveAbility(key)) {
                it.remove();
                LogWriter.info("Removed invalid ability reference from player data: " + key);
            }
        }
    }
}
