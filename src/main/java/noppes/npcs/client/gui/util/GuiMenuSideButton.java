package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import noppes.npcs.CustomNpcs;

public class GuiMenuSideButton extends GuiNpcButton{
	public static final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID,"textures/gui/menusidebutton.png");
    public static final ResourceLocation resource2 = new ResourceLocation(CustomNpcs.MODID,"textures/gui/menusidebutton2.png");

    public boolean active;
    public boolean rightSided;
    public ItemStack renderStack;
    public ResourceLocation renderResource;
    public int renderIconPosX = 0;
    public int renderIconPosY = 0;

    public GuiMenuSideButton(int id, int x, int y, String s){
        this(id, x, y, 200, 20, s);
    }

    public GuiMenuSideButton(int id, int x, int y, int width, int height, String s){
    	super(id, x, y, width, height, s);
        active = false;
    }

    @Override
    public int getHoverState(boolean flag){
        if (active)
            return 0;
        return super.getHoverState(flag);
    }

    @Override
    public void drawButton(Minecraft minecraft, int i, int j){
        if (!visible){
            return;
        }
        FontRenderer fontrenderer = minecraft.fontRenderer;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int width = this.width + (active?2:0);
        field_146123_n = i >= xPosition && j >= yPosition && i < xPosition + width && j < yPosition + height;
        int k = getHoverState(field_146123_n);

        if (this.rightSided) {
            minecraft.renderEngine.bindTexture(resource2);
            drawTexturedModalRect(xPosition, yPosition, 197 - width,  k * 22, width, height);
        } else {
            minecraft.renderEngine.bindTexture(resource);
            drawTexturedModalRect(xPosition, yPosition, 0,  k * 22, width, height);
        }
        mouseDragged(minecraft, i, j);

        // Render Custom Icon
        if (this.renderResource != null) {
            this.zLevel = 100.0F;
            minecraft.renderEngine.bindTexture(renderResource);
            drawTexturedModalRect(xPosition + 2, yPosition + height/2 - 8, renderIconPosX, renderIconPosY, 16, 16);
            this.zLevel = 0.0F;
        }


        if (this.renderStack != null) {
            RenderHelper.enableGUIStandardItemLighting();
            this.zLevel = 100.0F;
            RenderItem itemRenderer = RenderItem.getInstance();
            itemRenderer.zLevel = 100.0F;
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            itemRenderer.renderItemAndEffectIntoGUI(minecraft.fontRenderer,
                    minecraft.renderEngine, this.renderStack, xPosition + 2, yPosition + height/2 - 8);
            itemRenderer.renderItemOverlayIntoGUI(minecraft.fontRenderer,
                    minecraft.renderEngine, this.renderStack, xPosition + 2, yPosition + height/2 - 8);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            itemRenderer.zLevel = 0.0F;
            this.zLevel = 0.0F;
            RenderHelper.disableStandardItemLighting();
        }

        String text = "";
        float maxWidth = width * 0.75f;
        if(fontrenderer.getStringWidth(displayString) > maxWidth){
        	for(int h = 0; h < displayString.length(); h++){
        		char c = displayString.charAt(h);
        		if(fontrenderer.getStringWidth(text + c) > maxWidth)
        			break;
        		text += c;
        	}
        	text += "...";
        }
        else
        	text = displayString;
        if (active){
            drawCenteredString(fontrenderer, text, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffffa0);
        }
        else if (field_146123_n){
            drawCenteredString(fontrenderer, text, xPosition + width / 2, yPosition + (height - 8) / 2, 0xffffa0);
        }
        else{
            drawCenteredString(fontrenderer, text, xPosition + width / 2, yPosition + (height - 8) / 2, 0xe0e0e0);
        }
    }

    @Override
    protected void mouseDragged(Minecraft minecraft, int i, int j){
    }

    @Override
    public void mouseReleased(int i, int j){

    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j){
        return !active && visible && field_146123_n;
    }
}
