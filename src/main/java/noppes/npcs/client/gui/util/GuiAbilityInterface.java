package noppes.npcs.client.gui.util;

import kamkeel.npcs.client.renderer.TelegraphRenderer;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.ModelData;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

/**
 * Base GUI class for ability preview with fixed camera.
 *
 * Key concepts:
 * - NPC positioned on left side of preview area
 * - NPC faces a fixed direction (NPC_FACING_YAW)
 * - Viewing direction can be rotated (rotates entire 3D space)
 * - Fixed zoom level
 * - Preview entities rendered in same coordinate space as NPC
 * - NPC position changes (e.g. slam jump) tracked and rendered
 */
public class GuiAbilityInterface extends GuiNPCInterface2 {

    // ==================== FIXED CAMERA SETTINGS ====================
    /** Fixed camera zoom level (higher = more zoomed in) */
    protected static float CAMERA_ZOOM = 45.0f;
    /** NPC facing direction - affects anchor points and visual facing */
    protected static float NPC_FACING_YAW = 160.0f;
    /** Camera pitch (slight downward angle) */
    protected static float CAMERA_PITCH = 10.0f;

    // ==================== VIEWING ROTATION ====================
    /** Viewing direction rotation (rotates entire 3D space) */
    protected static float viewRotation = 0.0f;

    private GuiNpcButton btnLeft, btnRight;

    // ==================== NPC STATE ====================
    public ModelData playerdata;
    public EntityNPCInterface npc;

    /** NPC starting position (saved when preview starts, used to track movement) */
    protected double npcStartX, npcStartY, npcStartZ;
    /** Whether we're tracking NPC movement */
    protected boolean trackingNpcMovement = false;

    /** Entities to render in preview space */
    protected List<Entity> previewEntities = new ArrayList<>();

    /** Current telegraph to render in preview */
    protected TelegraphInstance previewTelegraph;

    // ==================== LAYOUT ====================
    /** Preview area bounds */
    protected int previewX, previewY, previewWidth, previewHeight;
    /** NPC render position on screen */
    protected int npcScreenX, npcScreenY;

    public int xOffset = 0;
    public int yOffset = 0;

    public GuiAbilityInterface(EntityNPCInterface npc) {
        super(npc);
        this.npc = npc;
        this.playerdata = ((EntityCustomNpc) npc).modelData;
        this.drawDefaultBackground = false;

        CAMERA_ZOOM = 30f;
        NPC_FACING_YAW = 310f;
        CAMERA_PITCH = 5f;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Calculate preview area (left side of GUI)
        previewX = guiLeft + 10;
        previewY = guiTop + 10;
        previewWidth = 200;
        previewHeight = 180;

        // NPC position: left side of preview, slightly below center
        npcScreenX = previewX + (int)(previewWidth * 0.33f) + xOffset;
        npcScreenY = previewY + (int)(previewHeight * 0.85f) + yOffset;

        // Viewing rotation buttons (below preview area)
        int btnY = guiTop + 192;
        addButton(btnLeft = new GuiNpcButton(680, guiLeft + 10, btnY, 20, 20, "<"));
        addButton(btnRight = new GuiNpcButton(681, guiLeft + 32, btnY, 20, 20, ">"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Handle held rotation buttons
        if (Mouse.isButtonDown(0)) {
            if (btnLeft != null && btnLeft.mousePressed(mc, mouseX, mouseY)) {
                viewRotation += partialTicks * 2;
            } else if (btnRight != null && btnRight.mousePressed(mc, mouseX, mouseY)) {
                viewRotation -= partialTicks * 2;
            }
        }

        // Draw background and UI elements
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw the 3D preview
        drawPreview(mouseX, mouseY, partialTicks);
    }

    /**
     * Draw the 3D preview with NPC and entities.
     */
    protected void drawPreview(int mouseX, int mouseY, float partialTicks) {
        GL11.glColor4f(1, 1, 1, 1);

        // Enable scissor to clip rendering to preview area
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        setScissorClip(previewX, previewY, previewWidth, previewHeight);

        // Get the entity to render (could be transformed player model)
        EntityLivingBase renderEntity = playerdata.getEntity(npc);
        if (renderEntity == null) {
            renderEntity = this.npc;
        }

        // Sync NPC properties to render entity
        EntityUtil.Copy(npc, renderEntity);

        // Set NPC facing direction (affects anchor points and visual facing)
        npc.prevRenderYawOffset = npc.renderYawOffset = NPC_FACING_YAW;
        npc.prevRotationYaw = npc.rotationYaw = NPC_FACING_YAW;
        npc.prevRotationYawHead = npc.rotationYawHead = NPC_FACING_YAW;

        // Also set on render entity
        renderEntity.prevRenderYawOffset = renderEntity.renderYawOffset = NPC_FACING_YAW;
        renderEntity.prevRotationYaw = renderEntity.rotationYaw = NPC_FACING_YAW;
        renderEntity.prevRotationYawHead = renderEntity.rotationYawHead = NPC_FACING_YAW;
        renderEntity.rotationPitch = 0;

        // Calculate NPC position delta for movement rendering (e.g. slam jump)
        // Interpolate with partial ticks for smooth movement between frames
        double npcDeltaX = 0, npcDeltaY = 0, npcDeltaZ = 0;
        if (trackingNpcMovement) {
            double interpX = npc.prevPosX + (npc.posX - npc.prevPosX) * partialTicks;
            double interpY = npc.prevPosY + (npc.posY - npc.prevPosY) * partialTicks;
            double interpZ = npc.prevPosZ + (npc.posZ - npc.prevPosZ) * partialTicks;
            npcDeltaX = interpX - npcStartX;
            npcDeltaY = interpY - npcStartY;
            npcDeltaZ = interpZ - npcStartZ;
        }

        // ==================== BEGIN 3D RENDERING ====================
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();

        // Move to NPC render position on screen
        GL11.glTranslatef(npcScreenX, npcScreenY, 300F);

        // Apply zoom (scale)
        GL11.glScalef(-CAMERA_ZOOM, CAMERA_ZOOM, CAMERA_ZOOM);

        // Flip for screen coordinates (Y is inverted)
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

        // Apply slight camera pitch (looking down at scene)
        GL11.glRotatef(CAMERA_PITCH, 1.0F, 0.0F, 0.0F);

        // Apply VIEWING rotation (rotates entire space, not just NPC)
        GL11.glRotatef(viewRotation, 0.0F, 1.0F, 0.0F);

        // Setup lighting (standard item lighting for consistent colors)
        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);

        // Adjust for entity offset
        GL11.glTranslatef(0.0F, renderEntity.yOffset, 0.0F);

        RenderManager.instance.playerViewY = 180F;
        ClientEventHandler.renderingEntityInGUI = true;

        // Render the NPC at its position delta (for movement like slam jumps)
        try {
            RenderManager.instance.renderEntityWithPosYaw(renderEntity, npcDeltaX, npcDeltaY, npcDeltaZ, 0.0F, partialTicks);
        } catch (Exception e) {
            playerdata.setEntityClass(null);
        }

        // Render preview entities at their positions relative to NPC start position
        renderPreviewEntities(partialTicks);

        // Render telegraph in preview space
        renderPreviewTelegraph(partialTicks);

        ClientEventHandler.renderingEntityInGUI = false;

        GL11.glPopMatrix();

        // Cleanup GL state
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

        // Disable scissor
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Clear depth buffer so UI renders on top
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Render preview entities in the same coordinate space as the NPC.
     * Called WITHIN the 3D matrix transformation.
     */
    protected void renderPreviewEntities(float partialTicks) {
        // Save current GL state that NPC render may have changed
        boolean lightingWasEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);

        // Use the start position as the reference point for entity offsets
        double refX = trackingNpcMovement ? npcStartX : npc.posX;
        double refY = trackingNpcMovement ? npcStartY : npc.posY;
        double refZ = trackingNpcMovement ? npcStartZ : npc.posZ;

        for (Entity entity : previewEntities) {
            if (entity == null || entity.isDead) continue;

            // Calculate entity position relative to reference (NPC start position)
            double offsetX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks - refX;
            double offsetY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks - refY;
            double offsetZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks - refZ;

            // The NPC model was translated by npc.yOffset, so we need to subtract it
            offsetY -= npc.yOffset;

            // Reset GL state before each entity to ensure colors render correctly
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDisable(GL11.GL_LIGHTING);

            try {
                RenderManager.instance.renderEntityWithPosYaw(entity, offsetX, offsetY, offsetZ, entity.rotationYaw, partialTicks);
            } catch (Exception e) {
                // Ignore rendering errors
            }
        }

        // Restore lighting state for any subsequent rendering
        if (lightingWasEnabled) {
            GL11.glEnable(GL11.GL_LIGHTING);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Render the telegraph in the 3D preview space.
     * Called within the GL matrix transformation.
     */
    protected void renderPreviewTelegraph(float partialTicks) {
        if (previewTelegraph == null) return;
        if (TelegraphRenderer.Instance == null) return;

        // Telegraph position is in world coords - convert to preview space
        double refX = trackingNpcMovement ? npcStartX : npc.posX;
        double refY = trackingNpcMovement ? npcStartY : npc.posY;
        double refZ = trackingNpcMovement ? npcStartZ : npc.posZ;

        double offsetX = previewTelegraph.getInterpolatedX(partialTicks) - refX;
        double offsetY = previewTelegraph.getInterpolatedY(partialTicks) - refY;
        double offsetZ = previewTelegraph.getInterpolatedZ(partialTicks) - refZ;

        // Adjust for NPC yOffset (same as entities)
        offsetY -= npc.yOffset;

        TelegraphRenderer.Instance.renderTelegraphInGUI(previewTelegraph, offsetX, offsetY, offsetZ, 1.0f, partialTicks);
    }

    public void setPreviewTelegraph(TelegraphInstance telegraph) {
        this.previewTelegraph = telegraph;
    }

    /**
     * Set scissor clip for preview area.
     */
    protected void setScissorClip(int x, int y, int width, int height) {
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();
        int scaledY = mc.displayHeight - (y + height) * scale;
        GL11.glScissor(x * scale, scaledY, width * scale, height * scale);
    }

    // ==================== MOVEMENT TRACKING ====================

    /**
     * Start tracking NPC movement from current position.
     */
    public void startTrackingMovement() {
        npcStartX = npc.posX;
        npcStartY = npc.posY;
        npcStartZ = npc.posZ;
        trackingNpcMovement = true;
    }

    /**
     * Stop tracking NPC movement.
     */
    public void stopTrackingMovement() {
        trackingNpcMovement = false;
    }

    // ==================== PREVIEW ENTITY MANAGEMENT ====================

    public void addPreviewEntity(Entity entity) {
        if (entity != null && !previewEntities.contains(entity)) {
            previewEntities.add(entity);
        }
    }

    public void removePreviewEntity(Entity entity) {
        previewEntities.remove(entity);
    }

    public void clearPreviewEntities() {
        previewEntities.clear();
    }

    public List<Entity> getPreviewEntities() {
        return previewEntities;
    }

    public float getNpcFacingYaw() {
        return NPC_FACING_YAW;
    }

    // ==================== BACKGROUND ====================

    @Override
    protected void drawBackground() {
        super.drawBackground();

        drawGradientRect(previewX, previewY, previewX + previewWidth, previewY + previewHeight,
                        0xc0101010, 0xd0101010);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // ==================== STANDARD OVERRIDES ====================

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void keyTyped(char par1, int par2) {
        super.keyTyped(par1, par2);
        if (par2 == 1) {
            close();
        }
    }

    public void close() {
        this.mc.displayGuiScreen((GuiScreen) null);
        this.mc.setIngameFocus();
    }

    @Override
    public void save() {
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 670) {
            close();
        }
    }
}
