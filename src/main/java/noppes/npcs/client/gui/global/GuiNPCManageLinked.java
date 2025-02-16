package noppes.npcs.client.gui.global;

import kamkeel.npcs.network.PacketClient;
import kamkeel.npcs.network.packets.request.effects.EffectRemovePacket;
import kamkeel.npcs.network.packets.request.effects.EffectSavePacket;
import kamkeel.npcs.network.packets.request.linked.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.item.SubGuiLinkedItem;
import noppes.npcs.client.gui.util.*;

import noppes.npcs.controllers.data.CustomEffect;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GuiNPCManageLinked extends GuiNPCInterface2 implements IScrollData, ISubGuiListener, ICustomScrollListener, IGuiData, GuiYesNoCallback {
	private int tab = 0;
    private boolean loadedNPC = false;
    private GuiCustomScroll scroll;

    public HashMap<String, Integer> data = new HashMap<>();
    private String selected = null;

    private LinkedItem linkedItem = null;
    public String originalName = "";

	private String search = "";

    private float zoomed = 70, rotation;

    public GuiNPCManageLinked(EntityNPCInterface npc){
    	super(npc);

        // Spoof NPC
        this.npc =  new EntityCustomNpc(Minecraft.getMinecraft().theWorld);
        this.npc.display.name = "Linked NPC";
        this.npc.height = 1.62f;
        this.npc.width = 0.43f;

        LinkedGetAllPacket.GetNPCs();
    }

    @Override
    public void initGui(){
        super.initGui();

        int y = guiTop + 8;

        this.addButton(new GuiNpcButton(10,guiLeft + 368, y, 45, 20, "npc"));
        this.addButton(new GuiNpcButton(11,guiLeft + 368, y += 22, 45, 20, "items"));
        getButton(10).enabled = tab == 1;
        getButton(11).enabled = tab == 0;

       	this.addButton(new GuiNpcButton(1,guiLeft + 368, y += 40, 45, 20, "gui.add"));
    	this.addButton(new GuiNpcButton(2,guiLeft + 368, y += 22, 45, 20, "gui.remove"));

        this.addButton(new GuiNpcButton(3,guiLeft + 368, y += 22, 45, 20, "gui.edit"));
        this.addButton(new GuiNpcButton(4,guiLeft + 368, y += 22, 45, 20, "gui.copy"));
        getButton(3).enabled = tab == 1;
        getButton(4).enabled = tab == 1;

        if(scroll == null){
	        scroll = new GuiCustomScroll(this,0,0);
	        scroll.setSize(143, 185);
        }
        scroll.guiLeft = guiLeft + 220;
        scroll.guiTop = guiTop + 4;
        scroll.setList(getSearchList());
        this.addScroll(scroll);
		this.addTextField(new GuiNpcTextField(55, this, fontRendererObj, guiLeft + 220, guiTop + 4 + 3 + 185, 143, 20, search));
    }

	@Override
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		if(getTextField(55) != null){
			if(getTextField(55).isFocused()){
				if(search.equals(getTextField(55).getText()))
					return;
				search = getTextField(55).getText().toLowerCase();
				scroll.setList(getSearchList());
                scroll.resetScroll();
			}
		}
	}

    @Override
    public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);

        if(tab == 0){
            if (hasSubGui())
                return;

            if (isMouseOverRenderer(i, j)) {
                zoomed += Mouse.getDWheel() * 0.035f;
                if (zoomed > 100)
                    zoomed = 100;
                if (zoomed < 10)
                    zoomed = 10;

                if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
                    rotation -= Mouse.getDX() * 0.75f;
                }
            }

            GL11.glColor4f(1, 1, 1, 1);

            EntityLivingBase entity = this.npc;
            int l = guiLeft + 150;
            int i1 = guiTop + 198;


            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glPushMatrix();
            GL11.glTranslatef(l, i1, 60F);

            GL11.glScalef(-zoomed, zoomed, zoomed);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            float f2 = entity.renderYawOffset;
            float f3 = entity.rotationYaw;
            float f4 = entity.rotationPitch;
            float f7 = entity.rotationYawHead;
            float f5 = (float) (l) - i;
            float f6 = (float) (i1 - 50) - j;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f6 / 800F) * 20F, 1.0F, 0.0F, 0.0F);
            entity.prevRenderYawOffset = entity.renderYawOffset = rotation;
            entity.prevRotationYaw = entity.rotationYaw = (float) Math.atan(f5 / 80F) * 40F + rotation;
            entity.rotationPitch = -(float) Math.atan(f6 / 80F) * 20F;
            entity.prevRotationYawHead = entity.rotationYawHead = entity.rotationYaw;
            GL11.glTranslatef(0.0F, entity.yOffset, 1F);
            RenderManager.instance.playerViewY = 180F;


            // Render Entity
            GL11.glPushMatrix();
            try {
                RenderManager.instance.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F);
            } catch (Exception ignored) {
            }
            GL11.glPopMatrix();

            entity.prevRenderYawOffset = entity.renderYawOffset = f2;
            entity.prevRotationYaw = entity.rotationYaw = f3;
            entity.rotationPitch = f4;
            entity.prevRotationYawHead = entity.rotationYawHead = f7;

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glPopMatrix();
        }

    }

	private List<String> getSearchList(){
		if(search.isEmpty()){
			return new ArrayList<String>(this.data.keySet());
		}
		List<String> list = new ArrayList<String>();
		for(String name : this.data.keySet()){
			if(name.toLowerCase().contains(search))
				list.add(name);
		}
		return list;
	}

    @Override
	public void buttonEvent(GuiButton button){
        if(button.id == 1){
            if(tab == 0){
                setSubGui(new SubGuiEditText("New"));
            } else {
                String name = "New";
                while (data.containsKey(name))
                    name += "_";
                LinkedItem linkedItem = new LinkedItem(name);
                PacketClient.sendClient(new LinkedItemSavePacket(linkedItem.writeToNBT(), ""));
            }
        }
        if(button.id == 2){
            if(tab == 0){
                if (data.containsKey(scroll.getSelected())) {
                    GuiYesNo guiyesno = new GuiYesNo(this, scroll.getSelected(), StatCollector.translateToLocal("gui.delete"), 0);
                    displayGuiScreen(guiyesno);
                }
            } else {
                if (data.containsKey(scroll.getSelected())) {
                    GuiYesNo guiyesno = new GuiYesNo(this, scroll.getSelected(), StatCollector.translateToLocal("gui.delete"), 1);
                    displayGuiScreen(guiyesno);
                }
            }
        }
        if(button.id == 10){
            tab = 0;
            LinkedGetAllPacket.GetNPCs();
        }
        if(button.id == 11){
            tab = 1;
            LinkedGetAllPacket.GetItems();
        }

        if(linkedItem == null)
            return;

        if(button.id == 3){
            setSubGui(new SubGuiLinkedItem(this, this.linkedItem));
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        NoppesUtil.openGUI(player, this);
        if (!result)
            return;
        if (id == 0) {
            if (data.containsKey(scroll.getSelected())) {
                PacketClient.sendClient(new LinkedNPCRemovePacket(scroll.getSelected()));
                initGui();
            }
        }
        if(id == 1){
            if (data.containsKey(scroll.getSelected())) {
                PacketClient.sendClient(new LinkedItemRemovePacket(data.get(scroll.getSelected())));
                initGui();
            }
        }
    }

    @Override
    public void drawBackground() {
        super.drawBackground();
        renderScreen();
    }


    private void renderScreen() {
        drawGradientRect(guiLeft + 5, guiTop + 4, guiLeft + 218, guiTop + 24, 0xC0101010, 0xC0101010);
        drawHorizontalLine(guiLeft + 5, guiLeft + 218, guiTop + 25, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
        drawGradientRect(guiLeft + 5, guiTop + 27, guiLeft + 218, guiTop + ySize + 9, 0xA0101010, 0xA0101010);
        if (tab == 0 && loadedNPC && this.npc != null) {
            String drawString = npc.display.getName();
            int textWidth = getStringWidthWithoutColor(drawString);
            int centerX = guiLeft + 5 + ((218 - 10 - textWidth) / 2);
            fontRendererObj.drawString(drawString, centerX, guiTop + 10, npc.getFaction().color, true);
            int y = guiTop + 18;

            String healthMenu = "Health:";
            fontRendererObj.drawString(healthMenu, guiLeft + 8, y += 12, 0xFFFFFF, true);

            healthMenu = "§c" + String.valueOf(this.npc.stats.maxHealth);
            fontRendererObj.drawString(healthMenu, guiLeft + 50, y, 0xFFFFFF, true);


            String damage = "Damage:";
            fontRendererObj.drawString(damage, guiLeft + 8, y += 12, 0xFFFFFF, true);

            damage = "§4" + String.valueOf(this.npc.stats.getAttackStrength());
            fontRendererObj.drawString(damage, guiLeft + 50, y, 0xFFFFFF, true);

            String moveSpeed = "Speed:";
            fontRendererObj.drawString(moveSpeed, guiLeft + 8, y += 12, 0xFFFFFF, true);

            moveSpeed = "§b" + String.valueOf(this.npc.ais.getWalkingSpeed());
            fontRendererObj.drawString(moveSpeed, guiLeft + 50, y, 0xFFFFFF, true);
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
		else if (subgui instanceof SubGuiLinkedItem){

        }
	}

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> data) {
        String name = scroll.getSelected();
        this.data = data;
        scroll.setList(getSearchList());

        if (name != null)
            scroll.setSelected(name);

        initGui();
    }

    @Override
    public void setSelected(String selected) {
        this.selected = selected;
        scroll.setSelected(selected);
        originalName = scroll.getSelected();
    }

	@Override
	public void save() {}

    public boolean isMouseOverRenderer(int x, int y) {
        return x >= guiLeft + 10 && x <= guiLeft + 10 + 200 && y >= guiTop + 6 && y <= guiTop + 6 + 204;
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        if (guiCustomScroll.id == 0) {
            loadedNPC = false;
            selected = scroll.getSelected();
            originalName = scroll.getSelected();
            if (selected != null && !selected.isEmpty()){
                if(tab == 0)
                    LinkedGetPacket.GetNPC(selected);
                else
                    LinkedGetPacket.GetItem(data.get(selected));
            }
        }
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll scroll) {}

    public void setGuiData(NBTTagCompound compound) {
        loadedNPC = false;
        this.linkedItem = null;
        if(compound.hasKey("NPCData")){
            // Linked NPC
            this.npc.display.readToNBT(compound.getCompoundTag("NPCData"));
            this.npc.stats.readToNBT(compound.getCompoundTag("NPCData"));
            this.npc.ais.readToNBT(compound.getCompoundTag("NPCData"));
            loadedNPC = true;
        } else {
            this.linkedItem = new LinkedItem();
            this.linkedItem.readFromNBT(compound, false);
        }
        initGui();
    }
}
