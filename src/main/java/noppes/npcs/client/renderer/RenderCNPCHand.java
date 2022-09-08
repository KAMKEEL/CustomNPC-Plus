package noppes.npcs.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.*;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderCNPCHand extends ItemRenderer {
    /** A reference to the Minecraft object. */
    private Minecraft mc;
    private ItemStack itemToRender;
    /** How far the current item has been equipped (0 disequipped and 1 fully up) */
    private float equippedProgress;
    private float prevEquippedProgress;
    /** The index of the currently held item (0-8, or -1 if not yet updated) */
    private int equippedItemSlot = -1;

    public RenderCNPCHand(Minecraft mc) {
        super(mc);
        this.mc = mc;
    }

    /**
     * Renders the active item in the player's hand when in first person mode. Args: partialTickTime
     */
    public void renderOverlayInFirstPerson(float partialTickTime)
    {
        float f1 = this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTickTime;
        EntityClientPlayerMP entityclientplayermp = this.mc.thePlayer;
        float f2 = entityclientplayermp.prevRotationPitch + (entityclientplayermp.rotationPitch - entityclientplayermp.prevRotationPitch) * partialTickTime;
        GL11.glPushMatrix();
        GL11.glRotatef(f2, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(entityclientplayermp.prevRotationYaw + (entityclientplayermp.rotationYaw - entityclientplayermp.prevRotationYaw) * partialTickTime, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();
        float f3 = ((EntityPlayerSP)entityclientplayermp).prevRenderArmPitch + (((EntityPlayerSP)entityclientplayermp).renderArmPitch - ((EntityPlayerSP)entityclientplayermp).prevRenderArmPitch) * partialTickTime;
        float f4 = ((EntityPlayerSP)entityclientplayermp).prevRenderArmYaw + (((EntityPlayerSP)entityclientplayermp).renderArmYaw - ((EntityPlayerSP)entityclientplayermp).prevRenderArmYaw) * partialTickTime;
        GL11.glRotatef((entityclientplayermp.rotationPitch - f3) * 0.1F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((entityclientplayermp.rotationYaw - f4) * 0.1F, 0.0F, 1.0F, 0.0F);

        ItemStack itemstack = this.itemToRender;
        RenderCNPCPlayer renderCNPCPlayer = new RenderCNPCPlayer();

        int i = this.mc.theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(entityclientplayermp.posX), MathHelper.floor_double(entityclientplayermp.posY), MathHelper.floor_double(entityclientplayermp.posZ), 0);
        int j = i % 65536;
        int k = i / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f5;
        float f6;
        float f7;

        float f10;
        float f13;

        if (itemstack == null && !entityclientplayermp.isInvisible())
        {
            GL11.glPushMatrix();
            f13 = 0.8F;
            f5 = entityclientplayermp.getSwingProgress(partialTickTime);
            f6 = MathHelper.sin(f5 * (float)Math.PI);
            f7 = MathHelper.sin(MathHelper.sqrt_float(f5) * (float)Math.PI);
            GL11.glTranslatef(-f7 * 0.3F, MathHelper.sin(MathHelper.sqrt_float(f5) * (float)Math.PI * 2.0F) * 0.4F, -f6 * 0.4F);
            GL11.glTranslatef(0.8F * f13, -0.75F * f13 - (1.0F - f1) * 0.6F, -0.9F * f13);
            GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            f5 = entityclientplayermp.getSwingProgress(partialTickTime);
            f6 = MathHelper.sin(f5 * f5 * (float)Math.PI);
            f7 = MathHelper.sin(MathHelper.sqrt_float(f5) * (float)Math.PI);
            GL11.glRotatef(f7 * 70.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-f6 * 20.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
            GL11.glRotatef(120.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(200.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScalef(1.0F, 1.0F, 1.0F);
            GL11.glTranslatef(5.6F, 0.0F, 0.0F);

            f10 = 1.0F;
            GL11.glScalef(f10, f10, f10);
            renderCNPCPlayer.renderFirstPersonArmOverlay(this.mc.thePlayer);
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
    }

    public void updateEquippedItem()
    {
        this.prevEquippedProgress = this.equippedProgress;
        EntityClientPlayerMP entityclientplayermp = this.mc.thePlayer;
        ItemStack itemstack = entityclientplayermp.inventory.getCurrentItem();
        boolean flag = this.equippedItemSlot == entityclientplayermp.inventory.currentItem && itemstack == this.itemToRender;

        if (this.itemToRender == null && itemstack == null)
        {
            flag = true;
        }

        if (itemstack != null && this.itemToRender != null && itemstack != this.itemToRender && itemstack.getItem() == this.itemToRender.getItem() && itemstack.getItemDamage() == this.itemToRender.getItemDamage())
        {
            this.itemToRender = itemstack;
            flag = true;
        }

        float f = 0.4F;
        float f1 = flag ? 1.0F : 0.0F;
        float f2 = f1 - this.equippedProgress;

        if (f2 < -f)
        {
            f2 = -f;
        }

        if (f2 > f)
        {
            f2 = f;
        }

        this.equippedProgress += f2;

        if (this.equippedProgress < 0.1F)
        {
            this.itemToRender = itemstack;
            this.equippedItemSlot = entityclientplayermp.inventory.currentItem;
        }
    }
}
