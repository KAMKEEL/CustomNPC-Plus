package noppes.npcs.controllers.data;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.enums.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.data.IAbilityAction;
import kamkeel.npcs.controllers.data.ability.data.entry.AbilityToggleEntry;
import kamkeel.npcs.controllers.data.ability.type.AbilityGuard;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.network.packets.data.ability.AbilityCooldownSyncPacket;
import kamkeel.npcs.network.packets.data.ability.PlayerAbilityStatePacket;
import kamkeel.npcs.network.packets.data.ability.PlayerAbilitySyncPacket;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphRemovePacket;
import kamkeel.npcs.network.packets.data.telegraph.TelegraphSpawnPacket;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import noppes.npcs.AbstractDataAbilities;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.api.ability.IPlayerAbilityData;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.player.PlayerAbilityEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manages player ability data - unlocked abilities, selection, cooldowns, and execution.
 * Players reference abilities by key (built-in keys or preset names), never customized copies.
 * Extends AbstractDataAbilities for shared ability lifecycle logic.
 * <p>
 * Key differences from NPC DataAbilities:
 * - Universal cooldown (shared across all abilities)
 * - Player manually selects ability (no weighted random)
 * - Conditions requiring a target are skipped
 * - Uses ability's base cooldownTicks only
 */
public class PlayerAbilityData extends AbstractDataAbilities implements IPlayerAbilityData {

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
    // PLAYER-SPECIFIC RUNTIME STATE (not saved)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Current ability's key (for cooldown tracking after completion).
     */
    private transient String currentAbilityKey;

    /**
     * Target for the current ability (can be null for self-targeting).
     */
    private transient EntityLivingBase currentTarget;

    /**
     * Last synced state flags byte for change detection (avoids spamming packets).
     */
    private transient byte lastSyncedFlags = 0;

    /**
     * World time when the last ability was activated (for double-press cancel detection).
     */
    private transient long lastAbilityActivationTime = -1;

    /**
     * Ticks after activation during which a second key press will cancel the ability.
     */
    private static final int CANCEL_WINDOW_TICKS = 10;

    // ═══════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════

    public PlayerAbilityData(PlayerData playerData) {
        this.playerData = playerData;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ABSTRACT METHOD IMPLEMENTATIONS
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected EntityLivingBase getEntity() {
        return playerData.player;
    }

    @Override
    protected EntityLivingBase getTarget() {
        return currentTarget;
    }

    @Override
    protected long getWorldTime() {
        EntityPlayer player = playerData.player;
        return player != null ? player.worldObj.getTotalWorldTime() : 0;
    }

    @Override
    protected void fireTickEvent(Ability ability, EntityLivingBase target) {
        EntityPlayer player = playerData.player;
        if (ScriptController.Instance == null || player == null) return;
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        if (handler == null) return;
        IPlayer iPlayer = (IPlayer) NpcAPI.Instance().getIEntity(player);
        PlayerAbilityEvent.TickEvent event = new PlayerAbilityEvent.TickEvent(
            iPlayer, ability, target, ability.getPhase().ordinal(), ability.getCurrentTick());
        EventHooks.onPlayerAbilityTick(handler, event);
    }

    @Override
    protected boolean fireExecuteEvent(Ability ability, EntityLivingBase target) {
        EntityPlayer player = playerData.player;
        if (ScriptController.Instance == null || player == null) return false;
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        if (handler == null) return false;
        IPlayer iPlayer = (IPlayer) NpcAPI.Instance().getIEntity(player);
        PlayerAbilityEvent.ExecuteEvent event = new PlayerAbilityEvent.ExecuteEvent(iPlayer, ability, target);
        return EventHooks.onPlayerAbilityExecute(handler, event);
    }

    @Override
    protected void fireCompleteEvent(Ability ability, EntityLivingBase target) {
        EntityPlayer player = playerData.player;
        if (ScriptController.Instance == null || player == null) return;
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        if (handler == null) return;
        IPlayer iPlayer = (IPlayer) NpcAPI.Instance().getIEntity(player);
        PlayerAbilityEvent.CompleteEvent event = new PlayerAbilityEvent.CompleteEvent(iPlayer, ability, target);
        EventHooks.onPlayerAbilityComplete(handler, event);
    }

    @Override
    protected void fireInterruptEvent(Ability ability, EntityLivingBase target,
                                      DamageSource source, float damage) {
        EntityPlayer player = playerData.player;
        if (ScriptController.Instance == null || player == null) return;
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        if (handler == null) return;
        IPlayer iPlayer = (IPlayer) NpcAPI.Instance().getIEntity(player);
        PlayerAbilityEvent.InterruptEvent event = new PlayerAbilityEvent.InterruptEvent(
            iPlayer, ability, target, source, damage);
        EventHooks.onPlayerAbilityInterrupt(handler, event);
    }

    @Override
    protected void spawnTelegraph(Ability ability, EntityLivingBase target) {
        EntityPlayer player = playerData.player;
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

    @Override
    protected void removeTelegraph(Ability ability) {
        EntityPlayer player = playerData.player;
        List<TelegraphInstance> telegraphs = ability.getTelegraphInstances();
        for (TelegraphInstance telegraph : telegraphs) {
            if (player instanceof EntityPlayerMP) {
                TelegraphRemovePacket.sendToTracking(telegraph.getInstanceId(), player);
            }
        }
        ability.setTelegraphInstances(null);
    }

    @Override
    protected void setAnimationData(Animation animation) {
        playerData.animationData.setEnabled(true);
        playerData.animationData.setAnimation(animation);
        playerData.animationData.updateClient();
        playingAbilityAnimation = true;
    }

    @Override
    protected void clearAnimationData() {
        playerData.animationData.setAnimation(null);
        playerData.animationData.updateClient();
        playingAbilityAnimation = false;
    }

    @Override
    protected void playAbilitySound(String sound) {
        if (sound != null && !sound.isEmpty()) {
            EntityPlayer player = playerData.player;
            if (player != null) {
                player.worldObj.playSoundAtEntity(player, sound, 1.0f, 1.0f);
            }
        }
    }

    @Override
    protected void captureLockedRotation() {
        EntityPlayer player = playerData.player;
        lockedYaw = player.rotationYaw;
        lockedPitch = player.rotationPitch;
        rotationLocked = true;
    }

    @Override
    protected void rollCooldown(Ability ability) {
        if (ability.isIgnoreCooldown()) return;

        int duration = ability.getCooldownTicks();
        long endTime = getWorldTime() + duration;

        if (ability.isPerAbilityCooldown()) {
            // Per-ability: only this ability goes on cooldown
            if (currentAbilityKey != null) {
                setPerAbilityCooldown(currentAbilityKey, endTime, duration);
            }
        } else {
            // Global: all global-cooldown abilities share this cooldown
            cooldownEndTime = endTime;
            globalCooldownDuration = duration;
        }
        syncCooldownToClient();
    }

    @Override
    protected void rollChainCooldown(ChainedAbility chain) {
        int duration = chain.getCooldownTicks();
        cooldownEndTime = getWorldTime() + duration;
        globalCooldownDuration = duration;
        syncCooldownToClient();
    }

    @Override
    protected void onAbilityComplete() {
        currentAbility = null;
        currentAbilityKey = null;
        currentTarget = null;
        lastAbilityActivationTime = -1;
        syncAbilityStateClear(playerData.player);
    }

    // ═══════════════════════════════════════════════════════════════════
    // HOOK OVERRIDES
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void onInterruptComplete() {
        // Player clears state immediately (unlike NPC which ticks through DAZED)
        // Stop the dazed animation that was started in interruptCurrentAbility()
        stopAbilityAnimation();
        currentAbility = null;
        currentAbilityKey = null;
        currentTarget = null;
        lastAbilityActivationTime = -1;
        syncAbilityStateClear(playerData.player);
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

        // Tick active toggles (independent of currentAbility)
        tickActiveToggles();

        if (chainDelayRemaining > 0 || (currentAbility != null && currentAbility.isExecuting())) {
            tickCurrentAbility();

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
     * Prefix used to identify chained ability keys in the unlocked list.
     */
    public static final String CHAIN_PREFIX = "chain:";

    /**
     * Resolve an action key to an IAbilityAction.
     * Handles "chain:" prefix for chained abilities, otherwise resolves from AbilityController.
     */
    private IAbilityAction resolveActionKey(String key) {
        if (key.startsWith(CHAIN_PREFIX)) {
            return AbilityController.Instance.resolveChainedAbility(key.substring(CHAIN_PREFIX.length()));
        }
        return AbilityController.Instance.resolveAbility(key);
    }

    /**
     * Activate a specific ability by key. Keys prefixed with "chain:" are
     * treated as chained abilities and resolved from {@link AbilityController}.
     *
     * @param player The player
     * @param key    The ability key (built-in or preset name), or "chain:name" for chains
     * @return true if the ability was started
     */
    public boolean activateAbility(EntityPlayer player, String key) {
        if (player.worldObj.isRemote || key == null || key.isEmpty()) return false;

        // Can't activate if already executing
        if (currentAbility != null && currentAbility.isExecuting()) return false;
        if (isExecutingChain()) return false;

        // Resolve via unified resolver
        IAbilityAction action = resolveActionKey(key);
        if (action == null) return false;

        // Check user type allows player
        if (!action.getAllowedBy().allowsPlayer()) return false;

        // Check conditions (skip target-requiring ones)
        if (!action.checkConditionsForPlayer(player)) return false;

        // Dispatch: chain vs ability
        if (action.isChain()) {
            if (isOnCooldown()) return false;

            currentAbilityKey = key;
            currentTarget = null;
            lastAbilityActivationTime = getWorldTime();
            return startChain((ChainedAbility) action, null);
        }

        // Regular ability activation
        Ability ability = (Ability) action;

        // Toggle abilities cycle state instead of executing
        if (ability.isToggleable()) {
            toggleAbility(key);
            return true;
        }

        // Check cooldown (per-ability cooldowns are independent from global)
        if (!ability.isIgnoreCooldown()) {
            if (ability.isPerAbilityCooldown()) {
                if (isOnPerAbilityCooldown(key)) return false;
            } else {
                if (isOnCooldown()) return false;
            }
        }

        // Fire extender start hook (e.g., resource cost checks)
        if (!AbilityController.Instance.fireOnAbilityStart(ability, player, null)) {
            return false;
        }

        // Fire start event (cancelable)
        if (firePlayerStartEvent(player, ability)) {
            return false; // Cancelled
        }

        // Start the ability
        currentAbility = ability;
        currentAbilityKey = key;
        currentTarget = null; // Players don't have auto-targets
        lastAbilityActivationTime = getWorldTime();
        ability.start(null);

        if (ability.getPhase() == AbilityPhase.ACTIVE) {
            // Windup was 0 — capture locks for immediate active phase, then execute
            if (ability.isRotationLockedDuringActive()) {
                captureLockedRotation();
            }
            if (ability.isMovementLockedDuringActive() && !ability.hasAbilityMovement()) {
                captureLockedPosition();
            }
            executeImmediate(ability, currentTarget);
        } else {
            // Normal windup flow
            if (ability.isRotationLockedDuringWindup()) {
                captureLockedRotation();
            }
            if (ability.isMovementLockedDuringWindup() && !ability.hasAbilityMovement()) {
                captureLockedPosition();
            }
            spawnTelegraph(ability, null);
            playAbilitySound(ability.getWindUpSound());
            playAbilityAnimation(ability.getWindUpAnimation());
        }

        return true;
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
            syncCooldownToClient();
        }
    }

    /**
     * Sync cooldown state (global + per-ability) to the client.
     * Sent as a lightweight packet separate from full ability sync.
     */
    public void syncCooldownToClient() {
        EntityPlayer player = playerData.player;
        if (player instanceof EntityPlayerMP) {
            AbilityCooldownSyncPacket.sendToPlayer((EntityPlayerMP) player);
        }
    }

    @Override
    protected void onToggleStateChanged(String key, boolean active, int state) {
        syncToClient();
    }

    @Override
    protected boolean fireToggleEvent(Ability ability, int oldState, int newState) {
        EntityPlayer player = playerData.player;
        if (ScriptController.Instance == null || player == null) return false;
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        if (handler == null) return false;
        IPlayer iPlayer = (IPlayer) NpcAPI.Instance().getIEntity(player);
        PlayerAbilityEvent.ToggleEvent event = new PlayerAbilityEvent.ToggleEvent(iPlayer, ability, oldState, newState);
        return EventHooks.onPlayerAbilityToggle(handler, event);
    }

    @Override
    protected boolean fireToggleUpdateEvent(Ability ability, int tick, int state) {
        EntityPlayer player = playerData.player;
        if (ScriptController.Instance == null || player == null) return true;
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        if (handler == null) return true;
        IPlayer iPlayer = (IPlayer) NpcAPI.Instance().getIEntity(player);
        PlayerAbilityEvent.ToggleUpdateEvent event = new PlayerAbilityEvent.ToggleUpdateEvent(iPlayer, ability, tick, state);
        EventHooks.onPlayerAbilityToggleUpdate(handler, event);
        return event.isEnabled();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SELECTION
    // ═══════════════════════════════════════════════════════════════════

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (index == -1 || (index >= 0 && index < unlockedAbilities.size())) {
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
    // COOLDOWNS (Player-specific overloads)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if on cooldown using player reference.
     */
    public boolean isOnCooldown(EntityPlayer player) {
        return player.worldObj.getTotalWorldTime() < cooldownEndTime;
    }

    /**
     * Check if a specific ability key is on cooldown.
     * Respects per-ability cooldown if the ability has it enabled.
     */
    public boolean isOnCooldown(String key, EntityPlayer player) {
        if (AbilityController.Instance != null) {
            Ability ability = AbilityController.Instance.resolveAbility(key);
            if (ability != null && ability.isPerAbilityCooldown()) {
                return isOnPerAbilityCooldown(key);
            }
        }
        return isOnCooldown(player);
    }

    public long getRemainingCooldown(EntityPlayer player) {
        long remaining = cooldownEndTime - player.worldObj.getTotalWorldTime();
        return remaining > 0 ? remaining : 0;
    }

    /**
     * Reset cooldown for a specific ability key.
     * Resets per-ability cooldown if applicable, and global cooldown.
     */
    public void resetCooldown(String key) {
        resetPerAbilityCooldown(key);
        cooldownEndTime = 0;
        syncCooldownToClient();
    }

    /**
     * Reset all cooldowns (global + all per-ability).
     */
    public void resetAllCooldowns() {
        cooldownEndTime = 0;
        resetAllPerAbilityCooldowns();
        syncCooldownToClient();
    }

    // ═══════════════════════════════════════════════════════════════════
    // IPlayerAbilityData INTERFACE
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public boolean activateAbility() {
        return activateAbility(playerData.player);
    }

    @Override
    public boolean activateAbility(String key) {
        return activateAbility(playerData.player, key);
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
        resetAllPerAbilityCooldowns();
        syncCooldownToClient();
    }

    // ═══════════════════════════════════════════════════════════════════
    // INTERRUPTION (convenience overload)
    // ═══════════════════════════════════════════════════════════════════

    public void interruptCurrentAbility() {
        interruptCurrentAbility(null, 0);
    }

    /**
     * Try to cancel the current ability via double-press.
     * Only succeeds if within the cancel window and not in DAZED phase.
     * Also handles cancellation during chain delay (between chain entries).
     *
     * @return true if the ability was cancelled
     */
    public boolean tryCancelAbility() {
        boolean hasExecutingAbility = currentAbility != null && currentAbility.isExecuting();
        boolean isInChainDelay = currentChain != null && chainDelayRemaining > 0;

        if (!hasExecutingAbility && !isInChainDelay) return false;

        // Don't allow cancel during DAZED
        if (hasExecutingAbility) {
            AbilityPhase phase = currentAbility.getPhase();
            if (phase == AbilityPhase.DAZED) return false;
            if (phase != AbilityPhase.WINDUP && phase != AbilityPhase.ACTIVE
                && phase != AbilityPhase.BURST_DELAY) return false;
        }

        long worldTime = getWorldTime();
        if (lastAbilityActivationTime < 0 || (worldTime - lastAbilityActivationTime) > CANCEL_WINDOW_TICKS) {
            // Outside cancel window — update time so a quick follow-up press can cancel
            lastAbilityActivationTime = worldTime;
            return false;
        }

        cancelCurrentAbility();
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ENTITY RECONSTRUCTION RESET
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Handle entity reconstruction events (dimension change, death/respawn, etc.).
     * Cleans up any executing ability state without firing script events (those were
     * already fired on the original entity), clears transient runtime state, and
     * re-syncs ability data to the client on the new entity.
     * <p>
     * This should be called whenever the player's entity object is recreated by Forge,
     * since the client-side entity loses all synced ability state.
     *
     * @param clearCooldowns true to fully reset cooldowns (dimension change),
     *                       false to preserve cooldowns (death/respawn — interrupt already rolled them)
     * @param clearToggles   true to clear active toggles (death/respawn),
     *                       false to keep them (dimension change)
     */
    public void onEntityReconstructed(boolean clearCooldowns, boolean clearToggles) {
        // Clean up any executing ability silently (no script events — already fired on old entity)
        if (currentAbility != null && currentAbility.isExecuting()) {
            removeTelegraph(currentAbility);
            currentAbility.cleanup();
            currentAbility.interrupt();
            stopAbilityAnimation();
            releaseRotationControl();
            releaseLockedPosition();
        }

        // Clear chain and concurrent state
        currentChain = null;
        chainEntryIndex = -1;
        chainDelayRemaining = -1;
        interruptConcurrentSlots();

        // Clear all transient state
        currentAbility = null;
        currentAbilityKey = null;
        currentTarget = null;
        lastAbilityActivationTime = -1;

        if (clearCooldowns) {
            cooldownEndTime = 0;
            resetAllPerAbilityCooldowns();
        }
        interruptCooldownRolled = false;

        if (clearToggles) {
            clearActiveToggles();
        }

        // Force-send cleared lock state to client. We must always send this
        // (not gated by lastSyncedFlags != 0) because the NEW entity's client
        // may have stale lock state from before the reconstruction.
        lastSyncedFlags = 0;
        EntityPlayer player = playerData.player;
        if (player instanceof EntityPlayerMP) {
            PlayerAbilityStatePacket.sendToPlayer((EntityPlayerMP) player, (byte) 0, 0, 0);
        }

        // Re-sync full ability data (unlocked list, selection, toggles) to client
        syncToClient();
    }

    /**
     * Fully reset ability state when changing dimensions.
     * Clears cooldowns so the player can immediately use abilities in the new dimension.
     * Keeps active toggles (dimension change shouldn't reset ongoing effects).
     */
    public void resetOnDimensionChange() {
        onEntityReconstructed(true, false);
    }

    /**
     * Reset ability state after death/respawn.
     * Preserves cooldowns (interrupt on death already rolled them) but clears
     * active toggles (death should end ongoing effects).
     */
    public void resetOnRespawn() {
        onEntityReconstructed(false, true);
    }

    // ═══════════════════════════════════════════════════════════════════
    // DAMAGE HANDLING
    // ═══════════════════════════════════════════════════════════════════

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
            interruptCurrentAbility(source, amount);
        }

        return amount;
    }

    // ═══════════════════════════════════════════════════════════════════
    // STATE QUERIES (Player-specific)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if the player's movement should be locked due to an executing ability.
     * Used by the tick handler to restrict player movement during ability phases.
     */
    public boolean isMovementLocked() {
        return currentAbility != null && currentAbility.isExecuting()
            && currentAbility.isMovementLockedForCurrentPhase();
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

    // ═══════════════════════════════════════════════════════════════════
    // ROTATION CONTROL (Player-specific application)
    // ═══════════════════════════════════════════════════════════════════

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
    // POSITION LOCKING (Player-specific application)
    // ═══════════════════════════════════════════════════════════════════

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
        player.motionY = 0;
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
        if (wasFlyingAtLock) {
            flags |= PlayerAbilityStatePacket.FLAG_WAS_FLYING_AT_LOCK;
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
    // SCRIPT EVENTS (Player-specific: start event only, others via abstract)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Fire the ability start event. Returns true if cancelled.
     */
    private boolean firePlayerStartEvent(EntityPlayer player, Ability ability) {
        if (ScriptController.Instance == null) return false;
        PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(player);
        if (handler == null) return false;
        IPlayer iPlayer = (IPlayer) NpcAPI.Instance().getIEntity(player);
        PlayerAbilityEvent.StartEvent event = new PlayerAbilityEvent.StartEvent(iPlayer, ability, null);
        return EventHooks.onPlayerAbilityStart(handler, event);
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

        // Active toggles (compound format with state)
        NBTTagList toggleList = new NBTTagList();
        for (Map.Entry<String, AbilityToggleEntry> entry : activeToggles.entrySet()) {
            NBTTagCompound toggleNbt = new NBTTagCompound();
            toggleNbt.setString("Key", entry.getKey());
            toggleNbt.setInteger("State", entry.getValue().getState());
            toggleList.appendTag(toggleNbt);
        }
        compound.setTag("ActiveToggles", toggleList);
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

    /**
     * Remove any unlocked ability keys that can no longer be resolved or
     * are no longer allowed for players (e.g. NPC-only abilities).
     * Called during load to clean up references to deleted/restricted abilities.
     * Supports both regular ability keys and "chain:" prefixed chained ability keys.
     */
    private void validateUnlockedAbilities() {
        if (AbilityController.Instance == null) return;

        Iterator<String> it = unlockedAbilities.iterator();
        while (it.hasNext()) {
            String key = it.next();
            IAbilityAction resolved = resolveActionKey(key);
            if (resolved == null) {
                it.remove();
                LogWriter.info("Removed invalid ability reference from player data: " + key);
            } else if (!resolved.getAllowedBy().allowsPlayer()) {
                it.remove();
                LogWriter.info("Removed non-player ability from player data: " + key);
            }
        }
    }
}
