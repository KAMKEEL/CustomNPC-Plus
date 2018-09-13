package noppes.npcs.client.gui;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import noppes.npcs.ServerEventsHandler;
import noppes.npcs.client.Client;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerMerchantAdd;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMerchantAdd extends GuiContainer
{
    private static final ResourceLocation merchantGuiTextures = new ResourceLocation("textures/gui/container/villager.png");

    /** Instance of IMerchant interface. */
    private IMerchant theIMerchant;
    private MerchantButton nextRecipeButtonIndex;
    private MerchantButton previousRecipeButtonIndex;
    private int currentRecipeIndex;
    private String field_94082_v;

    
    public GuiMerchantAdd()
    {
        super(new ContainerMerchantAdd(Minecraft.getMinecraft().thePlayer, ServerEventsHandler.Merchant, Minecraft.getMinecraft().theWorld));
        this.theIMerchant = ServerEventsHandler.Merchant;
        this.field_94082_v = I18n.format("entity.Villager.name");
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        super.initGui();
        
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.buttonList.add(this.nextRecipeButtonIndex = new MerchantButton(1, i + 120 + 27, j + 24 - 1, true));
        this.buttonList.add(this.previousRecipeButtonIndex = new MerchantButton(2, i + 36 - 19, j + 24 - 1, false));
        this.buttonList.add(new GuiNpcButton(4, i + xSize,  j + 20, 60, 20, "gui.remove"));
        this.buttonList.add(new GuiNpcButton(5, i + xSize,  j + 50, 60, 20, "gui.add"));
        this.nextRecipeButtonIndex.enabled = false;
        this.previousRecipeButtonIndex.enabled = false;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        this.fontRendererObj.drawString(this.field_94082_v, this.xSize / 2 - this.fontRendererObj.getStringWidth(this.field_94082_v) / 2, 6, CustomNpcResourceListener.DefaultTextColor);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, CustomNpcResourceListener.DefaultTextColor);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        Minecraft mc = Minecraft.getMinecraft();
        MerchantRecipeList merchantrecipelist = this.theIMerchant.getRecipes(mc.thePlayer);

        if (merchantrecipelist != null)
        {
            this.nextRecipeButtonIndex.enabled = this.currentRecipeIndex < merchantrecipelist.size() - 1;
            this.previousRecipeButtonIndex.enabled = this.currentRecipeIndex > 0;
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        boolean flag = false;
        Minecraft mc = Minecraft.getMinecraft();

        if (par1GuiButton == this.nextRecipeButtonIndex)
        {
            ++this.currentRecipeIndex;
            flag = true;
        }
        else if (par1GuiButton == this.previousRecipeButtonIndex)
        {
            --this.currentRecipeIndex;
            flag = true;
        }
        
        if(par1GuiButton.id == 4){
            MerchantRecipeList merchantrecipelist = this.theIMerchant.getRecipes(mc.thePlayer);
            if(currentRecipeIndex < merchantrecipelist.size()){
            	merchantrecipelist.remove(currentRecipeIndex);
            	if(currentRecipeIndex > 0)
            		currentRecipeIndex--;
            	Client.sendData(EnumPacketServer.MerchantUpdate, ServerEventsHandler.Merchant.getEntityId(), merchantrecipelist);
            }
        }
        
        if(par1GuiButton.id == 5){
        	ItemStack item1 = this.inventorySlots.getSlot(0).getStack();
        	ItemStack item2 = this.inventorySlots.getSlot(1).getStack();
        	ItemStack sold = this.inventorySlots.getSlot(2).getStack();
        	if(item1 == null && item2 != null){
        		item1 = item2;
        		item2 = null;
        	}
        	
        	if(item1 != null && sold != null){
        		item1 = item1.copy();
    			sold = sold.copy();
        		if(item2 != null)
        			item2 = item2.copy();
        		MerchantRecipe recipe = new MerchantRecipe(item1, item2, sold);
        		recipe.func_82783_a(Integer.MAX_VALUE - 8);

                MerchantRecipeList merchantrecipelist = this.theIMerchant.getRecipes(mc.thePlayer);
                merchantrecipelist.add(recipe);
            	Client.sendData(EnumPacketServer.MerchantUpdate, ServerEventsHandler.Merchant.getEntityId(), merchantrecipelist);
        	}
        	
        }

        if (flag)
        {
            ((ContainerMerchantAdd)this.inventorySlots).setCurrentRecipeIndex(this.currentRecipeIndex);
            ByteBuf bytebuf = Unpooled.buffer();

            try
            {
                bytebuf.writeInt(this.currentRecipeIndex);
                this.mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("MC|TrSel", bytebuf));
            }
            catch (Exception exception)
            {
                //field_147039_u.error("Couldn\'t send trade info", exception);
            }
            finally
            {
                bytebuf.release();
            }
        }
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(merchantGuiTextures);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        MerchantRecipeList merchantrecipelist = this.theIMerchant.getRecipes(mc.thePlayer);

        if (merchantrecipelist != null && !merchantrecipelist.isEmpty())
        {
            int i1 = this.currentRecipeIndex;
            MerchantRecipe merchantrecipe = (MerchantRecipe)merchantrecipelist.get(i1);

            if (merchantrecipe.isRecipeDisabled())
            {
                mc.getTextureManager().bindTexture(merchantGuiTextures);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDisable(GL11.GL_LIGHTING);
                this.drawTexturedModalRect(this.guiLeft + 83, this.guiTop + 21, 212, 0, 28, 21);
                this.drawTexturedModalRect(this.guiLeft + 83, this.guiTop + 51, 212, 0, 28, 21);
            }
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
	@Override
    public void drawScreen(int par1, int par2, float par3)
    {
        super.drawScreen(par1, par2, par3);
        Minecraft mc = Minecraft.getMinecraft();
        MerchantRecipeList merchantrecipelist = this.theIMerchant.getRecipes(mc.thePlayer);

        if (merchantrecipelist != null && !merchantrecipelist.isEmpty())
        {
            int k = (this.width - this.xSize) / 2;
            int l = (this.height - this.ySize) / 2;
            int i1 = this.currentRecipeIndex;
            MerchantRecipe merchantrecipe = (MerchantRecipe)merchantrecipelist.get(i1);
            GL11.glPushMatrix();
            ItemStack itemstack = merchantrecipe.getItemToBuy();
            ItemStack itemstack1 = merchantrecipe.getSecondItemToBuy();
            ItemStack itemstack2 = merchantrecipe.getItemToSell();
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glEnable(GL11.GL_LIGHTING);
            itemRender.zLevel = 100.0F;
            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, mc.getTextureManager(), itemstack, k + 36, l + 24);
            itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, mc.getTextureManager(), itemstack, k + 36, l + 24);

            if (itemstack1 != null)
            {
            	itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, mc.getTextureManager(), itemstack1, k + 62, l + 24);
            	itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, mc.getTextureManager(), itemstack1, k + 62, l + 24);
            }

            itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, mc.getTextureManager(), itemstack2, k + 120, l + 24);
            itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, mc.getTextureManager(), itemstack2, k + 120, l + 24);
            itemRender.zLevel = 0.0F;
            GL11.glDisable(GL11.GL_LIGHTING);

            if (this.func_146978_c(36, 24, 16, 16, par1, par2))
            {
                this.renderToolTip(itemstack, par1, par2);
            }
            else if (itemstack1 != null && this.func_146978_c(62, 24, 16, 16, par1, par2))
            {
                this.renderToolTip(itemstack1, par1, par2);
            }
            else if (this.func_146978_c(120, 24, 16, 16, par1, par2))
            {
                this.renderToolTip(itemstack2, par1, par2);
            }

            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableStandardItemLighting();
        }
    }

    /**
     * Gets the Instance of IMerchant interface.
     */
    public IMerchant getIMerchant()
    {
        return this.theIMerchant;
    }

    static ResourceLocation func_110417_h()
    {
        return merchantGuiTextures;
    }
    @SideOnly(Side.CLIENT)
    static class MerchantButton extends GuiButton
        {
            private final boolean field_146157_o;
            private static final String __OBFID = "CL_00000763";

            public MerchantButton(int par1, int par2, int par3, boolean par4)
            {
                super(par1, par2, par3, 12, 19, "");
                this.field_146157_o = par4;
            }

            @Override
            public void drawButton(Minecraft p_146112_1_, int p_146112_2_, int p_146112_3_)
            {
                if (this.visible)
                {
                    p_146112_1_.getTextureManager().bindTexture(GuiMerchantAdd.merchantGuiTextures);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    boolean flag = p_146112_2_ >= this.xPosition && p_146112_3_ >= this.yPosition && p_146112_2_ < this.xPosition + this.width && p_146112_3_ < this.yPosition + this.height;
                    int k = 0;
                    int l = 176;

                    if (!this.enabled)
                    {
                        l += this.width * 2;
                    }
                    else if (flag)
                    {
                        l += this.width;
                    }

                    if (!this.field_146157_o)
                    {
                        k += this.height;
                    }

                    this.drawTexturedModalRect(this.xPosition, this.yPosition, l, k, this.width, this.height);
                }
            }
        }
}
