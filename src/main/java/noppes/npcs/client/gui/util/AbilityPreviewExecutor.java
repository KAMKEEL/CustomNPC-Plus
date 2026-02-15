package noppes.npcs.client.gui.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.AnimationData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client-side executor for previewing abilities in the GUI.
 *
 * Uses unified execution: calls the REAL ability methods (onWindUpTick, onExecute, onActiveTick)
 * with previewMode=true. Abilities skip damage/effects/sounds when isPreview() returns true.
 *
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

    /** Executor's own phase tracking (separate from ability.getPhase()) */
    private enum ExecutorPhase { IDLE, WINDUP, ACTIVE, EXTENDED }
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

    // ==================== PREVIEW CONTENT ====================
    private TelegraphInstance previewTelegraph;
    private List<Entity> previewEntities = new ArrayList<>();

    // ==================== CALLBACKS ====================
    private GuiAbilityInterface parentGui;

    public AbilityPreviewExecutor() {
    }

    public void setParentGui(GuiAbilityInterface gui) {
        this.parentGui = gui;
    }

    /**
     * Start previewing an ability using unified execution.
     * Sets previewMode=true on the ability, then calls its real methods.
     */
    public void startPreview(Ability ability, EntityNPCInterface npc) {
        stop();

        this.previewAbility = ability;
        this.previewNpc = npc;
        this.currentTick = 0;
        this.playing = true;
        this.paused = false;
        this.maxPreviewDuration = ability.getMaxPreviewDuration();

        // Save NPC start position for restoring later
        this.npcStartX = npc.posX;
        this.npcStartY = npc.posY;
        this.npcStartZ = npc.posZ;

        // Enable preview mode on the ability
        ability.setPreviewMode(true);
        ability.setPreviewEntityHandler(this);

        // Create invisible fake target NPC for targeting
        createFakeTarget(npc);

        // Start tracking NPC movement
        if (parentGui != null) {
            parentGui.startTrackingMovement();
        }

        // Use the ability's real start() method with the fake target
        ability.start(fakeTarget);

        if (ability.getWindUpTicks() <= 0) {
            // No windup — go straight to active
            this.executorPhase = ExecutorPhase.ACTIVE;
            transitionToActive();
        } else {
            // Normal windup flow
            this.executorPhase = ExecutorPhase.WINDUP;

            // Create telegraph for preview
            previewTelegraph = ability.createTelegraph(npc, fakeTarget);

            // Start windup animation
            Animation windUpAnim = ability.getWindUpAnimation();
            if (windUpAnim != null) {
                AnimationData data = npc.display.animationData;
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
        if (previewNpc != null && npcStartX != 0 && npcStartY != 0 && npcStartZ != 0) {
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
            transitionToExtended();
            return;
        }

        // Safety timeout
        if (currentTick >= maxPreviewDuration) {
            transitionToExtended();
        }
    }

    private void tickExtended() {
        // Continue ticking entities so they can finish their animations
        if (currentTick >= EXTENDED_DURATION) {
            playing = false;
        }
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

    public boolean isPlaying() { return playing; }
    public boolean isPaused() { return paused; }
    public boolean isActive() { return playing || paused; }
    public AbilityPhase getPhase() {
        switch (executorPhase) {
            case WINDUP: return AbilityPhase.WINDUP;
            case ACTIVE: return AbilityPhase.ACTIVE;
            default: return AbilityPhase.IDLE;
        }
    }
    public int getCurrentTick() { return currentTick; }
    public TelegraphInstance getTelegraph() { return previewTelegraph; }
    public List<Entity> getPreviewEntities() { return previewEntities; }
    public Ability getPreviewAbility() { return previewAbility; }
    public EntityNPCInterface getPreviewNpc() { return previewNpc; }

    public String getStatusString() {
        if (!playing && !paused) {
            return "Stopped";
        } else if (paused) {
            return "Paused";
        } else if (executorPhase == ExecutorPhase.WINDUP) {
            return "Windup: " + currentTick + "/" + (previewAbility != null ? previewAbility.getWindUpTicks() : 0);
        } else if (executorPhase == ExecutorPhase.ACTIVE) {
            return "Active: " + currentTick + "/" + maxPreviewDuration;
        } else if (executorPhase == ExecutorPhase.EXTENDED) {
            return "Extended: " + currentTick + "/" + EXTENDED_DURATION;
        }
        return "";
    }
}
