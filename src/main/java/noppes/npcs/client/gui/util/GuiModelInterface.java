package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.ModelData;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiModelInterface extends GuiNPCInterface {
    public ModelData playerdata;

    private static float rotation = 0;

    private GuiNpcButton left, right, zoom, unzoom;

    private static float zoomed = 60;
    public float minSize = 10, maxSize = 100;

    public int xOffset = 0, xOffsetButton = 0;
    public int yOffset = 0, yOffsetButton = 0;
    public boolean followMouse = true, drawNPConSub = true;

    public boolean allowRotate = true;
    public boolean drawRenderButtons = true, drawXButton = true;

    public EntityCustomNpc npc;

    public GuiModelInterface(EntityCustomNpc npc) {
        this.npc = npc;
        playerdata = npc.modelData;
        xSize = 380;
        drawDefaultBackground = false;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();


        if (drawRenderButtons) {
            addButton(unzoom = new GuiNpcButton(666, guiLeft + 148 + xOffset + xOffsetButton, guiTop + 200 + yOffset + yOffsetButton, 20, 20, "-"));
            addButton(zoom = new GuiNpcButton(667, guiLeft + 214 + xOffset + xOffsetButton, guiTop + 200 + yOffset + yOffsetButton, 20, 20, "+"));
            addButton(left = new GuiNpcButton(668, guiLeft + 170 + xOffset + xOffsetButton, guiTop + 200 + yOffset + yOffsetButton, 20, 20, "<"));
            addButton(right = new GuiNpcButton(669, guiLeft + 192 + xOffset + xOffsetButton, guiTop + 200 + yOffset + yOffsetButton, 20, 20, ">"));
        }

        if (drawXButton) {
            addButton(new GuiNpcButton(670, width - 22, 2, 20, 20, "X"));
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        super.actionPerformed(btn);
        if (btn.id == 670) {
            close();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private final long start = -1;

    public boolean isMouseOverRenderer(int x, int y) {
        if (!allowRotate) {
            return false;
        }
        // Center of the entity rendering
        int centerX = guiLeft + 190 + xOffset; // Matches l in drawScreen()
        int centerY = guiTop + 180 + yOffset; // Matches i1 in drawScreen()

        // Define separate buffers for X and Y axes
        int xBuffer = 100; // Horizontal buffer
        int yBuffer = 150; // Vertical buffer

        // Check if the mouse is within the buffer area
        return mouseX >= centerX - xBuffer && mouseX <= centerX + xBuffer &&
            mouseY >= centerY - yBuffer && mouseY <= centerY + yBuffer;
    }

    public void preRender(EntityLivingBase entity) {
        EntityUtil.Copy(npc, entity);
    }

    public void postRender(EntityLivingBase entity) {

    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        if (Mouse.isButtonDown(0)) {
            if (this.left.mousePressed(this.mc, par1, par2)) {
                rotation += par3 * 1.5F;
            } else if (this.right.mousePressed(this.mc, par1, par2)) {
                rotation -= par3 * 1.5F;
            } else if (this.zoom.mousePressed(this.mc, par1, par2) && zoomed < maxSize) {
                zoomed += par3;
            } else if (this.unzoom.mousePressed(this.mc, par1, par2) && zoomed > minSize) {
                zoomed -= par3;
            }
        }

        if (isMouseOverRenderer(par1, par2)) {
            zoomed += Mouse.getDWheel() * 0.035f;
            if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
                rotation -= Mouse.getDX() * 0.75f;
            }
        }

        if (zoomed > maxSize)
            zoomed = maxSize;
        if (zoomed < minSize)
            zoomed = minSize;

        if (hasSubGui() && !drawNPConSub)
            return;

        this.drawDefaultBackground();
        GL11.glColor4f(1, 1, 1, 1);

        EntityLivingBase entity = playerdata.getEntity(npc);
        if (entity == null)
            entity = this.npc;

        preRender(entity);

        int l = guiLeft + 190 + xOffset;
        int i1 = guiTop + 180 + yOffset;
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(l, i1, 60F);


        GL11.glScalef(-zoomed, zoomed, zoomed);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f7 = entity.rotationYawHead;
        float f5 = (float) (l) - par1;
        float f6 = (float) (i1 - 50) - par2;
        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-(float) Math.atan(f6 / 800F) * 20F, 1.0F, 0.0F, 0.0F);
        entity.prevRenderYawOffset = entity.renderYawOffset = rotation;
        entity.prevRotationYaw = entity.rotationYaw = (float) Math.atan(f5 / 80F) * 40F + rotation;
        entity.rotationPitch = followMouse ? -(float) Math.atan(f6 / 40F) * 20F : 0;
        entity.prevRotationYawHead = entity.rotationYawHead = followMouse ? entity.rotationYaw : rotation;
        GL11.glTranslatef(0.0F, entity.yOffset, 1F);
        RenderManager.instance.playerViewY = 180F;

        try {
            RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        } catch (Exception e) {
            playerdata.setEntityClass(null);
        }
        entity.prevRenderYawOffset = entity.renderYawOffset = f2;
        entity.prevRotationYaw = entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = entity.rotationYawHead = f7;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0f, 500.065F);
        super.drawScreen(par1, par2, par3);
        GL11.glPopMatrix();

        postRender(entity);
    }

    @Override
    public void keyTyped(char par1, int par2) {
        super.keyTyped(par1, par2);
    }

    public void close() {
        this.mc.displayGuiScreen(null);
        this.mc.setIngameFocus();
    }

    @Override
    public void save() {


    }
}
