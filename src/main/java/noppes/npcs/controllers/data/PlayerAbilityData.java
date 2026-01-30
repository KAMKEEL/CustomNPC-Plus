package noppes.npcs.controllers.data;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.ability.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.UserType;
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
import noppes.npcs.api.ability.IPlayerAbilityData;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;

import kamkeel.npcs.network.packets.data.ability.PlayerAbilitySyncPacket;

import java.util.ArrayList;
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
        }
    }

    /**
     * Tick the currently executing ability.
     */
    private void tickCurrentAbility(EntityPlayer player) {
        AbilityPhase oldPhase = currentAbility.getPhase();

        // Tick advances time and possibly changes phase
        boolean phaseChanged = currentAbility.tick();

        switch (currentAbility.getPhase()) {
            case WINDUP:
                currentAbility.onWindUpTick(player, currentTarget, player.worldObj, currentAbility.getCurrentTick());
                break;

            case ACTIVE:
                if (phaseChanged && oldPhase == AbilityPhase.WINDUP) {
                    // Just entered ACTIVE phase
                    TelegraphInstance telegraph = currentAbility.getTelegraphInstance();
                    if (telegraph != null) {
                        telegraph.lockPosition();
                    }
                    removeTelegraph(currentAbility, player);

                    // Play active sound and animation
                    playAbilitySound(player, currentAbility.getActiveSound());
                    playAbilityAnimation(currentAbility.getActiveAnimation());

                    // Call onExecute
                    currentAbility.onExecute(player, currentTarget, player.worldObj);

                    if (currentAbility.getPhase() == AbilityPhase.IDLE) {
                        handleAbilityCompletion(player);
                        return;
                    }
                }
                currentAbility.onActiveTick(player, currentTarget, player.worldObj, currentAbility.getCurrentTick());

                if (currentAbility.getPhase() == AbilityPhase.IDLE) {
                    handleAbilityCompletion(player);
                    return;
                }
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

        currentAbility.onComplete(player, currentTarget);

        // Apply universal cooldown (using base cooldownTicks only, not min/max random)
        if (!currentAbility.isIgnoreCooldown()) {
            cooldownEndTime = player.worldObj.getTotalWorldTime() + currentAbility.getCooldownTicks();
        }

        // Stop animation
        stopAbilityAnimation();

        currentAbility = null;
        currentAbilityKey = null;
        currentTarget = null;
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

        // Start the ability
        currentAbility = ability;
        currentAbilityKey = key;
        currentTarget = null; // Players don't have auto-targets
        ability.start(null);

        // Spawn telegraph
        spawnTelegraph(ability, player, null);

        // Play wind up sound and animation
        playAbilitySound(player, ability.getWindUpSound());
        playAbilityAnimation(ability.getWindUpAnimation());

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
            currentAbility.interrupt();
            stopAbilityAnimation();
            currentAbility = null;
            currentAbilityKey = null;
            currentTarget = null;
        }
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
    // TELEGRAPH
    // ═══════════════════════════════════════════════════════════════════

    private void spawnTelegraph(Ability ability, EntityPlayer player, EntityLivingBase target) {
        TelegraphInstance telegraph = ability.createTelegraph(player, target);
        if (telegraph != null) {
            ability.setTelegraphInstance(telegraph);
            if (player instanceof EntityPlayerMP) {
                TelegraphSpawnPacket.sendToTracking(telegraph, player);
            }
        }
    }

    private void removeTelegraph(Ability ability, EntityPlayer player) {
        TelegraphInstance telegraph = ability.getTelegraphInstance();
        if (telegraph != null) {
            if (player instanceof EntityPlayerMP) {
                TelegraphRemovePacket.sendToTracking(telegraph.getInstanceId(), player);
            }
            ability.setTelegraphInstance(null);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════════════════════════

    private void playAbilityAnimation(Animation animation) {
        if (animation == null || AnimationController.Instance == null) return;
        playerData.animationData.setEnabled(true);
        playerData.animationData.setAnimation(animation);
        playerData.animationData.updateClient();
    }

    private void stopAbilityAnimation() {
        playerData.animationData.setAnimation(null);
        playerData.animationData.updateClient();
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
        if (selectedIndex >= unlockedAbilities.size()) {
            selectedIndex = Math.max(0, unlockedAbilities.size() - 1);
        }
    }
}
