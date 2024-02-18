package noppes.npcs.client.gui.player.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Party;
import noppes.npcs.controllers.data.Quest;
import org.lwjgl.opengl.GL11;
import tconstruct.client.tabs.InventoryTabCustomNpc;
import tconstruct.client.tabs.TabRegistry;

import java.util.*;

public class GuiParty extends GuiCNPCInventory implements ITextfieldListener, ITopButtonListener,ICustomScrollListener,  IGuiData, GuiYesNoCallback {
    private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/standardbg.png");
    private final EntityPlayer player;
    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean receivedData;
    private long renderTicks;

    private String selectedPlayer;
    private String selectedInvite;

    private boolean partyChanged = false;
    private boolean isLeader;

    private final HashMap<String, String> invites = new HashMap<>();

    private boolean showQuestText;
    private final Vector<String> questLogStatus = new Vector<>();
    private String questCompleteWith;

    public GuiParty(EntityPlayer player) {
        super();
        this.player = player;
        xSize = 280;
        ySize = 180;
        drawDefaultBackground = false;
        activeTab = 1;
        Client.sendData(EnumPacketServer.GetPartyData);
    }

    public void initGui(){
        super.initGui();
        this.selectedInvite = this.selectedPlayer = null;
        Party party = ClientCacheHandler.party;
        TabRegistry.addTabsToList(buttonList);
        TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabCustomNpc.class);

        if (receivedData) {
            if (party == null) {
                //
                //create party button
                //
                GuiNpcButton createPartyButton = new GuiNpcButton(200, guiLeft + xSize/2 + 40, guiTop + ySize/2 + 20, "party.createParty");
                createPartyButton.width = 100;
                this.addButton(createPartyButton);

                //party invites list
                //
                GuiCustomScroll inviteScroll = new GuiCustomScroll(this, 210, false);
                inviteScroll.setSize(135, 165);
                inviteScroll.guiLeft = guiLeft + 5;
                inviteScroll.guiTop = guiTop + 5;
                inviteScroll.setList(new ArrayList<>(this.invites.keySet()));
                this.addScroll(inviteScroll);

                GuiNpcButton acceptButton = new GuiNpcButton(215, guiLeft + 5, guiTop + ySize - 8, "party.accept");
                acceptButton.width = 65;
                this.addButton(acceptButton);

                GuiNpcButton ignoreButton = new GuiNpcButton(220, guiLeft + 75, guiTop + ySize - 8, "party.ignore");
                ignoreButton.width = 65;
                this.addButton(ignoreButton);
            } else {
                //
                //party player list
                //
                GuiCustomScroll playerScroll = new GuiCustomScroll(this, 300, false);
                playerScroll.setSize(135, this.isLeader ? 145 : 165);
                playerScroll.guiLeft = guiLeft + 5;
                playerScroll.guiTop = guiTop + 5;

                ArrayList<String> arrayList = new ArrayList<>();
                Collection<String> playerNames = party.getPlayerNames();
                for (String s : playerNames) {
                    if (party.getPartyLeaderName().equals(s)) {
                        arrayList.add(s + " §e[" + StatCollector.translateToLocal("party.leader").toUpperCase() + "]");
                    } else {
                        arrayList.add(s);
                    }
                }

                playerScroll.setList(arrayList);
                this.addScroll(playerScroll);

                if (party.getIsLocked()) {
                    int arrowButtonsY = guiTop + ySize - 8 - 20;

                    addButton(new GuiButtonNextPage(400, guiLeft + 144, arrowButtonsY, false));
                    addLabel(new GuiNpcLabel(401, "quest.objectives", guiLeft + 168, getButton(400).yPosition + 3));
                    getButton(400).visible = getLabel(401).enabled = this.showQuestText;

                    addButton(new GuiButtonNextPage(405, guiLeft + 286, arrowButtonsY, true));
                    String textString = StatCollector.translateToLocal("gui.text");
                    addLabel(new GuiNpcLabel(406, textString,
                        guiLeft + 284 - fontRendererObj.getStringWidth(textString), getButton(405).yPosition + 3));
                    getButton(405).visible = getLabel(406).enabled = !getButton(400).visible;

                    GuiNpcButton disbandButton = new GuiNpcButton(410, guiLeft + 164, arrowButtonsY + 19, "party.dropQuest");
                    disbandButton.width = 135;
                    this.addButton(disbandButton);
                }

                //toggle friendly fire
                //
                GuiNpcLabel friendlyFireLabel = new GuiNpcLabel(321, StatCollector.translateToLocal("party.friendlyFire") + ":",
                    guiLeft + xSize / 2 + 10, guiTop + ySize / 2 + 5);
                if (party.getIsLocked()) {
                    friendlyFireLabel.x = guiLeft + 5;
                    friendlyFireLabel.y = guiTop + ySize - 8 - 15;
                }
                this.addLabel(friendlyFireLabel);
                GuiNpcButton friendlyFireButton = new GuiNpcButton(320, friendlyFireLabel.x + 70, friendlyFireLabel.y - 6, new String[]{"gui.on", "gui.off"}, party.friendlyFire() ? 0 : 1);
                friendlyFireButton.width = 40;
                this.addButton(friendlyFireButton);

                if (this.isLeader) {
                    if (!party.getIsLocked()) {
                        //set leader button
                        //
                        GuiNpcButton leaderButton = new GuiNpcButton(305, guiLeft + 5, guiTop + ySize - 8 - 21, "party.makeLeader");
                        leaderButton.width = 65;
                        this.addButton(leaderButton);

                        //kick player button
                        //
                        GuiNpcButton kickButton = new GuiNpcButton(310, guiLeft + 75, guiTop + ySize - 8 - 21, "party.kick");
                        kickButton.width = 65;
                        this.addButton(kickButton);

                        //send invite button (opens subgui)
                        //
                        GuiNpcTextField playerTextField = new GuiNpcTextField(325, this, friendlyFireLabel.x, guiTop + ySize / 2 + 25, 100, 20, "");
                        this.addTextField(playerTextField);
                        GuiNpcButton inviteButton = new GuiNpcButton(330, playerTextField.xPosition + 105, playerTextField.yPosition, "party.invite");
                        inviteButton.width = 50;
                        this.addButton(inviteButton);
                    }

                    //disband party
                    //
                    GuiNpcButton disbandButton = new GuiNpcButton(335, guiLeft + 5, guiTop + ySize - 9, "party.disbandParty");
                    disbandButton.width = 135;
                    this.addButton(disbandButton);
                } else {
                    //leave party
                    //
                    GuiNpcButton leaveButton = new GuiNpcButton(335, guiLeft + 5, guiTop + ySize - 9, "party.leaveParty");
                    leaveButton.width = 100;
                    this.addButton(leaveButton);
                }
            }
        }
    }

    @Override
    public void confirmClicked(boolean flag, int i) {
        if (flag) {
            switch (i) {
                case 0:
                    Client.sendData(EnumPacketServer.SetPartyLeader, this.selectedPlayer);
                    break;
                case 1:
                    Client.sendData(EnumPacketServer.KickPlayer, this.selectedPlayer);
                    break;
                case 2:
                    Client.sendData(EnumPacketServer.DisbandParty);
                    break;
                case 3:
                    Client.sendData(EnumPacketServer.KickPlayer, this.player.getCommandSenderName());
                    break;
            }
            receivedData = false;
            initGui();
        }
        displayGuiScreen(this);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        Party party = ClientCacheHandler.party;

        switch (guibutton.id) {
            case 200:
                Client.sendData(EnumPacketServer.CreateParty);
                receivedData = false;
                break;
            case 215:
                if (this.selectedInvite != null && !this.selectedInvite.isEmpty()) {
                    Client.sendData(EnumPacketServer.AcceptInvite, this.selectedInvite);
                    receivedData = false;
                    return;
                }
                break;
            case 220:
                if (this.selectedInvite != null && !this.selectedInvite.isEmpty()) {
                    Client.sendData(EnumPacketServer.IgnoreInvite, this.selectedInvite);
                    receivedData = false;
                    return;
                }
                break;
            case 305:
                if (this.selectedPlayer != null && !this.selectedPlayer.isEmpty() && !party.getPartyLeaderName().equals(this.selectedPlayer)) {
                    GuiYesNo yesnoLeader = new GuiYesNo(this, StatCollector.translateToLocal("party.leaderConfirm"), this.selectedPlayer, 0);
                    displayGuiScreen(yesnoLeader);
                    return;
                }
                break;
            case 310:
                if (this.selectedPlayer != null && !this.selectedPlayer.isEmpty() && !party.getPartyLeaderName().equals(this.selectedPlayer)) {
                    GuiYesNo yesnoKick = new GuiYesNo(this, StatCollector.translateToLocal("party.kickConfirm"), this.selectedPlayer, 1);
                    displayGuiScreen(yesnoKick);
                    return;
                }
                break;
            case 320:
                party.toggleFriendlyFire();
                this.partyChanged = true;
                break;
            case 330:
                String inviteName = this.getTextField(325).getText();
                if (!inviteName.isEmpty() && !party.getPlayerNames(true).contains(inviteName.toLowerCase())) {
                    Client.sendData(EnumPacketServer.PartyInvite, inviteName);
                    this.getTextField(325).setText("");
                }
                break;
            case 335:
                if (this.isLeader) {
                    GuiYesNo yesnoDisband = new GuiYesNo(this, "Confirm", StatCollector.translateToLocal("party.disbandConfirm"), 2);
                    displayGuiScreen(yesnoDisband);
                } else {
                    GuiYesNo yesnoLeave = new GuiYesNo(this, "Confirm", StatCollector.translateToLocal("party.leaveConfirm"), 3);
                    displayGuiScreen(yesnoLeave);
                }
                break;
            case 400:
                this.showQuestText = false;
                break;
            case 405:
                this.showQuestText = true;
                break;
            case 410:
                Client.sendData(EnumPacketServer.SetPartyQuest, "", "");
                break;
        }
        initGui();
        if (guibutton.id == 100 && activeTab != 0) {
            activeTab = 0;
            mc.displayGuiScreen(new GuiQuestLog(mc.thePlayer));
        }
        if (guibutton.id == 101 && activeTab != 1) {
            activeTab = 1;
            mc.displayGuiScreen(new GuiParty(mc.thePlayer));
        }
        if (guibutton.id == 102 && activeTab != 2) {
            activeTab = 2;
            mc.displayGuiScreen(new GuiFaction());
        }
    }

    @Override
    public void drawScreen(int i, int j, float f){
        renderTicks++;

        drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(resource);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 252, 195);
        drawTexturedModalRect(guiLeft + 252, guiTop, 188, 0, 67, 195);
        super.drawScreen(i, j, f);

        if(!receivedData){
            String periods = "";
            for (int k = 0; k < (renderTicks/10)%4; k++) {
                periods += ".";
            }
            fontRendererObj.drawString(StatCollector.translateToLocal("gui.loading") + periods,guiLeft + xSize/2,guiTop + 80, CustomNpcResourceListener.DefaultTextColor);
            return;
        }

        if (ClientCacheHandler.party == null) {
            drawTextBlock("party.messageNoParty", guiLeft + 155, guiTop + ySize/2 - 20, 160);
        } else if (ClientCacheHandler.party.getQuest() != null) {
            Quest quest = (Quest) ClientCacheHandler.party.getQuest();
            if (showQuestText) {
                drawTextBlock(quest.getLogText(), guiLeft + 142, guiTop + 20, 174);
            } else {
                drawProgress();
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(guiLeft + 148, guiTop, 0);
            GL11.glScalef(1.24f, 1.24f, 1.24f);
            fontRendererObj.drawString(quest.getName(), (130 - fontRendererObj.getStringWidth(quest.getName())) / 2, 4, CustomNpcResourceListener.DefaultTextColor);
            GL11.glPopMatrix();
            drawHorizontalLine(guiLeft + 142, guiLeft + 312, guiTop + 17, +0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
        }
    }

    private void drawProgress() {
        String complete = this.questCompleteWith;
        if(complete != null && !complete.isEmpty())
            fontRendererObj.drawString(StatCollector.translateToLocalFormatted("quest.completewith", complete), guiLeft + 144, guiTop + 105, CustomNpcResourceListener.DefaultTextColor);

        int yoffset = guiTop + 22;
        for(String process : this.questLogStatus){
            int index = process.lastIndexOf(":");
            if(index > 0){
                String name = process.substring(0, index);
                String trans = StatCollector.translateToLocal(name);
                if(!trans.equals(name))
                    name = trans;
                trans = StatCollector.translateToLocal("entity." + name + ".name");
                if(!trans.equals("entity." + name + ".name")){
                    name = trans;
                }
                process = name + process.substring(index);
            }
            fontRendererObj.drawString("- " + process, guiLeft + 144, yoffset , CustomNpcResourceListener.DefaultTextColor);
            yoffset += 10;
        }
    }

    @Override
    public void keyTyped(char c, int i)
    {
        super.keyTyped(c, i);
        if (!GuiNpcTextField.isFieldActive()) {
            if (i == 1 || i == mc.gameSettings.keyBindInventory.getKeyCode()) // inventory key
            {
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        this.receivedData = true;
        ClientCacheHandler.party = null;

        if (compound.hasKey("PartyUUID")) {
            UUID uuid = UUID.fromString(compound.getString("PartyUUID"));
            ClientCacheHandler.party = new Party(uuid);
            Party party = ClientCacheHandler.party;
            party.readFromNBT(compound);
            this.questLogStatus.clear();
            this.questCompleteWith = "";
            if (compound.hasKey("QuestProgress")) {
                NBTTagList tagList = compound.getTagList("QuestProgress", 8);
                for (int i = 0; i < tagList.tagCount(); i++) {
                    this.questLogStatus.add(tagList.getStringTagAt(i));
                }
            }
            if (compound.hasKey("QuestCompleteWith")) {
                this.questCompleteWith = compound.getString("QuestCompleteWith");
            }
            this.isLeader = ClientCacheHandler.party.getPartyLeaderName().equals(this.player.getCommandSenderName());
        } else if (compound.hasKey("Disband")) {
            this.isLeader = false;
        } else if (compound.hasKey("PartyInvites")) {
            this.invites.clear();
            NBTTagList list = compound.getTagList("PartyInvites", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound listCompound = list.getCompoundTagAt(i);
                String inviteName = listCompound.getString("PartyLeader");
                String partyUUID = listCompound.getString("PartyUUID");
                this.invites.put(inviteName, partyUUID);
            }
        }

        initGui();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.save();
    }

    @Override
    public void save() {
        if (this.partyChanged) {
            Client.sendData(EnumPacketServer.SavePartyData, ClientCacheHandler.party.writeClientNBT());
        }
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        switch (guiCustomScroll.id) {
            case 210:
                this.selectedInvite = this.invites.get(guiCustomScroll.getSelected());
                break;
            case 300:
                this.selectedPlayer = guiCustomScroll.getSelected().replace(" §e[" + StatCollector.translateToLocal("party.leader").toUpperCase() + "]", "");
                break;
        }
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {

    }
}
