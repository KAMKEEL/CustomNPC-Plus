package noppes.npcs.client.gui.player.inventory;

import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.controllers.data.Slot;
import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.player.CheckPlayerValue;
import kamkeel.npcs.network.packets.request.linked.LinkedGetPacket;
import kamkeel.npcs.network.packets.request.linked.LinkedItemRemovePacket;
import kamkeel.npcs.network.packets.request.linked.LinkedNPCAddPacket;
import kamkeel.npcs.network.packets.request.linked.LinkedNPCRemovePacket;
import kamkeel.npcs.network.packets.request.profile.ProfileGetPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.controllers.data.PlayerFactionData;
import org.lwjgl.opengl.GL11;
import tconstruct.client.tabs.AbstractTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiProfiles extends GuiCNPCInventory implements ISubGuiListener, ICustomScrollListener, IGuiData, GuiYesNoCallback {

	private final ResourceLocation resource = new ResourceLocation("customnpcs","textures/gui/standardbg.png");
    private GuiCustomScroll scroll;
    public HashMap<String, Integer> data = new HashMap<>();
    private String selected = null;
    private Profile profile;
    private Slot slot;

	public GuiProfiles() {
		super();
		xSize = 280;
		ySize = 180;
        this.drawDefaultBackground = false;
        title = "";
        PacketClient.sendClient(new ProfileGetPacket());
	}

	@Override
    public void initGui()
    {
		super.initGui();

        int y = guiTop + 8;
        this.addButton(new GuiNpcButton(1,guiLeft + 368, y += 40, 45, 20, "gui.add"));
        this.addButton(new GuiNpcButton(2,guiLeft + 368, y += 22, 45, 20, "gui.remove"));
        this.addButton(new GuiNpcButton(3,guiLeft + 368, y += 22, 45, 20, "gui.rename"));
        this.addButton(new GuiNpcButton(4,guiLeft + 368, y += 22, 45, 20, "gui.change"));

        if(scroll == null){
            scroll = new GuiCustomScroll(this,0,0);
            scroll.setSize(143, 185);
        }
        scroll.guiLeft = guiLeft + 220;
        scroll.guiTop = guiTop + 4;
        scroll.setList(new ArrayList<String>(this.data.keySet()));
        this.addScroll(scroll);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton){
        if(guibutton instanceof AbstractTab)
            return;

        if (guibutton.id <= -100) {
            super.actionPerformed(guibutton);
            return;
        }
    }

	@Override
    public void drawScreen(int i, int j, float f)
    {
        super.drawScreen(i, j, f);
        if(hasSubGui())
            return;
    }

    @Override
    public void drawBackground() {
        super.drawBackground();
        renderScreen();
    }

    private void renderScreen() {
        // Draw the common background bars
        drawGradientRect(guiLeft + 5, guiTop + 4, guiLeft + 218, guiTop + 24, 0xC0101010, 0xC0101010);
        drawHorizontalLine(guiLeft + 5, guiLeft + 218, guiTop + 25, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
        drawGradientRect(guiLeft + 5, guiTop + 27, guiLeft + 218, guiTop + ySize + 9, 0xA0101010, 0xA0101010);

        if (this.profile != null) {
            // Top bar: display NPC display name (centered)
//            String topBarText = npc.display.getName();
//            int textWidth = getStringWidthWithoutColor(topBarText);
//            int centerX = guiLeft + 5 + ((218 - 10 - textWidth) / 2);
//            fontRendererObj.drawString(topBarText, centerX, guiTop + 10, npc.getFaction().color, true);
//
//            // Lower section: display NPC properties as label and value pairs
//            int y = guiTop + 30;
//            int xLabel = guiLeft + 8;
//            int xValue = guiLeft + 120;
//            int valueColor = 0xFFFFFF;
//            String label, value;
//
//            // Health
//            label = StatCollector.translateToLocal("stats.health") + ": ";
//            value = String.valueOf(npc.stats.maxHealth);
//            fontRendererObj.drawString(label, xLabel, y, 0x29d6b9, false);
//            fontRendererObj.drawString(value, xValue, y, valueColor, false);
//            y += 15;
//
//            // Damage (using getAttackStrength)
//            label = StatCollector.translateToLocal("stats.meleestrength") + ": ";
//            value = String.valueOf(npc.stats.getAttackStrength());
//            fontRendererObj.drawString(label, xLabel, y, 0xff5714, false);
//            fontRendererObj.drawString(value, xValue, y, valueColor, false);
//            y += 15;
//
//            // Attack Speed
//            label = StatCollector.translateToLocal("stats.meleespeed") + ": ";
//            value = String.valueOf(npc.stats.attackSpeed);
//            fontRendererObj.drawString(label, xLabel, y, 0xf7ca28, false);
//            fontRendererObj.drawString(value, xValue, y, valueColor, false);
//            y += 15;
//
//            // AI Type (npc.ai.onAttack: 0 = fight, 1 = panic, 2 = retreat, 3 = nothing)
//            label = StatCollector.translateToLocal("menu.ai") + ": ";
//            int onAttack = npc.ais.onAttack;
//            switch (onAttack) {
//                case 0:
//                    value = StatCollector.translateToLocal("gui.retaliate");
//                    break;
//                case 1:
//                    value = StatCollector.translateToLocal("gui.panic");
//                    break;
//                case 2:
//                    value = StatCollector.translateToLocal("gui.retreat");
//                    break;
//                case 3:
//                default:
//                    value = StatCollector.translateToLocal("gui.nothing");
//                    break;
//            }
//            fontRendererObj.drawString(label, xLabel, y, 0xce75fa, false);
//            fontRendererObj.drawString(value, xValue, y, valueColor, false);
//            y += 15;
//
//            // Walk Speed
//            label = StatCollector.translateToLocal("stats.speed") + ": ";
//            value = String.valueOf(npc.ais.getWalkingSpeed());
//            fontRendererObj.drawString(label, xLabel, y, 0xffae0d, false);
//            fontRendererObj.drawString(value, xValue, y, valueColor, false);
//            y += 15;
//
//            // Movement Type (0 = Ground, 1 = Flying)
//            label = StatCollector.translateToLocal("movement.type") + ": ";
//            int movementType = npc.ais.movementType;
//            if (movementType == 0) {
//                value = StatCollector.translateToLocal("movement.ground");
//            } else {
//                value = StatCollector.translateToLocal("movement.flying");
//            }
//            fontRendererObj.drawString(label, xLabel, y, 0x7cff54, false);
//            fontRendererObj.drawString(value, xValue, y, valueColor, false);
//            y += 15;
        }
    }

    @Override
    public void keyTyped(char c, int i)
    {
        if (i == 1 || (!hasSubGui() && isInventoryKey(i)))
        {
            close();
        }
    }
	@Override
	public void save() {}

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result)
            return;
        if (id == 1) {
            if (data.containsKey(scroll.getSelected())) {

                // DELETE PROFILE
                // PacketClient.sendClient(new LinkedNPCRemovePacket(scroll.getSelected()));

                initGui();
            }
        }
        if(id == 1){
            if (data.containsKey(scroll.getSelected())) {

                // RENAME PROFILE
                // PacketClient.sendClient(new LinkedItemRemovePacket(data.get(scroll.getSelected())));

                initGui();
            }
        }
        if(id == 1){
            // CREATE PROFILE
            // PacketClient.sendClient(new LinkedItemRemovePacket(data.get(scroll.getSelected())));

            initGui();
        }
        if(id == 1){
            // CHANGE PROFILE
            // PacketClient.sendClient(new LinkedItemRemovePacket(data.get(scroll.getSelected())));

            initGui();
        }
    }

    public int getStringWidthWithoutColor(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§') {
                if (i < text.length() - 1) {
                    i += 1;
                }
            } else {
                // If not a color code, calculate the width
                width += fontRendererObj.getCharWidth(c);
            }
        }
        return width;
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if(subgui instanceof SubGuiEditText){
            if(!((SubGuiEditText)subgui).cancelled){
                PacketClient.sendClient(new LinkedNPCAddPacket(((SubGuiEditText)subgui).text));
            }
        }
    }

    public boolean isMouseOverRenderer(int x, int y) {
        return x >= guiLeft + 10 && x <= guiLeft + 10 + 200 && y >= guiTop + 6 && y <= guiTop + 6 + 204;
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            selected = scroll.getSelected();
            if (selected != null && !selected.isEmpty()){
                // LOAD SLOT INFORMATION

            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {}

    public void setGuiData(NBTTagCompound compound) {
        this.profile = null;
        if(compound.hasKey("PROFILE")){
            // Load Profile
            this.profile = new Profile(mc.thePlayer, compound);
            this.data = new HashMap<>();
            for(Slot slot1 : profile.slots.values()){
                this.data.put(slot1.getName(), slot1.getId());
            }
        } else if(compound.hasKey("PROFILE_INFO")){

        }
        initGui();
    }

}
