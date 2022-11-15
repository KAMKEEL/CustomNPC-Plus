package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
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
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogImage;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.handler.data.IDialogImage;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiDialogInteract extends GuiNPCInterface implements IGuiClose
{
	private Dialog dialog;
    private int selected = 0;
    private List<TextBlockClient> lineBlocks = new ArrayList<TextBlockClient>();
    private List<Integer> options = new ArrayList<Integer>();
    private int totalRows = 0;

	private ScaledResolution scaledResolution;

	private String gradualText = "";
	private int currentBlock = 0;
	private int currentLine = 0;
	private int gradualTextTime = 0;
	private int optionStart = 0;

	private int instantBlockPos = 0;
	private int instantLinePos = 0;
	private int prevPausePos = -1;

	private static int textSpeed = 10;
	private static boolean textSoundEnabled = true;

	private int scrollY;
	private ResourceLocation wheel;
	private ResourceLocation[] wheelparts;
	private ResourceLocation indicator;

	private boolean isGrabbed = false;
	private int textSoundTime = 0;
	private int textPauseTime = 0;

	private HashMap<Integer,GuiDialogImage> dialogImages = new HashMap<>();

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
		this.scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		this.drawDefaultBackground = dialog.darkenScreen;
		setOptionOffset();
    }

	public void setOptionOffset() {
		optionStart = scaledResolution.getScaledHeight() - (options.size()) * (ClientProxy.Font.height() + dialog.optionSpaceY) - 20;
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
	        GL11.glTranslatef(l + dialog.npcOffsetX, i1 + dialog.npcOffsetY, 50F);
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
				GL11.glScalef(dialog.npcScale,dialog.npcScale,dialog.npcScale);
	            RenderManager.instance.renderEntityWithPosYaw(npc, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
	        } catch(Exception ignored){}

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
		setOptionOffset();

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 0.5f, 100.065F);
		if (dialog.renderGradual) {
			drawString(fontRendererObj, "Text Speed: " + textSpeed, 10, 10, 0xFFFFFF);
			drawString(fontRendererObj, "Text Sound: " + (textSoundEnabled ? "On" : "Off"), 10, 20, 0xFFFFFF);
		}

		for (IDialogImage dialogImage : dialog.dialogImages.values()) {
			if (dialogImage.getImageType() != 0)
				continue;

			GuiDialogImage image;
			if (dialogImages.containsKey(dialogImage.getId())) {
				image = dialogImages.get(dialogImage.getId());
			} else {
				image = new GuiDialogImage((DialogImage) dialogImage);
				dialogImages.put(dialogImage.getId(),image);
			}

			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GL11.glDisable(GL11.GL_ALPHA_TEST);

			GL11.glTranslatef(image.alignment%3*((float)(scaledResolution.getScaledWidth())/2), (float) (Math.floor((float)(image.alignment/3))*((float)(scaledResolution.getScaledHeight())/2)),0.0F);
			image.onRender(mc);

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
		}

		GL11.glPushMatrix();
		for (IDialogImage dialogImage : dialog.dialogImages.values()) {
			if (dialogImage.getImageType() != 1)
				continue;

			GuiDialogImage image;
			if (dialogImages.containsKey(dialogImage.getId())) {
				image = dialogImages.get(dialogImage.getId());
			} else {
				image = new GuiDialogImage((DialogImage) dialogImage);
				dialogImages.put(dialogImage.getId(),image);
			}

			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GL11.glDisable(GL11.GL_ALPHA_TEST);

			GL11.glTranslatef(guiLeft + dialog.textOffsetX, optionStart + dialog.textOffsetY - image.height * image.scale, 0.0F);
			image.onRender(mc);

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
		}
		GL11.glPopMatrix();

		if (!dialog.renderGradual) {
			int count = 0;
			blockLoop:
			for (int b = 0; b < lineBlocks.size(); b++) {
				TextBlockClient block = lineBlocks.get(b);

				int size = ClientProxy.Font.width(block.getName() + " ");
				drawDialogString(block.getName() + " ", -4 - size, count, false, block);

				for (int l = 0; l < block.lines.size(); l++) {
					IChatComponent line = block.lines.get(l);
					String drawText = line.getFormattedText();

					if (b >= instantBlockPos && l >= instantLinePos) {
						if (drawText.matches("(.*)(\\{(\\d+)})(.*)")) {
							if (textPauseTime > 0) {
								drawText = drawText.substring(0, prevPausePos);
								drawDialogString(drawText, 0, count, true, block);
								textPauseTime--;
								break blockLoop;
							}

							String strInt = "";
							for (int c = 0; c < drawText.length(); c++) {
								if (drawText.substring(c).matches("^(\\{(\\d+)})(.*)") && c > prevPausePos) {
									prevPausePos = c;
									strInt += drawText.charAt(++c);
								} else if (c > prevPausePos && !strInt.isEmpty()) {
									if (drawText.charAt(c) == '}')
										break;

									strInt += drawText.charAt(c);
								}
							}

							if (!strInt.isEmpty()) {
								drawText = drawText.substring(0, prevPausePos);

								drawDialogString(drawText, 0, count, true, block);

								instantBlockPos = b;
								instantLinePos = l;
								textPauseTime = Integer.parseInt(strInt);
								break blockLoop;
							}
						} else {
							prevPausePos = -1;
						}
					}

					drawDialogString(drawText, 0, count, true, block);
					count++;
				}
				count++;
			}
		} else {
			int count = 0;
			for (int pastBlock = 0; pastBlock < currentBlock; pastBlock++) {
				TextBlockClient block = lineBlocks.get(pastBlock);
				int size = ClientProxy.Font.width(block.getName() + " ");
				drawDialogString(block.getName() + " ", -4 - size, count, false, block);

				for (IChatComponent line : block.lines) {
					drawDialogString(line.getFormattedText(), 0, count, true, block);
					count++;
				}
				count++;
			}

			if (currentBlock < lineBlocks.size()) {
				TextBlockClient block = lineBlocks.get(currentBlock);
				int size = ClientProxy.Font.width(block.getName() + " ");
				drawDialogString(block.getName() + " ", -4 - size, count, false, block);

				for (int pastLine = 0; pastLine < currentLine; pastLine++) {
					IChatComponent line = block.lines.get(pastLine);
					drawDialogString(line.getFormattedText(), 0, count, true, block);
					count++;
				}

				if (currentLine < block.lines.size()) {
					IChatComponent line = block.lines.get(currentLine);
					try {
						if (textPauseTime > 0) {
							textPauseTime--;
						} else if (textSpeed > 10 || gradualTextTime%(11 - textSpeed) == 0) {
							int addChar = textSpeed > 10 ? textSpeed - 9 : 1;
							String addText = line.getFormattedText().substring(gradualText.length(), gradualText.length() + addChar);

							if (addText.matches("^(\\{(\\d+)})(.*)")) {
								StringBuilder numStr = new StringBuilder();
								int numLength;
								for (numLength = 1; numLength < addText.length(); numLength++) {
									if (addText.charAt(numLength) == '}')
										break;

									numStr.append(addText.charAt(numLength));
								}
								textPauseTime = Integer.parseInt(numStr.toString());
								addText = line.getFormattedText().substring(gradualText.length(), gradualText.length() + numLength + 1);
							} else if (addText.matches("(.+)\\{(\\d+)(.*)")) {
								StringBuilder str = new StringBuilder();
								for (char c : addText.toCharArray()) {
									if (c == '{')
										break;
									str.append(c);
								}

								addText = str.toString();
							} else if (addText.matches("(\\{(\\d+))$") || addText.equals("{")) {
								do {
									if (addText.length() == gradualText.length() + addText.length() + 1)
										break;

									addText += line.getFormattedText().substring(gradualText.length() + addText.length(), gradualText.length() + addText.length() + 1);
								} while (addText.matches("(.*)(\\{(\\d+))$"));

								textPauseTime = Integer.parseInt(addText.replace("{","").replace("}",""));
							}

							gradualText += addText;
							if (textSoundTime % 5 == 0) {
								Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(dialog.textSound), dialog.textPitch));
							}
							textSoundTime++;
						}
					} catch (IndexOutOfBoundsException exception) {
						gradualText = line.getFormattedText();
					}

					drawDialogString(gradualText, 0, count, true, block);

					if (gradualText.length() == line.getFormattedText().length()) {
						gradualText = "";
						currentLine++;
						if (currentLine >= lineBlocks.get(currentBlock).lines.size()) {
							currentBlock++;
						}
					}

					gradualTextTime++;
				}
			}
		}
		GL11.glPopMatrix();

		GL11.glPushMatrix();
			if (!options.isEmpty()) {
				if (!dialog.showWheel) {
					GL11.glTranslatef(dialog.optionOffsetX, dialog.optionOffsetY, 0);
					drawLinedOptions(j);
				} else {
					drawWheel();
				}
			}
        GL11.glPopMatrix();
    }

    private int selectedX = 0;
    private int selectedY = 0;
    private void drawWheel(){
    	int yoffset = optionStart + dialog.optionOffsetY;

        GL11.glColor4f(1, 1, 1, 1);
        mc.renderEngine.bindTexture(wheel);
        drawTexturedModalRect((width/2) - 31 + dialog.optionOffsetX, yoffset, 0, 0, 63, 40);

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
        drawTexturedModalRect((width/2) - 31 + dialog.optionOffsetX,yoffset, 0, 0, 85, 55);
        for(int slot:dialog.options.keySet()){
        	DialogOption option = dialog.options.get(slot);
        	if(option == null || option.optionType == EnumOptionType.Disabled)
        		continue;
            int color = option.optionColor;
        	if(slot == (selected))
        		color = 0x838FD8;
    		//drawString(fontRenderer, option.title, width/2 -50 ,yoffset+ 162 + slot * 13 , color);
        	if(slot == 0)
        		drawString(fontRendererObj, option.title, width/2 + 13 + dialog.optionOffsetX,yoffset - 6 , color);
        	if(slot == 1)
        		drawString(fontRendererObj, option.title, width/2 + 33 + dialog.optionOffsetX,yoffset + 12  , color);
        	if(slot == 2)
        		drawString(fontRendererObj, option.title, width/2 + 27 + dialog.optionOffsetX,yoffset + 32  , color);
        	if(slot == 3)
        		drawString(fontRendererObj, option.title, width/2 - 13 + dialog.optionOffsetX - ClientProxy.Font.width(option.title),yoffset - 6  , color);
        	if(slot == 4)
        		drawString(fontRendererObj, option.title, width/2 - 33 + dialog.optionOffsetX - ClientProxy.Font.width(option.title),yoffset + 12  , color);
        	if(slot == 5)
        		drawString(fontRendererObj, option.title, width/2 - 27 + dialog.optionOffsetX - ClientProxy.Font.width(option.title),yoffset + 32  , color);

        }
        mc.renderEngine.bindTexture(indicator);
        drawTexturedModalRect(width/2 + selectedX/4  - 2 + dialog.optionOffsetX,yoffset + 16 - selectedY/6, 0, 0, 8, 8);
    }
    private void drawLinedOptions(int j){
		int offset = optionStart;

        if(j >= offset){
			int selected = (j - offset - ClientProxy.Font.height() - dialog.optionOffsetY) / (ClientProxy.Font.height() + dialog.optionSpaceY);
	        if(selected < options.size())
		        this.selected = selected;
        }
        if(selected >= options.size())
        	selected = options.size() - 1;
        if(selected < 0)
        	selected = 0;

		if (dialog.showOptionLine) {
			drawHorizontalLine(guiLeft - 60, guiLeft + xSize + 120, offset, 0xFFFFFFFF);
		}

        for(int k = 0; k < options.size(); k++){
			GL11.glPushMatrix();
        	int id = options.get(k);
        	DialogOption option = dialog.options.get(id);
        	int y = offset + (k + 1) * ClientProxy.Font.height();
			offset += dialog.optionSpaceY;

        	if(selected == k){
        		drawString(fontRendererObj, ">", guiLeft - 60, y, 0xe0e0e0);
        	}

			GL11.glPushMatrix();
			for (IDialogImage dialogImage : dialog.dialogImages.values()) {
				if (dialogImage.getImageType() != 2)
					continue;

				GuiDialogImage image;
				if (dialogImages.containsKey(dialogImage.getId())) {
					image = dialogImages.get(dialogImage.getId());
				} else {
					image = new GuiDialogImage((DialogImage) dialogImage);
					dialogImages.put(dialogImage.getId(),image);
				}

				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
				GL11.glDisable(GL11.GL_ALPHA_TEST);

				GL11.glTranslatef(guiLeft - 30 + dialog.optionSpaceX * k, y, 0.0F);
				image.color = selected == k ? image.selectedColor : image.color;
				image.onRender(mc);

				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
			}
			GL11.glPopMatrix();

			drawString(fontRendererObj, NoppesStringUtils.formatText(option.title, player, npc), guiLeft - 30 + dialog.optionSpaceX * k, y, option.optionColor);
			GL11.glPopMatrix();
		}
    }

	private void drawDialogString(String text, int left, int count, boolean mainDialogText, TextBlockClient block){
		int lineOffset = dialog.renderGradual ? (currentBlock < lineBlocks.size() ? lineBlocks.get(currentBlock).lines.size() : lineBlocks.get(lineBlocks.size()-1).lines.size()) - currentLine : 0;
		int height = count - totalRows + lineOffset;
		int screenPos = optionStart;
		int y = (height * ClientProxy.Font.height()) + screenPos + scrollY;

		if (block.titlePos == 0 || mainDialogText) {
			if (y < screenPos - dialog.textHeight || y > screenPos - ClientProxy.Font.height()) {
				return;
			}
		}

		int offsetX, offsetY, color;
		if (mainDialogText) {
			offsetX = dialog.textOffsetX;
			offsetY = dialog.textOffsetY;
			color = block.color;
		} else {
			offsetX = dialog.titleOffsetX;
			offsetY = dialog.titleOffsetY;
			color = block.titleColor;

			if (block.titlePos == 1 && block.equals(lineBlocks.get(currentBlock < lineBlocks.size() ? currentBlock : currentBlock - 1))) {
				y = screenPos - ClientProxy.Font.height() - 5;
			}
		}

		text = text.replaceAll("\\{(\\d+)}","");
		if (!mainDialogText && block.titlePos == 2 && block.equals(lineBlocks.get(currentBlock < lineBlocks.size() ? currentBlock : currentBlock - 1))) {
			drawString(fontRendererObj, text, offsetX, offsetY, color);
		} else {
			drawString(fontRendererObj, text, guiLeft + left + offsetX, y + offsetY, color);
		}
	}

    public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color){
    	ClientProxy.Font.drawString(text, x, y, color);
    }

    @Override
    public void keyTyped(char c, int i){
    	if(i == mc.gameSettings.keyBindForward.getKeyCode() || i == Keyboard.KEY_UP){
			if (dialog.showWheel) {
				selected--;
			} else {
				selected++;
			}
    	}
    	if(i == mc.gameSettings.keyBindBack.getKeyCode() || i == Keyboard.KEY_DOWN){
			if (dialog.showWheel) {
				selected++;
			} else {
				selected--;
			}
    	}

		if(i == mc.gameSettings.keyBindForward.getKeyCode() || i == Keyboard.KEY_LEFT){
			textSpeed--;
			if (textSpeed < 1) {
				textSpeed = 1;
			}
		}
		if(i == mc.gameSettings.keyBindBack.getKeyCode() || i == Keyboard.KEY_RIGHT){
			textSpeed++;
		}

		if(i == mc.gameSettings.keyBindJump.getKeyCode() || i == Keyboard.KEY_SPACE){
			textSoundEnabled = !textSoundEnabled;
		}

		if (i == mc.gameSettings.keyBindBack.getKeyCode() || i == 201 && scrollY < (totalRows - 2) * ClientProxy.Font.height()) {//Page up
			scrollY += ClientProxy.Font.height() * 2;
		}
		if (i == mc.gameSettings.keyBindBack.getKeyCode() || i == 209 && scrollY > 0) {//Page down
			scrollY -= ClientProxy.Font.height() * 2;
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
    	if(selected == -1 && options.isEmpty() || selected >= 0) {
			scrollY = 0;
			handleDialogSelection();
		}
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

		if (!dialog.showPreviousBlocks) {
			lineBlocks.clear();
		}
    	lineBlocks.add(new TextBlockClient(player.getDisplayName(), option.title, dialog.textWidth, option.optionColor, player, npc));
		gradualText = "";
		currentBlock = lineBlocks.size()-1;
		currentLine = 0;
		gradualTextTime = 0;

		instantBlockPos = lineBlocks.size()-1;
		instantLinePos = 0;
		calculateRowHeight();
		textPauseTime = 0;

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
		this.dialogImages.clear();
		this.dialog = dialog;
		this.options = new ArrayList<Integer>();

    	if(dialog.sound != null && !dialog.sound.isEmpty()){
    		MusicController.Instance.stopMusic();
    		MusicController.Instance.playSound(dialog.sound, (float)npc.posX, (float)npc.posY, (float)npc.posZ);
    	}

		if (!dialog.showPreviousBlocks) {
			lineBlocks.clear();
		}
    	lineBlocks.add(new TextBlockClient(npc, dialog, player, npc));
		gradualText = "";
		currentBlock = lineBlocks.size()-1;
		currentLine = 0;
		gradualTextTime = 0;

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
		totalRows = 0;
        for(TextBlockClient block : lineBlocks){
			totalRows += block.lines.size() + 1;
        }
	}

	@Override
	public void setClose(int i, NBTTagCompound data) {
    	grabMouse(false);
	}
}

