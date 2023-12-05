package noppes.npcs.client.gui.player.moderndialog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.IDialogImage;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.client.ClientConfig;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.player.GuiDialogImage;
import noppes.npcs.client.gui.player.GuiDialogInteract;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiTexturedButton;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.constants.EnumOptionType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.*;

public class GuiModernQuestDialog extends GuiNPCInterface implements IGuiClose {
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
    private Dialog prevDialog;
    private int optionId;
    private Quest quest;

    public GuiModernQuestDialog(EntityNPCInterface npc, Quest quest, Dialog prevDialog, int optionId) {
        super(npc);
        ySize = 238;
        this.prevDialog = prevDialog;
        this.quest = quest;
        this.optionId = optionId;
    }

    public void initGui() {
        super.initGui();
        isGrabbed = false;
        guiTop = (height - ySize);
        calculateRowHeight();
        this.scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        this.drawDefaultBackground = prevDialog.darkenScreen;
        setOptionOffset();
        addButton(new GuiTexturedButton(0,"questgui.reject",720,326,156,40,decomposed.toString(),72,15));
        addButton(new GuiTexturedButton(1,"questgui.accept",812,326,156,40,decomposed.toString(),72,15));
    }

    public void setOptionOffset() {
        optionStart = scaledResolution.getScaledHeight() - (options.size()) * (ClientProxy.Font.height() + prevDialog.optionSpaceY) - 20;
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
        drawRect(x, y, width, y + 1,0xff000000+ prevDialog.colourData.getLineColour1());//0xff8d3800);//
        drawRect(x, y + 1, width, y + 2, 0xff000000+ prevDialog.colourData.getLineColour2()); //0xfffea53b);//
        drawRect(x, y + 2, width, y + 3, 0xff000000+ prevDialog.colourData.getLineColour3()); //0xff8d3800);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glColor4f(1, 1, 1, 1);
        this.drawGradientRect(0, 0, this.width, this.height, 0x66000000, 0x66000000);
        if (!prevDialog.hideNPC) {
            float scaleHeight = height/509f;
            float scaleWidth = width/960f;
            drawNpc(npc, -210+prevDialog.npcOffsetX+(int)(300*(1-scaleWidth)), 350+prevDialog.npcOffsetY-(int)(100*(1-scaleHeight)), 9.5F*prevDialog.npcScale*scaleHeight, -20);
        }
        setOptionOffset();

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.5f, 100.065F);

        for (IDialogImage dialogImage : prevDialog.dialogImages.values()) {
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
        for (IDialogImage dialogImage : prevDialog.dialogImages.values()) {
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

            GL11.glTranslatef(guiLeft + prevDialog.textOffsetX, optionStart + prevDialog.textOffsetY - image.height * image.scale, 0.0F);
            image.onRender(mc);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
        }
        GL11.glPopMatrix();
        int textBlockWidth = 700;
        String takeQuestString=translate("questgui.doyouaccept");
        int lineCount = getLineCount(takeQuestString, textBlockWidth);
        int gap = Math.max(16, Math.min((int) (2.6f * (float) lineCount), 32));
        int textPartHeight = 23 + 3 + lineCount * ClientProxy.Font.height() + 2 * gap;
        GL11.glPushMatrix();
        GL11.glTranslated(0.0, 0.5, 200.06500244140625);
        drawGradientRect(0, height - textPartHeight, this.width, this.height, 0x99000000, 0x99000000);
        drawLine(23, height - textPartHeight + 23, width - 23);
        GL11.glScalef(1.5f, 1.5f, 1);
        drawString(fontRendererObj, npc.getCommandSenderName(), (int) (47 / 1.5), (int) ((height - textPartHeight + 5) / 1.5), prevDialog.titleColor);
        GL11.glScalef(1 / 1.5f, 1 / 1.5f, 1);
        drawTextBlock(takeQuestString, (width - textBlockWidth) / 2, height - textPartHeight + 23 + 3 + gap, textBlockWidth,-1);

        Map<Integer, QuestData> activeQuests = PlayerDataController.Instance.getPlayerData(this.player).questData.activeQuests;
        boolean hadQuest = activeQuests.containsKey(quest.id);
        activeQuests.put(quest.id,new QuestData(quest));
        StringBuilder objectiveString = new StringBuilder();
        String[] questType = {translate("questgui.bringitems"), translate("questgui.readdialog"),
                translate("questgui.killmobs"), translate("questgui.findlocation"), translate("questgui.defeat")};
        for (IQuestObjective objective : quest.questInterface.getObjectives(this.player)) {
            if(objective!=null)
                objectiveString.append("- " + questType[quest.getType()] + ": ").append(objective.getText()).append("\n");
        }
        if(!hadQuest)
            activeQuests.remove(quest.id);
        int questLineCount = getLineCount(quest.logText, 180);
        int objectivesLineCount = getLineCount(objectiveString.toString(),180);
        int topToTextBottom = (int)(height*0.08) + 38+questLineCount*ClientProxy.Font.height()+20;
        int topToObjectivesBottom = topToTextBottom+19+objectivesLineCount*ClientProxy.Font.height()+14;
        int rewardCount = 0;

        List<Integer> facIDs = new ArrayList<Integer>();
        for (Integer facID : new Integer[]{quest.factionOptions.factionId, quest.factionOptions.faction2Id}) {
            if (facID != -1) facIDs.add(facID);
        }
        for (IItemStack reward : quest.getRewards().getItems()) {
            if (reward!=null && !(reward.getMCItemStack()==null)) rewardCount++;
        }
        int topToRewardsBottom = topToObjectivesBottom+(rewardCount==0?0:(36+13));
        int topToExpBottom = topToRewardsBottom+(quest.rewardExp==0?0:(12));
        int topToFactionBottom = topToExpBottom + (facIDs.size() * 15);
        int questBlockHeight = topToFactionBottom+28;
        drawGradientRect( width-285, (int)(height*0.08), width-285+260, questBlockHeight, 0x99000000, 0x99000000);
        GL11.glScalef(1.5f, 1.5f, 1);
        drawString(fontRendererObj, quest.getName(), (int) ((width-268) / 1.5), (int) ((height*0.095) / 1.5), -1);
        GL11.glScalef(1 / 1.5f, 1 / 1.5f, 1);
        drawLine(width-274,(int)(height*0.13),width-285+260-11);
        drawTextBlock(quest.logText, width-245, (int)(height*0.157), 180,0xb8b8b8);
        drawString(fontRendererObj, translate("questgui.objectives"),width-270, topToTextBottom, -1);
        drawLeftAllignedTextBlock(objectiveString.toString(), width-255, topToTextBottom+12, 180,0xb8b8b8);
        if(rewardCount!=0)
            drawString(fontRendererObj, translate("questgui.rewards"),width-270, topToObjectivesBottom, -1);
        for(int i=0;i<quest.rewardItems.getSizeInventory();i++){
            ItemStack rewardStack = quest.rewardItems.getStackInSlot(i);
            if(rewardStack==null) continue;
            Minecraft.getMinecraft().getTextureManager().bindTexture(decomposed);
            GL11.glDisable(GL11.GL_LIGHTING);
            int color = prevDialog.colourData.getSlotColour();
            GL11.glColor4f((color >> 16 & 0xff) / 255f, (color >> 8 & 0xff) / 255f, (color & 0xff) / 255f, 1);
            drawTexturedModalRect(width-270+26*i, topToObjectivesBottom+16, 0, 27, 24, 24);

            itemRender.renderItemAndEffectIntoGUI(fontRendererObj,Minecraft.getMinecraft().renderEngine, rewardStack, width-266+26*i, topToObjectivesBottom+20);
            itemRender.renderItemOverlayIntoGUI(fontRendererObj,Minecraft.getMinecraft().renderEngine, rewardStack, width-266+26*i, topToObjectivesBottom+20, ""+rewardStack.stackSize);
        }
        GL11.glDisable(GL11.GL_LIGHTING);
        if(quest.rewardExp!=0) {
            drawString(fontRendererObj, translate("questgui.experience"), width-270, topToRewardsBottom, 0xb8b8b8);
            int expPosX = width-270+ClientProxy.Font.width(translate("questgui.experience"));
            drawString(fontRendererObj, ""+quest.rewardExp, expPosX, topToRewardsBottom, -1);
            int expSymbolPosX = expPosX+ClientProxy.Font.width(""+quest.rewardExp+"  ");
            Minecraft.getMinecraft().getTextureManager().bindTexture(decomposed);
            drawTexturedModalRect(expSymbolPosX, topToRewardsBottom, 26, 27, 8, 8);
        }
        int fac1ID =  quest.factionOptions.factionId;
        if (fac1ID != -1) {
            String fac1Name = FactionController.getInstance().getFaction(fac1ID).getName();
            String fac1Color = (quest.factionOptions.decreaseFactionPoints) ? "§c-" : "§a+";
            int fac1Point = quest.factionOptions.factionPoints;
            int facIDIndex = facIDs.indexOf(fac1ID);
            drawString(fontRendererObj, fac1Name + " " + fac1Color + fac1Point, width-270, topToExpBottom + (facIDIndex * 12), 0xb8b8b8);
        }
        int fac2ID =  quest.factionOptions.faction2Id;
        if (fac2ID != -1) {
            String fac2Name = FactionController.getInstance().getFaction(fac2ID).getName();
            String fac2Color = (quest.factionOptions.decreaseFaction2Points) ? "§c-" : "§a+";
            int fac2Point = quest.factionOptions.faction2Points;
            int facIDIndex = facIDs.indexOf(fac2ID);
            drawString(fontRendererObj, fac2Name + " " + fac2Color + fac2Point, width-270, topToExpBottom + (facIDIndex * 12), 0xb8b8b8);
        }
        this.buttons.get(0).xPosition=width-240;
        this.buttons.get(1).xPosition=width-148;
        this.buttons.get(0).yPosition=topToFactionBottom;
        this.buttons.get(1).yPosition=topToFactionBottom;
        ((GuiTexturedButton)this.buttons.get(0)).scale=0.5f;
        ((GuiTexturedButton)this.buttons.get(1)).scale=0.5f;
        ((GuiTexturedButton)this.buttons.get(0)).color=prevDialog.colourData.getButtonRejectColour();
        ((GuiTexturedButton)this.buttons.get(1)).color=prevDialog.colourData.getButtonAcceptColour();
        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glColor4f(1,1,1,1);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }

    public String translate(String key){
        return StatCollector.translateToLocal(key);
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
        float f8 = (float) (guiTop + y) - 50.0F * scale * zoomed;
        entity.renderYawOffset = 0.0F;
        entity.rotationYaw = (float) Math.atan((double) (f7 / 80.0F)) * 40.0F + (float) rotation;
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
    protected void actionPerformed(GuiButton guibutton) {
        GuiNpcButton button = (GuiNpcButton) guibutton;
        if(button.id==0){
            NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog,prevDialog.id, -1);
            closed();
            close();
        }else if(button.id==1){
            if(optionId!=-2){
                NoppesUtilPlayer.sendData(EnumPlayerPacket.Dialog,prevDialog.id, optionId);
            }else{
                if(ClientConfig.useCustomGUIDesign){
                    CustomNpcs.proxy.openGui(player, new GuiModernDialogInteract(npc, prevDialog));
                }else {
                    CustomNpcs.proxy.openGui(player, new GuiDialogInteract(npc, prevDialog));
                }
            }

        }
    }

    private void closed() {
        grabMouse(false);
        NoppesUtilPlayer.sendData(EnumPlayerPacket.CheckQuestCompletion);
    }

    public void save() {

    }

    private void calculateRowHeight() {
        totalRows = 0;
        for (TextBlockClient block : lineBlocks) {
            totalRows += block.lines.size() + 1;
        }
    }

    public void drawTextBlock(String text, int x, int y, int width, int color) {
        TextBlockClient block = new TextBlockClient("", text, width, -1, player, npc);

        int count = 0;
        for (Iterator var9 = block.lines.iterator(); var9.hasNext(); count++) {
            IChatComponent line = (IChatComponent) var9.next();
            int height = y + count * ClientProxy.Font.height();
            drawCenteredString(fontRendererObj, line.getFormattedText(), x + width / 2, height, color);
        }
    }
    public void drawLeftAllignedTextBlock(String text, int x, int y, int width, int color) {
        TextBlockClient block = new TextBlockClient("", text, width, -1, player, npc);

        int count = 0;
        for (Iterator var9 = block.lines.iterator(); var9.hasNext(); count++) {
            IChatComponent line = (IChatComponent) var9.next();
            int height = y + count * ClientProxy.Font.height();
            drawString(fontRendererObj, line.getFormattedText(), x, height, color);
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

