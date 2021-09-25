package noppes.npcs.client.gui.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.controllers.DialogOption;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

public class GuiDialogInteract extends GuiNPCInterface implements IGuiClose
{
	private Dialog dialog;
    private int selected = 0;
    private List<TextBlockClient> lines = new ArrayList<TextBlockClient>();
    private List<Integer> options = new ArrayList<Integer>();
    private int rowStart = 0;
    private int rowTotal = 0;
    private int dialogHeight = 180;

	private ResourceLocation wheel;
	private ResourceLocation[] wheelparts;
	private ResourceLocation indicator;
	
	private boolean isGrabbed = false;
	
    public GuiDialogInteract(EntityNPCInterface npc, Dialog dialog){
    	super(npc);
		this.dialog = dialog;
    	appendDialog(dialog);
    	ySize = 238;

    	wheel = this.getResource("wheel.png");
    	indicator = this.getResource("indicator.png");
    	wheelparts = new ResourceLocation[]{getResource("wheel1.png"),getResource("wheel2.png"),getResource("wheel3.png"),
    			getResource("wheel4.png"),getResource("wheel5.png"),getResource("wheel6.png")};
    }
    
    public void initGui(){
    	super.initGui();
    	isGrabbed = false;
    	grabMouse(dialog.showWheel);
    	guiTop = (height - ySize);
    	calculateRowHeight();
    }
    
    public void grabMouse(boolean grab){
		 if(grab && !isGrabbed){
			 Minecraft.getMinecraft().mouseHelper.grabMouseCursor();
			 isGrabbed = true;
		 }
		 else if(!grab && isGrabbed){
			 Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
			 isGrabbed = false;
		 }
    }
     
    public void drawScreen(int i, int j, float f){
        GL11.glColor4f(1, 1, 1, 1);
    	
        if(!dialog.hideNPC){
	    	float l = (guiLeft - 70);
	    	float i1 =  (guiTop + ySize);
	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	        GL11.glPushMatrix();
	        GL11.glTranslatef(l, i1, 50F);
	        float zoomed = npc.height;
	        if(npc.width * 2 > zoomed)
	        	zoomed = npc.width * 2;
	        zoomed =  2 / zoomed * 40;
	        GL11.glScalef(-zoomed, zoomed, zoomed);
	        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
	        float f2 = npc.renderYawOffset;
	        float f3 = npc.rotationYaw;
	        float f4 = npc.rotationPitch;
	        float f7 = npc.rotationYawHead;
	        float f5 = (float)(l) - i;
	        float f6 = (float)(i1 - 50) - j;
	        int rotation = npc.ai.orientation;
	        npc.ai.orientation = 0;
	        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
	        RenderHelper.enableStandardItemLighting();
	        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(-(float)Math.atan(f6 / 80F) * 20F, 1.0F, 0.0F, 0.0F);
	        npc.renderYawOffset = 0;
	        npc.rotationYaw = (float)Math.atan(f5 / 80F) * 40F;
	        npc.rotationPitch = -(float)Math.atan(f6 / 80F) * 20F;
	        npc.prevRotationYawHead = npc.rotationYawHead = npc.rotationYaw;
	        GL11.glTranslatef(0.0F, npc.yOffset, 0.0F);
	        RenderManager.instance.playerViewY = 180F;
	
	        try{
	            RenderManager.instance.renderEntityWithPosYaw(npc, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        }
	        catch(Exception e){
	        }
	        npc.ai.orientation = rotation;
	        npc.renderYawOffset = f2;
	        npc.rotationYaw = f3;
	        npc.rotationPitch = f4;
	        npc.prevRotationYawHead = npc.rotationYawHead = f7;
	        GL11.glPopMatrix();
	        RenderHelper.disableStandardItemLighting();
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
	        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        }
        super.drawScreen(i, j, f);

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.5f, 100.065F);
        int count = 0;
        for(TextBlockClient block : lines){
        	int size = ClientProxy.Font.width(block.getName() + ": ");
    		drawString(block.getName() + ": ",  -4 - size, block.color, count);
        	for(IChatComponent line : block.lines){
        		drawString(line.getFormattedText(), 0, block.color, count);
                count++;
        	}
        	count++;
        }       
        
        if(!options.isEmpty()){
        	if(!dialog.showWheel)
        		drawLinedOptions(j);
        	else
        		drawWheel();
        }
        GL11.glPopMatrix();
    }

    private int selectedX = 0;
    private int selectedY = 0;
    private void drawWheel(){
    	int yoffset = guiTop + dialogHeight + 14;
        GL11.glColor4f(1, 1, 1, 1);
        mc.renderEngine.bindTexture(wheel);
        drawTexturedModalRect((width/2) - 31, yoffset, 0, 0, 63, 40);
        
        selectedX += Mouse.getDX();        
        selectedY += Mouse.getDY();
        int limit = 80;
        if(selectedX > limit)
        	selectedX = limit;
        if(selectedX < -limit)
        	selectedX = -limit;
        
        if(selectedY > limit)
        	selectedY = limit;
        if(selectedY < -limit)
        	selectedY = -limit;

        selected = 1;
    	if(selectedY < -20)
    		selected++;
    	if(selectedY > 54)
    		selected--;
    	
    	if(selectedX < 0)
    		selected += 3;
        mc.renderEngine.bindTexture(wheelparts[selected]);
        drawTexturedModalRect((width/2) - 31,yoffset, 0, 0, 85, 55);
        for(int slot:dialog.options.keySet()){
        	DialogOption option = dialog.options.get(slot);
        	if(option == null || option.optionType == EnumOptionType.Disabled)
        		continue;
            int color = option.optionColor;            	
        	if(slot == (selected))
        		color = 0x838FD8;
    		//drawString(fontRenderer, option.title, width/2 -50 ,yoffset+ 162 + slot * 13 , color);
        	if(slot == 0)
        		drawString(fontRendererObj, option.title, width/2 + 13,yoffset - 6 , color);
        	if(slot == 1)
        		drawString(fontRendererObj, option.title, width/2 + 33,yoffset + 12  , color);
        	if(slot == 2)
        		drawString(fontRendererObj, option.title, width/2 + 27,yoffset + 32  , color);
        	if(slot == 3)
        		drawString(fontRendererObj, option.title, width/2 - 13 - ClientProxy.Font.width(option.title),yoffset - 6  , color);
        	if(slot == 4)
        		drawString(fontRendererObj, option.title, width/2 - 33 - ClientProxy.Font.width(option.title),yoffset + 12  , color);
        	if(slot == 5)
        		drawString(fontRendererObj, option.title, width/2 - 27 - ClientProxy.Font.width(option.title),yoffset + 32  , color);
        	
        }
        mc.renderEngine.bindTexture(indicator);
        drawTexturedModalRect(width/2 + selectedX/4  - 2,yoffset + 16 - selectedY/6, 0, 0, 8, 8);
    }
    private void drawLinedOptions(int j){
    	drawHorizontalLine(guiLeft - 60, guiLeft + xSize + 120, guiTop + dialogHeight, 0xFFFFFFFF);
        int offset = dialogHeight + 4;
        if(j >= (int)((guiTop + offset))){
        	int selected = (int) ((j - (guiTop + offset)) / (ClientProxy.Font.height()));
	        if(selected < options.size())
		        this.selected = selected;
        }
        if(selected >= options.size())
        	selected = 0;
        if(selected < 0)
        	selected = 0;
        int count = 0;
        for(int k = 0; k < options.size(); k++){
        	int id = options.get(k);
        	DialogOption option = dialog.options.get(id);
        	int y = (int)((guiTop + offset  + (count * ClientProxy.Font.height())));
        	if(selected == k){
        		drawString(fontRendererObj, ">", guiLeft - 60, y, 0xe0e0e0);
        	}
        	drawString(fontRendererObj, NoppesStringUtils.formatText(option.title, player, npc), guiLeft - 30, y, option.optionColor);
        	count++;
        }
    }
    
    private void drawString(String text, int left, int color, int count){
    	int height = count - rowStart;
    	drawString(fontRendererObj, text, guiLeft + left, guiTop + (height * ClientProxy.Font.height()), color);
    }

    public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color){
    	ClientProxy.Font.drawString(text, x, y, color);
    	//super.drawString(fontRendererIn, text, x, y, color);
    }
    
    private int getSelected(){
    	if(selected <= 0)
    		return 0;
    	
    	if(selected < options.size())
    		return selected;
    	
    	return options.size() - 1;
    }

    @Override
    public void keyTyped(char c, int i){
    	if(i == mc.gameSettings.keyBindForward.getKeyCode() || i == Keyboard.KEY_UP){
			selected--;
    	}
    	if(i == mc.gameSettings.keyBindBack.getKeyCode() || i == Keyboard.KEY_DOWN){
    		selected++;
    	}
    	if(i == 28){
        	handleDialogSelection();
    	}
        if (closeOnEsc && (i == 1 || isInventoryKey(i))){
        	NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, dialog.id, -1);
        	closed();
            close();
        }
        super.keyTyped(c, i);
    }

    @Override
    public void mouseClicked(int i,int  j,int  k){
    	if(selected == -1 && options.isEmpty() || selected >= 0)
    		handleDialogSelection();
    }
    private void handleDialogSelection(){
    	int optionId = -1;
    	if(dialog.showWheel)
    		optionId = selected;
    	else if(!options.isEmpty())
    		optionId = options.get(selected);
    	NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, dialog.id, optionId);
    	if(dialog == null || !dialog.hasOtherOptions() || options.isEmpty()){
    		closed();
    		close();
        	return;
    	}
    	DialogOption option = dialog.options.get(optionId);
    	if(option == null || option.optionType != EnumOptionType.DialogOption){
    		closed();
    		close();
    		return;
    	}
    	
    	lines.add(new TextBlockClient(player.getDisplayName(), option.title, 280, option.optionColor, player, npc));
		calculateRowHeight();
    	
    	NoppesUtil.clickSound();
    	
    }
    private void closed(){
    	grabMouse(false);
    	NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion);
    }
    
	public void save() {
		
	}

	public void appendDialog(Dialog dialog) {
		closeOnEsc = !dialog.disableEsc;
		this.dialog = dialog;
		this.options = new ArrayList<Integer>();

    	if(dialog.sound != null && !dialog.sound.isEmpty()){
    		MusicController.Instance.stopMusic();
    		MusicController.Instance.playSound(dialog.sound, (float)npc.posX, (float)npc.posY, (float)npc.posZ);
    	}
    	
    	lines.add(new TextBlockClient(npc, dialog.text, 280, 0xe0e0e0, player, npc));
    	
		 for(int slot:dialog.options.keySet()){
			DialogOption option = dialog.options.get(slot);
			if(option == null || option.optionType == EnumOptionType.Disabled)
				continue;
			options.add(slot);
		 }
		 calculateRowHeight();
		 
		 grabMouse(dialog.showWheel);
	}
	private void calculateRowHeight(){
		if(dialog.showWheel){
	    	dialogHeight = ySize - 58;
		}
		else{
	    	dialogHeight = ySize - 3 * ClientProxy.Font.height() - 4;
	    	if(dialog.options.size() > 3){
	    		dialogHeight -= (dialog.options.size() - 3) * ClientProxy.Font.height();
	    	}
		}
        rowTotal = 0;
        for(TextBlockClient block : lines){
        	rowTotal += block.lines.size() + 1;
        }
        int max = dialogHeight / ClientProxy.Font.height();
        
        rowStart = rowTotal - max;
        if(rowStart < 0)
        	rowStart = 0;
	}

	@Override
	public void setClose(int i, NBTTagCompound data) {
    	grabMouse(false);
	}
}

