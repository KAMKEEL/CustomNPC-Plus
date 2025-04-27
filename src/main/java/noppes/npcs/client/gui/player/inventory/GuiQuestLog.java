package noppes.npcs.client.gui.player.inventory;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.player.CheckPlayerValue;
import kamkeel.npcs.network.packets.request.party.PartySetQuestPacket;
import kamkeel.npcs.network.packets.request.quest.QuestLogToServerPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NBTTags;
import noppes.npcs.QuestLogData;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import tconstruct.client.tabs.AbstractTab;

import java.util.*;

public class GuiQuestLog extends GuiCNPCInventory implements ICustomScrollListener, IGuiData, IPartyData, GuiYesNoCallback {

    private final ResourceLocation resource = new ResourceLocation("customnpcs", "textures/gui/standardbg.png");

    private EntityPlayer player;
    private GuiCustomScroll scroll;
    private HashMap<Integer, GuiMenuSideButton> sideButtons = new HashMap<Integer, GuiMenuSideButton>();
    private QuestLogData data = new QuestLogData();
    private boolean noQuests = false;
    private byte questPages = 1;
    private static long lastClicked = System.currentTimeMillis();
    private boolean isPartySet = false;

    private HashMap<String, String> questAlertsOnOpen;
    private String trackedQuestKeyOnOpen;

    private float sideButtonScroll = 0;
    private float destSideButtonScroll = 0;

    public GuiQuestLog() {
        super();
        this.player = mc.thePlayer;
        xSize = 280;
        ySize = 180;
        drawDefaultBackground = false;
        PacketClient.sendClient(new CheckPlayerValue(CheckPlayerValue.Type.QuestLog));
    }

    public void initGui() {
        super.initGui();

        sideButtons.clear();

        noQuests = false;

        if (data.categories.isEmpty()) {
            noQuests = true;
            return;
        }
        List<String> categories = new ArrayList<String>();
        categories.addAll(data.categories.keySet());
        Collections.sort(categories, String.CASE_INSENSITIVE_ORDER);
        int i = 0;
        for (String category : categories) {
            if (data.selectedCategory.isEmpty())
                data.selectedCategory = category;
            sideButtons.put(i, new GuiMenuSideButton(i, guiLeft - 69, this.guiTop + 2 + i * 21, 70, 22, category));
            i++;
        }
        sideButtons.get(categories.indexOf(data.selectedCategory)).active = true;

        if (scroll == null)
            scroll = new GuiCustomScroll(this, 0);

        scroll.setList(data.categories.get(data.selectedCategory));
        scroll.setSize(134, 174);
        scroll.guiLeft = guiLeft + 5;
        scroll.guiTop = guiTop + 15;
        addScroll(scroll);

        // Text Forward--
        addButton(new GuiButtonNextPage(1, guiLeft + 286, guiTop + 176, true));

        // Objectives Back--
        addButton(new GuiButtonNextPage(2, guiLeft + 144, guiTop + 176, false));

        boolean showParty = false;
        boolean showTrackAlerts = true;
        if (data.partyQuests.containsKey(data.selectedCategory + ":" + data.selectedQuest)) {
            showParty = true;
            if (data.partyOptions.containsKey(data.selectedCategory + ":" + data.selectedQuest)) {
                if (data.partyOptions.get(data.selectedCategory + ":" + data.selectedQuest).get(0).contains("only")) {
                    showTrackAlerts = false;
                }
            }
        }


        String partyQuestName = ClientCacheHandler.party != null ? ClientCacheHandler.party.getCurrentQuestName() : null;
        isPartySet = Objects.equals(partyQuestName, data.selectedQuest);
        if (showParty) {
            // Objectives Forward--
            addButton(new GuiButtonNextPage(11, guiLeft + 286, guiTop + 176, true));

            // Party Back--
            addButton(new GuiButtonNextPage(10, guiLeft + 144, guiTop + 176, false));

            GuiNpcButton partyButton = new GuiNpcButton(3, guiLeft + 150, guiTop + 151, 50, 20, new String[]{"party.party", "party.partying"}, isPartySet ? 1 : 0);
            addButton(partyButton);

            partyButton.enabled = ClientCacheHandler.party != null && data.hasSelectedQuest()
                && ClientCacheHandler.party.getPartyLeaderName().equals(this.player.getCommandSenderName());
            if (partyButton.enabled && isPartySet) {
                partyButton.packedFGColour = 0x32CD32;
            }

            getButton(11).visible = questPages == 0 && data.hasSelectedQuest();
            getButton(10).visible = questPages == 1 && data.hasSelectedQuest();
        }

        GuiNpcButton trackingButton = new GuiNpcButton(4, guiLeft + 260, guiTop + 151, 50, 20, new String[]{"quest.track", "quest.tracking"}, data.trackedQuestKey.equals(data.selectedCategory + ":" + data.selectedQuest) ? 1 : 0);
        addButton(trackingButton);
        GuiNpcButton alertButton = new GuiNpcButton(5, guiLeft + 205, guiTop + 151, 50, 20, new String[]{"quest.alerts", "quest.noAlerts"}, data.getQuestAlerts() ? 0 : 1);
        addButton(alertButton);

        if (getButton(1) != null)
            getButton(1).visible = questPages == 1 && data.hasSelectedQuest();
        if (getButton(2) != null)
            getButton(2).visible = questPages == 2 && data.hasSelectedQuest();
        if (getButton(3) != null)
            getButton(3).visible = !data.selectedQuest.isEmpty() && getButton(1).visible;
        if (getButton(4) != null) {
            getButton(4).visible = !data.selectedQuest.isEmpty() && getButton(1).visible;
            getButton(4).enabled = showTrackAlerts && !isPartySet;
        }
        if (getButton(5) != null) {
            getButton(5).visible = getButton(4).visible;
            getButton(5).enabled = showTrackAlerts && !isPartySet;
        }

        if (trackingButton.enabled && trackingButton.getValue() == 1) {
            trackingButton.packedFGColour = 0x32CD32;
        }
    }

    @Override
    public void confirmClicked(boolean flag, int i) {
        if (flag) {
            if (i == 0) {
                String key = data.selectedCategory + ":" + data.selectedQuest;
                if (data.partyQuests.containsKey(key)) {
                    int questID = data.partyQuests.get(key);
                    PacketClient.sendClient(new PartySetQuestPacket(questID));
                }
            }
            initGui();
        }
        displayGuiScreen(this);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton instanceof AbstractTab)
            return;

        if (guibutton.id <= -100) {
            super.actionPerformed(guibutton);
            return;
        }

        if (lastClicked > System.currentTimeMillis() - 5) {
            return;
        }
        if (guibutton.id == 11) {
            questPages = 1;
            lastClicked = System.currentTimeMillis();
        } else if (guibutton.id == 10) {
            questPages = 0;
        } else if (guibutton.id == 1) {
            questPages = 2;
        } else if (guibutton.id == 2) {
            questPages = 1;
            lastClicked = System.currentTimeMillis();
        }

        if (guibutton.id == 3) {
            if (Objects.equals(ClientCacheHandler.party.getCurrentQuestName(), data.selectedQuest)) {
                PacketClient.sendClient(new PartySetQuestPacket(-1));
            } else {
                GuiYesNo yesnoDisband = new GuiYesNo(this, "Confirm", StatCollector.translateToLocal("party.setQuestConfirm"), 0);
                displayGuiScreen(yesnoDisband);
            }
        }
        if (guibutton.id == 4) {
            if (!data.trackedQuestKey.equals(data.selectedCategory + ":" + data.selectedQuest)) {
                data.trackedQuestKey = data.selectedCategory + ":" + data.selectedQuest;
            } else {
                data.trackedQuestKey = "";
            }
        }
        if (guibutton.id == 5) {
            data.toggleQuestAlerts();
        }
        initGui();
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        if (scroll != null)
            scroll.visible = !noQuests;
        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 252, 195);
        drawTexturedModalRect(guiLeft + 252, guiTop, 188, 0, 67, 195);
        super.drawScreen(i, j, f);

        if (noQuests) {
            fontRendererObj.drawString(StatCollector.translateToLocal("quest.noquests"), guiLeft + 84, guiTop + 80, CustomNpcResourceListener.DefaultTextColor);
            return;
        }

        // Define constants for smoothing the scroll
        final float SMOOTHING_FACTOR = 0.1F; // Adjust this value for smoother or faster scrolling

        int maxScroll = Math.max(0, this.sideButtons.size() - 9) * 22;
        // Adjust scrolling only if the mouse is in the scroll zone
        if (isMouseInScrollZone(i, j)) {
            this.destSideButtonScroll = ValueUtil.clamp(this.destSideButtonScroll - Math.signum(Mouse.getDWheel()) * 22, -maxScroll, 0);
        }
        // Apply smoothing to the scroll transition
        this.sideButtonScroll += (this.destSideButtonScroll - this.sideButtonScroll) * SMOOTHING_FACTOR;

        // Loop through the side buttons and render them
        for (Map.Entry<Integer, GuiMenuSideButton> entry : this.sideButtons.entrySet()) {
            int buttonNumber = entry.getKey();
            GuiMenuSideButton button = entry.getValue();

            float rawYPosition = (this.guiTop + 2 + buttonNumber * 21 + this.sideButtonScroll);
            int smoothedYPosition = Math.round(rawYPosition);

            // Render the button if it's within the visible area
            if (smoothedYPosition >= this.guiTop && smoothedYPosition < this.guiTop + 22 * 8) {
                button.yPosition = smoothedYPosition;
                button.drawButton(mc, i, j);
            }
        }

        fontRendererObj.drawString(data.selectedCategory, guiLeft + 5, guiTop + 5, CustomNpcResourceListener.DefaultTextColor);

        if (!data.hasSelectedQuest())
            return;

        if (questPages == 1) {
            drawProgress();
            String title = StatCollector.translateToLocal("gui.text");
            fontRendererObj.drawString(title, guiLeft + 284 - fontRendererObj.getStringWidth(title), guiTop + 179, CustomNpcResourceListener.DefaultTextColor);

            if (data.partyQuests.containsKey(data.selectedCategory + ":" + data.selectedQuest)) {
                title = StatCollector.translateToLocal("party.party");
                fontRendererObj.drawString(title, guiLeft + 170, guiTop + 179, CustomNpcResourceListener.DefaultTextColor);
            }
        } else if (questPages == 0) {
            drawPartyOptions();
            String title = StatCollector.translateToLocal("quest.objectives");
            fontRendererObj.drawString(title, guiLeft + 284 - fontRendererObj.getStringWidth(title), guiTop + 179, CustomNpcResourceListener.DefaultTextColor);
        } else {
            drawQuestText();
            String title = StatCollector.translateToLocal("quest.objectives");
            fontRendererObj.drawString(title, guiLeft + 170, guiTop + 179, CustomNpcResourceListener.DefaultTextColor);
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(guiLeft + 148, guiTop, 0);
        GL11.glScalef(1.24f, 1.24f, 1.24f);
        fontRendererObj.drawString(data.selectedQuest, (130 - fontRendererObj.getStringWidth(data.selectedQuest)) / 2, 4, CustomNpcResourceListener.DefaultTextColor);
        GL11.glPopMatrix();
        drawHorizontalLine(guiLeft + 142, guiLeft + 312, guiTop + 17, +0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
    }

    private void drawQuestText() {
        TextBlockClient block = new TextBlockClient(data.getQuestText(), 174, true, player);
        int yoffset = guiTop + 5;
        for (int i = 0; i < block.lines.size(); i++) {
            String text = block.lines.get(i).getFormattedText();
            fontRendererObj.drawString(text, guiLeft + 142, guiTop + 20 + (i * fontRendererObj.FONT_HEIGHT), CustomNpcResourceListener.DefaultTextColor);
        }
    }

    private void drawProgress() {
        String complete = data.getComplete();
        if (complete != null && !complete.isEmpty())
            fontRendererObj.drawString(StatCollector.translateToLocalFormatted("quest.completewith", complete), guiLeft + 144, guiTop + 105, CustomNpcResourceListener.DefaultTextColor);

        int yoffset = guiTop + 22;
        for (String process : data.getQuestStatus()) {
            int index = process.lastIndexOf(":");
            if (index > 0) {
                String name = process.substring(0, index);
                String trans = StatCollector.translateToLocal(name);
                if (!trans.equals(name))
                    name = trans;
                trans = StatCollector.translateToLocal("entity." + name + ".name");
                if (!trans.equals("entity." + name + ".name")) {
                    name = trans;
                }
                process = name + process.substring(index);
            }
            fontRendererObj.drawString("- " + process, guiLeft + 144, yoffset, CustomNpcResourceListener.DefaultTextColor);
            yoffset += 10;
        }
    }

    private void drawPartyOptions() {
        int yoffset = guiTop + 22;
        for (String process : data.getPartyOptions()) {
            List<String> parts = Arrays.asList(process.split(":"));
            String drawString = StatCollector.translateToLocal(parts.get(0)) + ": " + StatCollector.translateToLocal(parts.get(1));
            fontRendererObj.drawString("- " + drawString, guiLeft + 144, yoffset, CustomNpcResourceListener.DefaultTextColor);
            yoffset += 10;
        }
    }

    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
    }

    @Override
    public void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        if (k == 0) {
            if (scroll != null)
                scroll.mouseClicked(i, j, k);
            for (GuiMenuSideButton button : new ArrayList<GuiMenuSideButton>(sideButtons.values())) {
                if (button.mousePressed(mc, i, j)) {
                    sideButtonPressed(button);
                }
            }
        }
    }

    private void sideButtonPressed(GuiMenuSideButton button) {
        if (button.active)
            return;
        NoppesUtil.clickSound();
        data.selectedCategory = button.displayString;
        data.selectedQuest = "";
        if (scroll != null)
            scroll.selected = -1;
        this.initGui();
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
        if (!scroll.hasSelected())
            return;
        data.selectedQuest = scroll.getSelected();
        initGui();
    }

    @Override
    public void keyTyped(char c, int i) {
        if (i == 1 || i == mc.gameSettings.keyBindInventory.getKeyCode()) // inventory key
        {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        QuestLogData data = new QuestLogData();
        data.readNBT(compound);
        this.data = data;
        this.questAlertsOnOpen = new HashMap<>(data.questAlerts);
        this.trackedQuestKeyOnOpen = data.trackedQuestKey;
        initGui();
    }

    @Override
    public void setPartyData(NBTTagCompound compound) {
        if (compound.hasKey("PartyUUID")) {
            UUID uuid = UUID.fromString(compound.getString("PartyUUID"));
            ClientCacheHandler.party = new Party(uuid);
            ClientCacheHandler.party.readFromNBT(compound);
            initGui();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.save();
    }

    @Override
    public void save() {
        if (this.data != null &&
            (!Objects.equals(this.questAlertsOnOpen, data.questAlerts) ||
                !Objects.equals(this.trackedQuestKeyOnOpen, data.trackedQuestKey))) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("Alerts", NBTTags.nbtStringStringMap(data.questAlerts));
            PacketClient.sendClient(new QuestLogToServerPacket(compound, this.data.trackedQuestKey));
        }
    }


    public boolean isMouseInScrollZone(int x, int y) {
        int scrollZoneLeft = guiLeft - 69; // Left boundary of the scroll zone
        int scrollZoneRight = guiLeft - 69 + 70; // Right boundary of the scroll zone
        int scrollZoneTop = guiTop + 2; // Top boundary of the scroll zone
        int scrollZoneBottom = guiTop + 2 + 8 * 21; // Bottom boundary of the scroll zone

        return x >= scrollZoneLeft && x <= scrollZoneRight && y >= scrollZoneTop && y <= scrollZoneBottom;
    }
}
