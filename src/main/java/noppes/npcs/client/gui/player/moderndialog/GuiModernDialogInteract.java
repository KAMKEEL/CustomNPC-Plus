package noppes.npcs.client.gui.player.moderndialog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.IDialogImage;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.player.GuiDialogImage;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogImage;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GuiModernDialogInteract extends GuiNPCInterface implements IGuiClose {
    private Dialog dialog;
    private int selected = 0;
    private List<TextBlockClient> lineBlocks = new ArrayList<TextBlockClient>();
    private List<Integer> options = new ArrayList<Integer>();
    private int totalRows = 0;

    private ScaledResolution scaledResolution;

    private int optionStart = 0;

    private static int textSpeed = 10;
    private static boolean textSoundEnabled = true;

    private int scrollY;
    private final ResourceLocation decomposed = new ResourceLocation("customnpcs", "textures/gui/dialog_menu_decomposed.png");

    private boolean isGrabbed = false;

    private final HashMap<Integer, GuiDialogImage> dialogImages = new HashMap<>();

    public GuiModernDialogInteract(EntityNPCInterface npc, Dialog dialog) {
        super(npc);
        this.dialog = dialog;

        appendDialog(dialog);
        ySize = 238;
    }

    public void initGui() {
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

    public void grabMouse(boolean grab) {
        if (grab && !isGrabbed) {
            Minecraft.getMinecraft().mouseHelper.grabMouseCursor();
            isGrabbed = true;
        } else if (!grab && isGrabbed) {
            Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
            isGrabbed = false;
        }
    }

    public void drawLine(int x, int y, int width) {
        drawRect(x, y, width, y + 1,0xff000000+ dialog.colourData.getLineColour1());
        drawRect(x, y + 1, width, y + 2, 0xff000000+ dialog.colourData.getLineColour2());
        drawRect(x, y + 2, width, y + 3, 0xff000000+ dialog.colourData.getLineColour3());
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glColor4f(1, 1, 1, 1);
        this.drawGradientRect(0, 0, this.width, this.height, 0x66000000, 0x66000000);
        if (!dialog.hideNPC) {
            float scaleHeight = height/509f;
            float scaleWidth = width/960f;
            drawNpc(npc, -210+dialog.npcOffsetX+(int)(300*(1-scaleWidth)), 350+dialog.npcOffsetY-(int)(100*(1-scaleHeight)), 9.5F*dialog.npcScale*scaleHeight, -20);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        setOptionOffset();

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.5f, 100.065F);

        for (IDialogImage dialogImage : dialog.dialogImages.values()) {
            if (dialogImage.getImageType() != 0)
                continue;

            GuiDialogImage image;
            if (dialogImages.containsKey(dialogImage.getId())) {
                image = dialogImages.get(dialogImage.getId());
            } else {
                image = new GuiDialogImage((DialogImage) dialogImage);
                dialogImages.put(dialogImage.getId(), image);
            }

            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GL11.glDisable(GL11.GL_ALPHA_TEST);

            GL11.glTranslatef(image.alignment % 3 * ((float) (scaledResolution.getScaledWidth()) / 2), (float) (Math.floor((float) (image.alignment / 3)) * ((float) (scaledResolution.getScaledHeight()) / 2)), 0.0F);
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
                dialogImages.put(dialogImage.getId(), image);
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
        int textBlockWidth = 700;
        int lineCount = getLineCount(dialog.text, textBlockWidth);
        int gap = Math.max(16, Math.min((int) (2.6f * (float) lineCount), 32));
        int textPartHeight = 23 + 3 + lineCount * ClientProxy.Font.height() + 2 * gap;
        GL11.glPushMatrix();
        GL11.glTranslated(0.0, 0.5, 200.06500244140625);
        drawGradientRect(0, height - textPartHeight, this.width, this.height, 0x99000000, 0x99000000);
        drawLine(23, height - textPartHeight + 23, width - 23);
        GL11.glScalef(1.5f, 1.5f, 1);
        drawString(fontRendererObj, npc.getCommandSenderName(), (int) (47 / 1.5), (int) ((height - textPartHeight + 5) / 1.5), dialog.titleColor);
        GL11.glScalef(1 / 1.5f, 1 / 1.5f, 1);
        drawTextBlock(dialog.text, (width - textBlockWidth) / 2, height - textPartHeight + 23 + 3 + gap, textBlockWidth);
        selected = -1;
        for (int i = 0; i < this.options.size(); i++) {
            int optionHeight = height/2-30 + i * (13 + 6);
            int optionNum = options.get(i);
            DialogOption option = dialog.options.get(optionNum);
            if (mouseX >= width-237 && mouseX <= width-14 && mouseY >= optionHeight && mouseY <= optionHeight + 13) {
                selected = i;
            }
            GL11.glEnable(GL11.GL_BLEND);
            Minecraft.getMinecraft().getTextureManager().bindTexture(decomposed);
            drawTexturedModalRect(width-233, optionHeight, 0, i == selected ? 13 : 0, 223, 13);
            GL11.glDisable(GL11.GL_BLEND);
            if (getQuestByOptionId(optionNum) != null) {
                drawString(fontRendererObj, "!", width-229, optionHeight + 3, 0x76e85b);
            } else {
                drawString(fontRendererObj, ">", width-229, optionHeight + 3, -1);
            }
            drawString(fontRendererObj, option.title, width-221, optionHeight + 3, option.optionColor);
        }
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

    public Quest getQuestByOptionId(int id) {
        DialogOption option = dialog.options.get(id);
        if (option != null && option.getDialog() != null && option.getDialog().hasQuest()) {
            return option.getDialog().getQuest();
        }
        return null;
    }

    public void drawNpc(EntityNPCInterface entity, int x, int y, float zoomed, int rotation) {
        EntityNPCInterface npc = entity;

        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f5 = entity.prevRotationYawHead;
        float f6 = entity.rotationYawHead;
        float scale = 1.0F;
        if ((double) entity.height > 2.4) {
            scale = 2.0F / entity.height;
        }
        float f7 = (float) (guiLeft + x);
        entity.renderYawOffset = 0.0F;
        entity.rotationYaw = (float) Math.atan((f7 / 80.0F)) * 40.0F + (float) rotation;
        entity.rotationPitch = 0;
        entity.rotationYawHead = 0.0F;
        entity.prevRotationYawHead = 0.0F;
        int orientation = 0;
        orientation = npc.ai.orientation;
        npc.ai.orientation = rotation;
        int visibleName = npc.display.showName;
        npc.display.showName = 1;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) guiLeft + (float) x, (float) (guiTop + y), 1050.0F);
        GL11.glScalef(1.0F, 1.0F, -1.0F);
        GL11.glTranslated(0.0, 0.0, 1000.0);
        GL11.glScalef(30.0F * scale * zoomed, 30.0F * scale * zoomed, 30.0F * scale * zoomed);
        GL11.glRotatef(180.0F, 0, 1, 0);
        GL11.glRotatef(180.0F, 0, 0, 1);
        GL11.glRotatef(-(float) rotation, 0, 1, 0);
        RenderHelper.enableStandardItemLighting();
        RenderManager.instance.playerViewY = 180F;
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderManager.instance.renderEntityWithPosYaw(npc, 0.0, 0.0, 0.0, 0.0F, 1.0F);
        GL11.glPopMatrix();
        entity.renderYawOffset = f2;
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = f5;
        entity.rotationYawHead = f6;
        npc.ai.orientation = orientation;
        npc.display.showName=visibleName;
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public void drawWorldBackground(int p_238651_2_) {
    }

    public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        ClientProxy.Font.drawString(text, x, y, color);
    }

    @Override
    public void keyTyped(char c, int i) {
        if (i == mc.gameSettings.keyBindForward.getKeyCode() || i == Keyboard.KEY_UP) {
            if (dialog.showWheel) {
                selected--;
            } else {
                selected++;
            }
        }
        if (i == mc.gameSettings.keyBindBack.getKeyCode() || i == Keyboard.KEY_DOWN) {
            if (dialog.showWheel) {
                selected++;
            } else {
                selected--;
            }
        }

        if (i == mc.gameSettings.keyBindForward.getKeyCode() || i == Keyboard.KEY_LEFT) {
            textSpeed--;
            if (textSpeed < 1) {
                textSpeed = 1;
            }
        }
        if (i == mc.gameSettings.keyBindBack.getKeyCode() || i == Keyboard.KEY_RIGHT) {
            textSpeed++;
        }

        if (i == mc.gameSettings.keyBindJump.getKeyCode() || i == Keyboard.KEY_SPACE) {
            textSoundEnabled = !textSoundEnabled;
        }

        if (i == mc.gameSettings.keyBindBack.getKeyCode() || i == 201 && scrollY < (totalRows - 2) * ClientProxy.Font.height()) {//Page up
            scrollY += ClientProxy.Font.height() * 2;
        }
        if (i == mc.gameSettings.keyBindBack.getKeyCode() || i == 209 && scrollY > 0) {//Page down
            scrollY -= ClientProxy.Font.height() * 2;
        }

        if (i == 28) {
            handleDialogSelection();
        }
        if (closeOnEsc && (i == 1 || isInventoryKey(i))) {
            NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, dialog.id, -1);
            closed();
            close();
        }
        super.keyTyped(c, i);
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        if (selected == -1 && options.isEmpty() || selected >= 0) {
            scrollY = 0;
            handleDialogSelection();
        }
    }

    private void handleDialogSelection() {
        int optionId = -1;
        if (dialog.showWheel)
            optionId = selected;
        else if (!options.isEmpty())
            optionId = options.get(selected);
        if(getQuestByOptionId(optionId)==null){
            NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog, dialog.id, optionId);
        }else{
            CustomNpcs.proxy.openGui(player, new GuiModernQuestDialog(npc,getQuestByOptionId(optionId),dialog,optionId));
        }

        if (dialog == null || !dialog.hasOtherOptions() || options.isEmpty()) {
            closed();
            close();
            return;
        }
        DialogOption option = dialog.options.get(optionId);
        if (option == null || option.optionType != EnumOptionType.DialogOption) {
            closed();
            close();
            return;
        }

        if (!dialog.showPreviousBlocks) {
            lineBlocks.clear();
        }
        lineBlocks.add(new TextBlockClient(player.getDisplayName(), option.title, dialog.textWidth, option.optionColor, player, npc));

        calculateRowHeight();

        NoppesUtil.clickSound();
    }

    private void closed() {
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

        if (dialog.sound != null && !dialog.sound.isEmpty()) {
            MusicController.Instance.stopMusic();
            MusicController.Instance.playSound(dialog.sound, (float) npc.posX, (float) npc.posY, (float) npc.posZ);
        }

        if (!dialog.showPreviousBlocks) {
            lineBlocks.clear();
        }
        lineBlocks.add(new TextBlockClient(npc, dialog, player, npc));

        for (int slot : dialog.options.keySet()) {
            DialogOption option = dialog.options.get(slot);
            if (option == null || option.optionType == EnumOptionType.Disabled)
                continue;
            options.add(slot);
        }
        calculateRowHeight();

        grabMouse(dialog.showWheel);
    }

    private void calculateRowHeight() {
        totalRows = 0;
        for (TextBlockClient block : lineBlocks) {
            totalRows += block.lines.size() + 1;
        }
    }

    public void drawTextBlock(String text, int x, int y, int width) {
        TextBlockClient block = new TextBlockClient("", text, width, -1, player, npc);

        int count = 0;
        for (Iterator var9 = block.lines.iterator(); var9.hasNext(); count++) {
            IChatComponent line = (IChatComponent) var9.next();
            int height = y + count * ClientProxy.Font.height();
            drawCenteredString(fontRendererObj, line.getFormattedText(), x + width / 2, height, dialog.color);
        }
    }

    public void drawCenteredString(FontRenderer p_73732_1_, String p_73732_2_, int p_73732_3_, int p_73732_4_, int p_73732_5_) {
        ClientProxy.Font.drawString(p_73732_2_, p_73732_3_ - ClientProxy.Font.width(p_73732_2_) / 2, p_73732_4_, p_73732_5_);
    }

    public int getLineCount(String text, int width) {
        TextBlockClient block = new TextBlockClient("", text, width, -1, player, npc);
        return block.lines.size();
    }

    @Override
    public void setClose(int i, NBTTagCompound data) {
        grabMouse(false);
    }
}

