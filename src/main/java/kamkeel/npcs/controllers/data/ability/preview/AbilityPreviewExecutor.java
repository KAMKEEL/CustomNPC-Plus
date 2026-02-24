package kamkeel.npcs.controllers.data.ability.preview;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.enums.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import kamkeel.npcs.controllers.data.ability.data.entry.ChainedAbilityEntry;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import kamkeel.npcs.controllers.data.ability.gui.GuiAbilityInterface;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client-side executor for previewing abilities in the GUI.
 * <p>
 * Uses unified execution: calls the REAL ability methods (onWindUpTick, onExecute, onActiveTick)
 * with previewMode=true. Abilities skip damage/effects/sounds when isPreview() returns true.
 * <p>
 * Lifecycle:
 * 1. WINDUP phase - real onWindUpTick() called, telegraph shown, entities may spawn
 * 2. ACTIVE phase - real onExecute() then onActiveTick() called, physics simulated
 * 3. EXTENDED phase - 100 extra ticks to observe results (entities continue updating)
 * 4. IDLE - preview complete
 */
@SideOnly(Side.CLIENT)
public class AbilityPreviewExecutor implements PreviewEntityHandler {

    // ==================== PREVIEW STATE ====================
    private Ability previewAbility;
    private EntityNPCInterface previewNpc;

    /**
     * Executor's own phase tracking (separate from ability.getPhase())
     */
    private enum ExecutorPhase {IDLE, WINDUP, ACTIVE, EXTENDED, CHAIN_DELAY}

    private ExecutorPhase executorPhase = ExecutorPhase.IDLE;

    private int currentTick = 0;
    private boolean playing = false;
    private boolean paused = false;

    // ==================== TIMING ====================
    private static final int EXTENDED_DURATION = 100;
    private int maxPreviewDuration = 200;

    // ==================== FAKE TARGET ====================
    private EntityNPCInterface fakeTarget;
    private static final double TARGET_DISTANCE = 5.0;

    // ==================== NPC START POSITION ====================
    private double npcStartX, npcStartY, npcStartZ;
    private boolean hasStartPosition = false;

    // ==================== PREVIEW CONTENT ====================
    private TelegraphInstance previewTelegraph;
    private List<Entity> previewEntities = new ArrayList<>();

    // ==================== CHAIN PREVIEW ====================
    private ChainedAbility previewChain;
    private List<Ability> chainAbilities;
    private List<Integer> chainDelays;
    private int chainIndex = -1;
    private int chainDelayRemaining = 0;
    private boolean chainWindUpAll = true;

    // ==================== CALLBACKS ====================
    private GuiAbilityInterface parentGui;

    public AbilityPreviewExecutor() {
    }

    public void setParentGui(GuiAbilityInterface gui) {
        this.parentGui = gui;
    }

    /**
     * Start previewing a single ability using unified execution.
     * Sets previewMode=true on the ability, then calls its real methods.
     */
    public void startPreview(Ability ability, EntityNPCInterface npc) {
        stop();

        this.previewNpc = npc;
        this.playing = true;
        this.paused = false;

        // Save NPC start position for restoring later
        this.npcStartX = npc.posX;
        this.npcStartY = npc.posY;
        this.npcStartZ = npc.posZ;
        this.hasStartPosition = true;

        // Create invisible fake target NPC for targeting
        createFakeTarget(npc);

        // Start tracking NPC movement
        if (parentGui != null) {
            parentGui.startTrackingMovement();
        }

        beginAbilityPreview(ability);
    }

    /**
     * Start previewing a chained ability — plays all entries in sequence with delays.
     * Each entry is resolved and deep-copied to avoid modifying originals.
     */
    public void startChainPreview(ChainedAbility chain, EntityNPCInterface npc) {
        stop();

        this.previewNpc = npc;
        this.playing = true;
        this.paused = false;
        this.previewChain = chain;
        this.chainWindUpAll = chain.isWindUpAll();

        // Save NPC start position for restoring later
        this.npcStartX = npc.posX;
        this.npcStartY = npc.posY;
        this.npcStartZ = npc.posZ;
        this.hasStartPosition = true;

        // Resolve all entries to deep-copied abilities
        chainAbilities = new ArrayList<>();
        chainDelays = new ArrayList<>();
        for (ChainedAbilityEntry entry : chain.getEntries()) {
            Ability resolved = entry.resolve();
            if (resolved != null && AbilityController.Instance != null) {
                Ability copy = AbilityController.Instance.fromNBT(resolved.writeNBT());
                if (copy != null) {
                    chainAbilities.add(copy);
                    chainDelays.add(entry.getDelayTicks());
                }
            }
        }

        if (chainAbilities.isEmpty()) {
            playing = false;
            return;
        }

        // Create invisible fake target NPC for targeting
        createFakeTarget(npc);

        // Start tracking NPC movement
        if (parentGui != null) {
            parentGui.startTrackingMovement();
        }

        chainIndex = 0;
        beginAbilityPreview(chainAbilities.get(0));
    }

    /**
     * Begin previewing a single ability (shared by single and chain preview).
     * Cleans up the previous ability if any (for chain advancement).
     */
    private void beginAbilityPreview(Ability ability) {
        // Clean up previous ability if any (for chain advancement)
        if (previewAbility != null) {
            previewAbility.cleanup();
            previewAbility.setPreviewMode(false);
            previewAbility.setPreviewEntityHandler(null);
            previewAbility.reset();
        }

        this.previewAbility = ability;
        this.currentTick = 0;
        this.maxPreviewDuration = ability.getMaxPreviewDuration();

        // Enable preview mode on the ability
        ability.setPreviewMode(true);
        ability.setPreviewEntityHandler(this);

        // Use the ability's real start() method with the fake target
        ability.start(fakeTarget);

        // Skip windup for non-first chain entries when windUpAll is false
        boolean skipWindup = previewChain != null && !chainWindUpAll && chainIndex > 0;

        if (ability.getWindUpTicks() <= 0 || skipWindup) {
            // No windup — go straight to active
            this.executorPhase = ExecutorPhase.ACTIVE;
            transitionToActive();
        } else {
            // Normal windup flow
            this.executorPhase = ExecutorPhase.WINDUP;

            // Create telegraph for preview
            previewTelegraph = ability.createTelegraph(previewNpc, fakeTarget);

            // Start windup animation
            Animation windUpAnim = ability.getWindUpAnimation();
            if (windUpAnim != null) {
                AnimationData data = previewNpc.display.animationData;
                data.setEnabled(true);
                data.setAnimation(windUpAnim);
                data.animation.paused = false;
            }
        }
    }

    /**
     * Create an invisible fake target NPC placed in front of the preview NPC.
     */
    private void createFakeTarget(EntityNPCInterface npc) {
        if (npc.worldObj == null) return;

        try {
            fakeTarget = new EntityCustomNpc(npc.worldObj);
            float facingYaw = GuiAbilityInterface.NPC_FACING_YAW;
            double yawRad = Math.toRadians(facingYaw);
            double targetX = npc.posX - Math.sin(yawRad) * TARGET_DISTANCE;
            double targetY = npc.posY;
            double targetZ = npc.posZ + Math.cos(yawRad) * TARGET_DISTANCE;
            fakeTarget.setPosition(targetX, targetY, targetZ);
            fakeTarget.prevPosX = targetX;
            fakeTarget.prevPosY = targetY;
            fakeTarget.prevPosZ = targetZ;
        } catch (Exception e) {
            fakeTarget = null;
        }
    }

    public EntityLivingBase getFakeTarget() {
        return fakeTarget;
    }

    public void play() {
        if (previewAbility == null || executorPhase == ExecutorPhase.IDLE) {
            return;
        }
        playing = true;
        paused = false;

        if (previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            if (data.animation != null) {
                data.animation.paused = false;
            }
        }
    }

    public void pause() {
        paused = true;

        // Immediately sync prevPos to pos for NPC and all entities to prevent interpolation jitter
        syncPrevPositions();

        if (previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            if (data.animation != null) {
                data.animation.paused = true;
            }
        }
    }

    /**
     * Sync prevPos to pos for the NPC and all preview entities.
     * Prevents rendering interpolation jitter when standing still or paused.
     */
    private void syncPrevPositions() {
        if (previewNpc != null) {
            previewNpc.prevPosX = previewNpc.posX;
            previewNpc.prevPosY = previewNpc.posY;
            previewNpc.prevPosZ = previewNpc.posZ;
        }
        for (Entity entity : previewEntities) {
            if (entity != null && !entity.isDead) {
                entity.prevPosX = entity.posX;
                entity.prevPosY = entity.posY;
                entity.prevPosZ = entity.posZ;
            }
        }
    }

    public void stop() {
        // Clean up the ability
        if (previewAbility != null) {
            previewAbility.cleanup();
            previewAbility.setPreviewMode(false);
            previewAbility.setPreviewEntityHandler(null);
            previewAbility.reset();
        }

        playing = false;
        paused = false;
        executorPhase = ExecutorPhase.IDLE;
        currentTick = 0;

        // Clear chain state
        previewChain = null;
        chainAbilities = null;
        chainDelays = null;
        chainIndex = -1;
        chainDelayRemaining = 0;

        previewTelegraph = null;

        if (parentGui != null) {
            for (Entity entity : previewEntities) {
                parentGui.removePreviewEntity(entity);
            }
            parentGui.stopTrackingMovement();
            parentGui.setPreviewTelegraph(null);
        }
        previewEntities.clear();

        // Reset NPC position to start position (undo movement from Slam/Charge/Dash)
        if (previewNpc != null && hasStartPosition) {
            previewNpc.posX = npcStartX;
            previewNpc.posY = npcStartY;
            previewNpc.posZ = npcStartZ;
            previewNpc.prevPosX = npcStartX;
            previewNpc.prevPosY = npcStartY;
            previewNpc.prevPosZ = npcStartZ;
            previewNpc.motionX = 0;
            previewNpc.motionY = 0;
            previewNpc.motionZ = 0;
        }

        fakeTarget = null;

        if (previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            data.setAnimation(new Animation());
            data.animation.paused = false;
        }
    }

    /**
     * Tick the preview forward. Calls real ability methods.
     */
    public void tick() {
        if (!playing || previewAbility == null || previewNpc == null) {
            return;
        }

        if (paused) {
            // Sync prevPos to pos for NPC and all entities to prevent interpolation jitter while paused
            syncPrevPositions();
            return;
        }

        // Sync NPC's prevPos to current pos BEFORE any updates to prevent jitter when standing still
        previewNpc.prevPosX = previewNpc.posX;
        previewNpc.prevPosY = previewNpc.posY;
        previewNpc.prevPosZ = previewNpc.posZ;

        currentTick++;

        if (previewTelegraph != null) {
            previewTelegraph.tick(null);
        }

        tickEntities();

        switch (executorPhase) {
            case WINDUP:
                tickWindup();
                break;
            case ACTIVE:
                tickActive();
                break;
            case EXTENDED:
                tickExtended();
                break;
            case CHAIN_DELAY:
                tickChainDelay();
                break;
            default:
                break;
        }
    }

    private void tickWindup() {
        // Call the REAL windup tick method
        previewAbility.onWindUpTick(previewNpc, fakeTarget, currentTick);

        if (currentTick >= previewAbility.getWindUpTicks()) {
            transitionToActive();
        }
    }

    private void tickActive() {
        // Call the REAL active tick method
        previewAbility.onActiveTick(previewNpc, fakeTarget, currentTick);

        // Simulate physics for movement abilities
        simulatePhysics();

        // Check if the ability signaled completion (phase went to IDLE)
        if (previewAbility.getPhase() == AbilityPhase.IDLE) {
            if (hasNextChainEntry()) {
                transitionToChainDelay();
            } else {
                transitionToExtended();
            }
            return;
        }

        // Safety timeout
        if (currentTick >= maxPreviewDuration) {
            if (hasNextChainEntry()) {
                transitionToChainDelay();
            } else {
                transitionToExtended();
            }
        }
    }

    private void tickExtended() {
        // Continue ticking entities so they can finish their animations
        if (currentTick >= EXTENDED_DURATION) {
            playing = false;
        }
    }

    // ==================== CHAIN ADVANCEMENT ====================

    private boolean hasNextChainEntry() {
        return previewChain != null && chainAbilities != null && chainIndex < chainAbilities.size() - 1;
    }

    private void transitionToChainDelay() {
        executorPhase = ExecutorPhase.CHAIN_DELAY;
        currentTick = 0;

        // Use the NEXT entry's delay
        int nextIndex = chainIndex + 1;
        chainDelayRemaining = (nextIndex < chainDelays.size()) ? chainDelays.get(nextIndex) : 0;

        // Clear current animation
        if (previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            data.setAnimation(new Animation());
        }
    }

    private void tickChainDelay() {
        if (chainDelayRemaining > 0) {
            chainDelayRemaining--;
            return;
        }
        advanceChainEntry();
    }

    private void advanceChainEntry() {
        chainIndex++;
        if (chainIndex >= chainAbilities.size()) {
            transitionToExtended();
            return;
        }
        beginAbilityPreview(chainAbilities.get(chainIndex));
    }

    private void transitionToActive() {
        executorPhase = ExecutorPhase.ACTIVE;
        currentTick = 0;

        previewTelegraph = null;

        // Call the REAL onExecute method (fires entities, initiates movement, etc.)
        previewAbility.onExecute(previewNpc, fakeTarget);

        // Start active animation
        Animation activeAnim = previewAbility.getActiveAnimation();
        if (activeAnim != null && previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            data.setAnimation(activeAnim);
            data.animation.paused = false;
        }
    }

    private void transitionToExtended() {
        executorPhase = ExecutorPhase.EXTENDED;
        currentTick = 0;

        if (previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            data.setAnimation(new Animation());
        }
    }

    /**
     * Simulate basic physics for movement abilities (Slam, Charge, Dash).
     * Applies gravity, drag, and ground clamping to NPC position.
     */
    private void simulatePhysics() {
        if (!previewAbility.hasAbilityMovement()) return;
        if (previewNpc == null) return;

        // Save previous position for interpolation
        previewNpc.prevPosX = previewNpc.posX;
        previewNpc.prevPosY = previewNpc.posY;
        previewNpc.prevPosZ = previewNpc.posZ;

        // Apply velocity
        previewNpc.posX += previewNpc.motionX;
        previewNpc.posY += previewNpc.motionY;
        previewNpc.posZ += previewNpc.motionZ;

        // Apply gravity
        previewNpc.motionY -= 0.08;
        previewNpc.motionY *= 0.98;

        // Apply horizontal drag
        previewNpc.motionX *= 0.91;
        previewNpc.motionZ *= 0.91;

        // Ground clamp: if below start position and falling, stop
        if (previewNpc.posY < npcStartY && previewNpc.motionY < 0) {
            previewNpc.posY = npcStartY;
            previewNpc.motionY = 0;
            previewNpc.onGround = true;
        }

        previewNpc.fallDistance = 0;
    }

    private void tickEntities() {
        Iterator<Entity> iter = previewEntities.iterator();
        while (iter.hasNext()) {
            Entity entity = iter.next();
            if (entity == null || entity.isDead) {
                if (parentGui != null) {
                    parentGui.removePreviewEntity(entity);
                }
                iter.remove();
                continue;
            }
            entity.onUpdate();
        }
    }

    // ==================== PreviewEntityHandler ====================

    @Override
    public void onEntitySpawned(Entity entity) {
        if (entity == null) return;
        previewEntities.add(entity);
        if (parentGui != null) {
            parentGui.addPreviewEntity(entity);
        }
    }

    @Override
    public void onEntityRemoved(Entity entity) {
        if (entity == null) return;
        previewEntities.remove(entity);
        if (parentGui != null) {
            parentGui.removePreviewEntity(entity);
        }
    }

    // ==================== GETTERS ====================

    public boolean isPlaying() {
        return playing;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isActive() {
        return playing || paused;
    }

    public boolean isChainPreview() {
        return previewChain != null;
    }

    public AbilityPhase getPhase() {
        switch (executorPhase) {
            case WINDUP:
                return AbilityPhase.WINDUP;
            case ACTIVE:
                return AbilityPhase.ACTIVE;
            default:
                return AbilityPhase.IDLE;
        }
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public TelegraphInstance getTelegraph() {
        return previewTelegraph;
    }

    public List<Entity> getPreviewEntities() {
        return previewEntities;
    }

    public Ability getPreviewAbility() {
        return previewAbility;
    }

    public EntityNPCInterface getPreviewNpc() {
        return previewNpc;
    }

    public String getStatusString() {
        if (!playing && !paused) {
            return "Stopped";
        }

        String chainPrefix = "";
        if (previewChain != null && chainAbilities != null) {
            chainPrefix = "Chain " + (chainIndex + 1) + "/" + chainAbilities.size() + " - ";
        }

        if (paused) {
            return chainPrefix + "Paused";
        } else if (executorPhase == ExecutorPhase.WINDUP) {
            return chainPrefix + "Windup: " + currentTick + "/" + (previewAbility != null ? previewAbility.getWindUpTicks() : 0);
        } else if (executorPhase == ExecutorPhase.ACTIVE) {
            return chainPrefix + "Active: " + currentTick + "/" + maxPreviewDuration;
        } else if (executorPhase == ExecutorPhase.EXTENDED) {
            return chainPrefix + "Extended: " + currentTick + "/" + EXTENDED_DURATION;
        } else if (executorPhase == ExecutorPhase.CHAIN_DELAY) {
            return chainPrefix + "Delay: " + chainDelayRemaining;
        }
        return "";
    }
}
