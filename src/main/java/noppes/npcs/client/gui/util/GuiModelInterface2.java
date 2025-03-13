package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.ModelData;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiModelInterface2 extends GuiNPCInterface2 {
    public ModelData playerdata;

    private static float rotation = 0;

    private GuiNpcButton left, right, zoom, unzoom;

    private static float zoomed = 60;

    public int xOffset = 0;
    public int yOffset = 0;

    public EntityNPCInterface npc;

    public GuiModelInterface2(EntityNPCInterface npc) {
        super(npc);
        this.npc = npc;
        playerdata = ((EntityCustomNpc) npc).modelData;
        //xSize = 380;
        drawDefaultBackground = false;
    }

    @Override
    public void initGui() {
        super.initGui();

        addButton(unzoom = new GuiNpcButton(666, guiLeft + 148 + xOffset, guiTop + 200 + yOffset, 20, 20, "-"));
        addButton(zoom = new GuiNpcButton(667, guiLeft + 214 + xOffset, guiTop + 200 + yOffset, 20, 20, "+"));
        addButton(left = new GuiNpcButton(668, guiLeft + 170 + xOffset, guiTop + 200 + yOffset, 20, 20, "<"));
        addButton(right = new GuiNpcButton(669, guiLeft + 192 + xOffset, guiTop + 200 + yOffset, 20, 20, ">"));
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 670) {
            close();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private final long start = -1;

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        if (Mouse.isButtonDown(0)) {
            if (left.mousePressed(mc, par1, par2))
                rotation += par3 * 2;
            else if (right.mousePressed(mc, par1, par2))
                rotation -= par3 * 2;
            else if (zoom.mousePressed(mc, par1, par2))
                zoomed += par3 * 2;
            else if (unzoom.mousePressed(mc, par1, par2) && zoomed > 10)
                zoomed -= par3 * 2;

        }

        super.drawScreen(par1, par2, par3);

        GL11.glColor4f(1, 1, 1, 1);

        EntityLivingBase entity = playerdata.getEntity(npc);
        if (entity == null)
            entity = this.npc;

        EntityUtil.Copy(npc, entity);

        int l = guiLeft + 190 + xOffset;
        int i1 = guiTop + 180 + yOffset;
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(l, i1, 50F);


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
        GL11.glRotatef(-(float) Math.atan(f6 / 80F) * 20F, 1.0F, 0.0F, 0.0F);
        entity.prevRenderYawOffset = entity.renderYawOffset = rotation;
        entity.prevRotationYaw = entity.rotationYaw = (float) Math.atan(f5 / 80F) * 40F + rotation;
        entity.rotationPitch = -(float) Math.atan(f6 / 80F) * 20F;
        entity.prevRotationYawHead = entity.rotationYawHead = entity.rotationYaw;
        GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
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
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void drawBackground() {
        super.drawBackground();

        int xPosGradient = guiLeft + 10;
        int yPosGradient = guiTop + 10;
        drawGradientRect(xPosGradient, yPosGradient, 200 + xPosGradient, 180 + yPosGradient, 0xc0101010, 0xd0101010);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void keyTyped(char par1, int par2) {
        super.keyTyped(par1, par2);
        if (par2 == 1) {
            close();
        }
    }

    public void close() {
        this.mc.displayGuiScreen(null);
        this.mc.setIngameFocus();
    }

    @Override
    public void save() {


    }

    public void setSave(boolean saveNPC) {
        if (this.getMenu() != null) {
            this.getMenu().saveNPC = saveNPC;
        }
    }
}
