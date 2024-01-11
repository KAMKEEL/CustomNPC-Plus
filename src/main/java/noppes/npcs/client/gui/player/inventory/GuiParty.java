package noppes.npcs.client.gui.player.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientCacheHandler;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Party;
import org.lwjgl.opengl.GL11;
import tconstruct.client.tabs.InventoryTabCustomNpc;
import tconstruct.client.tabs.TabRegistry;

import java.util.UUID;

public class GuiParty extends GuiCNPCInventory implements ITextfieldListener, ITopButtonListener,ICustomScrollListener,  IGuiData {
    private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/standardbg.png");
    private final EntityPlayer player;
    private Minecraft mc = Minecraft.getMinecraft();

    private boolean receivedData;
    private long renderTicks;

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
        Party party = ClientCacheHandler.party;
        TabRegistry.addTabsToList(buttonList);
        TabRegistry.updateTabValues(guiLeft, guiTop, InventoryTabCustomNpc.class);

        if (receivedData) {
            if (party == null) {
                //
                //create party button
                //
                GuiNpcButton createPartyButton = new GuiNpcButton(200, guiLeft + xSize/2 + 45, guiTop + ySize/2 + 20, "Create Party");
                createPartyButton.width = 100;
                this.addButton(createPartyButton);

                this.addLabel(new GuiNpcLabel(201, "You're not in a party!", guiLeft + xSize/2 + 30, guiTop + ySize/2 - 30));
                this.addLabel(new GuiNpcLabel(202, "Either wait for an invite", guiLeft + xSize/2 + 30, guiTop + ySize/2 - 20));
                this.addLabel(new GuiNpcLabel(203, "or create one.", guiLeft + xSize/2 + 30, guiTop + ySize/2 - 10));

                //party invites list
                //
                GuiCustomScroll inviteScroll = new GuiCustomScroll(this, 210, false);
                inviteScroll.setSize(150, 160);
                inviteScroll.guiLeft = guiLeft + 5;
                inviteScroll.guiTop = guiTop + 5;
                this.addScroll(inviteScroll);

                GuiNpcButton acceptButton = new GuiNpcButton(215, guiLeft + 15, guiTop + ySize - 12, "Accept");
                acceptButton.width = 60;
                this.addButton(acceptButton);

                GuiNpcButton ignoreButton = new GuiNpcButton(220, guiLeft + 80, guiTop + ySize - 12, "Ignore");
                ignoreButton.width = 60;
                this.addButton(ignoreButton);
            } else {
                //
                //party player list
                //
                GuiCustomScroll playerScroll = new GuiCustomScroll(this, 300, false);
                playerScroll.setSize(150, 160);
                playerScroll.guiLeft = guiLeft + 5;
                playerScroll.guiTop = guiTop + 5;
                this.addScroll(playerScroll);

                //set leader button
                //
                GuiNpcButton leaderButton = new GuiNpcButton(305, guiLeft + 5, guiTop + ySize - 12, "Make Leader");
                leaderButton.width = 70;
                this.addButton(leaderButton);

                //kick player button
                //
                GuiNpcButton kickButton = new GuiNpcButton(315, guiLeft + 85, guiTop + ySize - 12, "Kick");
                kickButton.width = 70;
                this.addButton(kickButton);

                //toggle friendly fire
                //
                GuiNpcButton friendlyFireButton = new GuiNpcButton(320, guiLeft + xSize/2 + 95, guiTop + ySize/2, new String[] {"On", "Off"}, 0); //party.friendlyFire() ? 0 : 1
                friendlyFireButton.width = 40;
                this.addButton(friendlyFireButton);

                GuiNpcLabel friendlyFireLabel = new GuiNpcLabel(321, "Friendly Fire:", guiLeft + xSize/2 + 20, guiTop + ySize/2 + 5);
                this.addLabel(friendlyFireLabel);

                //send invite button (opens subgui)
                //
                GuiNpcTextField playerTextField = new GuiNpcTextField(325, this, guiLeft + xSize/2 + 20, guiTop + ySize/2 + 40,100, 20, "");
                this.addTextField(playerTextField);

                GuiNpcButton inviteButton = new GuiNpcButton(330, guiLeft + xSize/2 + 125, guiTop + ySize/2 + 40, "Invite");
                inviteButton.width = 50;
                this.addButton(inviteButton);

                //disband party
                //
                GuiNpcButton disbandButton = new GuiNpcButton(335, guiLeft + xSize/2 + 45, guiTop + ySize/2 + 70, "Disband Party");
                disbandButton.width = 100;
                this.addButton(disbandButton);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        switch (guibutton.id) {
            case 200:
                Client.sendData(EnumPacketServer.CreateParty);
                receivedData = false;
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

        if (compound.hasKey("PartyUUID")) {
            Party party = ClientCacheHandler.party;
            UUID uuid = UUID.fromString(compound.getString("PartyUUID"));
            if (party == null || !party.getPartyUUID().equals(uuid)) {
                ClientCacheHandler.party = new Party(uuid);
                party = ClientCacheHandler.party;
            }
            party.readFromNBT(compound);
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

    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {

    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {

    }
}
