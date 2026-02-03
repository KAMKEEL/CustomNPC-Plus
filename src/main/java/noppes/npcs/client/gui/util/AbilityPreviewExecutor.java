package noppes.npcs.client.gui.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityPhase;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
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
 * Lifecycle:
 * 1. WINDUP phase - plays windup animation, entities spawn and charge
 * 2. ACTIVE phase - plays active animation, entities fire and move
 * 3. EXTENDED phase - continues for +100 ticks after animations to see results
 * 4. IDLE - preview complete
 *
 * Features:
 * - Invisible fake target NPC placed in front of the preview NPC for targeting
 * - NPC movement tracking for abilities that move the NPC (e.g. slam)
 */
@SideOnly(Side.CLIENT)
public class AbilityPreviewExecutor {

    // ==================== PREVIEW STATE ====================
    private Ability previewAbility;
    private EntityNPCInterface previewNpc;
    private AbilityPhase phase = AbilityPhase.IDLE;
    private int currentTick = 0;
    private boolean playing = false;
    private boolean paused = false;

    // ==================== TIMING ====================
    private static final int EXTENDED_DURATION = 100;
    private int activeDuration = 40;

    // ==================== FAKE TARGET ====================
    /** Invisible fake target NPC for ability targeting (placed 5 blocks in front of preview NPC) */
    private EntityNPCInterface fakeTarget;
    private static final double TARGET_DISTANCE = 5.0;

    // ==================== NPC START POSITION ====================
    /** Saved NPC position for restoring after movement abilities */
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
     * Start previewing an ability.
     */
    public void startPreview(Ability ability, EntityNPCInterface npc) {
        stop();

        this.previewAbility = ability;
        this.previewNpc = npc;
        this.phase = AbilityPhase.WINDUP;
        this.currentTick = 0;
        this.playing = true;
        this.paused = false;
        this.activeDuration = ability.getPreviewActiveDuration();

        // Save NPC start position for restoring later
        this.npcStartX = npc.posX;
        this.npcStartY = npc.posY;
        this.npcStartZ = npc.posZ;

        // Create invisible fake target NPC for targeting
        createFakeTarget(npc);

        // Set preview target on the ability
        ability.setPreviewTarget(fakeTarget);

        // Start tracking NPC movement
        if (parentGui != null) {
            parentGui.startTrackingMovement();
        }

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

        // Spawn preview entity on first tick if ability spawns during windup
        if (ability.spawnPreviewDuringWindup()) {
            spawnPreviewEntity();
        }
    }

    /**
     * Create an invisible fake target NPC placed in front of the preview NPC.
     * This entity is NOT rendered - it only exists for ability targeting logic.
     */
    private void createFakeTarget(EntityNPCInterface npc) {
        if (npc.worldObj == null) return;

        try {
            fakeTarget = new EntityCustomNpc(npc.worldObj);
            // Position target in front of NPC based on its facing direction
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

    /**
     * Get the fake target entity for abilities that need a target.
     */
    public EntityLivingBase getFakeTarget() {
        return fakeTarget;
    }

    public void play() {
        if (previewAbility == null || phase == AbilityPhase.IDLE) {
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

        if (previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            if (data.animation != null) {
                data.animation.paused = true;
            }
        }
    }

    public void stop() {
        playing = false;
        paused = false;
        phase = AbilityPhase.IDLE;
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
        }

        // Remove fake target
        fakeTarget = null;

        if (previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            data.setAnimation(new Animation());
            data.animation.paused = false;
        }
    }

    /**
     * Tick the preview forward.
     */
    public void tick() {
        if (!playing || paused || previewAbility == null || previewNpc == null) {
            return;
        }

        currentTick++;

        if (previewTelegraph != null) {
            previewTelegraph.tick(null);
        }

        tickEntities();

        switch (phase) {
            case WINDUP:
                tickWindup();
                break;
            case ACTIVE:
                tickActive();
                break;
            case IDLE:
                tickExtended();
                break;
            default:
                break;
        }
    }

    private void tickWindup() {
        previewAbility.onPreviewWindUpTick(previewNpc, currentTick);

        if (currentTick >= previewAbility.getWindUpTicks()) {
            transitionToActive();
        }
    }

    private void tickActive() {
        previewAbility.onPreviewActiveTick(previewNpc, currentTick);

        if (currentTick == 1 && !previewAbility.spawnPreviewDuringWindup()) {
            spawnPreviewEntity();
            fireEntities();
        }

        if (currentTick >= activeDuration) {
            transitionToExtended();
        }
    }

    private void tickExtended() {
        if (currentTick >= EXTENDED_DURATION) {
            playing = false;
        }
    }

    private void transitionToActive() {
        phase = AbilityPhase.ACTIVE;
        currentTick = 0;

        previewTelegraph = null;

        // Notify ability of execution (for movement abilities like Slam/Charge/Dash)
        previewAbility.onPreviewExecute(previewNpc);

        fireEntities();

        Animation activeAnim = previewAbility.getActiveAnimation();
        if (activeAnim != null && previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            data.setAnimation(activeAnim);
            data.animation.paused = false;
        }
    }

    private void transitionToExtended() {
        phase = AbilityPhase.IDLE;
        currentTick = 0;

        if (previewNpc != null) {
            AnimationData data = previewNpc.display.animationData;
            data.setAnimation(new Animation());
        }
    }

    private void spawnPreviewEntity() {
        Entity entity = previewAbility.createPreviewEntity(previewNpc);
        if (entity != null) {
            previewEntities.add(entity);
            if (parentGui != null) {
                parentGui.addPreviewEntity(entity);
            }
        }
    }

    private void fireEntities() {
        for (Entity entity : previewEntities) {
            if (entity instanceof EntityAbilityOrb) {
                ((EntityAbilityOrb) entity).startPreviewFiring();
            } else if (entity instanceof EntityAbilityBeam) {
                ((EntityAbilityBeam) entity).startPreviewFiring();
            } else if (entity instanceof EntityAbilityDisc) {
                ((EntityAbilityDisc) entity).startPreviewFiring();
            }
        }
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

    // ==================== GETTERS ====================

    public boolean isPlaying() { return playing; }
    public boolean isPaused() { return paused; }
    public boolean isActive() { return playing || paused; }
    public AbilityPhase getPhase() { return phase; }
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
        } else if (phase == AbilityPhase.WINDUP) {
            return "Windup: " + currentTick + "/" + (previewAbility != null ? previewAbility.getWindUpTicks() : 0);
        } else if (phase == AbilityPhase.ACTIVE) {
            return "Active: " + currentTick + "/" + activeDuration;
        } else if (phase == AbilityPhase.IDLE && playing) {
            return "Extended: " + currentTick + "/" + EXTENDED_DURATION;
        }
        return "";
    }
}
